package org.eclipse.compare.codereview.compareEditor;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import org.eclipse.compare.codereview.refactorChange.RefactorUtils;

public class Input implements ITypedElement, IStreamContentAccessor {

	String fContent;
	File file;

	public Input(File file) {
		if(file.exists()){
			this.file = file;
			this.fContent = RefactorUtils.readFileAsString(file);
		}
		else {
			this.file = null;
			this.fContent = "";
		}
		
	}

	public String getName() {
		// TODO Auto-generated method stub
		if(file == null)
			return "New File Added";
		return file.getName();
	}

	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		// TODO Auto-generated method stub
		String[] tmp = getName().split("[.]");
		return tmp[tmp.length - 1];
	}
	
	public File getFile() {
		return file;
	}

	public InputStream getContents() throws CoreException {
		// TODO Auto-generated method stub
		return new ByteArrayInputStream(fContent.getBytes());
	}

}
