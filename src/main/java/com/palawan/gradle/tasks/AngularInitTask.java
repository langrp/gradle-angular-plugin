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

import com.moowork.gradle.node.exec.NodeExecRunner;
import com.moowork.gradle.node.npm.NpmTask;
import com.palawan.gradle.dsl.AngularExtension;
import com.palawan.gradle.dsl.AngularJsonProject;
import com.palawan.gradle.util.AngularJsonHelper;
import org.gradle.api.GradleException;
import org.gradle.api.Incubating;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

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
@SuppressWarnings("UnstableApiUsage")
public class AngularInitTask extends NpmTask {

	private List<String> cliArguments;

	private boolean createNewApplication = false;
	private String mainProject;

	/**
	 * Constructor defines task actions and default CLI arguments
	 */
	public AngularInitTask() {

		doLast(this::generateNgScript);
		doLast(this::initializeAngularProject);

		cliArguments = new ArrayList<>();
		cliArguments.add("new");
		cliArguments.add(getProject().getName());
		cliArguments.add("--directory=.");
	}

	/**
	 * Task execution method
	 */
	@Override
	@TaskAction
	public void exec() {
		AngularExtension extension = AngularExtension.get(getProject());
		setWorkingDir(getProject().getProjectDir());
		String version = extension.getVersion() == null ? "latest" : extension.getVersion();
		setArgs(Arrays.asList("install", "@angular/cli@" + version));
		super.exec();
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
	public AngularInitTask setRouting(boolean routing) {
		cliArguments.add("--routing="+routing);
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
	public AngularInitTask setStyle(String style) {
		if (! Arrays.asList("css", "scss", "sass", "less", "styl").contains(style)) {
			throw new GradleException("Unknown parameter 'style' value '" + style + "'");
		}
		cliArguments.add("--style=" + style);
		createNewApplication = true;
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
	public AngularInitTask setSkipGit(boolean skipGit) {
		cliArguments.add("--skipGit="+skipGit);
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
	public AngularInitTask setMainProject(String mainProject) {
		this.mainProject = mainProject;
		cliArguments.set(1, mainProject);
		cliArguments.add("--newProjectRoot=./");
		return this;
	}

	/**
	 * Executes angular CLI command to create new project with parameters defined
	 * from command line.
	 * @param initTask Init task which executes this action
	 */
	private void initializeAngularProject(Task initTask) {
		if (createNewApplication) {
			final AngularExtension extension = AngularExtension.get(initTask.getProject());
			String ngScript = new File(extension.getNodeModules(), NgCliTask.ANGULAR_CLI).toString();
			cliArguments.add(0, ngScript);

			NodeExecRunner runner = new NodeExecRunner(initTask.getProject());
			runner.setArguments(cliArguments);
			runner.execute();

			organizeMainProject();
		}
	}

	/**
	 * Generates helper scripts for npm and ng execution used in case
	 * local nodejs is being used.
	 * @param initTask	Init task which executes this action
	 */
	private void generateNgScript(Task initTask) {
		final AngularExtension extension = AngularExtension.get(initTask.getProject());
		final String projectDir = initTask.getProject().getProjectDir().toString();

		if (extension.getDownload()) {
			String ng = AngularJsonHelper.getInstance().resourceToString("/META-INF/scripts/ng.sh");
			String npm = AngularJsonHelper.getInstance().resourceToString("/META-INF/scripts/npm.sh");
			String ngCmd = AngularJsonHelper.getInstance().resourceToString("/META-INF/scripts/ng.cmd");
			String npmCmd = AngularJsonHelper.getInstance().resourceToString("/META-INF/scripts/npm.cmd");

			final Path nodePath = Paths.get(projectDir).relativize(extension.getWorkDir().toPath());
			final Path nodeModulesPath = Paths.get(projectDir).relativize(extension.getNodeModules().toPath());
			final Path npmPath = Paths.get(projectDir).relativize(extension.getNpmWorkDir().toPath());

			final String nodeNorm = normalize(nodePath);
			final String nodeModulesNorm = normalize(nodeModulesPath);
			final String npmNorm = normalize(npmPath);
			final String nodeNormCmd = windows(nodePath);
			final String nodeModulesNormCmd = windows(nodeModulesPath);
			final String npmNormCmd = windows(npmPath);

			ng = ng.replace("<node_path>", nodeNorm).replace("<node_modules>", nodeModulesNorm);
			npm = npm.replace("<node_path>", nodeNorm).replace("<npm_path>", npmNorm);
			ngCmd = ngCmd.replace("<node_path>", nodeNormCmd).replace("<node_modules>", nodeModulesNormCmd);
			npmCmd = npmCmd.replace("<node_path>", nodeNormCmd).replace("<npm_path>", npmNormCmd);

			try {
				Files.write(Paths.get(projectDir, "npm"), npm.getBytes(StandardCharsets.UTF_8));
				Files.write(Paths.get(projectDir, "ng"), ng.getBytes(StandardCharsets.UTF_8));
				Files.write(Paths.get(projectDir, "npm.cmd"), npmCmd.getBytes(StandardCharsets.UTF_8));
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
	private void organizeMainProject() {
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
						.forEach(new Consumer<Path>() {
							List<Path> list = new ArrayList<>();

							@Override
							public void accept(Path path) {
								for (Path p : list) {
									if (path.startsWith(p)) {
										return;
									}
								}

								move(path, targetRoot.resolve(projectRoot.relativize(path)));
							}
						});

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
