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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
		final ISecurePreferences cache = secureStorage.node("/boa/datasets");

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
				submitJob(event, client, cache);
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

	public static void submitJob(final ExecutionEvent event, final BoaClient client, final ISecurePreferences cache) {
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

		final InputHandle datasetSelected = promptDatasetSelection(event, client, cache);

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
	 * @param cache
	 */
	@SuppressWarnings("unchecked")
	public static InputHandle promptDatasetSelection(final ExecutionEvent event, final BoaClient client, final ISecurePreferences cache) {
		if (startDatasetRefreshThread(cache, client)) {
			String[] items = new String[0];
			List<InputHandle> list;

 			try {
 				final byte[] bytes = cache.getByteArray("list", null);
 				if (bytes == null) {
 					showError(event,
 							"Job submission failed: Unable to obtain list of input datasets\n\n"
 							+ "Verify your Boa username/password are correct and your internet connection is stable.");
 					return null;
 				}
				list = (List<InputHandle>)bytesToObj(bytes);
 				if (list == null) {
 					showError(event,
 							"Job submission failed: Unable to obtain list of input datasets\n\n"
 							+ "Verify your Boa username/password are correct and your internet connection is stable.");
 					return null;
 				}

 				items = new String[list.size()];
 				for (int i = 0; i < list.size(); i++)
 					items[i] = list.get(i).getName();
			} catch (final StorageException e) {
				e.printStackTrace();
				showError(event,
						"Job submission failed: Unable to obtain list of input datasets: " + e.getLocalizedMessage() + "\n\n"
						+ "Verify your Boa username/password are correct and your internet connection is stable.");
				return null;
 			}

			/*
			 * Display the dataset choices
			 */
 			final InputSelectionDialog dlg = new InputSelectionDialog(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), "Boa", "Select the input dataset to query:", items, items[0], null);
 			if (dlg.open() == Window.CANCEL)
 				return null;

 			final String val = dlg.getValue();
 			for (final InputHandle h : list)
 				if (h.getName().equals(val))
 					return h;
		}
		return null;
	}

	private static Object bytesToObj(final byte[] b) {
		final ByteArrayInputStream bis = new ByteArrayInputStream(b);
		ObjectInput in = null;
		try {
			in = new ObjectInputStream(bis);
			return in.readObject(); 
		} catch (final ClassNotFoundException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bis.close();
			} catch (final IOException ex) {}
			try {
				if (in != null) in.close();
			} catch (final IOException ex) {}
		}
		return null;
	}

	private static byte[] objToBytes(final Object o) {
		final ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;

		try {
			out = new ObjectOutputStream(bos);   
			out.writeObject(o);
			return bos.toByteArray();
		} catch (final IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) out.close();
			} catch (final IOException ex) {}
			try {
				bos.close();
			} catch (final IOException ex) {}
		}
		return null;
	}

	/**
	 * Starts the Job thread that refreshes the Boa datasets cache. This occurs
	 * every 2 days after being activated. When refreshing, a progress bar (with
	 * a percentage) will render in the bottom right of the Eclipse window.
	 *
	 * @param cache
	 * @param client
	 * @return true if the last refresh cycle was successful, false otherwise
	 */
	public static boolean startDatasetRefreshThread(final ISecurePreferences cache, final BoaClient client) {
		final Job job = new Job("Refreshing Boa Cache") {
			protected IStatus run(final IProgressMonitor monitor) {
				try {
					monitor.beginTask("Refreshing Boa Cache", 3);

					final List<InputHandle> datasets = client.getDatasets();
					monitor.worked(1);

					try {
						cache.clear();
						monitor.worked(1);

						cache.putByteArray("list", objToBytes(datasets), false);
						monitor.worked(1);

						cache.flush();

						if (monitor.isCanceled())
							return Status.CANCEL_STATUS;
					} catch (final StorageException e) {
						e.printStackTrace();
						return Status.CANCEL_STATUS;
					} catch (final IOException e) {
						e.printStackTrace();
						return Status.CANCEL_STATUS;
					} finally {
						schedule(172800000); // start again in 48 hours
					}
					monitor.done();
					return Status.OK_STATUS;
				} catch (final BoaException e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
 			}
		};

		/*
		 * The following block of code should be used if a process needs to
		 * happen at the very end of the job.
		 *
		 * job.addJobChangeListener(new JobChangeAdapter() { public void
		 * done(IJobChangeEvent event) { if (!event.getResult().isOK())
		 * JOptionPane.showMessageDialog(null,
		 * "Job did not complete successfully"); } });
		 */

		job.schedule(); // start as soon as possible
		return true;
	}

	private static void showError(final ExecutionEvent event, final String msg) {
		MessageDialog.openError(HandlerUtil.getActiveWorkbenchWindow(event).getShell(), "Boa", msg);
	}
}
