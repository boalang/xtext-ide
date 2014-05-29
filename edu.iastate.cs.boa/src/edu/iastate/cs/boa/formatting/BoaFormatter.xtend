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
import org.eclipse.xtext.Keyword
import org.eclipse.xtext.ParserRule
import org.eclipse.xtext.util.Pair;

import edu.iastate.cs.boa.services.BoaGrammarAccess

/**
 * This class contains Boa's formatting description.
 *
 * see : http://www.eclipse.org/Xtext/documentation.html#formatting
 * on how and when to use it
 * 
 * @author rdyer
 */
class BoaFormatter extends AbstractDeclarativeFormatter {
	override protected void configureFormatting(FormattingConfig c) {
		var g = getGrammarAccess() as BoaGrammarAccess

		c.setAutoLinewrap(120)

		// add and preserve newlines around comments
		c.setLinewrap(0, 1, 2).before(g.SL_COMMENTRule)
		c.setLinewrap(1, 1, 2).after(g.SL_COMMENTRule)

		// various special tokens
		for (Keyword k : g.findKeywords("<<"))
			c.setSpace(" ").around(k)

		for (Keyword k : g.findKeywords(",")) {
			c.setNoSpace().before(k)
			c.setSpace(" ").after(k)
		}

		for (Keyword k : g.findKeywords(";"))
			c.setNoSpace().before(k)
		for (Keyword k : g.findKeywords("."))
			c.setNoSpace().around(k)

		c.setSpace(" ").around(g.typeDeclarationAccess.equalsSignKeyword_2)
		c.setSpace(" ").around(g.assignmentStatementAccess.equalsSignKeyword_1)

		// FIXME: doesnt handle this case "v := foo" properly
		c.setNoSpace().before(g.forVariableDeclarationAccess.colonKeyword_1)
		c.setSpace(" ").after(g.forVariableDeclarationAccess.colonKeyword_1)
		c.setSpace(" ").around(g.forVariableDeclarationAccess.equalsSignKeyword_3_0)

		c.setNoSpace().before(g.switchCaseAccess.colonKeyword_2)
		c.setNoSpace().before(g.switchStatementAccess.colonKeyword_7)
		c.setNoSpace().before(g.parameterAccess.colonKeyword_1)
		c.setNoSpace().before(g.componentAccess.colonKeyword_0_1)

		// braces
		for (Keyword k : g.findKeywords("{")) {
			c.setNoLinewrap().before(k)
			c.setIndentationIncrement().after(k)
			c.setLinewrap().after(k)
		}
		for (Keyword k : g.findKeywords("}")) {
			c.setIndentationDecrement().before(k)
			c.setLinewrap().before(k)
			c.setLinewrap().after(k)
		}

		// brackets/parens
		for (Pair<Keyword, Keyword> pair : g.findKeywordPairs("(", ")")) {
			c.setNoSpace().before(pair.getFirst)
			c.setNoSpace().after(pair.getFirst)
			c.setNoSpace().before(pair.getSecond)
		}
		val Keyword[] keys = #[
			g.foreachStatementAccess.foreachKeyword_0,
			g.existsStatementAccess.existsKeyword_0,
			g.ifallStatementAccess.ifallKeyword_0,
			g.ifStatementAccess.ifKeyword_0,
			g.forStatementAccess.forKeyword_0,
			g.whileStatementAccess.whileKeyword_0
		]
		for (Keyword k : keys)
			c.setSpace(" ").after(k)
		c.setSpace(" ").before(g.doStatementAccess.conditionExpressionParserRuleCall_4_0)

		for (Pair<Keyword, Keyword> pair : g.findKeywordPairs("[", "]")) {
			c.setNoSpace().before(pair.getFirst)
			c.setNoSpace().after(pair.getFirst)
			c.setNoSpace().before(pair.getSecond)
		}
		for (Pair<Keyword, Keyword> pair : g.findKeywordPairs("]", "["))
			c.setNoSpace().between(pair.getFirst, pair.getSecond)

		// line wrapping for statements/decls
		var ParserRule[] rules = #[
			g.typeDeclarationRule,
			g.staticVariableDeclarationRule,
			g.variableDeclarationRule,
			g.assignmentStatementRule,
			g.breakStatementRule,
			g.continueStatementRule,
			g.stopStatementRule,
			g.doStatementRule,
			g.forStatementRule,
			g.ifStatementRule,
			g.resultStatementRule,
			g.returnStatementRule,
			g.switchStatementRule,
			g.foreachStatementRule,
			g.existsStatementRule,
			g.ifallStatementRule,
			g.whileStatementRule,
			g.emptyStatementRule,
			g.emitStatementRule,
			g.expressionStatementRule
		]
		for (ParserRule r : rules) {
			c.setLinewrap(0, 1, 2).before(r)
			c.setLinewrap(1, 1, 2).after(r)
		}

		// block indenting
		c.setIndentationIncrement().before(g.switchCaseAccess.stmtsStatementParserRuleCall_3_0)
		c.setIndentationDecrement().after(g.switchCaseAccess.stmtsStatementParserRuleCall_3_0)
		c.setIndentationIncrement().before(g.switchStatementAccess.defaultStmtsStatementParserRuleCall_8_0)
		c.setIndentationDecrement().after(g.switchStatementAccess.defaultStmtsStatementParserRuleCall_8_0)

//		c.setIndentationIncrement().before(g.foreachStatementAccess.bodyStatementParserRuleCall_6_0)
//		c.setIndentationDecrement().after(g.foreachStatementAccess.bodyStatementParserRuleCall_6_0)
	}
}
