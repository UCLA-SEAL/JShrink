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
package com.google.code.configprocessor.maven;

import org.apache.commons.lang.*;
import org.codehaus.plexus.component.configurator.expression.*;

import com.google.code.configprocessor.expression.*;
import com.google.code.configprocessor.util.*;

/**
 * Resolver of placeholders.
 * 
 * @author Leandro Aparecido
 * @see org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator
 */
public class MavenExpressionResolver implements ExpressionResolver {

	/**
	 * True if placeholders must be replaced.
	 */
	private boolean replacePlaceholders;

	/**
	 * Evaluator of expressions to use.
	 */
	private ExpressionEvaluator evaluator;

	public MavenExpressionResolver(ExpressionEvaluator evaluator) {
		this(evaluator, true);
	}

	public MavenExpressionResolver(ExpressionEvaluator evaluator, boolean replacePlaceholders) {
		this.evaluator = evaluator;
		this.replacePlaceholders = replacePlaceholders;
	}

	/**
	 * Resolves the given text replacing any placeholders if necessary.
	 * 
	 * @param value Value to resolve.
	 * @return Resolved value with values replaced as necessary.
	 */
	public String resolve(String value, boolean isPropertiesValue) {
		String resolvedValue;

		if (replacePlaceholders) {
			try {
				Object aux = evaluator.evaluate(value);
				if ((aux != null) && !(aux instanceof String)) {
					throw new IllegalArgumentException("Expression [" + value + "] did not resolve to String");
				}
				resolvedValue = (String) aux;
				
				if (isPropertiesValue && !StringUtils.equals(value, resolvedValue)) {
					resolvedValue = PropertiesUtils.escapePropertyValue(resolvedValue);
				}
			} catch (ExpressionEvaluationException e) {
				throw new RuntimeException("Error resolving expression [" + value + "]", e);
			}
		} else {
			resolvedValue = value;
		}

		return resolvedValue;
	}

}
