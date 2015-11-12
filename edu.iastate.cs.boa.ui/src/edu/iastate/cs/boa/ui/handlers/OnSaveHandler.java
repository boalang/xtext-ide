
package edu.iastate.cs.boa.ui.handlers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocumentProvider;

import edu.iastate.cs.boa.ui.errors.ErrorStore;

/**
 * @author ankur
 */

public class OnSaveHandler extends XtextDocumentProvider {
	@Override
	public void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
			throws CoreException {
		super.doSaveDocument(monitor, element, document, overwrite);
		ErrorStore boaDebuggerHandler = new ErrorStore();
		boaDebuggerHandler.runBoaCompiler();
	}
}