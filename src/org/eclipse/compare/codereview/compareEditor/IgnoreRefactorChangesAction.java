/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.compare.codereview.compareEditor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.internal.CompareMessages;
import org.eclipse.compare.internal.MergeSourceViewer;
import org.eclipse.compare.internal.TextEditorPropertyAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.WhitespaceCharacterPainter;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

public class IgnoreRefactorChangesAction extends TextEditorPropertyAction {

	public static final String IGNORE_REFACTOR_CHANGES = "org.eclipse.compare.ignoreRefactorChanges";
	public static final String PREFERENCE_IGNORE_REFACTOR_CHANGES = "ignoreRefactorChanges";
	private IPreferenceStore fStore = EditorsUI.getPreferenceStore();

	public IgnoreRefactorChangesAction(MergeSourceViewer[] viewers) {
		super(CompareMessages.IgnoreRefactorChangesAction_0, viewers, PREFERENCE_IGNORE_REFACTOR_CHANGES);
		synchronizeWithPreference();	
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.internal.TextEditorPropertyAction#synchronizeWithPreference()
	 */
	protected void synchronizeWithPreference() {
		boolean checked = false;
		
		if (fStore != null) {
			checked = fStore.getBoolean(getPreferenceKey());
		}
		
		if (checked != isChecked()) {
			if (toggleState(checked))
				setChecked(checked);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.internal.TextEditorPropertyAction#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(getPreferenceKey())) {
			synchronizeWithPreference();
		}
	}

	protected boolean toggleState(boolean checked) {
		/*if (checked) {
			System.out.println("Ignore Refactoring Changes");
		} else {
			System.out.println("Don't Ignore Refactoring Changes");
		}*/
		return true;
	}
		
}
