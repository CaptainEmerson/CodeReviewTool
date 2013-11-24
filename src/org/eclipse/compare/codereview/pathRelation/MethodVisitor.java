package org.eclipse.compare.codereview.pathRelation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class MethodVisitor extends ASTVisitor {
	List<MethodDeclaration> methods = new ArrayList<MethodDeclaration>();
	List<VariableDeclarationFragment> vars = new ArrayList<VariableDeclarationFragment>();
	final HashMap<MethodDeclaration, ArrayList<MethodInvocation>> invocationsForMethods = new HashMap<MethodDeclaration, ArrayList<MethodInvocation>>();

	private MethodDeclaration activeMethod;

	@Override
	public boolean visit(MethodDeclaration node) {
		activeMethod = node;
		methods.add(node);
		return super.visit(node);
	}
	
	@Override
	public boolean visit(VariableDeclarationFragment node) {
		vars.add(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (invocationsForMethods.get(activeMethod) == null) {
			invocationsForMethods.put(activeMethod,
					new ArrayList<MethodInvocation>());
		}
		invocationsForMethods.get(activeMethod).add(node);
		return super.visit(node);
	}

	public List<MethodDeclaration> getMethods() {
		return methods;
	}
	
	public List<VariableDeclarationFragment> getVars() {
		return vars;
	}
	
	public HashMap<MethodDeclaration, ArrayList<MethodInvocation>> getInvocations() {
		return invocationsForMethods;
	}
}