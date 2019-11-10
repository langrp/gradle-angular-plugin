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

import com.moowork.gradle.node.NodeExtension;
import com.moowork.gradle.node.variant.Variant;
import groovy.lang.Closure;
import groovy.transform.Generated;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import com.palawan.gradle.internal.AngularSourceSetContainer;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Angular extension for gradle build file.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
@SuppressWarnings("UnstableApiUsage")
public class AngularExtension {

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
    @Nullable
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

    /**
     * Get complete location of "node_modules"
     * @return node_modules path
     */
    public File getNodeModules() {
        return new File(getNodeModulesDir(), NODE_MODULES);
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
    public File getNodeModulesTarget(String artifactGroup, String artifactName) {
        Path nodeArtifactPath;
        if (getGroup() == null) {
            nodeArtifactPath = Paths.get(getNodeModules().toString(), artifactGroup, artifactName);
        } else {
            nodeArtifactPath = Paths.get(getNodeModules().toString(), getGroup(), artifactName);
        }
        return nodeArtifactPath.toFile();
    }

    @Generated
    public File getWorkDir() {
        return nodeExtension.getWorkDir();
    }

    @Generated
    public void setWorkDir(File workDir) {
        nodeExtension.setWorkDir(workDir);
    }

    @Generated
    public File getNpmWorkDir() {
        return nodeExtension.getNpmWorkDir();
    }

    @Generated
    public void setNpmWorkDir(File npmWorkDir) {
        nodeExtension.setNpmWorkDir(npmWorkDir);
    }

    @Generated
    public File getYarnWorkDir() {
        return nodeExtension.getYarnWorkDir();
    }

    @Generated
    public void setYarnWorkDir(File yarnWorkDir) {
        nodeExtension.setYarnWorkDir(yarnWorkDir);
    }

    /**
     * Gets only parent folder of node_modules
     * @return Parent folder of node_modules
     */
    @Generated
    public File getNodeModulesDir() {
        return nodeExtension.getNodeModulesDir();
    }

    @Generated
    public void setNodeModulesDir(File nodeModulesDir) {
        nodeExtension.setNodeModulesDir(nodeModulesDir);
    }

    @Generated
    public String getNodeVersion() {
        return nodeExtension.getVersion();
    }

    @Generated
    public void setNodeVersion(String version) {
        nodeExtension.setVersion(version);
    }

    @Generated
    public String getNpmVersion() {
        return nodeExtension.getNpmVersion();
    }

    @Generated
    public void setNpmVersion(String npmVersion) {
        nodeExtension.setNpmVersion(npmVersion);
    }

    @Generated
    public String getYarnVersion() {
        return nodeExtension.getYarnVersion();
    }

    @Generated
    public void setYarnVersion(String yarnVersion) {
        nodeExtension.setYarnVersion(yarnVersion);
    }

    @Generated
    public String getDistBaseUrl() {
        return nodeExtension.getDistBaseUrl();
    }

    @Generated
    public void setDistBaseUrl(String distBaseUrl) {
        nodeExtension.setDistBaseUrl(distBaseUrl);
    }

    @Generated
    public String getNpmCommand() {
        return nodeExtension.getNpmCommand();
    }

    @Generated
    public void setNpmCommand(String npmCommand) {
        nodeExtension.setNpmCommand(npmCommand);
    }

    @Generated
    public String getYarnCommand() {
        return nodeExtension.getYarnCommand();
    }

    @Generated
    public void setYarnCommand(String yarnCommand) {
        nodeExtension.setYarnCommand(yarnCommand);
    }

    @Generated
    public boolean getDownload() {
        return nodeExtension.getDownload();
    }

    @Generated
    public boolean isDownload() {
        return nodeExtension.isDownload();
    }

    @Generated
    public void setDownload(boolean download) {
        nodeExtension.setDownload(download);
    }

    @Generated
    public Variant getVariant() {
        return nodeExtension.getVariant();
    }

    @Generated
    public void setVariant(Variant variant) {
        nodeExtension.setVariant(variant);
    }
}
