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

	public IgnoreRefactorChangesAction(MergeSourceViewer[] viewers, boolean[] needsPainters) {
		super(CompareMessages.IgnoreRefactorChangesAction_0, viewers, PREFERENCE_IGNORE_REFACTOR_CHANGES);
		synchronizeWithPreference();		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.internal.TextEditorPropertyAction#synchronizeWithPreference()
	 */
	protected void synchronizeWithPreference() {
		/*MessageBox msg = new MessageBox(new Shell());
		msg.setMessage("hello");
		msg.open();*/
		boolean checked = false;
		
		if (checked != isChecked()) {
			if (toggleState(checked))
				setChecked(checked);
		} else if (checked) {
			// TODO
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.compare.internal.TextEditorPropertyAction#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();
		if (property.equals(getPreferenceKey()) || AbstractTextEditor.PREFERENCE_SHOW_LEADING_SPACES.equals(property) || AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_SPACES.equals(property)
				|| AbstractTextEditor.PREFERENCE_SHOW_TRAILING_SPACES.equals(property) || AbstractTextEditor.PREFERENCE_SHOW_LEADING_IDEOGRAPHIC_SPACES.equals(property)
				|| AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_IDEOGRAPHIC_SPACES.equals(property) || AbstractTextEditor.PREFERENCE_SHOW_TRAILING_IDEOGRAPHIC_SPACES.equals(property)
				|| AbstractTextEditor.PREFERENCE_SHOW_LEADING_TABS.equals(property) || AbstractTextEditor.PREFERENCE_SHOW_ENCLOSED_TABS.equals(property)
				|| AbstractTextEditor.PREFERENCE_SHOW_TRAILING_TABS.equals(property) || AbstractTextEditor.PREFERENCE_SHOW_CARRIAGE_RETURN.equals(property)
				|| AbstractTextEditor.PREFERENCE_SHOW_LINE_FEED.equals(property) || AbstractTextEditor.PREFERENCE_WHITESPACE_CHARACTER_ALPHA_VALUE.equals(property)) {
			synchronizeWithPreference();
		}
	}

	protected boolean toggleState(boolean checked) {
		if (checked) {
			// TODO
		} else {
			// TODO
		}
		return true;
	}
		
}
