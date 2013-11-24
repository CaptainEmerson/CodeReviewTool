package org.eclipse.compare.codereview.pathRelation;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public abstract class ChangeRefactorInterface {

	static ArrayList<RenameChange> renameChangeList = null;

	public static ArrayList<RenameChange> processChangeTypeRelation(ArrayList<String[]> refactoringList,
			IProject project) {
		
		renameChangeList = new ArrayList<RenameChange>();

		for (String[] refactoringChange : refactoringList) {
			processChange(refactoringChange, project);
		}
		
		return renameChangeList;
	}

	private static void processChange(String[] refactoringChange,
			IProject project) {
		String[] idSubs = refactoringChange[0].split("[.]");
		RenameChange renameObject = renameCheck(refactoringChange, idSubs);
		if(renameObject!=null)	renameChangeList.add(renameObject);
	}

	private static RenameChange renameCheck(String[] refactoringChange,
			String[] idSubs) {
		RenameChange renameObject = null;
		if (idSubs[0].equals("org")) {
			if (idSubs[1].equals("eclipse")) {
				if (idSubs[2].equals("jdt")) {
					if (idSubs[3].equals("ui")) {
						if (idSubs[4].equals("rename")) {
							RenameChangeTypeProcess rename = new RenameChangeTypeProcess(
									refactoringChange);
							//rename.process(project);
							renameObject = rename.tempProcess();
						}
					}
				}
			}
		}
		return renameObject;
	}

	abstract public void process(IProject project);

	abstract public void createChangeObject(String[] refactoringChange);

}
