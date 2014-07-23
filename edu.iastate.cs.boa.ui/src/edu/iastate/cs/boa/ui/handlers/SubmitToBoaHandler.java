/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, Sambhav Srirama
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
package edu.iastate.cs.boa.ui.handlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.editor.XtextEditor;

/**
 * Handler for the command to submit Boa code to the server and run it.
 * 
 * @author sambhav
 * @author rdyer
 */
public class SubmitToBoaHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISecurePreferences secureStorage = SecurePreferencesFactory.getDefault();
		final ISecurePreferences node = secureStorage.node("/boa/credentials");

		String username = "";
		String password = "";

		try {
			username = node.get("username", "");
			password = node.get("password", "");
		} catch (final StorageException e) {}

		if (!ChangeBoaCredentialsHandler.validCredentials(username, password))
			ChangeBoaCredentialsHandler.promptUser();

		if (ChangeBoaCredentialsHandler.validCredentials(username, password))
			submitJob(event, username, password);

		return null;
	}

	public static void submitJob(final ExecutionEvent event, final String username, final String password) {
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

		try {
			final String request = "http://boa.cs.iastate.edu/api/submit.php?"
					+ "user=" + username
					+ "&pw=" + password
					+ "&source=" + URLEncoder.encode(document.get(), "UTF-8");

			final HttpURLConnection connect = (HttpURLConnection) new URL(request).openConnection();
			connect.setRequestMethod("GET");
			connect.connect();

			final int responseCode = connect.getResponseCode();
			final String responseMessage = connect.getResponseMessage();

			BufferedReader in = null;
			String source = "";
			try {
				in = new BufferedReader(new InputStreamReader(connect.getInputStream()));

				String line;
				while ((line = in.readLine()) != null)
					source += line;
			} finally {
				try {
					in.close();
				} catch (final Exception e) {}
			}

			if (responseCode == 200) {
				final String job = "http://boa.cs.iastate.edu/boa/?q=boa/job/public/" + source;
				try {
					final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser("Boa");
					browser.openURL(new URL(job));
				} catch (final PartInitException | MalformedURLException e1) {}
			} else {
				showError(event, "Job submission error (" + responseCode + " " + responseMessage + "): " + source);
			}
		} catch (final IOException e) {
			showError(event,
					"Job submission failed: " + e.getMessage() + "\n\n"
					+ "Verify your Boa username/password are correct and your internet connection is stable.");
		}
	}

	private static void showError(final ExecutionEvent event, final String msg) {
		MessageDialog.openError(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), "Boa", msg);
	}
}
