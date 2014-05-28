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
package edu.iastate.cs.boa.formatting

import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter
import org.eclipse.xtext.formatting.impl.FormattingConfig

import edu.iastate.cs.boa.services.BoaGrammarAccess
import org.eclipse.xtext.Keyword

/**
 * This class contains custom formatting description.
 *
 * see : http://www.eclipse.org/Xtext/documentation.html#formatting
 * on how and when to use it
 *
 * Also see {@link org.eclipse.xtext.xtext.XtextFormattingTokenSerializer} as an example
 * 
 * @author rdyer
 */
class BoaFormatter extends AbstractDeclarativeFormatter {
	override protected void configureFormatting(FormattingConfig c) {
		var g = getGrammarAccess() as BoaGrammarAccess

		// add and preserve newlines around comments
		c.setLinewrap(0, 1, 2).before(g.SL_COMMENTRule)
		c.setLinewrap(1, 1, 2).after(g.SL_COMMENTRule)

		c.setLinewrap(0, 1, 2).before(g.variableDeclarationRule)
		c.setLinewrap(1).after(g.variableDeclarationRule)
		c.setLinewrap(0, 1, 2).before(g.statementRule)
		c.setLinewrap(1).after(g.statementRule)

		for (Keyword k : g.findKeywords("="))
			c.setSpace(" ").around(k)
		for (Keyword k : g.findKeywords(",")) {
			c.setNoSpace().before(k)
			c.setSpace(" ").after(k)
		}
		for (Keyword k : g.findKeywords(";"))
			c.setNoSpace().before(k)

		for (Keyword k : g.findKeywords("{")) {
			c.setLinewrap(0).before(k)
			c.setIndentationIncrement().after(k)
			c.setLinewrap(1).after(k)
		}
		for (Keyword k : g.findKeywords("}")) {
			c.setIndentationDecrement().before(k)
			c.setLinewrap(1).before(k)
			c.setLinewrap(1).after(k)
		}

		for (Keyword k : g.findKeywords("[", "("))
			c.setNoSpace().after(k)
		for (Keyword k : g.findKeywords("]", ")"))
			c.setNoSpace().before(k)
	}
}
