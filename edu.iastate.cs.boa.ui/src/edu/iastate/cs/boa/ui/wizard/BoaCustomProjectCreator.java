/*
 * Copyright 2015, Hridesh Rajan, Robert Dyer, Sambhav Srirama,
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
package edu.iastate.cs.boa.ui.wizard;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.xtext.ui.XtextProjectHelper;

/**
 * Customize the Boa project creator, so it doesn't make a bunch of
 * unnecessary folders or use unnecessary natures.
 *
 * @author sambhav
 * @author rdyer
 */
public class BoaCustomProjectCreator extends BoaProjectCreator {
	@Override
	protected String[] getBuilders() {
		return new String[] {
		};
	}

	protected String[] getProjectNatures() {
		return new String[] {
			XtextProjectHelper.NATURE_ID // we want the Xtext nature by default
		};
	}

	@Override
	protected List<String> getAllFolders() {
		// no source folders for Boa projects
		return Collections.emptyList();
	}

	// since we generate files into the root, we have to provide
	// custom code for this method, which uses the root for iteration
	protected IFile getModelFile(IProject project) throws CoreException {
		final String expectedExtension = getPrimaryModelFileExtension();
		final IFile[] result = new IFile[1];
		project.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (IResource.FILE == resource.getType()) {
					if (expectedExtension.equals(resource.getFileExtension()))
						result[0] = (IFile) resource;
					return false;
				}
				return true;
			}
		});
		return result[0];
	}
}