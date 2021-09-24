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

import static com.google.code.configprocessor.util.IOUtils.*;

import java.io.*;
import java.util.*;

import com.google.code.configprocessor.*;
import com.google.code.configprocessor.log.*;

public class PropertiesUtils {

	private PropertiesUtils() {
	}

	/**
	 * Read additional properties file if specified.
	 * 
	 * @param input
	 * @param logAdapter
	 * 
	 * @return Properties read or null if not specified or file is empty.
	 * @throws ConfigProcessorException If processing cannot be performed.
	 */
	public static final Properties loadIfPossible(File input, LogAdapter logAdapter) throws ConfigProcessorException {
		Properties additional = null;

		if (input == null) {
			return additional;
		}

		if (!input.exists()) {
			throw new ConfigProcessorException("Additional properties file [" + input + "] does not exist");
		}

		additional = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(input);
			additional.load(fis);
			
			if (additional.isEmpty()) {
				return null;
			}
			
			return additional;
		} catch (Exception e) {
			throw new ConfigProcessorException("Error loading additional properties", e);
		} finally {
			close(fis, logAdapter);
		}
	}
	
	public static String escapePropertyValue(String value) {
		if (value == null) {
			return null;
		}
		char[] chars = value.toCharArray();
		StringBuilder sb = new StringBuilder(chars.length);

		for (int i = 0; i < chars.length; i++) {
			if (chars[i] == '\\') {
				sb.append("\\\\");
			} else if (chars[i] == '\r') {
				sb.append('\\').append(chars[i]);
				if (i < chars.length - 1 && chars[i + 1] == '\n') {
					sb.append(chars[i + 1]);
					i++;
				}
			} else if (chars[i] == '\n') {
				sb.append('\\').append(chars[i]);
			} else {
				sb.append(chars[i]);
			}
		}

		return sb.toString();
	}

}
