package org.eclipse.compare.codereview.refactorChange;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.DirContext;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.LibraryLocation;

public class NewJavaProject {
	
	public static IProject createNewProject(String projectName, String baseProjDirPath, String suffix){
		
		String inmProjectName = projectName.concat(suffix);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		String rootLoc = root.getLocation().toString();	

		File checkInmProj = new File(rootLoc + "/" + inmProjectName);
		if(checkInmProj.exists()) {
			try {
				RefactorUtils.deleteFile(checkInmProj);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
		}
		
		IProject project = root.getProject(inmProjectName);
		
		IProjectDescription description;
		try {
			project.create(null);
			project.open(null);
			
			
			description = project.getDescription();			
			description.setNatureIds(new String[] { JavaCore.NATURE_ID });
			project.setDescription(description, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IJavaProject javaProject = JavaCore.create(project);
		
		IFolder binFolder = project.getFolder("bin");
		try {
			binFolder.create(false, true, null);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			javaProject.setOutputLocation(binFolder.getFullPath(), null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<IClasspathEntry> entries = new ArrayList<IClasspathEntry>();
		IVMInstall vmInstall = JavaRuntime.getDefaultVMInstall();
		LibraryLocation[] locations = JavaRuntime.getLibraryLocations(vmInstall);
		for (LibraryLocation element : locations) {
		 entries.add(JavaCore.newLibraryEntry(element.getSystemLibraryPath(), null, null));
		}
		//add libs to project class path
		try {
			javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IFolder sourceFolder = project.getFolder("src");
		try {
			sourceFolder.create(false, true, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		IPackageFragmentRoot packageFragmentroot = javaProject.getPackageFragmentRoot(sourceFolder);
		IClasspathEntry[] oldEntries;
		try {
			oldEntries = javaProject.getRawClasspath();
			IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries[oldEntries.length] = JavaCore.newSourceEntry(packageFragmentroot.getPath());
			javaProject.setRawClasspath(newEntries, null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File source = new File(baseProjDirPath + "/src");
	    File desc = new File(rootLoc + "/" + inmProjectName + "/src");
		try {
			RefactorUtils.copyFolder(source, desc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		File source1 = new File(baseProjDirPath + "/.settings");
	    File desc1 = new File(rootLoc + "/" + inmProjectName + "/.settings");
		try {
			RefactorUtils.copyFolder(source1, desc1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			project.refreshLocal(2, new NullProgressMonitor());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*try {
			IPackageFragment pack = javaProject.getPackageFragmentRoot(sourceFolder).createPackageFragment("old", false, null);
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		return project;
	}

}
