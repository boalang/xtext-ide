/*
 * Copyright 2014, Hridesh Rajan, Sambhav Srirama,
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

package edu.iastate.cs.boa.ui.errors;

import java.io.File;
import java.io.FileWriter;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/** 
 * @author ankur
 */

public class ErrorStore extends AbstractHandler {
	int i = 1;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
			runBoaCompiler();
			return null;
	}
	
	public void runBoaCompiler() {
		String path_to_program = getPathToProgram();
		String[] errorOutput = null;
		
		try {
			FetchCompilerError errorValidation = new FetchCompilerError();
			errorOutput = errorValidation.typecheck(errorValidation.load(path_to_program));
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		writeToAFile(errorOutput);			
	}
	
	private void writeToAFile(String[] errorOutput) {
		String location = ErrorStore.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		String filePath = location + "error.txt";
			
		try {
			if(errorOutput!=null) {
				String content = errorOutput[0] + "\n"+errorOutput[1] + "\n"+errorOutput[2] + "\n";
				File file = new File(filePath);
				FileWriter fw = new FileWriter(file, false);
				
				if(errorOutput[0]!=null)
					fw.write(content);
				else
					fw.write("");
				fw.close();
			}
				
		} catch (Exception e) {
				e.printStackTrace();
		}	
	}

	private String getPathToProgram() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench == null ? null : workbench.getActiveWorkbenchWindow();
		IWorkbenchPage activePage = window == null ? null : window.getActivePage();
		IEditorPart editor = activePage == null ? null : activePage.getActiveEditor();
		IEditorInput input = editor == null ? null : editor.getEditorInput();
		IPath path = input instanceof FileEditorInput ? ((FileEditorInput) input).getPath() : null;
		
		if (path != null) {
			return path.toFile().getAbsolutePath();
		}
		
		showMessage("Unable to debug program. Please ensure that the file is in the workspace and has the Boa nature attached");
		
		return "";
	}

	private void showMessage(final String content) {
		MessageDialog.openInformation(null, "Boa View", content);
	}

}
