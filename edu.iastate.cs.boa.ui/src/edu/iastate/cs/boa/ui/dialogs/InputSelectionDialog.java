// modified from: http://dev.eclipse.org/svnroot/tools/org.eclipse.buckminster/trunk/org.eclipse.buckminster.ui/src/java/org/eclipse/buckminster/ui/ComboInputDialog.java
// @author rdyer
/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cloudsmith Inc. - creation of new class ComboInputDialog from original org.eclipse.jface.dialogs.InputDialog
 *******************************************************************************/
package edu.iastate.cs.boa.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InputSelectionDialog extends Dialog {
	private final String title;
	private final String message;

	private String value;
	private final IInputValidator validator;

	private final String[] choices;
	private Combo combo;

	private Text errorMessageText;
	private String errorMessage;

	public InputSelectionDialog(final Shell parent, final String title, final String message, final String[] choices, final String value, final IInputValidator validator) {
		super(parent);

		this.title = title;
		this.message = message;
		this.choices = choices;
		if (value == null)
			this.value = "";
		else
			this.value = value;
		this.validator = validator;
	}

	public String getValue() {
		return value;
	}

	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;

		if (errorMessageText != null && !errorMessageText.isDisposed()) {
			errorMessageText.setText(errorMessage == null ? " \n " : errorMessage);

			final boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
			errorMessageText.setEnabled(hasError);
			errorMessageText.setVisible(hasError);
			errorMessageText.getParent().update();

			final Control button = getButton(IDialogConstants.OK_ID);
			if (button != null)
				button.setEnabled(errorMessage == null);
		}
	}

	@Override
	protected void buttonPressed(final int buttonId) {
		if (buttonId == IDialogConstants.OK_ID)
			value = combo.getText();
		else
			value = null;
		super.buttonPressed(buttonId);
	}

	@Override
	protected void configureShell(final Shell shell) {
		super.configureShell(shell);
		if (title != null)
			shell.setText(title);
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);

		combo.setFocus();
		if (value != null)
			combo.setText(value);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);

		if (message != null) {
			final Label label = new Label(composite, SWT.WRAP);
			label.setText(message);
			final GridData data = new GridData(GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL | GridData.HORIZONTAL_ALIGN_FILL
					| GridData.VERTICAL_ALIGN_CENTER);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			label.setLayoutData(data);
			label.setFont(parent.getFont());
		}

		combo = new Combo(composite, SWT.NONE);
		combo.setItems(choices);
		combo.setVisibleItemCount(choices.length);
		combo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
		errorMessageText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		errorMessageText.setBackground(errorMessageText.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		setErrorMessage(errorMessage);

		applyDialogFont(composite);

		return composite;
	}

	protected void validateInput() {
		setErrorMessage(validator != null ? validator.isValid(combo.getText()) : null);
	}
}