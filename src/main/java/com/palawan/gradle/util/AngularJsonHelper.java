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

package com.palawan.gradle.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.palawan.gradle.dsl.AngularExtension;
import com.palawan.gradle.dsl.AngularJson;
import com.palawan.gradle.dsl.NgPackage;
import com.palawan.gradle.dsl.PackageJson;
import com.palawan.gradle.internal.JacksonAngularJson;
import com.palawan.gradle.internal.JacksonNgPackage;
import com.palawan.gradle.internal.JacksonPackageJson;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedArtifact;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author Langr, Petr
 * @since 1.0.0
 */
public class AngularJsonHelper {

    private static final AngularJsonHelper INSTANCE = new AngularJsonHelper();
    /** Angular build script file name */
    public static final String ANGULAR_JSON_FILE_NAME = "angular.json";
    /** NodeJS library package descriptor file name */
    public static final String NODE_LIBRARY_DESCRIPTOR = "package.json";
    /** Gradle build timestamp file */
    public static final String TIMESTAMP_FILE = "timestamp.gradle";

    /**
     * Singleton instance of helper for angular descriptor operations
     * @return  Singleton instance
     */
    public static AngularJsonHelper getInstance() {
        return INSTANCE;
    }

    private final ObjectMapper mapper;

    private AngularJsonHelper() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Locates angular build script for given project. The method
     * may throw {@link GradleException} if no build script is found
     * or the file is not able to read.
     * @param project   Project for which angular descriptor is fetched
     * @return          Angular descriptor
     */
    public Optional<AngularJson> getAngularJson(Project project) {
        try {
            File file = ProjectUtil.getTopLevelProject(project).file(ANGULAR_JSON_FILE_NAME);
            if (!file.exists()) {
                return Optional.empty();
            }

            return Optional.of(new JacksonAngularJson(file, (ObjectNode) mapper.readTree(file)));

        } catch (IOException e) {
            throw new GradleException("Unable to read "+ANGULAR_JSON_FILE_NAME+" file");
        }
    }

    /**
     * Locates and returns library descriptor for angular sub-project.
     * @param ngPackage Build script of required library
     * @return          Parsed descriptor
     */
    public NgPackage getNgPackageJson(File ngPackage) {
        try {
            if (!ngPackage.exists()) {
                throw new GradleException("Angular library '" + ngPackage.getName() + "' does not exist.");
            }

            return new JacksonNgPackage(ngPackage, (ObjectNode) mapper.readTree(ngPackage));

        } catch (IOException e) {
            throw new GradleException("Unable to read angular.json file");
        }
    }

    /**
     * Tries to locate package json file under given directory.
     * @param directory Directory to locate package json file
     * @return  Optional implementation of package json interface
     */
    public Optional<PackageJson> getPackageJson(Path directory) {
        Path packagePath = directory.resolve(NODE_LIBRARY_DESCRIPTOR);

        try {
            if (Files.exists(packagePath)) {
                File file = packagePath.toFile();
                return Optional.of(new JacksonPackageJson((ObjectNode) mapper.readTree(file), file));
            }

            return Optional.empty();
        } catch (IOException e) {
            throw new GradleException(String.format("Unable to read '%s' file", packagePath));
        }
    }

    /**
     * Updates angular project descriptor file with given parsed object.
     * @param angularJson   Parsed and updated descriptor data
     */
    public void updateAngularJson(AngularJson angularJson) {
        JacksonAngularJson angular = (JacksonAngularJson) angularJson;

        try {
            mapper.writerWithDefaultPrettyPrinter()
                .writeValue(angular.getFile(), angular.getRoot());

        } catch (IOException e) {
            throw new GradleException("Unable to modify angular.json");
        }
    }

    /**
     * Updates angular sub-project descriptor
     * @param ngPackage Parsed and update descriptor data
     */
    public void updateNgPackageJson(NgPackage ngPackage) {
        JacksonNgPackage json = (JacksonNgPackage) ngPackage;

        try {
            mapper.writerWithDefaultPrettyPrinter()
                .writeValue(json.getFile(), json.getRoot());

        } catch (IOException e) {
            throw new GradleException("Unable to modify " + ngPackage.getFile());
        }
    }

    /**
     * Updates package json file descriptor
     * @param packageJson   Package json to be updated
     */
    public void updatePackageJson(PackageJson packageJson) {
        JacksonPackageJson json = (JacksonPackageJson) packageJson;

        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(json.getFile(), json.getRoot());

        } catch (IOException e) {
            throw new GradleException("Unable to modify " + json.getFile());
        }
    }

    /**
     * Reads, process and updates json based file.
     * @param jsonFile  Json file to be updated
     * @param updater   Function which accepts parsed Json and returns
     *                  {@code true} if file needs to be updated.
     */
    public void updateJsonBaseFile(Path jsonFile, Function<ObjectNode, Boolean> updater) {
        File file = jsonFile.toFile();

        try {
            ObjectNode root = (ObjectNode) mapper.readTree(file);
            if (updater.apply(root)) {
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValue(file, root);
            }

        } catch (IOException e) {
            throw new GradleException("Unable to read and update " + jsonFile, e);
        }
    }

    /**
     * Verify whether artifact in given location {@code artifactFolder}
     * was changed against to node_modules artifact at {@code libraryLocation}
     * @param libraryLocation   Library node module location
     * @param artifactFolder    New artifact location
     * @return                  Returns {@code true} if module version/timestamp
     *                          differs
     */
    public boolean artifactUpdated(File libraryLocation, File artifactFolder) {
        Path libraryPackagePath = Paths.get(libraryLocation.toString(), NODE_LIBRARY_DESCRIPTOR);
        Path artifactPackagePath = Paths.get(artifactFolder.toString(), NODE_LIBRARY_DESCRIPTOR);
        Path libraryTimestampPath = Paths.get(libraryLocation.toString(), TIMESTAMP_FILE);
        Path artifactTimestampPath = Paths.get(artifactFolder.toString(), TIMESTAMP_FILE);

        if (!Files.exists(libraryPackagePath) || !Files.exists(artifactPackagePath)) {
            return true;
        }

        try {
            String libraryVersion = getPackageVersion(libraryPackagePath.toFile()).orElse(null);
            String artifactVersion = getPackageVersion(artifactPackagePath.toFile()).orElse(null);

            if (Objects.equals(libraryVersion, artifactVersion)) {
                String libStamp = new String(Files.readAllBytes(libraryTimestampPath), StandardCharsets.UTF_8);
                String artStamp = new String(Files.readAllBytes(artifactTimestampPath), StandardCharsets.UTF_8);
                return Files.exists(libraryTimestampPath) && Files.exists(artifactTimestampPath) &&
                        ! Objects.equals(libStamp, artStamp);
            }

        } catch (IOException e) {
            throw new GradleException("Unable to read NodeJS library from " + libraryLocation);
        }

        return true;

    }

    /**
     * Verify whether given resolved artifact {@code artifact} differs with
     * node module stored in node_modules for the artifact.
     * @param project   Project instance to help process compressed files
     * @param extension Angular extension to define node module location
     * @param artifact  Updated artifact
     * @return Returns {@code true} if module version/timestamp differs
     */
    @SuppressWarnings("UnstableApiUsage")
    public boolean artifactUpdated(Project project, AngularExtension extension, ResolvedArtifact artifact) {
        File libraryLocation = extension.getNodeModulesTarget(
                artifact.getModuleVersion().getId().getGroup(),
                artifact.getName());
        if (ArtifactTypeDefinition.ZIP_TYPE.equals(artifact.getType())) {
            Optional<File> artifactFile = project.zipTree(artifact.getFile()).getFiles().stream()
                    .filter(f -> NODE_LIBRARY_DESCRIPTOR.equals(f.getName()))
                    .map(File::getParentFile)
                    .reduce((f1, f2) -> f1.toPath().getNameCount() < f2.toPath().getNameCount() ? f1 : f2);
            return artifactFile.map(file -> artifactUpdated(libraryLocation, file)).orElse(true);

        } else if (ArtifactTypeDefinition.DIRECTORY_TYPE.equals(artifact.getType())) {
            return artifactUpdated(libraryLocation, artifact.getFile());
        }
        return true;
    }

    /**
     * Generate build timestamp file to recognize changed
     * version in snapshot builds.
     * @param project   Name of project
     * @param baseDir   Destination folder
     */
    public void generateTimestamp(String project, File baseDir) {
        try {
            Files.write(
                    Paths.get(baseDir.toString(), TIMESTAMP_FILE),
                    Long.toString(Instant.now().getEpochSecond()).getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            throw new GradleException("Unable to write timestamp of '" + project + "' build.");
        }
    }

    /**
     * Gets resource as string. Useful for small resource files
     * @param path  Resource path
     * @return      Resource file content as string
     */
    public String resourceToString(String path) {
        URL input = getClass().getResource(path);
        try(InputStream stream = input.openStream()) {
            char[] buffer = new char[1445];
            StringBuilder builder = new StringBuilder();
            InputStreamReader in = new InputStreamReader(stream, StandardCharsets.UTF_8);

            int read;
            while ((read = in.read(buffer)) > 0) {
                builder.append(buffer, 0, read);
            }

            return builder.toString();

        } catch (IOException e) {
            throw new GradleException("Unable to read url: " + input, e);
        }
    }

    private Optional<String> getPackageVersion(File file) throws IOException {
        return Optional.ofNullable(mapper.readTree(file))
                .map(r -> r.get("version"))
                .map(JsonNode::asText);
    }

}
