/*
 * MIT License
 *
 * Copyright (c) 2022 Petr Langr
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
 *
 */

package com.palawan.gradle.tasks;

import com.palawan.gradle.dsl.AngularExtension;
import com.palawan.gradle.internal.ExecutableData;
import com.palawan.gradle.util.ProjectUtil;
import org.gradle.api.tasks.OutputDirectory;

import java.io.File;
import java.util.List;

/**
 * Installs angular dependency
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class AngularInstall extends PackagerTask {

    /** Angular CLI package name */
    public static final String ANGULAR_PACKAGE_NAME = "@angular/cli";

    /**
     * Task execution method
     */
    @Override
    protected ExecutableData executableData(List<String> arguments) {
        AngularExtension extension = AngularExtension.get(getProject());
        setWorkingDir(getProject().getProjectDir());
        String version = extension.getVersion() == null ? "latest" : extension.getVersion();

        return super.executableData(List.of(packager.get().getAddCommand(), ANGULAR_PACKAGE_NAME + "@" + version));
    }

    @OutputDirectory
    public File getAngularPackageFile() {
        return ProjectUtil.getNodeModules(getProject()).resolve(ANGULAR_PACKAGE_NAME).toFile();
    }
}
