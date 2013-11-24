package org.eclipse.compare.codereview.compareEditor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.Image;

public class EditorInput extends CompareEditorInput {
	
	private Input input1 = null;
	private Input input2 = null;

	public EditorInput(CompareConfiguration configuration, File input1, File input2) {
		super(configuration);
		this.input1 = new Input(input1);
		this.input2 = new Input(input2);		
	}

	@Override
	protected Object prepareInput(IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		Differencer d = new Differencer();
		Object diff = d.findDifferences(false, new NullProgressMonitor(), null,
				null, input1, input2);
		
/*		((DiffNode) diff).setKind(Differencer.PSEUDO_CONFLICT);*/
		
		
		/*Input ancestor = 
		            new Input("Common");
		Input left = 
		            new Input("Left");
		Input right = 
		            new Input("Right");
		         return new DiffNode(null, Differencer.CONFLICTING, 
		            ancestor, left, right);*/
		return diff;
	}
	
	public String readFileAsString(File filePath){
	            StringBuffer fileData = new StringBuffer(1000);
	            BufferedReader reader = null;
				try {
					reader = new BufferedReader(
					        new FileReader(filePath));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            char[] buf = new char[1024];
	            int numRead=0;
	            try {
					while((numRead=reader.read(buf)) != -1){
					    String readData = String.valueOf(buf, 0, numRead);
					    fileData.append(readData);
					    buf = new char[1024];
					}
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            return fileData.toString();
	        }

}

/*class Input implements ITypedElement, IStreamContentAccessor {

	String fContent;

	public Input(String s) {
		fContent = s;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "name";
	}

	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getType() {
		// TODO Auto-generated method stub
		return "txt";
	}

	public InputStream getContents() throws CoreException {
		// TODO Auto-generated method stub
		return new ByteArrayInputStream(fContent.getBytes());
	}

}*/
