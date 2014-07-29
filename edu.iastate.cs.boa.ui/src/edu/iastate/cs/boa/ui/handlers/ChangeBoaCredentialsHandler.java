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
package edu.iastate.cs.boa.ui.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.*;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.iastate.cs.boa.ui.dialogs.PasswordDialog;

public class ChangeBoaCredentialsHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
    	promptUser(event);
		return null;
	}

	public static void promptUser(final ExecutionEvent event) {
		final ISecurePreferences secureStorage = SecurePreferencesFactory.getDefault();
		final ISecurePreferences node = secureStorage.node("/boa/credentials");

		try {
			// ask for their Boa username
			final InputDialog dlg = new InputDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(),
				"Boa", "Enter your Boa username:", "", new IInputValidator() {
					public String isValid(final String newText) {
						int len = newText.length();
						if (len < 1) return "Invalid username";
						return null;
					}
				});
			if (dlg.open() == Window.CANCEL)
				return;

			final String username = dlg.getValue();

			if (!validUser(username)) {
	    		MessageDialog.openError(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), "Boa", "Username can not be blank.");
				promptUser(event);
				return;
			}

			node.put("username", username, false);

			// ask for their Boa password
			final InputDialog passwordDlg = new PasswordDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(),
					"Boa", "Enter your Boa password:", "", new IInputValidator() {
						public String isValid(final String newText) {
							int len = newText.length();
							if (len < 1) return "Invalid password";
							return null;
						}
					});
			if (passwordDlg.open() == Window.CANCEL)
				return;

			final String password = new String(passwordDlg.getValue());
			if (!validPassword(password)) {
	    		MessageDialog.openError(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), "Boa", "The password can not be blank.");
				return;
			}
			node.put("password", password, true);

			node.flush();
		} catch (final StorageException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean validUser(final String username) {
		return username != null && username.length() > 0;
	}

	public static boolean validPassword(final String password) {
		return password != null && password.length() > 0;
	}

	public static boolean validCredentials(final String username, final String password) {
		return validUser(username) && validPassword(password);
	}
}
