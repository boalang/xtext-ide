/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, Sambhav Srirama,
 *                 Iowa State University of Science and Technology
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

package edu.iastate.cs.boa.ui.views;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.JobHandle;
import edu.iastate.cs.boa.NotLoggedInException;

/**
 * @author ssrirama
 */

public class BoaJobOutputView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.iastate.cs.boa.ui.views.BoaJobOutput";

	ISecurePreferences secureStorage;
	ISecurePreferences credentials;
	private ISecurePreferences jobID;
	BoaClient client;

	public BoaJobOutputView() {
		secureStorage = SecurePreferencesFactory.getDefault();
		credentials = secureStorage.node("/boa/credentials");
		jobID = secureStorage.node("/boa/jobID");
		client = new BoaClient();
	}

	/**
	 * This method renders the columns in the table, then populates them with
	 * the user's Boa job information.
	 * 
	 * @param parent
	 *            The parent GUI object
	 */
	public void createPartControl(Composite parent) {
		try {
			client.login(credentials.get("username", ""),
					credentials.get("password", ""));

			JobHandle job = client.getJob(jobID.getInt("jobID", 0));

			/*
			 * Create a text editor, build it into our GUI by referencing the
			 * parent GUI object, populate it with the job's output, then set it
			 * to wrap the text.
			 */
			Text output = new Text(parent, SWT.WRAP);
			output.setText(job.getOutput());
			output.setEditable(false); // don't let the user edit

			client.close();
		} catch (NotLoggedInException e) {
			e.printStackTrace();
		} catch (BoaException e) {
			e.printStackTrace();
		} catch (StorageException e) {
			e.printStackTrace();
		}

		makeActions(client);
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BoaJobOutputView.this.fillContextMenu(manager);
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(final IMenuManager manager) {

	}

	private void fillContextMenu(final IMenuManager manager) {
	}

	private void fillLocalToolBar(final IToolBarManager manager) {
	}

	private void makeActions(final BoaClient client) {
	}

	private void hookDoubleClickAction() {

	}

	private void showMessage(String message) {
		MessageDialog.openInformation(null, "Boa View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {

	}
}