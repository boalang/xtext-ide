
package edu.iastate.cs.boa.ui.handlers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocumentProvider;

public class BoaSaveHandler extends XtextDocumentProvider {
	@Override
	public void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
			throws CoreException {
		
		super.doSaveDocument(monitor, element, document, overwrite);
		BoaDebuggerHandler boaDebuggerHandler = new BoaDebuggerHandler();
		boaDebuggerHandler.runBoaCompiler();
	}
}