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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.ui.editor.XtextEditor;

/**
 * @author sambhav
 */
public class SubmitToBoaHandler extends AbstractHandler {
	/**
	 * the command has been executed, so extract the needed information from the
	 * application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);

		final IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (!(part instanceof XtextEditor))
			return notBoaError(window);

		final XtextEditor editor = (XtextEditor) part;
		if (!editor.getLanguageName().equals("edu.iastate.cs.boa.Boa"))
			return notBoaError(window);

		final IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());

		/*
		 * Open a connection to the Boa web service to submit a job
		 */
		try {
			final String request = "http://boa.cs.iastate.edu/ide/submit.php?user=" + "tutorial"
					+ "&pw=" + "icse14tutorial"
					+ "&source="+ URLEncoder.encode(document.get(), "UTF-8");

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
				try { in.close(); } catch (final Exception e) {}
			}

			if (responseCode == 200) {
				final String job = "http://boa.cs.iastate.edu/boa/?q=boa/job/public/" + source;
				try {
					final IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport().createBrowser("Boa");
					browser.openURL(new URL(job));
				} catch (final PartInitException | MalformedURLException e1) {
					e1.printStackTrace();
				}
			} else {
				MessageDialog.openInformation(window.getShell(), "Boa",
					"Job submission error (" + responseCode + " " + responseMessage + "): " + source);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private Object notBoaError(final IWorkbenchWindow window) {
		MessageDialog.openError(window.getShell(), "Boa", "Active window does not contain a Boa program.");
		return null;
	}
}
