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

package com.palawan.gradle.dsl;

import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.tasks.SourceSetOutput;

/**
 * Interface representing angular source set. The plugin uses the
 * source set as set of compilable files by angular builder. It
 * can represent angular application or particular angular library.
 * Each source set created will define gradle task to operate on
 * the application or library. This allows to create either project
 * dependencies of main application on library or just task dependency
 * of one source set build to another without having separate gradle
 * project.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public interface SourceSet {

    /**
     * The name of the main source set.
     */
    String SOURCE_SET_MAIN = "main";

    /**
     * Name of angular source. For NodeJS project, this will be either "main"
     * for default project as per package.json, or project name.
     * @return Source set name
     */
    String getName();

    /**
     * Gets base name as used for compile task or any other gradle stuff.
     * @return  Base source set name
     */
    String getBaseName();

    /**
     * Get file collection of compile dependencies that needs to be resolved
     * in order to proceed with compilation of project.
     * @return  File collection for all managed dependencies
     */
    FileCollection getCompilePath();

    /**
     * Set dependency collection for compile task of this component source set.
     * Typically this will be consumed project configuration.
     * @param compilePath Compile task dependency
     */
    void setCompilePath(FileCollection compilePath);

    /**
     * Get name of consumed configuration to be resolved on compile of this
     * component source set.
     * @return  Compile configuration name
     */
    String getCompileConfigurationName();

    /**
     * Get compile task name for this source set. In multi project configuration
     * there are multiple source set therefore compile task is created to each.
     * @return Compile task name for this source set.
     */
    String getCompileTaskName();

    /**
     * Get archive task name to pack this source set output
     * @return Distribution task name
     */
    String getDistributionTaskName();

    /**
     * Get publish to node_modules task name
     * @return  Publishing task name
     */
    String getPublishToNodeModulesTaskName();

    /**
     * Get directory definition of all source files as a input to compile task.
     * @return Source files and directories
     */
    SourceDirectorySet getDirectory();

    /**
     * Get compile output source set.
     * @return Compile output source set
     */
    SourceSetOutput getOutput();

    /**
     * Define compile output source set.
     * @param output Compile output source set
     */
    void setOutput(SourceSetOutput output);

}
