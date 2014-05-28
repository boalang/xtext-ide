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
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class BoaPluginHandler extends AbstractHandler {

	public BoaPluginHandler() {
	}

	/**
	 * the command has been executed, so extract the needed information from the
	 * application context.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		/*
		 * Create and initialize variables
		 */
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		URL url = null;
		IDocument document = null;
		HttpURLConnection connect = null;
		int responseCode = 0;
		String responseMessage = "";
		String job = "", request = "";
		String source = "";
		/*
		 * From here until "mark" is a block of code courtesy of
		 * http://stackoverflow
		 * .com/questions/2395928/grab-selected-text-from-eclipse-java-editor
		 */
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().getActiveEditor();
		if (part instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) part;
			IDocumentProvider provider = editor.getDocumentProvider();
			document = provider.getDocument(editor.getEditorInput());
		}
		// mark

		/*
		 * Open a connection to the Boa web service to submit a job
		 */
		try {
			/*
			 * The following uses a deprecated version of URLEncoder. It works
			 * fine right now, but it would be best to find another version that
			 * is supported.
			 */
			request = "http://boa.cs.iastate.edu/ide/submit.php?user=tutorial&pw=icse14tutorial&source="
					+ URLEncoder.encode(document.get());
			job = "http://boa.cs.iastate.edu/boa/?q=boa/job/public/";
			url = new URL(request);
			connect = (HttpURLConnection) url.openConnection();
			connect.setRequestMethod("GET");
			connect.connect();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					connect.getInputStream()));
			String line;
			
			while ((line = in.readLine()) != null)
				source += line;
			in.close();

			responseCode = connect.getResponseCode();
			responseMessage = connect.getResponseMessage();

		} catch (IOException e) {
			e.printStackTrace();
		}

		if (responseCode == 200) {
			if (MessageDialog.openQuestion(window.getShell(), "Boa",
					"Job was successfully submitted. Go to submission board?")) {
				job += source;
				IWebBrowser browser = null;
				try {
					browser = PlatformUI.getWorkbench().getBrowserSupport()
							.createBrowser("Boa");
					browser.openURL(new URL(job));
				} catch (PartInitException | MalformedURLException e1) {
					e1.printStackTrace();
				}
			}

		} else {
			MessageDialog.openInformation(window.getShell(), "Boa",
					"Job was not successfully submitted!\nError "
							+ responseCode + ": " + responseMessage);
		}
		return null;
	}
}
