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

import com.palawan.gradle.internal.AngularSourceSetContainer;
import groovy.lang.Closure;
import groovy.transform.Generated;
import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;

import javax.annotation.Nullable;
import java.nio.file.Path;

/**
 * Angular extension for gradle build file.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class AngularExtension {
    /** Node modules directory name */
    public static final String NODE_MODULES = "node_modules";

    /**
     * Extract angular extension from the project.
     * @param project   Angular extension
     * @return  Extracted angular extension
     */
    public static AngularExtension get(Project project) {
        AngularExtension extension = project.getExtensions().findByType(AngularExtension.class);
        if (extension == null) {
            throw new GradleException("Unable to get AngularExtension");
        }

        return extension;
    }

    private String version;
    private String group;
    private String output;
    private final AngularJson angularJson;
    private final NodeExtension nodeExtension;
    private final SourceSetContainer sources;
    private Action<NodeExtension> nodeExtensionAction;

    public AngularExtension(NodeExtension nodeExtension, AngularJson angularJson, ObjectFactory objectFactory) {
        this.nodeExtension = nodeExtension;
        this.angularJson = angularJson;
        this.sources = objectFactory.newInstance(AngularSourceSetContainer.class);
    }

    /**
     * Get value of version
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set value for property version
     *
     * @param version Set value of version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get value of group
     *
     * @return group
     */
    @Nullable
    public String getGroup() {
        return group;
    }

    /**
     * Set value for property group
     *
     * @param group Set value of group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Get value of output
     *
     * @return output
     */
    @Nullable
    public String getOutput() {
        return output;
    }

    /**
     * Set value for property output
     *
     * @param output Set value of output
     */
    public void setOutput(String output) {
        this.output = output;
    }

    /**
     * Get value of angularJson
     *
     * @return angularJson
     */
    @Nullable
    public AngularJson getAngularJson() {
        return angularJson;
    }

    /**
     * Get value of sources
     *
     * @return sources
     */
    public SourceSetContainer getSources() {
        return sources;
    }

    /**
     * Configure sources with given closure
     * @param closure Configurer
     * @return  Configured result?
     */
    public Object sources(Closure<SourceSetContainer> closure) {
        return sources.configure(closure);
    }

    public void node(Action<NodeExtension> action) {
        action.execute(nodeExtension);
        nodeExtensionAction = action;
    }

    public void configureNode(NodeExtension extension) {
        if (nodeExtensionAction != null) {
            nodeExtensionAction.execute(extension);
        }
    }

    @Generated
    public Boolean getDownload() {
        return nodeExtension.getDownload();
    }

    @Generated
    public Path getWorkingDirPath() {
        return nodeExtension.getWorkingDirPath();
    }
}
