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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;

public class ChangeBoaCredentialsHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		promptUser();
		return null;
	}

	public static void promptUser() {
		final ISecurePreferences secureStorage = SecurePreferencesFactory.getDefault();
		final ISecurePreferences node = secureStorage.node("/boa/credentials");

		try {
			// ask for their Boa username
			final String username = JOptionPane.showInputDialog(null,
					"Enter your Boa username:", "Boa Username",
					JOptionPane.PLAIN_MESSAGE);

			// they hit cancel
			if (username == null)
				return;

			if (!validUser(username)) {
				JOptionPane.showMessageDialog(null, "Username can not be blank.");
				promptUser();
				return;
			}

			try {
				node.put("username", URLEncoder.encode(username, "UTF-8"), false);
			} catch (final UnsupportedEncodingException e) {}

			// ask for their Boa password
			final JPanel panel = new JPanel();
			final JPasswordField pass = new JPasswordField(10);
			panel.add(new JLabel("Enter your Boa password:\n"));
			panel.add(pass);
			final String[] options = new String[] { "OK", "Cancel" };
			final int option = JOptionPane.showOptionDialog(null, panel,
					"Boa Password", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

			if (option == 0) {
				final String password = new String(pass.getPassword());
				if (!validPassword(password)) {
					JOptionPane.showMessageDialog(null, "The password can not be blank.");
					return;
				}
				try {
					node.put("password", URLEncoder.encode(password, "UTF-8"), true);
				} catch (final UnsupportedEncodingException e) {}
			}

			node.flush();
		} catch (final StorageException | IOException e) {}
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
