package org.eclipse.compare.codereview.compareEditor;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.MergeViewerContentProvider;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.internal.merge.DocumentMerger;
import org.eclipse.compare.internal.merge.DocumentMerger.IDocumentMergerInput;
import org.eclipse.compare.patch.IHunk;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.swt.widgets.Composite;

public class MyJavaMergeViewer extends RefacTextMergeViewer{
	
	private DocumentMerger fMergerTmp;

	@SuppressWarnings("restriction")
	public MyJavaMergeViewer(Composite parent,
			CompareConfiguration configuration) {
		super(parent, configuration);
		/*fMergerTmp = new DocumentMerger(new IDocumentMergerInput() {
			public ITokenComparator createTokenComparator(String line) {
				return createTokenComparator(line);
			}
			public CompareConfiguration getCompareConfiguration() {
				return getCompareConfiguration();
			}
			public IDocument getDocument(char contributor) {
				switch (contributor) {
				case MergeViewerContentProvider.LEFT_CONTRIBUTOR:
					return fLeft.getSourceViewer().getDocument();
				case  MergeViewerContentProvider.RIGHT_CONTRIBUTOR:
					return fRight.getSourceViewer().getDocument();
				case  MergeViewerContentProvider.ANCESTOR_CONTRIBUTOR:
					return fAncestor.getSourceViewer().getDocument();
				}
				return null;
			}
			public int getHunkStart() {
				return getHunkStart();
			}
			public Position getRegion(char contributor) {
				switch (contributor) {
				case MergeViewerContentProvider.LEFT_CONTRIBUTOR:
					return fLeft.getRegion();
				case MergeViewerContentProvider.RIGHT_CONTRIBUTOR:
					return fRight.getRegion();
				case MergeViewerContentProvider.ANCESTOR_CONTRIBUTOR:
					return fAncestor.getRegion();
				}
				return null;
			}
			public boolean isHunkOnLeft() {
				ITypedElement left = ((ICompareInput)getInput()).getRight();
				return left != null && Utilities.getAdapter(left, IHunk.class) != null;
			}
			public boolean isIgnoreAncestor() {
				return isIgnoreAncestor();
			}
			public boolean isPatchHunk() {
				return isPatchHunk();
			}

			public boolean isShowPseudoConflicts() {
				return false;
			}
			public boolean isThreeWay() {
				return isThreeWay();
			}
			public boolean isPatchHunkOk() {
				return isPatchHunkOk();
			}
			
		});*/
	}

}
