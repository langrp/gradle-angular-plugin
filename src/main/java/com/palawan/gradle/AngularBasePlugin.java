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

import com.palawan.gradle.dsl.NodeExtension;
import com.palawan.gradle.tasks.AngularInstall;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.PluginContainer;
import com.palawan.gradle.dsl.AngularExtension;
import com.palawan.gradle.dsl.AngularJson;
import com.palawan.gradle.tasks.AngularInit;
import com.palawan.gradle.tasks.AngularCli;
import com.palawan.gradle.util.AngularJsonHelper;
import com.palawan.gradle.util.ProjectUtil;

import javax.inject.Inject;
import java.util.Optional;

/**
 * @author Langr, Petr
 * @since 1.0.0
 */
public class AngularBasePlugin implements Plugin<Project> {

    /** Angular task group */
    public static final String ANGULAR_GROUP = "Angular";
    /** Angular extension name */
    private static final String EXTENSION_NAME = "angular";

    /** Angular install task name */
    private static final String INSTALL_TASK = "ngInstall";
    /** Angular initialize task name */
    private static final String INITIALIZE_TASK = "ngInit";
    /** Angular CLI task name */
    private static final String CLI_COMMAND_TASK = "ng";


    private final ObjectFactory objectFactory;

    @Inject
    public AngularBasePlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(Project project) {
        applyPlugins(project);
        addExtension(project);

        configureAngularInstallTask(project);
        configureInitializeTask(project);
        configureAngularCli(project);
        evaluateVersions(project);
    }

    private void applyPlugins(Project project) {
        PluginContainer plugins = project.getPlugins();
        plugins.apply(BasePlugin.class);
        plugins.apply(NodePlugin.class);
    }

    private void addExtension(Project project) {
        NodeExtension nodeExtension = project.getExtensions().getByType(NodeExtension.class);
        Optional<AngularJson> angularJson = AngularJsonHelper.getInstance().getAngularJson(project);
        project.getExtensions().create(EXTENSION_NAME, AngularExtension.class,
                                       nodeExtension,
                                       angularJson.orElse(null),
                                       objectFactory);

        if (!ProjectUtil.isTopLevelAngularProject(project)) {
            ProjectUtil.getTopLevelProject(project)
                    .getExtensions()
                    .getByType(AngularExtension.class)
                    .configureNode(nodeExtension);
        }
    }

    private void configureAngularInstallTask(Project project) {
        if (ProjectUtil.isTopLevelAngularProject(project)) {
            project.getTasks().register(INSTALL_TASK, AngularInstall.class, t -> {
                t.setGroup(ANGULAR_GROUP);
                t.setDescription("Installs Angular using preferred package manager");
            });
        }
    }

    private void configureInitializeTask(Project project) {
        if (ProjectUtil.isTopLevelAngularProject(project)) {
            project.getTasks().register(INITIALIZE_TASK, AngularInit.class, t -> {
                t.setGroup(ANGULAR_GROUP);
                t.setDescription("Initializes angular project.");
                t.dependsOn(INSTALL_TASK);
            });
        }
    }

    private void configureAngularCli(Project project) {
        if (ProjectUtil.isTopLevelAngularProject(project)) {
            project.getExtensions().getExtraProperties().set(AngularCli.class.getSimpleName(), AngularCli.class);
            project.getTasks().register(CLI_COMMAND_TASK, AngularCli.class, t -> {
                t.setGroup(ANGULAR_GROUP);
                t.setDescription("Executes angular-cli command '--cmd' with additional arguments '--args'");
                t.dependsOn(INSTALL_TASK);
            });
        }
    }

    private void evaluateVersions(Project project) {
        project.afterEvaluate(p -> {
            AngularExtension extension = AngularExtension.get(p);
            if (extension.getAngularJson() != null) {
                if (ProjectUtil.isTopLevelAngularProject(p)) {
                    extension.getAngularJson().getProjects().values().stream()
                            .filter(a -> !p.getChildProjects().containsKey(a.getName()))
                            .forEach(a -> a.setVersion(p.getVersion().toString()));
                } else {
                    extension.getAngularJson().getProject(p.getName())
                            .ifPresent(a -> a.setVersion(p.getVersion().toString()));
                }
            }
        });
    }

}
