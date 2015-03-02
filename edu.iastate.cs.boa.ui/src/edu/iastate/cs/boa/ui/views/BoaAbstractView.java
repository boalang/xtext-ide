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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.part.ViewPart;

import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.LoginException;

/**
 * The base class of any view used in Boa.
 *
 * @author rdyer
 */
public abstract class BoaAbstractView extends ViewPart {
	static BoaClient client;
	ISecurePreferences secureStorage;
	ISecurePreferences credentials;

	public BoaAbstractView() {
		super();
		secureStorage = SecurePreferencesFactory.getDefault();
		credentials = secureStorage.node("/boa/credentials");
		client = new BoaClient();
		try {
			client.login(credentials.get("username", ""),
					credentials.get("password", ""));
		} catch (final LoginException e) {
			e.printStackTrace();
		} catch (final StorageException e) {
			e.printStackTrace();
		}
	}

	protected void showMessage(String message) {
		MessageDialog.openInformation(null, "Boa View", message);
	}
}