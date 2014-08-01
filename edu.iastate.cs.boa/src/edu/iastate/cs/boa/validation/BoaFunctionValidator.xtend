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

import edu.iastate.cs.boa.boa.FunctionExpression
import edu.iastate.cs.boa.boa.ReturnStatement

import org.eclipse.xtext.validation.Check

/**
 * @author rdyer
 */
class BoaFunctionValidator extends BoaValidator {
	public static val UNREACHABLE_CODE = "edu.iastate.cs.boa.UnreachableCode"
	public static val MISSING_RETURN = "edu.iastate.cs.boa.MissingReturn"

	@Check
	def void checkNoStatementAfterReturn(ReturnStatement ret) {
		val statements = containingBlock(ret).stmts
		if (statements != null && statements.last != null && statements.last != ret)
			// put the error on the statement after the return
			error("Unreachable code",
				statements.get(statements.indexOf(ret) + 1),
				null, // EStructuralFeature
				UNREACHABLE_CODE)
	}

	@Check
	def void checkNoMissingReturn(FunctionExpression ret) {
		if (ret.type.^return == null)
			return

		val statements = ret.body.stmts
		if (statements == null || statements.last == null || !(statements.last instanceof ReturnStatement))
			// put the error on the statement after the return
			error("Return statement missing",
				ret.type,
				null, // EStructuralFeature
				MISSING_RETURN)
	}
}
