package org.eclipse.compare.codereview.reviewinterface;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.text.Position;

/*import edu.dlf.refactoring.ui.IManualRefactoringInfo;
import edu.dlf.refactoring.ui.IManualRefactoringPosition;*/

public class RefactoringInfoHandler {/*
	private Collection<IManualRefactoringInfo> collectedResults;
	private ArrayList<RefactoringInfo> refactoringInfoList;

	public RefactoringInfoHandler(
			Collection<IManualRefactoringInfo> collectedResults) {
		super();
		this.collectedResults = collectedResults;
		processRefactoringInfo();

	}

	private void processRefactoringInfo() {
		for (Iterator<IManualRefactoringInfo> results = collectedResults
				.iterator(); results.hasNext();) {
			final IManualRefactoringInfo result = (IManualRefactoringInfo) results
					.next();
			for (Iterator<IManualRefactoringPosition> lefts = result
					.getLeftRefactoringPostions().iterator(), rights = result
					.getRightRefactoringPostions().iterator(); lefts.hasNext()
					&& rights.hasNext();) {
				IManualRefactoringPosition left = (IManualRefactoringPosition) lefts
						.next();
				IManualRefactoringPosition right = (IManualRefactoringPosition) rights
						.next();
				String file = right.getPath();
				if (findRefactorInfo(file) != null) {
					Position fLeftPos = new Position(left.getStartOffset(),
							left.getLength());
					Position fRightPos = new Position(right.getStartOffset(),
							right.getLength());
					MyDiff d = new MyDiff(fLeftPos, fRightPos);

					findRefactorInfo(file).addDiff(d);
				} else {
					String relatedFile = left.getPath();
					RefactoringInfo info = new RefactoringInfo(file,
							relatedFile);

					Position fLeftPos = new Position(left.getStartOffset(),
							left.getLength());
					Position fRightPos = new Position(right.getStartOffset(),
							right.getLength());

					MyDiff d = new MyDiff(fLeftPos, fRightPos);

					info.addDiff(d);
				}
			}

		}
	}

	private RefactoringInfo findRefactorInfo(String file) {
		for (RefactoringInfo info : refactoringInfoList) {
			if (info.getFileName().equals(file)) {
				return info;
			}
		}
		return null;
	}

*/}
