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

import com.moowork.gradle.node.exec.NodeExecRunner;
import com.palawan.gradle.dsl.AngularExtension;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.process.ExecResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Task to execute Angular-cli commands from gradle if needed.
 *
 * @author Langr, Petr
 * @since 1.0.0
 */
public class NgCliTask extends DefaultTask {

	/**
	 * Angular cli script within node_modules.
	 */
	public static final String ANGULAR_CLI = "@angular/cli/bin/ng";

	private String cliCommand;
	private List<String> arguments;

	private ExecResult result;

	@TaskAction
	public void execute() {

		final AngularExtension extension = AngularExtension.get(getProject());
		String ngScript = new File(extension.getNodeModules(), ANGULAR_CLI).toString();

		List<String> cliArguments = new ArrayList<>(arguments == null ? 2 : arguments.size() + 2);
		cliArguments.add(ngScript);
		cliArguments.add(cliCommand);

		if (arguments != null) {
			cliArguments.addAll(arguments);
		}

		NodeExecRunner runner = new NodeExecRunner( getProject() );
		runner.setArguments(cliArguments);
		result = runner.execute();

	}

	@Internal
	public ExecResult getResult() {
		return this.result;
	}

	@Option(
			option = "cmd",
			description = "Defines angular-cli command to execute."
	)
	public NgCliTask setCommand(String command) {
		this.cliCommand = command;
		return this;
	}

	@Option(
			option = "args",
			description = "Define additional angular-cli arguments for the command."
	)
	public NgCliTask setArguments(String arguments) {
		this.arguments = Arrays.asList(arguments.split("\\s+"));
		return this;
	}

}
