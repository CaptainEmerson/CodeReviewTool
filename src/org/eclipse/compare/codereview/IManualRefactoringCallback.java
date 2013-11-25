package org.eclipse.compare.codereview;

import java.util.Collection;

public interface IManualRefactoringCallback {
	void callBack(Collection<IManualRefactoringInfo> results);
}
