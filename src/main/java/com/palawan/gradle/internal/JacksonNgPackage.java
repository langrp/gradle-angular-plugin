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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.palawan.gradle.dsl.NgPackage;
import com.palawan.gradle.util.AngularJsonHelper;

import java.io.File;
import java.nio.file.Path;

import static com.palawan.gradle.util.PathUtil.normalize;

/**
 * Implementation of {@link NgPackage} interface using Jackson parser.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class JacksonNgPackage extends JsonBase implements NgPackage {

    private final File file;
    private final Path basePath;
    private final ObjectNode root;

    public JacksonNgPackage(File file, ObjectNode root) {
        this.file = file;
        this.root = root;
        this.basePath = file.getParentFile().toPath();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public boolean setDestination(Path destination) {
        TextNode node = new TextNode(normalize(basePath.relativize(destination)));
        if (root.get("dest") == null || !root.get("dest").asText().equals(node.asText())) {
            root.set("dest", node);
            return true;
        }
        return false;
    }

    @Override
    public void update() {
        AngularJsonHelper.getInstance().updateNgPackageJson(this);
    }

    public ObjectNode getRoot() {
        return root;
    }

}
