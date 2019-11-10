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

package com.palawan.gradle.internal;

import com.palawan.gradle.dsl.SourceSet;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.SourceSetOutput;
import org.gradle.util.GUtil;

/**
 * Implementation of {@link SourceSet} for angular project as defined
 * in by {@link com.palawan.gradle.dsl.AngularJsonProject}.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public class AngularProjectSourceSet implements SourceSet {

    /** Angular language */
    private static final String LANG = "angular";
    /** Compile task name base */
    private static final String COMPILE_TASK_BASE = "compile";
    /** Distribution task suffix */
    private static final String DISTRIBUTION_SUFFIX = "distZip";
    /** Configuration base name */
    public static final String CONFIGURATION_BASE = "angular";
    /** Publish task base name */
    public static final String PUBLISH = "publish";
    /** Publish to node modules suffix */
    public static final String NODE_MODULES = "ToNodeModules";

    private final String name;
    private final String baseName;
    private final String displayName;
    private FileCollection compilePath;
    private final SourceDirectorySet source;
    private SourceSetOutput output;

    public AngularProjectSourceSet(String name, ObjectFactory objectFactory) {
        this.name = name;
        this.baseName = name.equals("main") ? "" : GUtil.toLowerCamelCase(name);
        this.displayName = GUtil.toWords(name);
        this.source = objectFactory.sourceDirectorySet("src", displayName + " Angular source");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBaseName() {
        return baseName.isEmpty() ? "main" : baseName;
    }

    @Override
    public FileCollection getCompilePath() {
        return compilePath;
    }

    @Override
    public void setCompilePath(FileCollection compilePath) {
        this.compilePath = compilePath;
    }

    @Override
    public String getCompileConfigurationName() {
        return configurationNameOf(CONFIGURATION_BASE);
    }

    @Override
    public String getCompileTaskName() {
        return getTaskName(COMPILE_TASK_BASE, LANG);
    }

    @Override
    public String getDistributionTaskName() {
        return getTaskName("", DISTRIBUTION_SUFFIX);
    }

    @Override
    public String getPublishToNodeModulesTaskName() {
        return getTaskName(PUBLISH, NODE_MODULES);
    }

    @Override
    public SourceDirectorySet getDirectory() {
        return source;
    }

    @Override
    public SourceSetOutput getOutput() {
        return output;
    }

    @Override
    public void setOutput(SourceSetOutput output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "source set " + displayName;
    }

    private String getTaskName(String taskName, String suffix) {
        return GUtil.toLowerCamelCase(taskName + " " + baseName + " " + suffix);
    }

    private String configurationNameOf(String baseName) {
        return uncapitalize(this.baseName + capitalize(baseName));
    }

    static String capitalize(String str) {
        return str.length() != 0 ?
            Character.toTitleCase(str.charAt(0)) + str.substring(1) :
            str;
    }

    static String uncapitalize(String str) {
        return str.length() != 0 ?
            Character.toLowerCase(str.charAt(0)) + str.substring(1) :
            str;
    }

}
