package org.eclipse.compare.codereview.compareEditor;

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
*******************************************************************************/
  
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.jface.viewers.Viewer;
 
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.IViewerCreator;
 
 
/**
  * Required when creating a JavaMergeViewer from the plugin.xml file.
  */
public class RefacJavaContentViewerCreator implements IViewerCreator {
	@SuppressWarnings("restriction")
	public Viewer createViewer(Composite parent, CompareConfiguration mp) {
		return new RefacTextMergeViewer(parent, mp);
		}  
	}
