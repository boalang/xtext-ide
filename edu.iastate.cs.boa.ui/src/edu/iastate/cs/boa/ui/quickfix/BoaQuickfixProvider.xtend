/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, 
 *                 Iowa State University of Science and Technology,
 *                 and Bowling Green State University
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
package edu.iastate.cs.boa.ui.quickfix

import org.eclipse.xtext.ui.editor.quickfix.Fix
import org.eclipse.xtext.ui.editor.quickfix.IssueResolutionAcceptor
import org.eclipse.xtext.validation.Issue

import edu.iastate.cs.boa.validation.BoaFunctionValidator
import edu.iastate.cs.boa.boa.FunctionExpression
import edu.iastate.cs.boa.boa.Block

/**
 * Custom quickfixes.
 *
 * see http://www.eclipse.org/Xtext/documentation.html#quickfixes
 * 
 * @author rdyer
 */
class BoaQuickfixProvider extends org.eclipse.xtext.ui.editor.quickfix.DefaultQuickfixProvider {
	@Fix(BoaFunctionValidator::MISSING_RETURN)
	def changeReturnToVoid(Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, 'Remove return type', 'Remove return type', 'remove.gif') [
			element, context |
			val parent = element.eContainer as FunctionExpression
			parent.type.setReturn(null)
		]
	}

	@Fix(BoaFunctionValidator::UNREACHABLE_CODE)
	def removeStatement(Issue issue, IssueResolutionAcceptor acceptor) {
		acceptor.accept(issue, 'Remove', 'Remove', 'remove.gif') [
			element, context |
			var b = element.eContainer as Block
			var statements = b.stmts
			var start = statements.indexOf(element)
			while (statements.length > start)
				statements.remove(start)
		]
	}
}
