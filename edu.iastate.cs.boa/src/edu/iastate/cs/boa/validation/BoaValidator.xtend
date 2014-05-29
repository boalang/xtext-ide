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
package edu.iastate.cs.boa.validation

import org.eclipse.xtext.validation.Check
import edu.iastate.cs.boa.boa.ReturnStatement
import org.eclipse.emf.ecore.EObject
import edu.iastate.cs.boa.boa.Block
import org.eclipse.xtext.EcoreUtil2
import edu.iastate.cs.boa.boa.FunctionExpression

import org.eclipse.xtext.validation.ComposedChecks

/**
 * Custom validation rules.
 *
 * see http://www.eclipse.org/Xtext/documentation.html#validation
 * 
 * @author rdyer
 */
@ComposedChecks(validators = #[typeof(BoaDataTypeValidator)])
class BoaValidator extends AbstractBoaValidator {
	public static val UNREACHABLE_CODE = "edu.iastate.cs.boa.UnreachableCode"
	public static val MISSING_RETURN = "edu.iastate.cs.boa.MissingReturn"

	@Check
	def void checkNoStatementAfterReturn(ReturnStatement ret) {
		val statements = containingBlock(ret).stmts
		if (statements.last != ret)
			// put the error on the statement after the return
			error("Unreachable code",
				statements.get(statements.indexOf(ret)+1),
				null, // EStructuralFeature
				UNREACHABLE_CODE)
	}

	@Check
	def void checkNoMissingReturn(FunctionExpression ret) {
		if (ret.type.^return == null)
			return

		val statements = ret.body.stmts
		if (!(statements.last instanceof ReturnStatement))
			// put the error on the statement after the return
			error("Return statement missing",
				statements.last,
				null, // EStructuralFeature
				MISSING_RETURN)
	}

	def static containingBlock(EObject e) {
		EcoreUtil2.getContainerOfType(e, typeof(Block))
	}
}
