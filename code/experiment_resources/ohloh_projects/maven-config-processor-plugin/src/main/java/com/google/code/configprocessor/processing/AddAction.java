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

import org.apache.commons.lang.*;

public class AddAction extends AbstractAction {

	private static final long serialVersionUID = -5444028483023476286L;
	
	private static final boolean DEFAULT_IGNORE_ROOT = true;

	private boolean first;
	private boolean last;
	private String after;
	private String before;
	private String inside;
	private String file;
	private boolean ignoreRoot;
	private NestedAction nestedAction;

	public AddAction() {
		this(null, null);
	}

	public AddAction(String name, String value) {
		this(name, value, null, null);
	}

	public AddAction(String file, String after, String before) {
		super(null, null);
		
		this.file = file;
		this.after = after;
		this.before = before;
		this.ignoreRoot = DEFAULT_IGNORE_ROOT;
	}

	public AddAction(String name, String value, String after, String before) {
		super(name, value);

		this.after = after;
		this.before = before;
		this.ignoreRoot = DEFAULT_IGNORE_ROOT;
	}
	
	public void validate() throws ActionValidationException {
		if (getFile() == null && getValue() == null) {
			throw new ActionValidationException("File or value are required", this);
		}
		if (getFile() != null && getValue() != null) {
			throw new ActionValidationException("Cannot define both file and value", this);
		}
		if (isFirst() && isLast()) {
			throw new ActionValidationException("Cannot add in both positions first and last", this);
		}
		if ((isFirst() || isLast()) && (getBefore() != null || getAfter() != null)) {
			throw new ActionValidationException("Cannot define first or last and before or after", this);
		}
		if (getInside() != null && (isFirst() || isLast() || getBefore() != null || getAfter() != null)) {
			throw new ActionValidationException("Inside cannot be defined with any absolute or relative positions", this);
		}
	}

	@Override
	protected String getActionName() {
		return "Add";
	}

	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public boolean isLast() {
		return last;
	}

	public void setLast(boolean last) {
		this.last = last;
	}

	public String getAfter() {
		return StringUtils.trimToNull(after);
	}
	
	public void setAfter(String after) {
		this.after = after;
	}

	public String getBefore() {
		return StringUtils.trimToNull(before);
	}
	
	public void setBefore(String before) {
		this.before = before;
	}
	
	public String getInside() {
		return StringUtils.trimToNull(inside);
	}
	
	public void setInside(String inside) {
		this.inside = inside;
	}
	
	public String getFile() {
		return StringUtils.trimToNull(file);
	}

	public void setFile(String file) {
		this.file = file;
	}
	
	public boolean isIgnoreRoot() {
		return ignoreRoot;
	}
	
	public void setIgnoreRoot(boolean ignoreRoot) {
		this.ignoreRoot = ignoreRoot;
	}
	
	public NestedAction getNestedAction() {
		return nestedAction;
	}
	
	public void setNestedAction(NestedAction nestedAction) {
		this.nestedAction = nestedAction;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((after == null) ? 0 : after.hashCode());
		result = prime * result + ((before == null) ? 0 : before.hashCode());
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + (ignoreRoot ? 1231 : 1237);
		result = prime * result + ((inside == null) ? 0 : inside.hashCode());
		result = prime * result + ((nestedAction == null) ? 0 : nestedAction.hashCode());
		result = prime * result + (first ? 1231 : 1237);
		result = prime * result + (last ? 1231 : 1237);
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
		if (!(obj instanceof AddAction)) {
			return false;
		}
		AddAction other = (AddAction) obj;
		if (isFirst() != other.isFirst()) {
			return false;
		}
		if (isLast() != other.isLast()) {
			return false;
		}
		if (after == null) {
			if (other.after != null) {
				return false;
			}
		} else if (!after.equals(other.after)) {
			return false;
		}
		if (before == null) {
			if (other.before != null) {
				return false;
			}
		} else if (!before.equals(other.before)) {
			return false;
		}
		if (file == null) {
			if (other.file != null) {
				return false;
			}
		} else if (!file.equals(other.file)) {
			return false;
		}
		if (isIgnoreRoot() != other.isIgnoreRoot()) {
			return false;
		}
		if (inside == null) {
			if (other.inside != null) {
				return false;
			}
		} else if (!inside.equals(other.inside)) {
			return false;
		}
		if (nestedAction == null) {
			if (other.nestedAction != null) {
				return false;
			}
		} else if (!nestedAction.equals(other.nestedAction)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getActionName() + " [name=" + getName() + ";value=" + getValue() + ";after=" + getAfter() + ";before=" + getBefore() + ";inside=" + getInside() + ";file=" + getFile() + ";ignoreRoot=" + isIgnoreRoot() + ";first=" + isFirst() + ";last=" + isLast() + "]";
	}
}
