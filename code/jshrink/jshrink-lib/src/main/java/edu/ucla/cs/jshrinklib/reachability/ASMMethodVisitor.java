package edu.ucla.cs.jshrinklib.reachability;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.*;
import java.util.regex.Pattern;

class ASMMethodVisitor extends MethodVisitor {
	private final MethodData currentMethod;
	private Set<FieldData> fieldReferences;
	private Map<MethodData, Set<MethodData>> virtualCallMap;
	private Set<MethodData> virtualCalls;

	public ASMMethodVisitor(int api, MethodData method, Set<FieldData> fieldReferences, Map<MethodData, Set<MethodData>> virtualCallMap) {
		super(api);
		this.currentMethod = method;
		this.fieldReferences = fieldReferences;
		this.virtualCallMap = virtualCallMap;
		this.virtualCalls = new HashSet<MethodData>();
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		//System.out.println("visitAnnotation: desc="+desc+" visible="+visible);
		this.currentMethod.setAnnotation(Type.getType(desc).getClassName());
		return null;//super.visitAnnotation(desc, visible);
	}

	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		if(fieldReferences == null) {
			// no need to collect field references
			return;
		}

		boolean isStatic;
		if(opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {
			isStatic = true;
		} else {
			isStatic = false;
		}

		String className = owner.replaceAll(Pattern.quote("/"), ".");

		// note that for parameterized types we can only get the generic type from the type description
		// due to type erasure in Java
		String type = Type.getType(desc).getClassName();
		FieldData field = new FieldData(name, className, isStatic, type);
		fieldReferences.add(field);
	}

	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		if(opcode == Opcodes.INVOKEVIRTUAL) {
			String className = owner.replaceAll(Pattern.quote("/"), ".");
			String returnType = Type.getReturnType(desc).getClassName();
			Type[] ts = Type.getArgumentTypes(desc);
			List<String> argsList = new ArrayList<String>();
			for(Type t : ts) {
				argsList.add(t.getClassName());
			}

			// Java bytecode does not have the visibility of a method in its call site
			// so I am simply setting it to be public and non-static by default
			MethodData methodData = new MethodData(name, className, returnType,
					argsList.toArray(new String[argsList.size()]), true, false);
			virtualCalls.add(methodData);
		}
	}

	@Override
	public void visitEnd() {
		this.virtualCallMap.put(currentMethod, virtualCalls);
	}
}