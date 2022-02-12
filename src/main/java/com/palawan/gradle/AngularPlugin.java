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

package com.palawan.gradle;

import com.palawan.gradle.dsl.AngularExtension;
import com.palawan.gradle.dsl.AngularJsonProject;
import com.palawan.gradle.dsl.SourceSet;
import com.palawan.gradle.tasks.NodeInstallTask;
import com.palawan.gradle.tasks.NodeSetupTask;
import com.palawan.gradle.tasks.PackagerSetupTask;
import com.palawan.gradle.tasks.PackagerTask;
import com.palawan.gradle.util.AngularJsonHelper;
import com.palawan.gradle.util.ProjectUtil;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.ResolvedConfiguration;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponentFactory;
import org.gradle.api.distribution.Distribution;
import org.gradle.api.distribution.DistributionContainer;
import org.gradle.api.distribution.plugins.DistributionPlugin;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.internal.ConventionMapping;
import org.gradle.api.internal.IConventionAware;
import org.gradle.api.internal.artifacts.ArtifactAttributes;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet;
import org.gradle.api.internal.tasks.DefaultSourceSetOutput;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.PluginContainer;
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Zip;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author Langr, Petr
 * @since 1.0.0
 */
public class AngularPlugin implements Plugin<Project> {

    /**
     * The name of the exposed configuration, where exported artifact is defined
     */
    private static final String CONFIGURATION_EXPOSED = "nodeLibrary";

    /**
     * The main angular component
     */
    private static final String COMPONENT_NAME = "angular";

    private final ObjectFactory objectFactory;
    private final SoftwareComponentFactory softwareComponentFactory;

    @Inject
    public AngularPlugin(ObjectFactory objectFactory, SoftwareComponentFactory softwareComponentFactory) {
        this.objectFactory = objectFactory;
        this.softwareComponentFactory = softwareComponentFactory;
    }

    @Override
    public void apply(Project project) {
        applyPlugins( project );
        AngularExtension extension = AngularExtension.get(project);

        // Allow init tasks only
        if (extension.getAngularJson() != null) {
            configureSourceSetDefaults(project, extension);

            SourceSet mainSourceSet = configureSourceSet(project, extension);
            configureNodeTasks(project);
            configureConfigurations(project, mainSourceSet);
            configureDistributions(project, extension, mainSourceSet);
        }
    }

    private void applyPlugins(Project project) {
        PluginContainer plugins = project.getPlugins();
        plugins.apply(AngularBasePlugin.class);
        plugins.apply(DistributionPlugin.class);
    }

    private void configureSourceSetDefaults(final Project project, final AngularExtension angular) {
        angular.getSources().all(sourceSet -> {
            defineSourceSetConfigurations(sourceSet, project.getConfigurations());
            definePathsForSourceSet(sourceSet, project);
            Provider<PackagerTask> compileTask = createCompileTask(sourceSet, project);
            configureOutputDirectoryForSourceSet(sourceSet, compileTask, project);
            configurePublishToNodeModulesTask(sourceSet, project);
        });
    }

    private void defineSourceSetConfigurations(SourceSet sourceSet, ConfigurationContainer configurations) {
        String compileConfigurationName = sourceSet.getCompileConfigurationName();

        Configuration compileConfiguration = configurations.maybeCreate(compileConfigurationName);
        compileConfiguration.setVisible(true);
        compileConfiguration.setDescription("Angular dependencies for " + sourceSet);
        compileConfiguration.setCanBeConsumed(false);
        compileConfiguration.setCanBeResolved(true);

        configureAttributes(compileConfiguration);
    }

    private void configureAttributes(Configuration configuration) {
        configuration.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, objectFactory.named(Usage.class, "ng-api"));
        configuration.getAttributes().attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objectFactory.named(LibraryElements.class, "zip"));
        configuration.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, objectFactory.named(Bundling.class, "external"));
        configuration.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, objectFactory.named(Category.class, "library"));
    }

    private void definePathsForSourceSet(final SourceSet sourceSet, final Project project) {
        ConventionMapping outputConventionMapping = ((IConventionAware)sourceSet.getOutput()).getConventionMapping();
        outputConventionMapping.map("resourcesDir", () -> {
            AngularExtension angular = AngularExtension.get(project);

            if (!project.getSubprojects().isEmpty() && angular.getOutput() != null) {
                throw new GradleException("angular.output property can not be defined on multi-project.");
            }

            final Path outputPath = angular.getOutput() != null ?
                Paths.get(angular.getOutput()) :
                Paths.get(project.getBuildDir().toString(), "angular", sourceSet.getName());

            angular.getAngularJson().getProject(getAngularProject(project, sourceSet))
					.ifPresent(p -> p.setOutputPath(outputPath));

            return outputPath.toFile();
        });
    }

    private Provider<PackagerTask> createCompileTask(final SourceSet sourceSet, final Project project) {
        return project.getTasks().register(sourceSet.getCompileTaskName(), PackagerTask.class, task -> {
            Project topLevelProject = ProjectUtil.getTopLevelProject(project);
            sourceSet.getDirectory().getSrcDirs().forEach(task.getInputs()::dir);
            task.getOutputs().dir(project.getObjects().fileCollection().from(sourceSet.getOutput()));
            task.setGroup("build");
            task.setDescription("Compiles " + sourceSet.getOutput());
            task.setArguments(List.of("run", "build", "--project", getAngularProject(project, sourceSet)));
            task.setWorkingDir(topLevelProject.getProjectDir());
            task.dependsOn(topLevelProject.getTasks().withType(NodeInstallTask.class));
            resolveNodeDependencies(sourceSet, task);
            task.doLast(t -> AngularJsonHelper.getInstance().generateTimestamp(
                    sourceSet.getName(),
                    sourceSet.getOutput().getResourcesDir()));
        });
    }

    private String getAngularProject(Project project, SourceSet sourceSet) {
		if (ProjectUtil.isTopLevelAngularProject(project)) {
			if (SourceSet.SOURCE_SET_MAIN.equals(sourceSet.getName())) {
				return AngularExtension.get(project)
						.getAngularJson()
						.getDefaultProject()
						.map(AngularJsonProject::getName)
						.orElseThrow(() -> new GradleException("No default angular project defined!"));
			}
			return sourceSet.getName();
		} else {
			return project.getName();
		}
	}

    private void configureOutputDirectoryForSourceSet(final SourceSet sourceSet, final Provider<PackagerTask> compileTask, final Project project) {
        AngularExtension angular = AngularExtension.get(project);
        sourceSet.getDirectory().setOutputDir(project.provider(sourceSet.getOutput()::getResourcesDir));
        angular.getAngularJson().getProject(getAngularProject(project, sourceSet))
                .map(AngularJsonProject::getSourceRoot).map(Path::toFile).ifPresent(sourceSet.getDirectory()::srcDir);
        DefaultSourceSetOutput sourceSetOutput = Cast.cast(DefaultSourceSetOutput.class, sourceSet.getOutput());
        sourceSetOutput.addClassesDir(sourceSet.getDirectory().getClassesDirectory());
        sourceSetOutput.builtBy(compileTask);
    }

    private SourceSet configureSourceSet(Project project, AngularExtension extension) {
        if (ProjectUtil.isTopLevelAngularProject(project)) {
            extension.getAngularJson().getProjects().values().stream()
                    .filter(((Predicate<AngularJsonProject>)AngularJsonProject::isDefault).negate())
					.map(AngularJsonProject::getName)
					.filter(((Predicate<String>)project.getChildProjects()::containsKey).negate())
                    .forEach(extension.getSources()::create);
        }
        return extension.getSources().create(SourceSet.SOURCE_SET_MAIN);
    }

    private void configureNodeTasks(Project project) {
        if (!ProjectUtil.isTopLevelAngularProject(project)) {
            project.afterEvaluate(p -> {
                p.getTasks().withType(NodeSetupTask.class).all(t -> t.setEnabled(false));
                p.getTasks().withType(PackagerSetupTask.class).all(t -> t.setEnabled(false));
                p.getTasks().withType(NodeInstallTask.class).all(t -> t.setEnabled(false));
            });
        }
    }

    private void configureConfigurations(Project project, SourceSet mainSourceSet) {
        ConfigurationContainer configurations = project.getConfigurations();
        Configuration angularConfiguration = configurations.getByName(mainSourceSet.getCompileConfigurationName());
        Configuration libraryConfiguration = configurations.maybeCreate(CONFIGURATION_EXPOSED);
        libraryConfiguration.setVisible(false);
        libraryConfiguration.setCanBeResolved(false);
        libraryConfiguration.setCanBeConsumed(true);
        libraryConfiguration.setDescription("Node library for main.");
        libraryConfiguration.extendsFrom(angularConfiguration);
        configureAttributes(libraryConfiguration);
    }

    private void configureDistributions(Project project, AngularExtension angular, SourceSet mainSourceSet) {
        final DistributionContainer distributions = project.getExtensions().getByType(DistributionContainer.class);

        angular.getSources().all(sourceSet -> {
            Distribution dist = distributions.maybeCreate(sourceSet.getBaseName());
            dist.contents(s -> {
                s.into("/");
                s.from(sourceSet.getOutput().getClassesDirs());
            });
            Zip sourceDistTask = project.getTasks().maybeCreate(sourceSet.getDistributionTaskName(), Zip.class);
            sourceDistTask.dependsOn(sourceSet.getCompileTaskName());

            String tarTaskName = sourceDistTask.getName().substring(0, sourceDistTask.getName().length() - 3) + "Tar";
            project.getTasks().named(tarTaskName, t -> t.setEnabled(false));
        });

        TaskProvider<Zip> distTask = project.getTasks().named(mainSourceSet.getDistributionTaskName(), Zip.class);
        Configuration configuration = project.getConfigurations().getByName(CONFIGURATION_EXPOSED);
        PublishArtifact zipArchive = new LazyPublishArtifact(distTask);
        project.getExtensions().getByType(DefaultArtifactPublicationSet.class).addCandidate(zipArchive);
        addZip(configuration, zipArchive);
        registerSoftwareComponents(project);
    }

    private void configurePublishToNodeModulesTask(SourceSet sourceSet, Project project) {
        project.getTasks().register(sourceSet.getPublishToNodeModulesTaskName(), Copy.class, task -> {
            task.from(sourceSet.getOutput().getClassesDirs());
            task.into(ProjectUtil.getNodeModulesTarget(project, project.getGroup().toString(), getAngularProject(project, sourceSet)));
            task.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);
            task.setGroup("publishing");
            task.setDescription("Publishes build of '" + sourceSet.getName() + "' into " + ProjectUtil.getNodeModules(project));
            task.dependsOn(sourceSet.getCompileTaskName());
            task.doFirst(new Action<Task>() {
                @Override
                public void execute(Task task) {
                    File library = task.getOutputs().getFiles().getSingleFile();
                    File artifact = sourceSet.getOutput().getClassesDirs().getSingleFile();

                    if (AngularJsonHelper.getInstance().artifactUpdated(library, artifact)) {
                        task.getProject().delete(task.getOutputs());
                    }
                }
            });
        });
    }

    private void resolveNodeDependencies(SourceSet sourceSet, Task compileTask) {
        ConfigurationContainer configurations = compileTask.getProject().getConfigurations();
        Configuration compileConfiguration = configurations.getByName(sourceSet.getCompileConfigurationName());

        compileTask.getInputs().files(compileConfiguration);
        compileTask.doFirst(new Action<Task>() {
            @Override
            public void execute(Task task) {
                Project project = task.getProject();
                AngularExtension extension = AngularExtension.get(project);
                ResolvedConfiguration resolvedConfiguration = compileConfiguration.getResolvedConfiguration();

                for (ResolvedArtifact artifact : resolvedConfiguration.getResolvedArtifacts()) {
                    File nodeArtifact = ProjectUtil.getNodeModulesTarget(
                            project,
                            artifact.getModuleVersion().getId().getGroup(),
                            artifact.getName()).toFile();

                    if (AngularJsonHelper.getInstance().artifactUpdated(project, artifact)) {
                        project.delete(nodeArtifact);
                        project.copy(spec -> {
                            spec.from(project.zipTree(artifact.getFile()));
                            spec.into(nodeArtifact);
                        });
                    }
                }
            }
        });
    }

    private void addZip(Configuration configuration, PublishArtifact zipArchive) {
        ConfigurationPublications publications = configuration.getOutgoing();
        publications.getArtifacts().add(zipArchive);
        publications.getAttributes().attribute(ArtifactAttributes.ARTIFACT_FORMAT, ArtifactTypeDefinition.ZIP_TYPE);
    }

    private void registerSoftwareComponents(Project project) {
        ConfigurationContainer configurations = project.getConfigurations();
        AdhocComponentWithVariants component = softwareComponentFactory.adhoc(COMPONENT_NAME);
        component.addVariantsFromConfiguration(configurations.getByName(CONFIGURATION_EXPOSED), new JavaConfigurationVariantMapping("compile", false));
        project.getComponents().add(component);
    }

}
