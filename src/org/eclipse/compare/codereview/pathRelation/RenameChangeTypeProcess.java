package org.eclipse.compare.codereview.pathRelation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import org.eclipse.compare.codereview.pathRelation.ChangeRefactorInterface;
import org.eclipse.compare.codereview.pathRelation.MethodVisitor;

public class RenameChangeTypeProcess extends ChangeRefactorInterface{
	
	private RenameChange renameObject;
	
	/*Rename Types*/
	private final String FIELD = "field";
	private final String LOCAL_VAR = "local.variable";
	private final String METHOD = "method";
	private final String TYPE = "type";
	private final String PACKAGE = "package";
	/**/

	public RenameChangeTypeProcess(String[] refactoringChange) {		
		createChangeObject(refactoringChange);
	}

	@Override
	public void process(IProject project) {
		if(renameObject.getRenameType().equals(LOCAL_VAR)){
			CompilationUnit source = parse();
			MethodVisitor visitor = new MethodVisitor();
			source.accept(visitor);
			
			List<VariableDeclarationFragment> vars = visitor.getVars();
			for(VariableDeclarationFragment var: vars){
				SimpleName name = var.getName();
				if(name.getIdentifier().equals(renameObject.getOldText())){
					name.setIdentifier(renameObject.getNewText());
				}
			}
			
			// get the ast rewriter
			final ASTRewrite rewriter = ASTRewrite.create(source.getAST());
			String newSource = source.toString();
			// now write this source to some other file.
			
			Document document = new Document(source.toString());
			try {
				rewriter.rewriteAST().apply(document);
			} catch (MalformedTreeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (JavaModelException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (BadLocationException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			try{
		          // Create file 
		          FileWriter fstream = new FileWriter("D:\\Study Time\\Courses\\SE\\Project\\runtime-EclipseApplication\\Source.java");
		          BufferedWriter out = new BufferedWriter(fstream);
		          out.write(newSource);
		          //Close the output stream
		          out.close();
		    }catch (Exception e){//Catch exception if any
		          System.err.println("Error: " + e.getMessage());
		    }
			
			System.out.println();
		}
	}
	
	public RenameChange tempProcess () {
		return renameObject;
	}

	@Override
	public void createChangeObject(String[] refactoringChange) {
		
		String[] idSubs = refactoringChange[0].split("[.]");
		
		String[] commentSubs = refactoringChange[1].split("[-]");
		
		String inputSubs = refactoringChange[3];
		
		String renameType = "";		
		String projectName = commentSubs[1].split("[:]")[1].replace("'", "").trim();		
		String packageName = "";		
		String className = "";			
		String fileName = "";	
		String methodName = "";			
		String oldText = "";			
		String newText = refactoringChange[2];
		
		boolean flagP = false;
		boolean flagS = false;
		char[] storP = new char[inputSubs.length()];
		char[] storS = new char[inputSubs.length()];
		int j = 0, k=0;
		for(char inputPkg : inputSubs.toCharArray()){
			if(inputPkg == '<'){
				flagP = true;
				continue;
			}
			if(inputPkg == '{'){
				flagP = false;
				flagS = true;
				continue;
			}
			if(inputPkg == '['){
				break;
			}
			if(flagP){
				storP[j++]=inputPkg;
			}
			if(flagS){
				storS[k++]=inputPkg;
			}
		}
		
		packageName = String.valueOf(storP).trim();
		
		/*packageName = new String[tempPkg.length];
		for(int i=0; i<packageName.length; i++){
			packageName[i] = tempPkg[i];
		}*/
		
		fileName = String.valueOf(storS).trim();
		
		/*if(idSubs[5].equals("field")){
			renameType = FIELD;
			methodName="";
			
			String pathValues[] = commentSubs[2].split("[:]")[1].replace("'", "").trim().split("[.]");		
			oldText = pathValues[pathValues.length - 1];

			String pathValues1[] = commentSubs[3].split("[:]")[1].replace("'", "").trim().split("[.]");
			//newText = pathValues1[pathValues1.length - 1];
			
			className = pathValues[pathValues.length - 2];
			
			packageName = new String[pathValues.length-2];
			for(int i=0; i<packageName.length; i++){
				packageName[i] = pathValues[i];
			}
			
		}
		else if(idSubs[5].equals("local")){
			renameType = LOCAL_VAR;
			
			String pathValues[] = commentSubs[2].split("[:]")[1].replace("'", "").trim().split("[.]");		
			oldText = pathValues[pathValues.length - 1];

			String pathValues1[] = commentSubs[3].split("[:]")[1].replace("'", "").trim().split("[.]");
			//newText = pathValues1[pathValues1.length - 1];
			
			methodName = pathValues[pathValues.length - 2];
			
			className = pathValues[pathValues.length - 3];
			
			packageName = new String[pathValues.length-3];
			for(int i=0; i<packageName.length; i++){
				packageName[i] = pathValues[i];
			}
		}
		else if(idSubs[5].equals("method")){
			renameType = METHOD;
			
			String pathValues[] = commentSubs[2].split("[:]")[1].replace("'", "").trim().split("[.]");		
			oldText = pathValues[pathValues.length - 1];
			methodName = oldText;

			String pathValues1[] = commentSubs[3].split("[:]")[1].replace("'", "").trim().split("[.]");
			//newText = pathValues1[pathValues1.length - 1];
			
			className = pathValues[pathValues.length - 2];
			
			packageName = new String[pathValues.length-2];
			for(int i=0; i<packageName.length; i++){
				packageName[i] = pathValues[i];
			}
		}
		else*/ if(idSubs[5].equals("package")){
			renameType = PACKAGE;
			String pathValuesTemp[] = commentSubs[2].split("[:]")[1].replace("'", "").trim().split("[//]");
			//String pathValues[] = pathValuesTemp[pathValuesTemp.length - 1].split("[.]");
			oldText = pathValuesTemp[pathValuesTemp.length - 1];
			
			/*String pathValuesTemp1[] = commentSubs[3].split("[:]")[1].replace("'", "").trim().split("[//]");
			String pathValues1[] = pathValuesTemp1[pathValuesTemp1.length - 1].split("[.]");*/
			//newText = pathValues1[pathValues1.length - 1];
			
			RenameChange alreadyExistingPkg = RenameChange.findAlreadyExistingNewPkgName(oldText);
			if(alreadyExistingPkg != null){
				alreadyExistingPkg.setNewText(newText);
				alreadyExistingPkg.setNewPackageName(newText);
			}
			else {			
				RenameChange renameObject = new RenameChange(renameType, projectName, packageName, newText, fileName, oldText, newText);		
			
				this.renameObject = renameObject; 
			}
			
			RenameChange alreadyExistingSource = RenameChange.findAlreadyExistingSourceWithNewPkgName(oldText);
			if(alreadyExistingSource != null){
				alreadyExistingPkg.setNewPackageName(newText);
			}
			
		}
		else if(idSubs[5].equals("type")){
			renameType = TYPE;
			String pathValues[] = commentSubs[2].split("[:]")[1].replace("'", "").trim().split("[.]");
			oldText = pathValues[pathValues.length - 1];
			
			if(!fileName.replace(".java", "").equals(oldText)) return;

			/*String pathValues1[] = commentSubs[3].split("[:]")[1].replace("'", "").trim().split("[.]");*/
			//newText = pathValues1[pathValues1.length - 1];
			
			String newPackageName = packageName;
			
			RenameChange alreadyExistingPkg = RenameChange.findAlreadyExistingNewPkgName(packageName);
			if(alreadyExistingPkg != null){
				newPackageName = packageName;
				packageName = alreadyExistingPkg.getOldPackageName();				
			}
			
			RenameChange alreadyExistingSource = RenameChange.findAlreadyExistingNewSourceName(oldText);
			if(alreadyExistingSource != null){
				alreadyExistingSource.setNewText(newText);
				alreadyExistingSource.setOldPackageName(packageName);
				alreadyExistingSource.setNewPackageName(newPackageName);
			}
			else {			
				RenameChange renameObject = new RenameChange(renameType, projectName, packageName, newPackageName, fileName, oldText, newText);		
				
				this.renameObject = renameObject;
			}
			
			
			
		}
		
	}
	
	/**
	 * * Reads a ICompilationUnit and creates the AST DOM for manipulating the *
	 * Java source file * * @param unit * @return
	 */

	private static CompilationUnit parse() {
		/*IPath path = unit.getPath();
		String source = path.toString();
		String sourceRelativePath = source
				.substring(0, source.lastIndexOf("/"));
		String sourceName = source.substring(source.lastIndexOf("/") + 1,
				source.length());*/
		String versionsRelativePath = /*sourceRelativePath.concat("/versions/")
				.concat(sourceName);
		versionsRelativePath = */ "D:\\Study Time\\Courses\\SE\\Project\\runtime-EclipseApplication\\Source.java";

		char[] doc = getFileContents(new File(versionsRelativePath));
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(doc);
		parser.setResolveBindings(true);
		// parser.setProject(javaProject);
		return (CompilationUnit) parser.createAST(null); // parse
	}

	public static char[] getFileContents(File file) {
		// char array to store the file contents in
		char[] contents = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
				// append the content and the lost new line.
				sb.append(line + "\n");
			}
			contents = new char[sb.length()];
			sb.getChars(0, sb.length() - 1, contents, 0);

			assert (contents.length > 0);
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}

		return contents;
	}	

}
