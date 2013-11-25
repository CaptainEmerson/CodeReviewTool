package org.eclipse.compare.codereview.reviewinterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.compare.codereview.IManualRefactoringInfo;
import org.eclipse.jface.text.Position;

public class RefactoringInfoHandler {
	private Collection<IManualRefactoringInfo> collectedResults;
	private ArrayList<RefactoringInfo> refactoringInfoList;
	
	public RefactoringInfoHandler(
			Collection<IManualRefactoringInfo> collectedResults) {
		super();
		this.collectedResults = collectedResults;
		processRefactoringInfo();
		
	}

	private void processRefactoringInfo() {
		Iterator<IManualRefactoringInfo> results = collectedResults.iterator();
		for (Iterator<IManualRefactoringInfo> iterator = results; iterator.hasNext();) {
			final IManualRefactoringInfo result = (IManualRefactoringInfo) iterator.next();
			String file = result.getRightRefactoringPostion().getPath();
			if(findRefactorInfo(file)!=null){
				Position fLeftPos = new Position(result.getLeftRefactoringPostion().getStartOffset(), result.getLeftRefactoringPostion().getLength());
				Position fRightPos = new Position(result.getLeftRefactoringPostion().getStartOffset(), result.getLeftRefactoringPostion().getLength());
				MyDiff d = new MyDiff(fLeftPos, fRightPos);
				
				findRefactorInfo(file).addDiff(d);
			}
			else {
				String relatedFile = result.getLeftRefactoringPostion().getPath();
				RefactoringInfo info = new RefactoringInfo(file, relatedFile);
				
				Position fLeftPos = new Position(result.getLeftRefactoringPostion().getStartOffset(), result.getLeftRefactoringPostion().getLength());
				Position fRightPos = new Position(result.getLeftRefactoringPostion().getStartOffset(), result.getLeftRefactoringPostion().getLength());
				
				
				MyDiff d = new MyDiff(fLeftPos, fRightPos);
				
				info.addDiff(d);
			}
		}		
	}
	
	private RefactoringInfo findRefactorInfo(String file){
		for(RefactoringInfo info : refactoringInfoList){
			if(info.getFileName().equals(file)){
				return info;
			}
		}
		return null;
	}
	
	
}
