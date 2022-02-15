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
import com.fasterxml.jackson.databind.node.TextNode;
import com.palawan.gradle.dsl.PackageJson;
import com.palawan.gradle.util.AngularJsonHelper;

import java.io.File;
import java.util.Objects;
import java.util.function.BinaryOperator;

/**
 * Implementation of {@link PackageJson} using Jackson parser.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class JacksonPackageJson extends JsonBase implements PackageJson {

	private final File file;
	private final ObjectNode root;

	public JacksonPackageJson(ObjectNode root, File file) {
		this.root = Objects.requireNonNull(root, "No root defined");
		this.file = Objects.requireNonNull(file, "File is required");
	}

	@Override
	public String getVersion() {
		return root.get("version").asText();
	}

	@Override
	public void setVersion(String version) {
		if (!Objects.equals(version, getVersion())) {
			TextNode versionNode = new TextNode(version);
			root.set("version", versionNode);
			AngularJsonHelper.getInstance().updatePackageJson(this);
		}
	}

	@Override
	public void updateScripts(BinaryOperator<String> updater) {
		getByPath(root, "scripts")
				.filter(JsonNode::isObject)
				.ifPresent(n -> n.fields()
						.forEachRemaining(e -> e.setValue(
								new TextNode(updater.apply(e.getKey(), e.getValue().asText()))
						)));

		AngularJsonHelper.getInstance().updatePackageJson(this);
	}

	/**
	 * Gets package file
	 * @return	Package file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * Root implementation
	 * @return	Root
	 */
	public ObjectNode getRoot() {
		return root;
	}

}
