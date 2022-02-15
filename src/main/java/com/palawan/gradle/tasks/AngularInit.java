/*
 * Copyright (c) 2019 Petr Langr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.palawan.gradle.tasks;

import com.palawan.gradle.dsl.AngularExtension;
import com.palawan.gradle.dsl.AngularJsonProject;
import com.palawan.gradle.internal.ExecutableData;
import com.palawan.gradle.util.AngularJsonHelper;
import com.palawan.gradle.util.ProjectUtil;
import org.gradle.api.GradleException;
import org.gradle.api.Incubating;
import org.gradle.api.Task;
import org.gradle.api.tasks.options.Option;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.palawan.gradle.util.PathUtil.normalize;
import static com.palawan.gradle.util.PathUtil.windows;

/**
 * Initialize task to create new angular project from scratch. Currently supported
 * arguments to angular-cli are only {@code routing}, {@code skipGit} and {@code style}.
 *
 * <p>Additional incubating feature of the task is project organization in flavor of
 * gradle multi-project. Using additional parameter {@code mainProject}=&lt;name&gt;
 * will cause reorganizing generated project into sub directory as per given name.
 * Later such directory can be used for gradle sub-project.</p>
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class AngularInit extends AngularCli {

	private String mainProject;

	/**
	 * Constructor defines task actions and default CLI arguments
	 */
	public AngularInit() {

		doLast(this::preparePackagerScripts);
		doLast(this::organizeMainProject);
		doLast(this::generateNgScript);

		mainProject = getProject().getName();
	}

	/**
	 * Executes angular CLI command to create new project with parameters defined
	 * from command line.
	 */
	@Override
	protected ExecutableData executableData(List<String> arguments) {
		cleanWorkingDir();

		List<String> args = Stream.concat(Stream.of(
				"new", mainProject,
				"--directory=.",
				"--package-manager=" + packager.get().getNpmPackage()),
				arguments.stream())
				.collect(Collectors.toList());
;
		return super.executableData(args);
	}

	/**
	 * Activate angular routing for newly created project
	 * @param routing	Should routing be activated?
	 * @return	This task instance
	 */
	@Option(
			option = "routing",
			description = "When true, generates a routing module for the initial project."
	)
	public AngularInit setRouting(boolean routing) {
		addArgument("--routing="+routing);
		return this;
	}

	/**
	 * Specify angular stylesheet type for newly created project
	 * @param style Stylesheet type for angular application
	 * @return	This task instance
	 */
	@Option(
			option = "style",
			description = "The file extension or preprocessor to use for style files. Supported css|scss|sass|less|styl"
	)
	public AngularInit setStyle(String style) {
		if (! Arrays.asList("css", "scss", "sass", "less", "styl").contains(style)) {
			throw new GradleException("Unknown parameter 'style' value '" + style + "'");
		}
		addArgument("--style=" + style);
		return this;
	}

	/**
	 * Should git creation be skipped for newly created project?
	 * @param skipGit	Flag to skip git creation
	 * @return	This task instance
	 */
	@Option(
			option = "skipGit",
			description = "When true, does not initialize a git repository."
	)
	public AngularInit setSkipGit(boolean skipGit) {
		addArgument("--skipGit="+skipGit);
		return this;
	}

	/**
	 * Incubating feature to reorganize newly create project as gradle
	 * sub-project of the processed project.
	 * @param mainProject Sub-project or main angular project name
	 * @return	This task instance
	 */
	@Option(
			option = "mainProject",
			description = "Creates sub-project with main application and" +
					" reorganizes generated project to follow gradle standards"
	)
	@Incubating
	public AngularInit setMainProject(String mainProject) {
		this.mainProject = mainProject;
		addArgument("--newProjectRoot=./");
		return this;
	}

	/**
	 * Removes package.json from working directory.
	 * This helps the ng new to create fresh new project after installation of angular
	 */
	private void cleanWorkingDir() {
		Path packageJson = getWorkingDirOrProjectDir().toPath().resolve(AngularJsonHelper.NODE_LIBRARY_DESCRIPTOR);

		try {
			if (Files.exists(packageJson)) {
				Files.delete(packageJson);
			}
		} catch (IOException e) {
			throw new GradleException("Unable to delete old package.json", e);
		}
	}

	/**
	 * Append project parameter name to build script
	 * @param initTask	Init task which executes this action
	 */
	private void preparePackagerScripts(Task initTask) {
		AngularJsonHelper.getInstance().getPackageJson(getWorkingDirOrProjectDir().toPath())
				.ifPresent(p -> p.updateScripts((name, value) ->
					"build".equalsIgnoreCase(name) ? value + " $npm_config_project" : value
				));
	}

	/**
	 * Generates helper scripts for npm and ng execution used in case
	 * local nodejs is being used.
	 * @param initTask	Init task which executes this action
	 */
	private void generateNgScript(Task initTask) {
		final AngularExtension extension = AngularExtension.get(initTask.getProject());

		if (extension.getDownload()) {
			final String projectDir = initTask.getProject().getProjectDir().toString();


			String ng = AngularJsonHelper.getInstance().resourceToString("/META-INF/scripts/ng.sh");
			String ngCmd = AngularJsonHelper.getInstance().resourceToString("/META-INF/scripts/ng.cmd");

			final Path nodePath = Paths.get(projectDir).relativize(extension.getWorkingDirPath());
			final Path nodeModulesPath = Paths.get(projectDir).relativize(ProjectUtil.getNodeModules(initTask.getProject()));

			final String nodeNorm = normalize(nodePath);
			final String nodeModulesNorm = normalize(nodeModulesPath);
			final String nodeNormCmd = windows(nodePath);
			final String nodeModulesNormCmd = windows(nodeModulesPath);

			ng = ng.replace("<node_path>", nodeNorm).replace("<node_modules>", nodeModulesNorm);
			ngCmd = ngCmd.replace("<node_path>", nodeNormCmd).replace("<node_modules>", nodeModulesNormCmd);

			try {
				Files.write(Paths.get(projectDir, "ng"), ng.getBytes(StandardCharsets.UTF_8));
				Files.write(Paths.get(projectDir, "ng.cmd"), ngCmd.getBytes(StandardCharsets.UTF_8));

			} catch (IOException e) {
				throw new GradleException("Unable to generate npm, ng scripts.", e);
			}
		}

	}

	/**
	 * Incubating feature to organize project into sub-project of
	 * gradle. It will first move all files defined in angular.json
	 * under created project and then update angular.json file
	 * with new location.
	 */
	private void organizeMainProject(Task initTask) {
		if (mainProject != null) {
			Path projectRoot = getProject().getProjectDir().toPath();
			Path targetRoot = projectRoot.resolve(mainProject);
			AngularJsonProject angularProject = AngularJsonHelper.getInstance()
					.getAngularJson(getProject())
					.flatMap(a -> a.getProject(mainProject))
					.orElseThrow(() -> new GradleException("Angular init failed, see above error."));

			try {
				if (!Files.exists(targetRoot)) {
					Files.createDirectories(targetRoot);
				}

				angularProject.getProjectFiles()
						.sorted(Comparator.comparing(Path::getNameCount))
						.skip(1L) // skip root './'
						.forEach(p -> move(p, targetRoot.resolve(projectRoot.relativize(p))));

				angularProject.setRoot(targetRoot);

			} catch (IOException e) {
				throw new GradleException("Unable to organize project", e);
			}

		}
	}

	private static void move(Path source, Path target) {
		try {
			if (Files.exists(source)) {
				Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
			}

		} catch (IOException e) {
			throw new GradleException("Unable to organize project", e);
		}
	}

}
