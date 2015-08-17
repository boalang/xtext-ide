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
package edu.iastate.cs.boa.ui.labeling

//import org.eclipse.xtext.resource.IEObjectDescription

/**
 * Provides labels for IEObjectDescriptions and IResourceDescriptions.
 *
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#label-provider
 *
 * @author rdyer
 */
class BoaDescriptionLabelProvider extends org.eclipse.xtext.ui.label.DefaultDescriptionLabelProvider {
	// Labels and icons can be computed like this:
//	override text(IEObjectDescription ele) {
//		ele.name.toString
//	}

//	override image(IEObjectDescription ele) {
//		ele.EClass.name + '.gif'
//	}
}