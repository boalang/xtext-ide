/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 and Iowa State University of Science and Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.iastate.cs.boa.ui.syntaxcoloring;

import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper;

/**
 * @author rdyer
 */
public class BoaAntlrTokenToAttributeIdMapper extends DefaultAntlrTokenToAttributeIdMapper {
	override protected calculateId(String tokenName, int tokenType) {
		if ("RULE_INTEGER_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.NUMBER_ID
		if ("RULE_FLOATING_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.NUMBER_ID
		if ("RULE_STRING_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.STRING_ID
		if ("RULE_REGEX_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.REGEX_ID
		if ("RULE_CHARACTER_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.STRING_ID
		if ("RULE_TIME_LIT".equals(tokenName))
			return BoaHighlightingConfiguration.TIME_ID

		super.calculateId(tokenName, tokenType)
	}
}
