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
package edu.iastate.cs.boa.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.iastate.cs.boa.ui.views.BoaJobDetailsView;
import edu.iastate.cs.boa.ui.views.BoaJobOutputView;
import edu.iastate.cs.boa.ui.views.BoaJobsView;

/**
 * A handler to open Boa views.
 *
 * @author ssrirama
 * @author rdyer
 */
public class OpenBoaView extends AbstractHandler {
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		openJobListView();
		return null;
	}

	public static void openJobListView() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(BoaJobsView.ID);
		} catch (final PartInitException e) {
		}
	}

	public static void openDetailsView() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(BoaJobDetailsView.ID);
		} catch (final PartInitException e) {
		}
	}

	public static void openOutputView() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage().showView(BoaJobOutputView.ID);
		} catch (final PartInitException e) {
		}
	}
}