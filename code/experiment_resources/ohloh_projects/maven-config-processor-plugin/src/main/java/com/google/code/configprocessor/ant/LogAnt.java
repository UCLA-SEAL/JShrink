/*
 * Copyright (C) 2009 Leandro de Oliveira Aparecido <lehphyro@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.code.configprocessor.ant;

import org.apache.tools.ant.*;

import com.google.code.configprocessor.log.*;

public class LogAnt implements LogAdapter {

	private Task task;

	public LogAnt(Task task) {
		this.task = task;
	}

	public void debug(String msg) {
		task.log(msg, Project.MSG_DEBUG);
	}

	public void error(String msg, Throwable t) {
		task.log(msg, t, Project.MSG_ERR);
	}

	public void info(String msg) {
		task.log(msg, Project.MSG_INFO);
	}

	public void warn(String msg) {
		task.log(msg, Project.MSG_WARN);
	}

	public void verbose(String msg) {
		task.log(msg, Project.MSG_VERBOSE);
	}

}
