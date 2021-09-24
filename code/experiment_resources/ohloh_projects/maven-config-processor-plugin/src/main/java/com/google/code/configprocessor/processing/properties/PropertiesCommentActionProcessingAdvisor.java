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
package com.google.code.configprocessor.processing.properties;

import com.google.code.configprocessor.expression.*;
import com.google.code.configprocessor.processing.*;
import com.google.code.configprocessor.processing.properties.model.*;

public class PropertiesCommentActionProcessingAdvisor extends AbstractPropertiesActionProcessingAdvisor {

	private static final int INSERT_OFFSET = 1;

	private CommentAction action;

	public PropertiesCommentActionProcessingAdvisor(CommentAction action, ExpressionResolver expressionResolver) {
		super(expressionResolver);
		this.action = action;
	}

	@Override
	public PropertiesFileItemAdvice process(PropertiesFileItem item) {
		if (item instanceof PropertyMapping) {
			PropertyMapping mapping = (PropertyMapping) item;

			if (mapping.getPropertyName().trim().equals(action.getName())) {
				StringBuilder sb = new StringBuilder();
				sb.append(Comment.PREFIX_1);
				sb.append(mapping.getPropertyName());
				sb.append(PropertyMapping.SEPARATOR_1);

				String value = mapping.getPropertyValue();
				if (value != null) {
					sb.append(value);

					int lineBreakIndex = value.indexOf(PropertyMapping.PROPERTY_VALUE_LINE_SEPARATOR);
					while (lineBreakIndex >= 0) {
						int index = Comment.PREFIX_1.length();
						index += mapping.getPropertyName().length();
						index += lineBreakIndex;
						index += PropertyMapping.PROPERTY_VALUE_LINE_SEPARATOR.length();
						index += PropertiesActionProcessor.LINE_SEPARATOR.length();
						index += INSERT_OFFSET;

						sb.insert(index, Comment.PREFIX_1);
						lineBreakIndex = value.indexOf(PropertyMapping.PROPERTY_VALUE_LINE_SEPARATOR, lineBreakIndex + INSERT_OFFSET);
					}
				}

				return new PropertiesFileItemAdvice(PropertiesFileItemAdviceType.MODIFY, new Comment(sb.toString()));
			}
		}

		return super.process(item);
	}

}
