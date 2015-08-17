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

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.editor.XtextEditor;

import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.InputHandle;
import edu.iastate.cs.boa.JobHandle;
import edu.iastate.cs.boa.LoginException;
import edu.iastate.cs.boa.ui.dialogs.InputSelectionDialog;

/**
 * Handler for the command to submit Boa code to the server and run it.
 *
 * @author sambhav
 * @author rdyer
 */
public class SubmitToBoaHandler extends AbstractHandler {
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final ISecurePreferences secureStorage = SecurePreferencesFactory.getDefault();
		final ISecurePreferences node = secureStorage.node("/boa/credentials");

		String username = null;
		String password = null;

		try {
			username = node.get("username", "");
			password = node.get("password", "");
		} catch (final StorageException e) {}

		if (!ChangeBoaCredentialsHandler.validCredentials(username, password))
			ChangeBoaCredentialsHandler.promptUser(event);

		if (ChangeBoaCredentialsHandler.validCredentials(username, password)) {
			final BoaClient client = new BoaClient();
			try {
				client.login(username, password);
				submitJob(event, client);
			} catch (final LoginException e) {
				showError(event, e.getLocalizedMessage());
			} finally {
				try {
					client.close();
				} catch (final BoaException e1) { }
			}
		}

		return null;
	}

	public static void submitJob(final ExecutionEvent event, final BoaClient client) {
		final IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (!(part instanceof XtextEditor)) {
			showError(event, "Active window does not contain a Boa program.");
			return;
		}

		final XtextEditor editor = (XtextEditor) part;
		if (!editor.getLanguageName().equals("edu.iastate.cs.boa.Boa")) {
			showError(event, "Active window does not contain a Boa program.");
			return;
		}

		final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		final InputHandle datasetSelected = promptDatasetSelection(event, client);

		if (datasetSelected != null) {
			try {
				final JobHandle job = client.query(document.get(), datasetSelected);

				try {
					final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser("Boa");
					// FIXME the client API doesnt support the call yet, so hard code a URL for now
//					browser.openURL(job.getUrl());
					browser.openURL(new URL("http://boa.cs.iastate.edu/boa/?q=boa/job/" + job.getId()));
				} catch (final PartInitException e) {
					e.printStackTrace();
				} catch (final MalformedURLException e) {
					e.printStackTrace();
				}
			} catch (final BoaException e) {
				e.printStackTrace();
				showError(event,
						"Job submission failed: " + e.getMessage() + "\n\n"
						+ "Verify your Boa username/password are correct and your internet connection is stable.");
			}
		}
	}

	/**
	 * This method renders a pop-up box with a drop down menu. The options are
	 * input data sets that the Boa program in the current active editor can be
	 * ran against.
	 *
	 * @param event
	 * @param client
	 */
	public static InputHandle promptDatasetSelection(final ExecutionEvent event, final BoaClient client) {
		try {
			final String[] items = client.getDatasetNames();

			final InputSelectionDialog dlg = new InputSelectionDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), "Boa", "Select the input dataset to query:", items, items[0], null);
			if (dlg.open() == Window.CANCEL)
				return null;

			return client.getDataset(dlg.getValue());
		} catch (final BoaException e) {
			e.printStackTrace();
			showError(event,
					"Job submission failed: Unable to obtain list of input datasets: " + e.getLocalizedMessage() + "\n\n"
					+ "Verify your Boa username/password are correct and your internet connection is stable.");
		}

		return null;
	}

	private static void showError(final ExecutionEvent event, final String msg) {
		MessageDialog.openError(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), "Boa", msg);
	}
}