package org.eclipse.compare.codereview.pathRelation;

import java.util.ArrayList;

public class RenameChange {
	
	private final static String TYPE = "type";
	private final static String PACKAGE = "package";
	
	private String renameType;
	
	private String projectName;
	
	private String oldPackageName;
	
	private String newPackageName;
	
/*	private String className;*/
	
	private String fileName;
	
/*	private String methodName;*/
	
	private String oldText;
	
	private String newText;
	
	public RenameChange(String renameType, String projectName,
			String oldPackageName, String newPackageName, /*String className, String methodName, */String fileName,
			String oldText, String newText) {
		this.renameType = renameType;
		this.projectName = projectName;
		this.oldPackageName = oldPackageName;
		this.newPackageName = newPackageName;
/*		this.className = className;*/
		this.fileName = fileName;
/*		this.methodName = methodName;*/
		this.oldText = oldText;
		this.newText = newText;
	}

	public String getFileName() { 
		return fileName;
	}

	public String getRenameType() {
		return renameType;
	}

	public String getProjectName() {
		return projectName;
	}

/*	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}*/

	public String getOldPackageName() {
		return oldPackageName;
	}

	public String getNewPackageName() {
		return newPackageName;
	}

	public String getOldText() {
		return oldText;
	}

	public String getNewText() {
		return newText;
	}
	
	public void setNewText(String newText) {
		this.newText = newText;
	}
	
	public void setNewPackageName(String newPackageName) {
		this.newPackageName = newPackageName;
	}
	
	public void setOldPackageName(String oldPackageName) {
		this.oldPackageName = oldPackageName;
	}
	
	public boolean pkgExists(String type, String newName){
		if(getRenameType().equals(type) && getNewText().equals(newName)){
			return true;
		}
		return false;
	}
	
	public boolean sourceExistsWithNewPkgName(String type, String pkgName){
		if(getRenameType().equals(type) && getNewPackageName().equals(pkgName)){
			return true;
		}
		return false;
	}
	
	public static RenameChange findAlreadyExistingNewPkgName(String pkgName){
		for(RenameChange instance : ChangeRefactorInterface.renameChangeList){
			if(instance.pkgExists(PACKAGE, pkgName)) return instance;
		}
		return null;
	}
	
	public static RenameChange findAlreadyExistingNewSourceName(String sourceName){
		for(RenameChange instance : ChangeRefactorInterface.renameChangeList){
			if(instance.pkgExists(TYPE, sourceName)) return instance;
		}
		return null;
	}
	
	public static RenameChange findAlreadyExistingSourceWithNewPkgName(String pkgName){
		for(RenameChange instance : ChangeRefactorInterface.renameChangeList){
			if(instance.pkgExists(TYPE, pkgName)) return instance;
		}
		return null;
	}
	
	public static RenameChange findSourcesInPkg(String pkg, String source, ArrayList<RenameChange> renameChangeRelationList){
		for(RenameChange instance : renameChangeRelationList){
			if(instance.getRenameType().equals(TYPE) && instance.getNewPackageName().equals(pkg) && instance.getNewText().equals(source)){
				return instance;
			}
		}
		return null;
	}
	
	public static RenameChange findPkg(String pkg, ArrayList<RenameChange> renameChangeRelationList){
		for(RenameChange instance : renameChangeRelationList){
			if(instance.getRenameType().equals(PACKAGE) && instance.getNewPackageName().equals(pkg)){
				return instance;
			}
		}
		return null;
	}
	
}
