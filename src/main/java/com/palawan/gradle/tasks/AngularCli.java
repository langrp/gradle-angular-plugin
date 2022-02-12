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

package com.palawan.gradle.tasks;

import com.palawan.gradle.internal.ExecutableData;
import com.palawan.gradle.util.ProjectUtil;
import org.gradle.api.tasks.options.Option;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Task to execute Angular-cli commands from gradle if needed.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class AngularCli extends CommandExecutionTask {

	/**
	 * Angular cli script within node_modules.
	 */
	public static final String ANGULAR_CLI = "bin/ng.js";

	@Override
	protected ExecutableData executableData(List<String> arguments) {
		Path ngScript = ProjectUtil.getNodeModules(getProject())
				.resolve(AngularInstall.ANGULAR_PACKAGE_NAME).resolve(ANGULAR_CLI);

		List<String> args = Stream.concat(Stream.of(ngScript.toString()), arguments.stream())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		ExecutableData executable = getNodeExtension().getNodeManager().executableData(args);

		// Other than main packagers must be available on classpath
		if (getNodeExtension().getDownload()) {
			executable.withPathLocation(packager.get().getExecutableBinDir().toString());
		}

		return executable;
	}

	@Option(
			option = "cmd",
			description = "Command to execute on packager."
	)
	@Override
	public void setCommand(String command) {
		super.setCommand(command);
	}

	@Option(
			option = "args",
			description = "Command arguments."
	)
	@Override
	public void setArguments(List<String> arguments) {
		super.setArguments(arguments);
	}
}
