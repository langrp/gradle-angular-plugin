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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.palawan.gradle.dsl.AngularJson;
import com.palawan.gradle.dsl.AngularJsonProject;
import com.palawan.gradle.util.AngularJsonHelper;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

/**
 * Implementation of {@link AngularJson} interface using Jackson parser.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class JacksonAngularJson extends JsonBase implements AngularJson {

    private final File file;
    private final ObjectNode root;
    private Map<String, AngularJsonProject> projects;

    public JacksonAngularJson(File file, ObjectNode root) {
        this.file = file;
        this.root = root;
        this.projects = new HashMap<>();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public Optional<AngularJsonProject> getDefaultProject() {
        return getDefaultProjectName().flatMap(this::getProject);
    }

    @Override
    public Map<String, AngularJsonProject> getProjects() {
        ObjectNode projectNode = (ObjectNode) root.get("projects");
        Iterator<String> names = projectNode.fieldNames();

        while (names.hasNext()) {
            projects.computeIfAbsent(names.next(), this::loadProject);
        }

        return Collections.unmodifiableMap(projects);
    }

    @Override
    public void update() {
        AngularJsonHelper.getInstance().updateAngularJson(this);
    }

    @Override
    public Optional<AngularJsonProject> getProject(String name) {
        return Optional.ofNullable(projects.computeIfAbsent(name, this::loadProject));
    }

    @Nullable
    private AngularJsonProject loadProject(String name) {
        return getByPath(root, "projects." + name)
            .map(ObjectNode.class::cast)
            .map(r -> new JacksonAngularJsonProject(
                    this,
                    r,
                    name,
                    file.getParentFile().toPath(),
                    getDefaultProjectName().filter(name::equals).isPresent())
            ).orElse(null);
    }

    public ObjectNode getRoot() {
        return root;
    }

    private Optional<String> getDefaultProjectName() {
        return Optional.ofNullable(root.get("defaultProject")).map(JsonNode::asText);
    }

}
