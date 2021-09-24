package edu.ucla.cs.jshrinklib.util;

import java.io.*;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import edu.ucla.cs.jshrinklib.reachability.ASMClassVisitor;
import edu.ucla.cs.jshrinklib.reachability.FieldData;
import edu.ucla.cs.jshrinklib.reachability.MethodData;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class ASMUtils {

	public static void readClassFromJarFile(JarFile jarFile, Set<String> classes,
    		Set<MethodData> methods, Set<FieldData> fields, Map<MethodData, Set<FieldData>> fieldReferences, Map<MethodData, Set<MethodData>> virtualMethodCalls) {
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
        	final JarEntry entry = entries.nextElement();
        	// there is a module-info.class in jars built by Java 9
            if (entry.getName().endsWith(".class") && !entry.getName().equals("module-info")) {
            	try {
                	ClassReader cr = new ClassReader(jarFile.getInputStream(entry));
                	cr.accept(new ASMClassVisitor(Opcodes.ASM5, classes, methods, fields, fieldReferences, virtualMethodCalls), ClassReader.SKIP_DEBUG);
                } catch (IllegalArgumentException ex) {
                	continue;
                } catch (IOException ex){
	                //Not sure if here is the best way to handle it, but ok for the meantime
                    System.err.println("An an exception was thrown when reading data from .jar file:");
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        }
    }
    
    public static void readClassFromDirectory(File dirPath, Set<String> classes,
    		Set<MethodData> methods, Set<FieldData> fields, Map<MethodData, Set<FieldData>> fieldReferences, Map<MethodData, Set<MethodData>> virtualMethodCalls) {
    	
    	if(!dirPath.exists()) {
    		// fix NPE due to non-existent file
    		System.err.println(dirPath.getAbsolutePath() + " does not exist.");
    		return;
    	}
    	
    	for(File f : dirPath.listFiles()) {
    		if(f.isDirectory()) {
    			readClassFromDirectory(f, classes, methods, fields, fieldReferences, virtualMethodCalls);
    		} else {
    			String fName = f.getName();
    			if(fName.endsWith(".class")) {
    				try {
    					FileInputStream fis = new FileInputStream(f);
						ClassReader cr = new ClassReader(fis);
						cr.accept(new ASMClassVisitor(Opcodes.ASM5, classes, methods, fields, fieldReferences, virtualMethodCalls), ClassReader.SKIP_DEBUG);
						fis.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
    			}
    		}
    	}
    }

    public static void readClass(File dir, Set<String> classes, Set<MethodData> methods, Set<FieldData> fields,
								 Map<MethodData, Set<FieldData>> fieldReferences, Map<MethodData, Set<MethodData>> virtualMethodCalls){
    	try {
		    if (dir.isDirectory()) {
			    readClassFromDirectory(dir, classes, methods, fields, fieldReferences, virtualMethodCalls);
		    } else if (dir.getName().endsWith(".jar")) {
			    JarFile j = null;
			    try {
				    j = new JarFile(dir);
				    readClassFromJarFile(j, classes, methods, fields, fieldReferences, virtualMethodCalls);
			    } catch (IOException e) {
				    e.printStackTrace();
			    }
		    } else {
			    throw new IOException("Cannot read classes from '" + dir.getAbsolutePath() +
				    "'. It is neither a directory or a jar.");
		    }
	    }catch(IOException e){
    		e.printStackTrace();
    		System.exit(1);
	    }
    }

    public static String changeType(String descriptor, String changeFrom, String changeTo) {
		String newDescriptor = changeTypeInternal(descriptor, changeFrom, changeTo);
		if (!descriptor.equals(newDescriptor)) {
			System.out.printf("changeType: original: %s && new: %s\n", descriptor, newDescriptor);
		}
		return changeTypeInternal(descriptor, changeFrom, changeTo);
	}

	private static String changeTypeInternal(String descriptor, String changeFrom, String changeTo) {
		Type type = Type.getType(descriptor);
//		Type newType = Type.getType(descriptor);
		String newDescriptor = descriptor;
		if (type.getSort() == Type.OBJECT) {
			if (type.getInternalName().equals(changeFrom)) {
				newDescriptor = "L" + changeTo + ";";
			}
		} else if (type.getSort() == Type.ARRAY) {
			newDescriptor =  "[" + changeTypeInternal(descriptor.substring(1), changeFrom, changeTo);
		}
		return newDescriptor;
	}
	public static String formatClassName(String className) {
		return className.replace('.','/');
	}

	public static void writeClass(String filePath, ClassWriter writer) {
		File originalFile = new File(filePath);
		try {
			FileOutputStream outputStream = new FileOutputStream(originalFile);
			outputStream.write(writer.toByteArray());
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
