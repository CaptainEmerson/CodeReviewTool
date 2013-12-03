package org.eclipse.compare.codereview.compareEditor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.codereview.pathRelation.RenameChange;
import org.eclipse.compare.codereview.refactorChange.RefactorUtils;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.CompareEditor;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.internal.merge.DocumentMerger;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.DiffTreeViewer;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.IDiffContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class ChangeResourceCompareEditorInput extends CompareEditorInput{
	
	private RefactorDiffContainer fRoot;
	private DiffTreeViewer fDiffViewer;
	private IAction fOpenAction;
	private IProject fProject;
	public IProject getfProject() {
		return fProject;
	}

	private String oldProjectPath;
	private String newInmProjectPath;
	private ArrayList<RenameChange> renameChangeRelationList;
	
	class RefactorDiffNode extends DiffNode {
		private ITypedElement fLastId;
		private String name;
		private IDiffContainer parent;
		private String oldFilePath;
		private String inmFilePath;
		private String currentFilePath;
		private IAdaptable fResource;

		public RefactorDiffNode(IDiffContainer parent, String fLastName, IAdaptable fResource) {
			super(parent, 0);
			this.name = fLastName;
			this.fResource = fResource;
		}
		
		public String getOldFilePath() {
			return oldFilePath;
		}

		public void setOldFilePath(String oldFilePath) {
			this.oldFilePath = oldFilePath;
		}

		public String getInmFilePath() {
			return inmFilePath;
		}

		public void setInmFilePath(String inmFilePath) {
			this.inmFilePath = inmFilePath;
		}

		public String getCurrentFilePath() {
			return currentFilePath;
		}

		public void setCurrentFilePath(String currentFilePath) {
			this.currentFilePath = currentFilePath;
		}

		public String getName() {
			return name;
		}

		public IDiffContainer getParent() {
			return parent;
		}

		public String toString() {
			return getName();
		}
		
		public ITypedElement getId() {
			ITypedElement id = super.getId();
			if (id == null)
				return fLastId;
			fLastId = id;
			return id;
		}
		
		public Image getImage() {
			return CompareUI.getImage(fResource);
		}
	}
	
	
	
	class RefactorDiffContainer extends DiffNode {

		private ITypedElement fLastId;
		private String fLastName;
		private IAdaptable fResource;

		public RefactorDiffContainer(IDiffContainer parent, int description, ITypedElement left, ITypedElement right, String fLastName, IAdaptable fResource) {
			super(parent, description, null, left, right);
			this.fLastName = fLastName;
			this.fResource = fResource;
		}

		public String getName() {
			if (fLastName == null)
				fLastName = super.getName();
			return fLastName;
		}

		public ITypedElement getId() {
			ITypedElement id = super.getId();
			if (id == null)
				return fLastId;
			fLastId = id;
			return id;
		}
		
		public Image getImage() {
			return CompareUI.getImage(fResource);
		}
	}
	
	/*
	 * Creates an compare editor input for the given selection.
	 */
	public ChangeResourceCompareEditorInput(IProject project,
			String oldProjectPath, String newInmProjectPath,
			ArrayList<RenameChange> renameChangeRelationList) {
		super(new CompareConfiguration());

		// buffered merge mode: don't ask for confirmation
		// when switching between modified resources
		getCompareConfiguration().setProperty(
				CompareEditor.CONFIRM_SAVE_PROPERTY, new Boolean(false));
		getCompareConfiguration();
		CompareConfiguration.setfLeftIsLocal(false);
		getCompareConfiguration().setProperty(
				"LEFT_IS_LOCAL", new Boolean(false));
		getCompareConfiguration().setLeftEditable(false);
		getCompareConfiguration().setRightEditable(false);
		
		this.fProject = project;
		this.oldProjectPath = oldProjectPath;
		this.newInmProjectPath = newInmProjectPath;
		this.renameChangeRelationList = renameChangeRelationList;

	}
			
	public Viewer createDiffViewer(Composite parent) {
		fDiffViewer= new DiffTreeViewer(parent, getCompareConfiguration()) {
			protected void fillContextMenu(IMenuManager manager) {
				
				if (fOpenAction == null) {
					fOpenAction= new Action() {
						public void run() {
							handleOpen(null);
						}
					};
					Utilities.initAction(fOpenAction, getBundle(), "action.CompareContents."); //$NON-NLS-1$
				}
				
				boolean enable= false;
				ISelection selection= getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection ss= (IStructuredSelection)selection;
					if (ss.size() == 1) {
						Object element= ss.getFirstElement();
						if (element instanceof RefactorDiffContainer) {
							ITypedElement te= ((RefactorDiffContainer) element).getId();
							if (te != null)
								enable= !ITypedElement.FOLDER_TYPE.equals(te.getType());
						} else
							enable= true;
					}
				}
				fOpenAction.setEnabled(enable);
				
				manager.add(fOpenAction);
				
				super.fillContextMenu(manager);
			}
		};
		return fDiffViewer;
	}
	
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof ChangeResourceCompareEditorInput) {
			ChangeResourceCompareEditorInput other = (ChangeResourceCompareEditorInput) obj;
			return other.getfProject().equals(fProject);
		}
		return false;
	}
	
	/*
	 * Performs a two-way or three-way diff on the current selection.
	 */
	public Object prepareInput(IProgressMonitor pm)
			throws InvocationTargetException {

		try {

			pm.beginTask("Opening Compare Window", IProgressMonitor.UNKNOWN); //$NON-NLS-1$			
			String title = "Review :" + fProject.getName();
			setTitle(title);
			createCompareInput();

			return fRoot;

		} finally {
			pm.done();
		}
	}
	
	private void createCompareInput() {

		try {
			char separator = Path.SEPARATOR;
			IJavaProject javaProject = JavaCore.create(fProject);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			String finalProjectPath = fProject.getLocation().toString();
			fRoot = new RefactorDiffContainer(null, 0, null, null, javaProject.getElementName(), (IAdaptable) fProject);
			RefactorDiffContainer pkg = null;
			for (IPackageFragment mypackage : packages) {
				if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
					if (mypackage.getElementName().equals(""))
						pkg = null;
					else
						pkg = new RefactorDiffContainer((IDiffContainer) fRoot,
								0, null, null, mypackage.getElementName(), (IAdaptable) mypackage);
					RefactorDiffNode source = null;
					if(mypackage
							.getCompilationUnits().length==0){
						RenameChange pkgInstance = RenameChange.findPkg(
								mypackage.getElementName(),
								renameChangeRelationList);
						if(pkgInstance==null){
							pkg.setKind(0);
						}
						else {
							pkg.setKind(Differencer.RIGHT + Differencer.CHANGE);
						}
					}
					for (ICompilationUnit unit : mypackage
							.getCompilationUnits()) {
						if (pkg == null)
							source = new RefactorDiffNode(fRoot, unit.getElementName(), (IAdaptable) unit);
						else
							source = new RefactorDiffNode(pkg, unit.getElementName(), (IAdaptable) unit);
						RenameChange sourceInstance = RenameChange
								.findSourcesInPkg(mypackage.getElementName(),
										unit.getElementName().split("[.]")[0],
										renameChangeRelationList);
						if (sourceInstance == null) {
							RenameChange pkgInstance = RenameChange.findPkg(
									mypackage.getElementName(),
									renameChangeRelationList);
							if (pkgInstance == null) {
								String[] samePkgs = mypackage.getElementName()
										.split("[.]");
								String oldFilePath = oldProjectPath + separator
										+ "src" + separator;
								String inmFilePath = newInmProjectPath
										+ separator + "src" + separator;
								String newFilePath = finalProjectPath
										+ separator + "src" + separator;

								for (int o = 0; o < samePkgs.length; o++) {
									oldFilePath += samePkgs[o] + separator;
									inmFilePath += samePkgs[o] + separator;
									newFilePath += samePkgs[o] + separator;
								}

								oldFilePath += unit.getElementName();
								inmFilePath += unit.getElementName();
								newFilePath += unit.getElementName();

								source.setOldFilePath(oldFilePath);
								source.setInmFilePath(inmFilePath);
								source.setCurrentFilePath(newFilePath);
								if (pkg != null)
									pkg.setKind(Differencer.NO_CHANGE);
								source.setKind(Differencer.NO_CHANGE);
								
								if(!(new File(oldFilePath)).exists()){
									source.setKind(Differencer.DELETION);
								}
							} else {
								String[] oldPkgs = pkgInstance.getOldText()
										.split("[.]");
								String[] newPkgs = pkgInstance.getNewText()
										.split("[.]");
								String oldFilePath = oldProjectPath + separator
										+ "src" + separator;
								String inmFilePath = newInmProjectPath
										+ separator + "src" + separator;
								String newFilePath = finalProjectPath
										+ separator + "src" + separator;
								
								String oldPkgPath = "";
								for (int o = 0; o < oldPkgs.length; o++) {
									oldPkgPath += oldPkgs[o] + separator;
								}
								oldFilePath += oldPkgPath;
								String newPkgPath = "";
								for (int n = 0; n < newPkgs.length; n++) {
									newPkgPath += newPkgs[n] + separator;
								}
								
								inmFilePath += newPkgPath;
								newFilePath += newPkgPath;
								
								if (pkg != null) {
									if (oldPkgPath.equals(newPkgPath))
										pkg.setKind(0);
									else
										pkg.setKind(Differencer.RIGHT + Differencer.CHANGE);
								}
								
								oldFilePath += unit.getElementName();
								inmFilePath += unit.getElementName();
								newFilePath += unit.getElementName();
								
								Input oldInput = new Input(new File(oldFilePath));
								Input newInput = new Input(new File(newFilePath));
								Differencer d = new Differencer();
								Object diff = d.findDifferences(false, null, null,
										null, oldInput, newInput);
								
								if(diff==null)
									source.setKind(0);
								else
									source.setKind(Differencer.RIGHT + Differencer.CHANGE);

								source.setOldFilePath(oldFilePath);
								source.setInmFilePath(inmFilePath);
								source.setCurrentFilePath(newFilePath);
								if(!(new File(oldFilePath)).exists()){
									source.setKind(Differencer.DELETION);
								}
							}
						} else {
							String[] oldPkgs = sourceInstance
									.getOldPackageName().split("[.]");
							String[] newPkgs = sourceInstance
									.getNewPackageName().split("[.]");

							String oldFilePath = oldProjectPath + separator
									+ "src" + separator;
							String inmFilePath = newInmProjectPath + separator
									+ "src" + separator;
							String newFilePath = finalProjectPath + separator
									+ "src" + separator;

							String oldPkgPath = "";
							for (int o = 0; o < oldPkgs.length; o++) {
								if (!oldPkgs[o].equals(""))
									oldPkgPath += oldPkgs[o] + separator;
							}

							oldFilePath += oldPkgPath;
							String newPkgPath = "";
							for (int n = 0; n < newPkgs.length; n++) {
								if (!newPkgs[n].equals("")) {
									newPkgPath += newPkgs[n] + separator;
								}
							}
							inmFilePath += newPkgPath;
							newFilePath += newPkgPath;
							
							if (pkg != null) {
								if (oldPkgPath.equals(newPkgPath))
									pkg.setKind(0);
								else
									pkg.setKind(Differencer.RIGHT + Differencer.CHANGE);
							}

							oldFilePath += sourceInstance.getOldText()
									+ ".java";
							inmFilePath += sourceInstance.getNewText()
									+ ".java";
							newFilePath += sourceInstance.getNewText()
									+ ".java";
							
							source.setKind(Differencer.RIGHT + Differencer.CHANGE);

							source.setOldFilePath(oldFilePath);
							source.setInmFilePath(inmFilePath);
							source.setCurrentFilePath(newFilePath);
							if(!(new File(oldFilePath)).exists()){
								source.setKind(Differencer.DELETION);
							}
						}
					}
				}
			}

		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Object getInitializedDiffInputData(ISelection selection){
		
		Object input= getElement(selection);
		getCompareConfiguration().setInmMergerResult(null);
		
		if(getElement(selection) instanceof RefactorDiffNode){
			RefactorDiffNode node = (RefactorDiffNode) getElement(selection);
			Input left = new Input(new File(node.getOldFilePath()));
			Input right = new Input(new File(node.getCurrentFilePath()));
			
			input = new DiffNode(Differencer.CHANGE, null, left, right);
			initLabels(left, right);
			
			Input inm = new Input(new File(node.getInmFilePath()));
			
			CompareEditorInput tmpInput = new ChangeCompareEditorInput(inm, right);
			CompareUI.openCompareEditor(tmpInput, OpenStrategy.activateOnOpen());
			DocumentMerger diff = ((TextMergeViewer) tmpInput.getfContentInputPane()
					.getViewer()).getfMerger();
			IWorkbenchPage workBenchPage = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getActivePage();
			IEditorPart editor = RefactorUtils.findReusableCompareEditor(tmpInput,
					workBenchPage, new Class[] { ChangeCompareEditorInput.class });
			
			if (editor != null) { workBenchPage.closeEditor(editor, false); }
			
			if(((Input) left).getFile()== null)
				getCompareConfiguration().setInmMergerResult(null);
			else
				getCompareConfiguration().setInmMergerResult(diff);
		}	
		
		
		
		return input;
	}
	
	protected void feed1(final ISelection selection) {
		BusyIndicator.showWhile(fComposite.getDisplay(),
			new Runnable() {
				public void run() {
					if (selection == null || selection.isEmpty()) {
						Object input= fStructureInputPane.getInput();
						if (input != null)
							internalSetContentPaneInput(input);
						if (!Utilities.okToUse(fStructurePane1) || !Utilities.okToUse(fStructurePane2))
							return;
						fStructurePane2.setInput(null); // clear downstream pane
						fStructurePane1.setInput(null);
					} else {
						Object input= getInitializedDiffInputData(selection);				
						
						internalSetContentPaneInput(input);
						if (!Utilities.okToUse(fStructurePane1) || !Utilities.okToUse(fStructurePane2))
							return;						
						if (structureCompareOnSingleClick() || hasUnusableContentViewer())
							fStructurePane1.setInput(input);
						fStructurePane2.setInput(null); // clear downstream pane
						if (fStructurePane1.getInput() != input)
							fStructurePane1.setInput(null);
					}
				}
			}
		);
	}
	
	private void initLabels(Input left, Input right) {
		CompareConfiguration cc = getCompareConfiguration();
		if (left != null) {
			if(left.getName().equals("New File Added"))
				cc.setLeftLabel("No History :: " + left.getName());
			else 
				cc.setLeftLabel("Local Revision : " + left.getName());
		}
		if (right != null) {
			cc.setRightLabel("Local : " + right.getName());
		}
	}
	
	protected void feedDefault1(final ISelection selection) {
		BusyIndicator.showWhile(fComposite.getDisplay(),
			new Runnable() {
				public void run() {
					if (!selection.isEmpty()) {
						Object input= getInitializedDiffInputData(selection);
						fStructurePane1.setInput(input);
					}
				}
			}
		);
	}
	
	protected void feed2(final ISelection selection) {
		BusyIndicator.showWhile(fComposite.getDisplay(),
			new Runnable() {
				public void run() {
					if (selection.isEmpty()) {
						Object input= fStructurePane1.getInput();
						internalSetContentPaneInput(input);
						fStructurePane2.setInput(null);
					} else {
						Object input= getInitializedDiffInputData(selection);
						internalSetContentPaneInput(input);
						fStructurePane2.setInput(input);
					}
				}
			}
		);
	}
	
	protected void feed3(final ISelection selection) {
		BusyIndicator.showWhile(fComposite.getDisplay(),
			new Runnable() {
				public void run() {
					if (selection.isEmpty())
						internalSetContentPaneInput(fStructurePane2.getInput());
					else {
						Object input= getInitializedDiffInputData(selection);
						internalSetContentPaneInput(input);
					}
				}
			}
		);
		
	}

	public String getToolTipText() {
		if (fProject != null) {
			String title = "Review :" + fProject.getName();
			return title;
		}
		// fall back
		return super.getToolTipText();
	}
	
	protected void handleDispose() {
		super.handleDispose();
		CompareConfiguration.setfLeftIsLocal(true);
	}
	
	public boolean canRunAsJob() {
		return true;
	}

}
