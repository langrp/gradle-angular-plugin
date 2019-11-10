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

import org.gradle.api.Project;

/**
 * Project utility methods.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class ProjectUtil {

    private ProjectUtil() {}

    /**
     * Get the most higher gradle project in its hierarchy. The method
     * returns highest parent of given project for multi-project
     * configuration. Otherwise it returns the same project.
     * @param project   Project to get highest parent from
     * @return  The highest level project
     */
    public static Project getTopLevelProject(Project project) {

        while (!isTopLevelProject( project )) {
            project = project.getRootProject();
        }

        return project;
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

}
