/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, 
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
package edu.iastate.cs.boa.generator

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.xtext.generator.IGenerator
import org.eclipse.xtext.generator.JavaIoFileSystemAccess
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.validation.CheckMode
import org.eclipse.xtext.validation.IResourceValidator

import com.google.inject.Inject
import com.google.inject.Provider

/**
 * @author rdyer
 */
class Main {
	
	def static main(String[] args) {
		if (args.empty) {
			System::err.println('Aborting: no path to EMF resource provided!')
			return
		}
		val injector = new edu.iastate.cs.boa.BoaStandaloneSetup().createInjectorAndDoEMFRegistration()
		val main = injector.getInstance(typeof(Main))
		main.runGenerator(args.get(0))
	}
	
	@Inject Provider<ResourceSet> resourceSetProvider
	
	@Inject IResourceValidator validator
	
	@Inject IGenerator generator
	
	@Inject JavaIoFileSystemAccess fileAccess

	def protected runGenerator(String string) {
		// load the resource
		val set = resourceSetProvider.get
		val resource = set.getResource(URI::createURI(string), true)
		
		// validate the resource
		val issues = validator.validate(resource, CheckMode::ALL, CancelIndicator::NullImpl)
		if (!issues.isEmpty()) {
			for (issue : issues) {
				System::err.println(issue)
			}
			return
		}
		
		// configure and start the generator
		fileAccess.setOutputPath('src-gen/')
		generator.doGenerate(resource, fileAccess)
		System::out.println('Code generation finished.')
	}
}
