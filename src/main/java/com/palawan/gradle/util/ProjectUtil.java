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

package com.palawan.gradle.util;

import com.palawan.gradle.AngularBasePlugin;
import com.palawan.gradle.dsl.AngularExtension;
import org.gradle.api.Project;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Project utility methods.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class ProjectUtil {

    private ProjectUtil() {}

    /**
     * Get complete location of "node_modules" for the project.
     * @return node_modules path
     */
    public static Path getNodeModules(Project project) {
        return getTopLevelProject(project).getProjectDir().toPath().resolve(AngularExtension.NODE_MODULES);
    }

    /**
     * Gets target location inside node_modules directory for given artifact.
     * Artifact group is used here to define parent folder name. This behavior
     * can be overridden by configuration of this extension group attribute.
     * @param artifactGroup Artifact group, used only if this extension does
     *                      not define group.
     * @param artifactName  Artifact name
     * @return              Location of given artifact
     */
    public static Path getNodeModulesTarget(Project project, String artifactGroup, String artifactName) {
        AngularExtension extension = AngularExtension.get(project);
        Path nodeArtifactPath;
        if (extension.getGroup() == null) {
            nodeArtifactPath = Paths.get(getNodeModules(project).toString(), artifactGroup, artifactName);
        } else {
            nodeArtifactPath = Paths.get(getNodeModules(project).toString(), extension.getGroup(), artifactName);
        }
        return nodeArtifactPath;
    }

    /**
     * Get the most higher gradle project in its hierarchy. The method
     * returns highest parent of given project for multi-project
     * configuration. Otherwise it returns the same project.
     * @param project   Project to get highest parent from
     * @return  The highest level project
     */
    public static Project getTopLevelProject(Project project) {

        Project root = project;

        while (project != null) {
            project = project.getParent();
            if (project != null && project.getPlugins().hasPlugin(AngularBasePlugin.class)) {
                root = project;
            }
        }

        return root;
    }

    /**
     * Is the given project top level project. Method has usage
     * specifically for multi-project configuration.
     * @param project   Project to test for top level.
     * @return  Returns {@code true} if given project is top
     * level project.
     */
    public static boolean isTopLevelProject(Project project) {
        return project == project.getRootProject();
    }

	/**
	 * Is the given project top level project and also angular project.
	 * Method has usage specifically for multi-project configuration.
	 * @param project	Project to test for top level.
	 * @return	Returns {@code true} if given project is top level
	 * and angular-base plugin is applied.
	 */
	public static boolean isTopLevelAngularProject(Project project) {
    	return getTopLevelProject(project) == project;
	}

}
