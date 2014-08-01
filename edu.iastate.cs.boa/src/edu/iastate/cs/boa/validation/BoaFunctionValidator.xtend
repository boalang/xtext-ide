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
package edu.iastate.cs.boa.validation

import edu.iastate.cs.boa.boa.Block
import edu.iastate.cs.boa.boa.BreakStatement
import edu.iastate.cs.boa.boa.ContinueStatement
import edu.iastate.cs.boa.boa.FunctionExpression
import edu.iastate.cs.boa.boa.ReturnStatement
import edu.iastate.cs.boa.boa.StopStatement

import org.eclipse.xtext.validation.Check

/**
 * @author rdyer
 */
class BoaFunctionValidator extends BoaValidator {
	public static val UNREACHABLE_CODE = "edu.iastate.cs.boa.UnreachableCode"
	public static val MISSING_RETURN = "edu.iastate.cs.boa.MissingReturn"

	@Check
	def void checkNoUnreachable(Block b) {
		val statements = b.stmts
		val exit = statements.findFirst[s | s instanceof ReturnStatement || s instanceof BreakStatement || s instanceof ContinueStatement || s instanceof StopStatement]

		if (exit != null && statements.last != null && statements.last != exit)
			// put the error on the first unreachable statement
			error("Unreachable code",
				statements.get(statements.indexOf(exit) + 1),
				null, // EStructuralFeature
				UNREACHABLE_CODE)
	}

	@Check
	def void checkNoMissingReturn(FunctionExpression func) {
		if (func.type.^return == null)
			return

		val statements = func.body.stmts
		if (statements == null || statements.last == null || !(statements.last instanceof ReturnStatement))
			// put the error on the statement after the return
			error("Return statement missing",
				func.type,
				null, // EStructuralFeature
				MISSING_RETURN)
	}
}
