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

import java.io.File;
import java.util.Map;
import java.util.Optional;

/**
 * Interface representing angular.json file and operations done by plugin
 * on this particular file.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public interface AngularJson {

    /**
     * Gets angular.json file
     * @return Angular json file
     */
    File getFile();

    /**
     * Gets default angular project
     * @return Default angular project
     */
    Optional<AngularJsonProject> getDefaultProject();

    /**
     * Gets angular project for given project name
     * @param name Project name
     * @return Angular project
     */
    Optional<AngularJsonProject> getProject(String name);

    /**
     * Gets angular json projects definition
     * @return Angular projects
     */
    Map<String, AngularJsonProject> getProjects();

    /**
     * Updates angular fie with changed values
     */
    void update();

}
