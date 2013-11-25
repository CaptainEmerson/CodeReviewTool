package org.eclipse.compare.codereview.reviewinterface;

import java.util.ArrayList;

import org.eclipse.jface.text.Position;

public class RefactoringInfo {
	private String fileName;
	private String relatedFile;
	private ArrayList<MyDiff> fChangeDiffs;
	public String getFileName() {
		return fileName;
	}
	public String getRelatedFile() {
		return relatedFile;
	}
	public ArrayList<MyDiff> getfChangeDiffs() {
		return fChangeDiffs;
	}
	public RefactoringInfo(String fileName, String relatedFile) {
		super();
		this.fileName = fileName;
		this.relatedFile = relatedFile;
		fChangeDiffs = new ArrayList<MyDiff>();
	}
	
	public void addDiff(MyDiff d){
		fChangeDiffs.add(d);
	}
}

class MyDiff {
	/** character range in left document */
	Position fLeftPos;
	/** character range in right document */
	Position fRightPos;
	
	public MyDiff(Position fLeftPos, Position fRightPos) {
		super();
		this.fLeftPos = fLeftPos;
		this.fRightPos = fRightPos;
	}
}