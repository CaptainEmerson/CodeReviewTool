package org.eclipse.compare.codereview.compareEditor;

/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *  IBM Corporation - initial API and implementation
 *******************************************************************************/

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.*;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ui.IWorkbenchPage;

public class ChangeCompareEditorInput extends CompareEditorInput {
	
	private ITypedElement left;
	private ITypedElement right;
	
	/*
	 * Returns <code>true</code> if the other object is of type
	 * <code>CompareFileRevisionEditorInput</code> and both of their
	 * corresponding left and right objects are identical. The content is not
	 * considered.
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof ChangeCompareEditorInput) {
			ChangeCompareEditorInput other = (ChangeCompareEditorInput) obj;
			return other.getLeft().equals(left)
					&& other.getRight().equals(right);
		}
		return false;
	}
	
	/**
	 * Creates a new CompareFileRevisionEditorInput.
	 * @param left 
	 * @param right 
	 * @param page 
	 */
	public ChangeCompareEditorInput(ITypedElement left, ITypedElement right) {
		super(new CompareConfiguration());
		this.left = left;
		this.right = right;
		setTitle(right.getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.LocalResourceCompareEditorInput#internalPrepareInput(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected ICompareInput prepareCompareInput(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		ICompareInput input = createCompareInput();
		getCompareConfiguration().setLeftEditable(false);
		getCompareConfiguration().setRightEditable(false);
		initLabels(input);
		return input;
	}

	private ICompareInput createCompareInput() {
		/*DiffNode input = new DiffNode(left,right);
		return input;*/
		
		Differencer d = new Differencer();
		Object diff = d.findDifferences(false, new NullProgressMonitor(), null,
				null, left, right);
		
		DiffNode input = (DiffNode)diff;
		
		input.setKind(Differencer.CHANGE);
		
		return input;
	}

	private void initLabels(ICompareInput input) {
		CompareConfiguration cc = getCompareConfiguration();
		if (getLeft() != null) {
			cc.setLeftLabel("Local Revision : " + getLeft().getName());
		}
		if (getRight() != null) {
			cc.setRightLabel("Local : " + getRight().getName());
		}
	}

	public ITypedElement getRight() {
		return right;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		String leftFile = ((Input) left).getFile().toString();
		String rightFile = ((Input) right).getFile().toString();
		return "Compare Current " + leftFile + "and Revision " + rightFile; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.compare.CompareEditorInput#getTitle()
	 */
	public String getTitle() {
		String leftFile = ((Input) left).getName();
		String rightFile = ((Input) right).getName();
		return "Compare Current " + leftFile + " and Revision " + rightFile; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SaveableCompareEditorInput#contentsCreated()
	 */
	protected void contentsCreated() {
		super.contentsCreated();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.synchronize.SaveableCompareEditorInput#handleDispose()
	 */
	protected void handleDispose() {
		super.handleDispose();
	}

	public ITypedElement getLeft() {
		return left;
	}

	@Override
	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {		
		final ICompareInput input = prepareCompareInput(monitor);
		if (input != null)
			setTitle(input.getName());
		return input;
	}
}
