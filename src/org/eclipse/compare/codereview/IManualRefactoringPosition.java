package org.eclipse.compare.codereview;

public interface IManualRefactoringPosition {
	String getPath();
	int getStartOffset();
	int getLength();
}