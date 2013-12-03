package org.eclipse.compare.codereview;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareUI;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.lang.Object;

import org.eclipse.compare.codereview.pathRelation.ChangeRefactorInterface;
import org.eclipse.compare.codereview.pathRelation.RenameChange;
import org.eclipse.compare.codereview.refactorChange.ApplyRefactoringScriptWizard;
import org.eclipse.compare.codereview.refactorChange.NewJavaProject;
import org.eclipse.compare.codereview.refactorChange.RefactorUtils;
import org.eclipse.compare.codereview.refactorChange.RefactoringHistoryWizard;
import org.eclipse.compare.codereview.reviewinterface.RefactoringInfoHandler;
import org.eclipse.compare.codereview.views.CodeCompareView;
import org.eclipse.compare.codereview.compareEditor.ChangeResourceCompareEditorInput;
import org.eclipse.compare.codereview.compareEditor.EditorInput;
import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.compare.structuremergeviewer.ICompareInputChangeListener;


public class ReviewAction extends BaseReviewAction implements IObjectActionDelegate {

	/*protected ResourceCompareInput fInput;*/
	protected IWorkbenchPage fWorkbenchPage;
	protected boolean showSelectAncestorDialog = true;
	
	private static String newProjectName = null;
	private static String newInmProjectPath = null;
	
	ArrayList<File> logFiles = null;
	
	Cursor busyCursor = null;
	
	RefactoringInfoHandler fnlRefactorInfo = null;

	public void run(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
					.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = (IProject) ((IAdaptable) element)
							.getAdapter(IProject.class);
				}

				final IProject finalPrj = project;
				
				FileDialog dialog = new FileDialog(new Shell(), SWT.OPEN);
				dialog.setText("Select the base version of the project..");
				dialog.setFilterExtensions(new String[] { "*.project" });
				dialog.setFilterPath(finalPrj.getLocation().toString());
				String result = dialog.open();
				
				if(result == null) {
					return;
				}
				
				File baseProjFilePath = new File(result);
				final String baseProjDir = baseProjFilePath.getParent();
				
				IPath lPath= Path.fromOSString(baseProjFilePath.getAbsolutePath()); 
				IFile lFile = ResourcesPlugin.getWorkspace().getRoot().getFile(lPath);
				TextFileDocumentProvider lProvider = new TextFileDocumentProvider();
				IDocument lDoc= lProvider.getDocument(lFile);
				
				/*IProjectDescription description;
				try {
					description = ResourcesPlugin
							   .getWorkspace().loadProjectDescription(
							        new Path(result));
					IProject newProject = ResourcesPlugin.getWorkspace()
							   .getRoot().getProject(description.getName());
							newProject.create(description, null);
							newProject.open(null);
				} catch (CoreException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/

				
				if (finalPrj.isOpen()) {

					try {

						Job job = new Job("Code Refactoring Replay") {
							@Override
							protected IStatus run(final IProgressMonitor monitor) {
								
								busyCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_WAIT);
								
								// set total number of work units
								monitor.beginTask("Replaying Refactoring", 100);

								monitor.subTask("Creating new project");

								
								/* * Creating new project as the intermediate
								 * version of the the base project*/
								/*
								final IProject oldProject = NewJavaProject
										.createNewProject(finalPrj.getName(), baseProjDir, "_Temp");*/
								 
								final IProject newProject = NewJavaProject
										.createNewProject(finalPrj.getName(), baseProjDir, "_AfterRefac");
								/*try {
									newProject.setHidden(true);
									newProject.close(monitor);
								} catch (CoreException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}*/
								
								newProjectName = newProject.getName();
								newInmProjectPath = newProject.getLocation().toString();
								monitor.worked(20);

								IPath projPath = finalPrj.getLocation();
								String refactoringLogPath = projPath.toString()
										.concat("/.refactorings");
								File dir = new File(refactoringLogPath);

								logFiles = new ArrayList<File>();
								if (dir.isDirectory()) {
									findFileInDirRecursively(dir,
											"refactorings.history");
								}

								String refactoringLogFilePath = projPath
										.toString()
										+ "/"
										+ newProjectName
										+ "Log.xml";
								final File file = new File(
										refactoringLogFilePath);
								if(file.exists()) file.delete();

								monitor.subTask("Creating refactoring script");
								ReadAndPrintXMLFile(file, logFiles);
								monitor.worked(20);

								monitor.subTask("Applying Script");
								
								/*ApplyRefactoringScriptWizard refac = new ApplyRefactoringScriptWizard(
										new File(
												"C:\\Users\\Admin\\Desktop\\new1.xml"));*/
								
								ApplyRefactoringScriptWizard refac = new ApplyRefactoringScriptWizard(file);								 	
								refac.performFinish();
								
								ArrayList<String[]> attrList = ReadXMLFileForAttrVal(file);
								final ArrayList<RenameChange> renameChangeRelationList = ChangeRefactorInterface.processChangeTypeRelation(attrList, finalPrj);
								String relationFilePath = projPath
										.toString()
										+ "/"
										+ newProjectName
										+ "ChangeRelation.dat";
								WriteRenameChangeRelationFile(relationFilePath, renameChangeRelationList);
								
								monitor.worked(50);
								
								/*IManualRefactoringInterface manInt = ServiceLocator
										.ResolveType(IManualRefactoringInterface.class);
								manInt.startRefactoringDetection(newProject,
										finalPrj, new IManualRefactoringCallback() {
											
											public void callBack(Collection<IManualRefactoringInfo> results) {
												System.out
														.println("Results");
												// use results to hide manual refactoring
												for (IManualRefactoringInfo res : results) {
													System.out.println(res);
												}
												//assignValue(results);
											}
										});*/
															
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
								if (monitor.isCanceled()) {
									return Status.CANCEL_STATUS;
								}
								
								// Use this to open a Shell in the UI thread
								Display.getDefault().asyncExec(new Runnable() {
									private CodeCompareView view;

									public void run() {
										if(file.exists()) file.delete();
										
										
										busyCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_NO);
										IWorkspace workspace = ResourcesPlugin.getWorkspace();
										IWorkspaceRoot root = workspace.getRoot();
										try {
											newProject.delete(false, false, monitor);
											//oldProject.delete(false, false, monitor);
											root.refreshLocal(IResource.DEPTH_INFINITE, monitor);
										} catch (CoreException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
										monitor.done();
										
										/*view = new CodeCompareView(finalPrj, baseProjDir, newInmProjectPath, renameChangeRelationList);
										
										IWorkbenchPage activePage = PlatformUI.getWorkbench()
												.getActiveWorkbenchWindow().getActivePage();
										if (activePage != null) {
											try {
												activePage.showView(CodeCompareView.ID, null,
														IWorkbenchPage.VIEW_VISIBLE);
											} catch (PartInitException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}*/
										
										CompareUI.openCompareEditor(new ChangeResourceCompareEditorInput(
												finalPrj, baseProjDir, newInmProjectPath, renameChangeRelationList));
										
										/*IWorkbenchPage activePage = PlatformUI.getWorkbench()
												.getActiveWorkbenchWindow()
												.getActivePage();
										if (activePage
												.findView(CodeCompareView.ID) == null) {
											try {
												// open the view
												activePage
														.showView(CodeCompareView.ID);
												// and maximize it
												activePage.toggleZoom(activePage
														.findViewReference(CodeCompareView.ID));
											} catch (PartInitException es) {
												es.printStackTrace();
											}
										}*/
										
										/*File input1 = new File("D:/Study Time/Courses/SE/Project/runtime-EclipseApplication/Test_Base" + "/src");
										File input3 = new File(newInmProjectPath + "/src/newPkg/SourceNew1.java");
										File input2 = new File(finalPrj.getLocation().toString() + "/src");
										CompareUI.openCompareEditor(new EditorInput(
												new CompareConfiguration(),input1,input2));
										CompareUI.openCompareEditor(new EditorInput(
												new CompareConfiguration(),input3,input2));*/
									}
								});
								
								return Status.OK_STATUS;
							}
							
						};
						job.schedule();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}

		/*IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		// Get all projects in the workspace
		IProject[] projects = root.getProjects();
		// Loop over all projects
		for (IProject project : projects) {
			try {
				if (project.isOpen() && project.isNatureEnabled(JDT_NATURE)) {
					analyseMethods(project);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}*/
	}
	
	/*private void assignValue(
			Collection<IManualRefactoringInfo> results) {
		RefactoringInfoHandler refactorInfo = new RefactoringInfoHandler(results);
		fnlRefactorInfo = refactorInfo;
		
	}*/

	protected boolean isEnabled(ISelection selection) {
		return true;
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		fWorkbenchPage= targetPart.getSite().getPage();
	}
	
	private void WriteRenameChangeRelationFile(
			String relationFilePath,
			ArrayList<RenameChange> renameChangeRelationList) {
		File file = new File(relationFilePath);
		try {
			// if file doesnt exists, then create it
			if (!file.exists()) {

				file.createNewFile();

			}
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (RenameChange instance : renameChangeRelationList) {
				StringBuffer content = new StringBuffer("");
				content.append(instance.getRenameType() + "::"
						+ instance.getOldPackageName() + "::"
						+ instance.getNewPackageName() + "::"
						+ instance.getFileName() + "::" + instance.getOldText()
						+ "::" + instance.getNewText() + "\n");

				bw.write(content.toString());

			}

			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void findFileInDirRecursively(File dir, String string) {
		File[] files = dir.listFiles();
		for(File file : files){
			if(file.isDirectory()){
				findFileInDirRecursively(file, string);
			}
			else if(file.getName().equals(string)){
				logFiles.add(file);
			}
		}
	}

	public static ArrayList<String[]> ReadXMLFileForAttrVal(File file) {
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(file);

			// normalize text representation
			doc.getDocumentElement().normalize();
			System.out.println("Root element of the doc is "
					+ doc.getDocumentElement().getNodeName());

			// Get the root element
			Node root = doc.getFirstChild();

			NodeList listOfRefactorings = doc
					.getElementsByTagName("refactoring");

			ArrayList<String[]> attrList = new ArrayList<String[]>();

			for (int s = 0; s < listOfRefactorings.getLength(); s++) {

				Node node = listOfRefactorings.item(s);
				String[] attrs = new String[4];
				if (node.hasAttributes()) {
					Attr attr1 = (Attr) node.getAttributes().getNamedItem("id");
					if (attr1 != null) {
						String attribute = attr1.getValue();
						attrs[0] = attribute;
					}
					Attr attr2 = (Attr) node.getAttributes().getNamedItem(
							"comment");
					if (attr2 != null) {
						String attribute = attr2.getValue();
						attrs[1] = attribute;
					}
					Attr attr3 = (Attr) node.getAttributes().getNamedItem(
							"name");
					if (attr3 != null) {
						String attribute = attr3.getValue();
						attrs[2] = attribute;
					}
					Attr attr4 = (Attr) node.getAttributes().getNamedItem(
							"input");
					if (attr4 != null) {
						String attribute = attr4.getValue();
						attrs[3] = attribute;
					}
				}
				attrList.add(attrs);
			}//
			return attrList;

		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}
	
	public static void ReadAndPrintXMLFile(File newFile, ArrayList<File> logFiles) {
		try {

			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document doc = docBuilder.parse(logFiles.get(0));

			// normalize text representation
			doc.getDocumentElement().normalize();
			System.out.println("Root element of the doc is "
					+ doc.getDocumentElement().getNodeName());
			
			// Get the root element
			Node root = doc.getFirstChild();

			NodeList listOfRefactorings = doc
					.getElementsByTagName("refactoring");
			
			for (int i = 1; i < logFiles.size(); i++) {
				Document merge = docBuilder.parse(logFiles.get(i));
				NodeList listOfRefactoringsMerge = merge
						.getElementsByTagName("refactoring");
				for (int s = 0; s < listOfRefactoringsMerge.getLength(); s++) {

					Node n= (Node) doc.importNode(listOfRefactoringsMerge.item(s), true);  
					listOfRefactorings.item(s).getParentNode().appendChild(n);
				}//							
			}
			
			/*for (int s = 0; s < listOfRefactorings.getLength(); s++) {

				Node refactorNode = listOfRefactorings.item(s);
				if (refactorNode.getNodeType() == Node.ELEMENT_NODE) {

					Element changeElement = (Element) refactorNode;
					if(changeElement.hasAttribute("stamp")){
						changeElement.removeAttribute("stamp");						
					}
					changeElement.setAttribute("project", selectedProject);
					doc.getDocumentElement().appendChild(changeElement);
				}// end of if clause
			}//			
*/			
			
			filePutContents(doc, newFile);
			
			File tempFile = includeProjectNameInXML(newFile);
			
			if(newFile.exists()) newFile.delete();
			tempFile.renameTo(newFile);
			if(tempFile.exists()) tempFile.delete();

		} catch (SAXParseException err) {
			System.out.println("** Parsing error" + ", line "
					+ err.getLineNumber() + ", uri " + err.getSystemId());
			System.out.println(" " + err.getMessage());

		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();

		} catch (Throwable t) {
			t.printStackTrace();
		}

	}
	
	private static File includeProjectNameInXML(File newFile) {
		try {

			String tempFilePath = newFile.getParent() + "\\" + "temp.xml";
			File tempFile = new File(tempFilePath);
			if (tempFile.exists())
				tempFile.delete();

			FileInputStream fstream = new FileInputStream(newFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					fstream));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				String toWrite = strLine.replaceAll("stamp=\"\\d+\"",
						"project=\"" + newProjectName + "\"");

				FileWriter fwriter = new FileWriter(tempFilePath, true);
				PrintWriter outputFile = new PrintWriter(fwriter);

				outputFile.println(toWrite);
				outputFile.close();

			}

			br.close();
			fstream.close();

			return tempFile;

		}

		catch (Exception e) {
			return null;
		}

	}

	protected static void filePutContents(Document doc,File file){
	    try{                    
	        TransformerFactory tFactory = TransformerFactory.newInstance();
	        Transformer transformer = tFactory.newTransformer();    
	        transformer.setOutputProperty(OutputKeys.INDENT,"yes");

	        DOMSource source = new DOMSource(doc);
	        //StreamResult result = new StreamResult(System.out);//problem was this
	        StreamResult result = new StreamResult(file);//correct way
	                transformer.transform(source, result);

	    }catch(TransformerConfigurationException tce){
	        /*ERRO do Transformer   */      
	        System.out.println("* Transformer Factory error");
	        System.out.println(" " + tce.getMessage());

	        Throwable x = tce;
	        if (tce.getException() != null)
	            x = tce.getException();
	            x.printStackTrace(); 
	    }catch(TransformerException te){
	        /*ERRO da Factory*/
	        System.out.println("* Transformation error");
	        System.out.println(" " + te.getMessage());

	        Throwable x = te;
	        if (te.getException() != null)
	            x = te.getException();
	            x.printStackTrace();
	    }
	}
}
