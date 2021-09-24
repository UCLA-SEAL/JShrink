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
package com.google.code.configprocessor.util;

import java.io.*;

import com.google.code.configprocessor.log.*;

public class IOUtils {

	private IOUtils() {
	}

	public static void copy(Reader input, Writer output) throws IOException {
		char[] buffer = new char[4096];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
	}

	public static final void forceMkdirs(File file) throws IOException {
		if (file.exists() && file.isFile()) {
			throw new IOException("File [" + file + "] exists and is not a directory. Unable to create directory.");
		}
		if (!file.mkdirs()) {
			throw new IOException("Unable to create directory: " + file);
		}
	}

	public static final void close(Closeable closeable, LogAdapter logAdapter) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				if (logAdapter != null) {
					logAdapter.error("Error closing: " + closeable, e);
				}
			}
		}
	}
}
