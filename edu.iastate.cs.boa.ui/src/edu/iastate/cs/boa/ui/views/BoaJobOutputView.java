/*
 * Copyright 2015, Hridesh Rajan, Sambhav Srirama,
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
package edu.iastate.cs.boa.ui.views;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.JobHandle;
import edu.iastate.cs.boa.NotLoggedInException;

/**
 * @author ssrirama
 */
public class BoaJobOutputView extends BoaAbstractView {
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.iastate.cs.boa.ui.views.BoaJobOutput";

	private ISecurePreferences jobID;
	protected static Text output;
	protected static Action refreshDisplay;

	public BoaJobOutputView() {
		super();
		jobID = secureStorage.node("/boa/jobID");
	}

	/**
	 * This method renders the in-view text editor then populates it with the
	 * selected job's output
	 * 
	 * @param parent
	 *            The parent GUI object
	 */
	public void createPartControl(Composite parent) {
		output = new Text(parent, SWT.WRAP);
		output.setText("Attempting to download job output now...");
		
		makeActions(client);
		refreshDisplay.run(); // populate view

		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	/**
	 * Registers this plugin with Eclipse and configures the context menu
	 * manager so we can add items to it later.
	 */
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BoaJobOutputView.this.fillContextMenu(manager);
			}
		});
	}

	/**
	 * Standard Eclipse configuration stuff, we don't mess with this.
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(final IMenuManager manager) {
	}

	private void fillContextMenu(final IMenuManager manager) {
	}

	/**
	 * Adds items to the toolbar manager so that "refresh" and "visualize" show
	 * up in the toolbar
	 * 
	 * @param manager
	 *            The manager that we add toolbar items to
	 */
	private void fillLocalToolBar(final IToolBarManager manager) {
		manager.add(refreshDisplay);
	}

	/**
	 * Configures the "refresh" button and "visualize" button such that pressing
	 * them will, respectively, fetch the latest job ID from the cache to
	 * display it's output, and open the Boa Visualizations view.
	 * 
	 * @param client
	 *            The inherited BoaClient object. It should already be logged in
	 *            with a valid session.
	 */
	private void makeActions(final BoaClient client) {
		refreshDisplay = new Action() {
			@Override
			public void run() {
				Runnable displayOutput = new ThreadToDisplayOutput();
				Display.getDefault().asyncExec(displayOutput);
			}
		};
		refreshDisplay.setToolTipText("Refresh");
		refreshDisplay.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));
	}

	public class ThreadToDisplayOutput implements Runnable {

		@Override
		public void run() {
			try {
				int id = jobID.getInt("jobID", 0);
				if(id == 0){
					output.setText("No job selected or unable to fetch job information");
					output.setEditable(false);
					return;
				}
				output.setText("Attempting to download job output now...");
				JobHandle job = client.getJob(id);
				String jobOutput = job.getOutput();

				if (validJobOutput(jobOutput)) {
					output.setText(jobOutput);
				} else {
					output.setText("Empty or null output");
				}

				output.setEditable(false);
			} catch (NotLoggedInException e) {
				e.printStackTrace();
			} catch (BoaException e) {
				output.setText("Empty or null output");
				e.printStackTrace();
			} catch (StorageException e) {
				output.setText("Empty or null output");
				e.printStackTrace();
			}

		}

	}

	private boolean validJobOutput(String input) {
		return input != null && input.length() > 0;
	}

	/**
	 * Configures the behavior of the action taken when a user double-clicks.
	 */
	private void hookDoubleClickAction() {
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}
}