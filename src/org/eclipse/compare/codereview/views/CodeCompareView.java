package org.eclipse.compare.codereview.views;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareEditorInput;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.ITokenComparator;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.contentmergeviewer.TokenComparator;
import org.eclipse.compare.internal.MergeViewerContentProvider;
import org.eclipse.compare.internal.NullViewer;
import org.eclipse.compare.internal.Utilities;
import org.eclipse.compare.internal.merge.DocumentMerger;
import org.eclipse.compare.internal.merge.DocumentMerger.IDocumentMergerInput;
import org.eclipse.ui.editors.text.*;
import org.eclipse.compare.patch.IHunk;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.eclipse.compare.codereview.compareEditor.ChangeCompareEditorInput;
import org.eclipse.compare.codereview.compareEditor.EditorInput;
import org.eclipse.compare.codereview.compareEditor.Input;
import org.eclipse.compare.codereview.pathRelation.RenameChange;
import org.eclipse.compare.codereview.refactorChange.RefactorUtils;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view
 * shows data obtained from the model. The sample creates a dummy model on the
 * fly, but a real implementation would connect to the model available either in
 * this or another plug-in (e.g. the workspace). The view is connected to the
 * model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be
 * presented in the view. Each view can present the same model objects using
 * different labels and icons, if needed. Alternatively, a single label provider
 * can be shared between views in order to ensure that objects of the same type
 * are presented in the same way everywhere.
 * <p>
 */

public class CodeCompareView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.eclipse.compare.views.CodeCompare";

	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	private static TreeParent root;
	private ImageRegistry imageRegistry;
	private final String SOURCE = "Source";
	private final String PACKAGE = "Package";
	private final String PROJECT = "project";

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	class TreeObject implements IAdaptable {
		private String name;
		private TreeParent parent;
		private String oldFilePath;
		private String inmFilePath;

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

		private String currentFilePath;

		public TreeObject(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void setParent(TreeParent parent) {
			this.parent = parent;
		}

		public TreeParent getParent() {
			return parent;
		}

		public String toString() {
			return getName();
		}

		public Object getAdapter(Class key) {
			return null;
		}
	}

	class TreeParent extends TreeObject {
		private ArrayList children;

		public TreeParent(String name) {
			super(name);
			children = new ArrayList();
		}

		public void addChild(TreeObject child) {
			children.add(child);
			child.setParent(this);
		}

		public void removeChild(TreeObject child) {
			children.remove(child);
			child.setParent(null);
		}

		public TreeObject[] getChildren() {
			return (TreeObject[]) children.toArray(new TreeObject[children
					.size()]);
		}

		public boolean hasChildren() {
			return children.size() > 0;
		}
	}

	class ViewContentProvider implements IStructuredContentProvider,
			ITreeContentProvider {
		private TreeParent invisibleRoot;

		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
			/* root = null; */
		}

		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot == null)
					initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}

		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject) child).getParent();
			}
			return null;
		}

		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return ((TreeParent) parent).getChildren();
			}
			return new Object[0];
		}

		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent)
				return ((TreeParent) parent).hasChildren();
			return false;
		}

		/*
		 * We will set up a dummy model to initialize tree heararchy. In a real
		 * code, you will connect to a real model and expose its hierarchy.
		 */
		private void initialize() {
			if (root != null) {
				invisibleRoot = new TreeParent("");
				invisibleRoot.addChild(root);
			}
		}
	}

	class ViewLabelProvider extends LabelProvider {

		public String getText(Object obj) {
			return obj.toString();
		}

		public Image getImage(Object obj) {

			// initializeImageRegistry();
			String imageKey = ISharedImages.IMG_OBJ_FILE;
			if (obj instanceof TreeParent)
				imageKey = ISharedImages.IMG_OBJ_FOLDER;
			if (obj == root)
				imageKey = ISharedImages.IMG_OBJ_PROJECT;
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(imageKey);
		}
	}

	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public CodeCompareView() {

	}

	/**
	 * The constructor.
	 * 
	 * @param newInmProjectPath
	 * @param newInmProjectPath
	 * @param renameChangeRelationList
	 */
	public CodeCompareView(IProject project, String oldProjectPath,
			String newInmProjectPath,
			ArrayList<RenameChange> renameChangeRelationList) {
		try {
			IJavaProject javaProject = JavaCore.create(project);
			IPackageFragment[] packages = javaProject.getPackageFragments();
			String finalProjectPath = project.getLocation().toString();
			finalProjectPath = finalProjectPath.replace('/', '\\');
			newInmProjectPath = newInmProjectPath.replace('/', '\\');
			root = new TreeParent(javaProject.getElementName());
			TreeParent pkgs[] = new TreeParent[packages.length];
			int i = 0;
			for (IPackageFragment mypackage : packages) {
				if (mypackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
					if (mypackage.getElementName().equals(""))
						pkgs[i] = new TreeParent("(default package)");
					else
						pkgs[i] = new TreeParent(mypackage.getElementName());
					TreeObject source[] = new TreeObject[mypackage
							.getCompilationUnits().length];
					int j = 0;
					for (ICompilationUnit unit : mypackage
							.getCompilationUnits()) {
						source[j] = new TreeObject(unit.getElementName());
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
								String oldFilePath = oldProjectPath + "\\src\\";
								String inmFilePath = newInmProjectPath
										+ "\\src\\";
								String newFilePath = finalProjectPath
										+ "\\src\\";

								for (int o = 0; o < samePkgs.length; o++) {
									oldFilePath += samePkgs[o] + "\\";
									inmFilePath += samePkgs[o] + "\\";
									newFilePath += samePkgs[o] + "\\";
								}

								oldFilePath += unit.getElementName();
								inmFilePath += unit.getElementName();
								newFilePath += unit.getElementName();

								source[j].setOldFilePath(oldFilePath);
								source[j].setInmFilePath(inmFilePath);
								source[j].setCurrentFilePath(newFilePath);
							} else {
								String[] oldPkgs = pkgInstance.getOldText()
										.split("[.]");
								String[] newPkgs = pkgInstance.getNewText()
										.split("[.]");
								String oldFilePath = oldProjectPath + "\\src\\";
								String inmFilePath = newInmProjectPath
										+ "\\src\\";
								String newFilePath = finalProjectPath
										+ "\\src\\";

								for (int o = 0; o < oldPkgs.length; o++) {
									oldFilePath += oldPkgs[o] + "\\";
								}

								for (int n = 0; n < newPkgs.length; n++) {
									inmFilePath += newPkgs[n] + "\\";
									newFilePath += newPkgs[n] + "\\";
								}

								oldFilePath += unit.getElementName();
								inmFilePath += unit.getElementName();
								newFilePath += unit.getElementName();

								source[j].setOldFilePath(oldFilePath);
								source[j].setInmFilePath(inmFilePath);
								source[j].setCurrentFilePath(newFilePath);
							}
						} else {
							String[] oldPkgs = sourceInstance
									.getOldPackageName().split("[.]");
							String[] newPkgs = sourceInstance
									.getNewPackageName().split("[.]");

							String oldFilePath = oldProjectPath + "\\src\\";
							String inmFilePath = newInmProjectPath + "\\src\\";
							String newFilePath = finalProjectPath + "\\src\\";

							for (int o = 0; o < oldPkgs.length; o++) {
								if (!oldPkgs[o].equals(""))
									oldFilePath += oldPkgs[o] + "\\";
							}

							for (int n = 0; n < newPkgs.length; n++) {
								if (!newPkgs[n].equals("")) {
									inmFilePath += newPkgs[n] + "\\";
									newFilePath += newPkgs[n] + "\\";
								}
							}

							oldFilePath += sourceInstance.getOldText()
									+ ".java";
							inmFilePath += sourceInstance.getNewText()
									+ ".java";
							newFilePath += sourceInstance.getNewText()
									+ ".java";

							source[j].setOldFilePath(oldFilePath);
							source[j].setInmFilePath(inmFilePath);
							source[j].setCurrentFilePath(newFilePath);
						}

						pkgs[i].addChild(source[j]);
						j++;
					}
					if (j != 0)
						root.addChild(pkgs[i]);
					i++;
				}
			}

		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		/*
		 * PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),
		 * "Demo.viewer");
		 */
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				CodeCompareView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				showMessage("Action 1 executed");
			}
		};
		action1.setText("Action 1");
		action1.setToolTipText("Action 1 tooltip");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));

		action2 = new Action() {
			public void run() {
				showMessage("Action 2 executed");
			}
		};
		action2.setText("Action 2");
		action2.setToolTipText("Action 2 tooltip");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				if (!(obj instanceof TreeParent)) {
					if (obj instanceof TreeObject) {
						TreeObject source = (TreeObject) obj;
						doubleClickAction(source);
					}
				}
			}
		};
	}

	private void doubleClickAction(TreeObject source) {
		/*
		 * String message = "Source : " + source.getName() +
		 * "\n\t Old File Path : " + source.getOldFilePath() +
		 * "\n\t Inm File Path : " + source.getInmFilePath() +
		 * "\n\t Final File Path : " + source.getCurrentFilePath();
		 * showMessage(message);
		 */

		File oldinput = new File(source.getOldFilePath());
		File inminput = new File(source.getInmFilePath());
		File newinput = new File(source.getCurrentFilePath());

		Input oldInput = new Input(oldinput);
		Input inmInput = new Input(inminput);
		Input newInput = new Input(newinput);

		openInCompare(oldInput, newInput, inmInput);
		/*
		 * CompareUI.openCompareEditor(new EditorInput( new
		 * CompareConfiguration(),oldinput,newinput));
		 * CompareUI.openCompareEditor(new EditorInput( new
		 * CompareConfiguration(),inminput,newinput));
		 */
	}

	private void openInCompare(ITypedElement oldInput, ITypedElement newInput,
			ITypedElement inmInput) {

		CompareEditorInput input = createCompareEditorInput(inmInput, newInput);
		CompareUI.openCompareEditor(input, OpenStrategy.activateOnOpen());
		DocumentMerger diff = ((TextMergeViewer) input.getfContentInputPane()
				.getViewer()).getfMerger();
		IWorkbenchPage workBenchPage = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor = RefactorUtils.findReusableCompareEditor(input,
				workBenchPage, new Class[] { ChangeCompareEditorInput.class });
		
		 if (editor != null) { workBenchPage.closeEditor(editor, false); }
		 

		CompareEditorInput inputOrig = createCompareEditorInput(oldInput,
				newInput);
		IWorkbenchPage workBenchPage1 = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorPart editor1 = RefactorUtils.findReusableCompareEditor(
				inputOrig, workBenchPage1,
				new Class[] { ChangeCompareEditorInput.class });
		if (editor1 != null) {
			IEditorInput otherInput = editor1.getEditorInput();
			if (otherInput.equals(inputOrig)) {
				// simply provide focus to editor
				if (OpenStrategy.activateOnOpen())
					workBenchPage1.activate(editor1);
				else
					workBenchPage1.bringToTop(editor1);
			} else {
				// if editor is currently not open on that input either re-use
				// existing
				CompareUI.reuseCompareEditor(inputOrig,
						(IReusableEditor) editor1);
				if (OpenStrategy.activateOnOpen())
					workBenchPage1.activate(editor1);
				else
					workBenchPage1.bringToTop(editor1);
			}
		} else {
			inputOrig.getCompareConfiguration().setInmMergerResult(diff);
			CompareUI.openCompareEditor(inputOrig,
					OpenStrategy.activateOnOpen());
			// ((DiffNode)input.getCompareResult()).setLeft(inmInput);
			/*((TextMergeViewer) inputOrig.getfContentInputPane().getViewer())
					.setfMergerExcpt(diff);*/
		}
	}

	protected ChangeCompareEditorInput createCompareEditorInput(
			ITypedElement left, ITypedElement right) {
		return new ChangeCompareEditorInput(left, right);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(viewer.getControl().getShell(),
				"Sample View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	public void initializeImageRegistry() {
		Bundle bundle = Platform.getBundle("coderefactoring.views");
		ImageDescriptor newSourceImage = ImageDescriptor
				.createFromURL(FileLocator.find(bundle, new Path(
						"icons/java.png"), null));
		imageRegistry = new ImageRegistry();
		imageRegistry.put(SOURCE, newSourceImage);

		ImageDescriptor newPkgImage = ImageDescriptor.createFromURL(FileLocator
				.find(bundle, new Path("icons/pkg.png"), null));
		imageRegistry = new ImageRegistry();
		imageRegistry.put(PACKAGE, newPkgImage);

		ImageDescriptor newProjImage = ImageDescriptor
				.createFromURL(FileLocator.find(bundle, new Path(
						"icons/proj.png"), null));
		imageRegistry = new ImageRegistry();
		imageRegistry.put(PROJECT, newProjImage);
	}

	public Image getImages(String key) {
		return imageRegistry.get(key);
	}
}