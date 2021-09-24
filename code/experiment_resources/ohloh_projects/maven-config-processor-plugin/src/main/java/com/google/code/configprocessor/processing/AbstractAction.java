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

public abstract class AbstractAction implements Action {

	private static final long serialVersionUID = 2344519551554135862L;
	
	private String name;
	private String value;
	private boolean strict;

	public AbstractAction(String name, String value) {
		this.name = name;
		this.value = value;
		this.strict = true;
	}

	public String getName() {
		return StringUtils.trimToNull(name);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return StringUtils.trimToNull(value);
	}

	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean isStrict() {
		return strict;
	}
	
	public void setStrict(boolean strict) {
		this.strict = strict;
	}

	protected abstract String getActionName();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((getValue() == null) ? 0 : getValue().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractAction other = (AbstractAction) obj;
		if (getName() == null) {
			if (other.getName() != null) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		if (getValue() == null) {
			if (other.getValue() != null) {
				return false;
			}
		} else if (!getValue().equals(other.getValue())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getActionName() + " [name=" + getName() + ";value=" + getValue() + "]";
	}
}
