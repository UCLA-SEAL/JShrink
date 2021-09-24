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
package com.google.code.configprocessor.processing;

import java.util.regex.*;

import org.apache.commons.lang.*;

public class ModifyAction extends AbstractAction {

	private static final long serialVersionUID = 3614101885803457281L;
	
	private static final String DEFAULT_PATTERN_FLAGS = "CASE_INSENSITIVE";
	private static final char PATTERN_FLAG_SEPARATOR = ',';
	
	private String find;
	private String replace;
	private String flags;

	public ModifyAction() {
		this(null, null);
	}

	public ModifyAction(String name, String value) {
		super(name, value);
	}

	public void validate() throws ActionValidationException {
		if (getName() == null) {
			if (getValue() == null) {
				if (getFind() == null || getReplace() == null) {
					throw new ActionValidationException("Find/Replace are required when not modifying a specific property", this);
				}
			} else {
				throw new ActionValidationException("Value must not be used when not modifying a specific property", this);
			}
		} else {
			if (getFind() != null || getReplace() != null) {
				throw new ActionValidationException("Find/Replace cannot be used when modifying a specific property", this);
			}
		}
	}
	
	@Override
	protected String getActionName() {
		return "Modify";
	}

	protected int parseFlags() {
		int flagsToUse = 0;
		String flagsToTest = getFlags() == null ? DEFAULT_PATTERN_FLAGS : getFlags();
		String[] flagArray = StringUtils.split(flagsToTest, PATTERN_FLAG_SEPARATOR);
		for (String flag : flagArray) {
			if ("UNIX_LINES".equals(flag)) {
				flagsToUse |= Pattern.UNIX_LINES;
			} else if ("CASE_INSENSITIVE".equals(flag)) {
				flagsToUse |= Pattern.CASE_INSENSITIVE;
			} else if ("COMMENTS".equals(flag)) {
				flagsToUse |= Pattern.COMMENTS;
			} else if ("MULTILINE".equals(flag)) {
				flagsToUse |= Pattern.MULTILINE;
			} else if ("LITERAL".equals(flag)) {
				flagsToUse |= Pattern.LITERAL;
			} else if ("DOTALL".equals(flag)) {
				flagsToUse |= Pattern.DOTALL;
			} else if ("UNICODE_CASE".equals(flag)) {
				flagsToUse |= Pattern.UNICODE_CASE;
			} else if ("CANON_EQ".equals(flag)) {
				flagsToUse |= Pattern.CANON_EQ;
			} else {
				throw new IllegalArgumentException("Unknown flag: " + flag);
			}
		}
		
		return flagsToUse;
	}

	public String getFind() {
		return StringUtils.trimToNull(find);
	}

	public void setFind(String find) {
		this.find = find;
	}

	public String getReplace() {
		return StringUtils.trimToNull(replace);
	}

	public void setReplace(String replace) {
		this.replace = replace;
	}

	public String getFlags() {
		return StringUtils.trimToNull(flags);
	}
	
	public void setFlags(String flags) {
		this.flags = flags;
	}
	
	public Pattern getPattern() {
		return Pattern.compile(getFind(), parseFlags());
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((getFind() == null) ? 0 : getFind().hashCode());
		result = prime * result + ((getReplace() == null) ? 0 : getReplace().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ModifyAction other = (ModifyAction) obj;
		if (getFind() == null) {
			if (other.getFind() != null) {
				return false;
			}
		} else if (!getFind().equals(other.getFind())) {
			return false;
		}
		if (getReplace() == null) {
			if (other.getReplace() != null) {
				return false;
			}
		} else if (!getReplace().equals(other.getReplace())) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return getActionName() + " [name=" + getName() + ";value=" + getValue() + ";find=" + getFind() + ";replace=" + getReplace() + ";flags=" + getFlags() + "]";
	}
}
