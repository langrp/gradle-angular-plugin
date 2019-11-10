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

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Interface representing angular project block from angular.json file and operations
 * done on this particular block of file.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public interface AngularJsonProject {

    enum Type {
        LIBRARY,
        APPLICATION,
        UNKNOWN;

        public static Type from(String lowerCase) {
            if (LIBRARY.toString().equals(lowerCase)) {
                return LIBRARY;
            } else if (APPLICATION.toString().equals(lowerCase)) {
                return APPLICATION;
            }
            return UNKNOWN;
        }

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

    /**
     * Gets project name
     * @return Project name
     */
    String getName();

    /**
     * Gets project type
     * @return Project type
     */
    Type getProjectType();

    /**
     * Gets project root location
     * @return Project root location
     */
    Path getRoot();

    /**
     * Define new project root directory. The method must be called
     * when all files were moved to new location, in order to update
     * all dependent files.
     * @param root New project directory
     */
    void setRoot(Path root);

    /**
     * Gets project source code root location
     * @return Project source location
     */
    Path getSourceRoot();

    /**
     * If specified returns ng-package.json file referenced by
     * build properties
     * @return Packager configuration file
     */
    Optional<NgPackage> getNgPackageFile();

    /**
     * Change output path for this project.
     * @param outputPath New output path
     */
    void setOutputPath(Path outputPath);

    /**
     * Is the project default project in angular.json?
     * @return Returns {@code true} if project is default.
     */
    boolean isDefault();

    /**
     * Gets project version
     * @return Project version
     */
    String getVersion();

    /**
     * Sets new project version;
     * @param version New version
     */
    void setVersion(String version);

    /**
     * Retrieves all defined project files/directories within project.
     * @return  Stream of project files or directories as defined
     * in project section of angular.json file.
     */
    Stream<Path> getProjectFiles();

}
