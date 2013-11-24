package org.eclipse.compare.codereview.refactorChange;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.eclipse.compare.codereview.compareEditor.ChangeCompareEditorInput;

public class RefactorUtils {
	
	public static void copyFolder(File src, File dest) throws IOException {

		if (src.isDirectory()) {

			// if directory not exists, create it
			if (!dest.exists()) {
				dest.mkdir();
			}

			// list all the directory contents
			String files[] = src.list();

			for (String file : files) {
				// construct the src and dest file structure
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				// recursive copy
				copyFolder(srcFile, destFile);
			}

		} else {
			// if file, then copy it
			// Use bytes stream to support all file types
			InputStream in = new FileInputStream(src);
			OutputStream out = new FileOutputStream(dest);

			byte[] buffer = new byte[1024];

			int length;
			// copy the file content in bytes
			while ((length = in.read(buffer)) > 0) {
				out.write(buffer, 0, length);
			}

			in.close();
			out.close();
		}
	}
	
	public static void deleteFile(File file) throws IOException {
		if(file.isDirectory()){
			 
    		//directory is empty, then delete it
    		if(file.list().length==0){
 
    		   file.delete();
 
    		}else{
 
    		   //list all the directory contents
        	   String files[] = file.list();
 
        	   for (String temp : files) {
        	      //construct the file structure
        	      File fileDelete = new File(file, temp);
 
        	      //recursive delete
        	      deleteFile(fileDelete);
        	   }
 
        	   //check the directory again, if empty then delete it
        	   if(file.list().length==0){
           	     file.delete();
        	   }
    		}
 
    	}else{
    		//if file, then delete it
    		file.delete();
    	}
	}
	
	/**
	 * Returns an editor that can be re-used. An open compare editor that has
	 * un-saved changes cannot be re-used.
	 * 
	 * Contributors:
	 *     IBM Corporation - initial API and implementation
	 * 
	 * @param input
	 *            the input being opened
	 * @param page
	 * @param editorInputClasses 
	 * @return an EditorPart or <code>null</code> if none can be found
	 */
	public static IEditorPart findReusableCompareEditor(
			CompareEditorInput input, IWorkbenchPage page,
			Class[] editorInputClasses) {
		IEditorReference[] editorRefs = page.getEditorReferences();
		// first loop looking for an editor with the same input
		for (int i = 0; i < editorRefs.length; i++) {
			IEditorPart part = editorRefs[i].getEditor(false);
			if (part != null && part instanceof IReusableEditor) {
				for (int j = 0; j < editorInputClasses.length; j++) {
					// check if the editor input type 
					// complies with the types given by the caller
					if (editorInputClasses[j].isInstance(part.getEditorInput())
							&& part.getEditorInput().equals(input))
						return part;
				}
			}
		}
		/*// if none found and "Reuse open compare editors" preference is on use
		// a non-dirty editor
		if (AbstractUIPlugin.getPreferenceStore()
				.getBoolean(IPreferenceIds.REUSE_OPEN_COMPARE_EDITOR)) {
			for (int i = 0; i < editorRefs.length; i++) {
				IEditorPart part = editorRefs[i].getEditor(false);
				if (part != null
						&& (part.getEditorInput() instanceof ChangeCompareEditorInput)
						&& part instanceof IReusableEditor && !part.isDirty()) {
					return part;
				}
			}
		}*/

		// no re-usable editor found
		return null;
	}
	
	
	public static String readFileAsString(File filePath){
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
