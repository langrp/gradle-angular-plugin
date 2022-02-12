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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.palawan.gradle.dsl.AngularJsonProject;
import com.palawan.gradle.dsl.NgPackage;
import com.palawan.gradle.dsl.PackageJson;
import com.palawan.gradle.util.AngularJsonHelper;
import groovy.transform.Generated;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.palawan.gradle.util.PathUtil.join;
import static com.palawan.gradle.util.PathUtil.normalize;

/**
 * Implementation of {@link AngularJsonProject} interface using Jackson
 * parser.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class JacksonAngularJsonProject extends JsonBase implements AngularJsonProject {

	// Field location constants
    private static final String ROOT = "root";
    private static final String SOURCE_ROOT = "sourceRoot";
    private static final String PROJECT_TYPE = "projectType";
    private static final String BUILD_PATH = "architect.build";
    private static final String BUILD_OPTIONS_PATH = BUILD_PATH + ".options";
    private static final String BUILD_PROJECT_PATH = BUILD_OPTIONS_PATH + ".project";
    private static final String BUILD_TS_CONFIG_PATH = BUILD_OPTIONS_PATH + ".tsConfig";
    private static final String BUILD_OUTPUT_FIELD = "outputPath";
    private static final String PROD_FILE_REPLACEMENTS_PATH = BUILD_PATH + ".configurations.production.fileReplacements";
    private static final String TEST_OPTIONS_PATH = "architect.test.options";
	private static final String TEST_TS_CONFIG_PATH = TEST_OPTIONS_PATH + ".tsConfig";
	private static final String LINT_TS_CONFIG_PATH = "architect.lint.options.tsConfig";

    private final Path baseDir;
    private final String name;
    private final boolean isDefault;
    private final ObjectNode root;
    private final JacksonAngularJson angularJson;
    private volatile PackageJson packageJson;

    JacksonAngularJsonProject(JacksonAngularJson angularJson, ObjectNode root, String name, Path baseDir, boolean isDefault) {
        this.angularJson = angularJson;
        this.root = root;
        this.name = name;
        this.baseDir = baseDir;
        this.isDefault = isDefault;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Type getProjectType() {
        return Type.from(root.get(PROJECT_TYPE).asText());
    }

    @Override
    public Path getRoot() {
        return Paths.get(baseDir.toString(), root.get(ROOT).asText());
    }

    @Override
	@Generated // exclude from JaCoCo
    public void setRoot(Path root) {
        if (!getRoot().equals(root)) {
            updateRootPath(root);
            angularJson.update();
        }
    }


    @Override
    public Path getSourceRoot() {
        JsonNode source = root.get(SOURCE_ROOT);
        if (source == null) {
            source = root.get(ROOT);
        }
        return Paths.get(baseDir.toString(), source.asText());
    }

    @Override
    public Optional<NgPackage> getNgPackageFile() {
        return getByPath(root, BUILD_PROJECT_PATH)
            .map(JsonNode::asText)
            .map(p -> Paths.get(baseDir.toString(), p).toFile())
            .map(AngularJsonHelper.getInstance()::getNgPackageJson);
    }

	/**
	 * Defines output path for compiled result. The configuration
	 * location differs for application and library. Library typically
	 * defines its own package.json file, which is referenced from
	 * project definition under {@link #BUILD_PROJECT_PATH}.
	 * @param outputPath New output path
	 * @see AngularJsonProject#setOutputPath(Path)
	 */
    @Override
    public void setOutputPath(Path outputPath) {
        Optional<NgPackage> ngPackage = getNgPackageFile();
        if (ngPackage.isPresent()) {
            NgPackage p = ngPackage.get();
            if (p.setDestination(outputPath)) {
                p.update();
            }
        } else {
            defineOutputPath(outputPath);
        }
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public String getVersion() {
        return getPackageJson()
                .map(PackageJson::getVersion)
                .orElse("unknown");
    }

    @Override
    public void setVersion(String version) {
        getPackageJson().ifPresent(p -> p.setVersion(version));
    }

    @Override
    public Stream<Path> getProjectFiles() {
        return Stream.concat(
                Stream.of(getRoot(), getSourceRoot(), baseDir.resolve("e2e"), baseDir.resolve("tslint.json")),
                getKnownFileFields().stream().map(this::fieldValueToPath)
        ).filter(Objects::nonNull);
    }

	/**
	 * Defines output path defines within angular.json file project
	 * block.
	 * @param outputPath New output path
	 */
	private void defineOutputPath(Path outputPath) {
        final TextNode outputNode = new TextNode(normalize(baseDir.relativize(outputPath)));
        getByPath(root, BUILD_OPTIONS_PATH)
            .filter(ObjectNode.class::isInstance)
            .map(ObjectNode.class::cast)
            .filter(n -> !outputNode.asText().equals(n.get(BUILD_OUTPUT_FIELD).asText()))
            .ifPresent(n -> {
                n.set(BUILD_OUTPUT_FIELD, outputNode);
                angularJson.update();
            });
    }

	/**
	 * Gets package.json file descriptor used to read project version
	 * for application project type. Library typically defines version
	 * within its own file package.json. This method receives parsed
	 * package.json file based on project type.
	 * @return	If available package.json for application or library
	 */
	private Optional<PackageJson> getPackageJson() {
        Optional<PackageJson> local = Optional.ofNullable(packageJson);
        if (!local.isPresent()) {
            if (Type.LIBRARY.equals(getProjectType())) {
                local = AngularJsonHelper.getInstance().getPackageJson(getRoot());
            } else {
                local = AngularJsonHelper.getInstance().getPackageJson(baseDir);
            }
            packageJson = local.orElse(null);
        }
        return local;
    }

	/**
	 * Updates project root path for new given path. This is complex
	 * operation, which requires to update all nested properties
	 * within project block. Those fields may not be completely
	 * identified automatically and may require manual update.
	 * @param path	New project root path
	 */
	@Generated // exclude from JaCoCo
	private void updateRootPath(Path path) {
        String oldPath = root.get(ROOT).asText();
        String newPath = normalize(baseDir.relativize(path));

        root.set(ROOT, new TextNode(newPath));
        updatePath(oldPath, newPath, root, SOURCE_ROOT);

        for (String fieldPath : getKnownFileFields()) {
            updatePath(oldPath, newPath, fieldPath);
            updateExtendingJson(baseDir.resolve(oldPath), path, fieldPath);
        }

        updatePath(oldPath, newPath, PROD_FILE_REPLACEMENTS_PATH);

    }

	/**
	 * Updates value of given project block json field defined as path
	 * {@code fieldPath}. Updated value is based on old root path and
	 * new root path, since project uses relative paths.
	 * @param oldPath	Old project root directory
	 * @param newPath	New project root directory
	 * @param fieldPath Json field path notation
	 */
	@Generated // exclude from JaCoCo
    private void updatePath(String oldPath, String newPath, String fieldPath) {
        String fieldName = fieldPath.substring(fieldPath.lastIndexOf('.') + 1);
        String parentElement = fieldPath.substring(0, fieldPath.lastIndexOf('.'));
        Optional<JsonNode> parent = getByPath(root, parentElement);
        if (parent.isPresent()) {
			JsonNode node = parent.get().get(fieldName);
			if (node != null && node.isArray()) {
				updatePath(oldPath, newPath, (ArrayNode) node);
			} else if (parent.get().isObject()) {
                updatePath(oldPath, newPath, (ObjectNode) parent.get(), fieldName);
            }
        }
    }

	@Generated // exclude from JaCoCo
    private void updatePath(String oldPath, String newPath, ArrayNode parent) {
        for (int i = 0; i < parent.size(); i++) {
            JsonNode currentNode = parent.get(i);
            if (currentNode.isObject()) {
                ObjectNode objectNode = (ObjectNode) currentNode;
                Iterator<String> fields = currentNode.fieldNames();
                while (fields.hasNext()) {
                    updatePath(oldPath, newPath, objectNode, fields.next());
                }
            } else {
                String old = currentNode.asText();
                if (old.startsWith(oldPath)) {
					parent.set(i, new TextNode(join(newPath, old.substring(oldPath.length()))));
                } else {
                    parent.set(i, new TextNode(join(newPath, old)));
                }
            }
        }
    }

	@Generated // exclude from JaCoCo
    private void updatePath(String oldPath, String newPath, ObjectNode parent, String fieldName) {
        String old = parent.get(fieldName).asText();
        if (old.startsWith(oldPath)) {
            parent.set(fieldName, new TextNode(join(newPath, old.substring(oldPath.length()))));
        } else {
            parent.set(fieldName, new TextNode(join(newPath, old)));
        }
    }

	/**
	 * Updates dependent files for change of project root directory
	 */
	@Generated // exclude from JaCoCo
	private void updateExtendingJson(Path oldPath, Path newPath, String fieldPath) {
		getByPath(root, fieldPath)
				.filter(JsonNode::isTextual)
                .map(JsonNode::asText)
                .filter(n -> n.endsWith("json"))
				.ifPresent(n -> updateExtendingJsonFile(oldPath, newPath, n));
	}

	@Generated // exclude from JaCoCo
	private void updateExtendingJsonFile(Path oldPath, Path newPath, String file) {
		final Path tsConfigPath = baseDir.resolve(file);
		Function<ObjectNode, Boolean> updater = n -> {
			if (n.get("extends") != null) {
				Path updated = newPath.relativize(oldPath.resolve(n.get("extends").asText()));
				n.set("extends", new TextNode(normalize(updated)));
				return true;
			}
			return false;
		};
		AngularJsonHelper.getInstance()
				.updateJsonBaseFile(tsConfigPath, updater);
	}

    @Nullable
	@Generated // exclude from JaCoCo
    private Path fieldValueToPath(String fieldPath) {
        return getByPath(root, fieldPath)
				.map(JsonNode::asText)
				.filter(s -> !s.isEmpty())
				.map(baseDir::resolve)
				.orElse(null);
    }

    private static List<String> getKnownFileFields() {
        return Arrays.asList(
                BUILD_OPTIONS_PATH + ".index",
                BUILD_OPTIONS_PATH + ".main",
                BUILD_OPTIONS_PATH + ".polyfills",
                BUILD_TS_CONFIG_PATH,
                BUILD_OPTIONS_PATH + ".assets",
                BUILD_OPTIONS_PATH + ".styles",
                BUILD_OPTIONS_PATH + ".scripts",
                TEST_OPTIONS_PATH + ".main",
                TEST_OPTIONS_PATH + ".polyfills",
                TEST_TS_CONFIG_PATH,
                TEST_OPTIONS_PATH + ".karmaConfig",
                TEST_OPTIONS_PATH + ".assets",
                TEST_OPTIONS_PATH + ".styles",
                TEST_OPTIONS_PATH + ".scripts",
                "architect.lint.options.tsConfig",
                "architect.e2e.options.protractorConfig"
        );
    }

}
