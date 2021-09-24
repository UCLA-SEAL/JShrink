package edu.ucla.cs.jshrinklib.util;

import edu.ucla.cs.jshrinklib.reachability.ASMClassVisitor;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.apache.commons.io.FileUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.JasminClass;
import soot.util.JasminOutputStream;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassFileUtils {

	public static final String ORIGINAL_FILE_POST_FIX="_original"; //package private as used by tests

	public static long getSize(File file) throws IOException{

		if(!file.exists()){
			throw new IOException("File '" + file.getAbsolutePath() + " does not exist");
		}

		long length=0;
		if(file.isDirectory()){
			for(File innerFile : file.listFiles()){
				length += getSize(innerFile);
			}
		} else {
			length += file.length();
		}

		return length;
	}

	//SPECIAL NOTE!!!! : This assumes all jars in the classpaths are decompressed!
	public static List<File> getClassFile(SootClass sootClass, Collection<File> paths) {
		String classPath = sootClass.getName().replaceAll("\\.", File.separator) + ".class";

		ArrayList<File> files = new ArrayList<>();
		for (File p : paths) {
			File test = new File(p + File.separator + classPath);
			if (test.exists()) {
				files.add(test);
			}
		}

		return files;
	}

	public static List<File> classInPath(String qualifiedClassName, Collection<File> paths){
		String classFile = qualifiedClassName.replaceAll("\\.", File.separator) + ".class";

		List<File> toReturn = new ArrayList<File>();
		for(File path : paths){
			if(path.isDirectory()){
				if((new File(path.getAbsolutePath()
					+ File.separator + classFile).exists())){
					toReturn.add(path);
				}
			} else if(path.getAbsolutePath().endsWith(classFile)){
				toReturn.add(path);
			} else { //... check to see if it's a jar file
				JarFile jarFile = null;
				try{
					jarFile = new JarFile(path);
				}catch(IOException e){
					continue;
				}
				assert(jarFile != null);

				final Enumeration<JarEntry> entries = jarFile.entries();
				while (entries.hasMoreElements()) {
					final JarEntry entry = entries.nextElement();
					String entryName = entry.getName();
					if(entryName.equals(classFile)){
						toReturn.add(path);
					}
				}
			}
		}

		return toReturn;
	}

	public static void decompressJar(File jarFile) throws IOException{

		ZipFile jarToReturn = null;
		try {
			jarToReturn = new ZipFile(jarFile);
		} catch (ZipException e) {
			throw new IOException("File '" + jarFile.getAbsolutePath() + "' is not a zipped file. " +
					"Are you sure it's a valid Jar?");
		}

		try {
			//Extract the jar file into a temporary directory
			File tempDir = File.createTempFile("jar_expansion", "tmp");
			tempDir.delete();
			if(!tempDir.mkdir()){
				throw new IOException("Could not 'mkdir " + tempDir.getAbsolutePath() + "'");
			}

			try {
				jarToReturn.extractAll(tempDir.getAbsolutePath());
			} catch(ZipException e){
				throw new IOException("Failed to extract .jar file. Following exception thrown:" +
						System.lineSeparator() + e.getLocalizedMessage());
			}

			jarToReturn.getFile().delete();
			FileUtils.moveDirectory(tempDir, jarToReturn.getFile());

		} catch(IOException e){
			throw new IOException("Failed to create a temporary directory. The following exception was thrown:"
					+ System.lineSeparator() + e.getLocalizedMessage());
		}
	}

	public static void compressJar(File file) throws IOException{
		try {
			ZipFile zipFile = new ZipFile(File.createTempFile("tmpJarFile", ".jar_tmp"));
			zipFile.getFile().delete();
			ZipParameters zipParameters = new ZipParameters();
			zipParameters.setCompressionLevel(9);

			//It's in a busy state otherwise... hope this is ok
			zipFile.getProgressMonitor().setState(ProgressMonitor.STATE_READY);

			boolean created=false;
			for(File f : file.listFiles()){
				if(f.isDirectory()){
					if(!created){
						zipFile.createZipFileFromFolder(f,zipParameters,false, 0);
						created=true;
					} else {
						zipFile.addFolder(f, zipParameters);
					}
				} else{
					if(!created){
						zipFile.createZipFile(f, zipParameters);
						created=true;
					} else {
						zipFile.addFile(f, zipParameters);
					}
				}
			}

			// Regular file.delete(), does not always work. I have to force it (I don't know why)
			FileUtils.forceDelete(file);
			FileUtils.moveFile(zipFile.getFile(), file);


		} catch(ZipException|IOException e){
			throw new IOException("Unable to create zip (Jar) file '" + file.getAbsolutePath() + "'" +
					" Following exception thrown:" + System.lineSeparator() + e.getLocalizedMessage());
		}
	}

	/*
	Note: The following two methods require the jars to be decompressed to function correctly
	(assuming jars are contained within the classpaths).
	*/
	public static void removeClass(SootClass sootClass, Collection<File> classPath) throws IOException{
		List<File> filesToReturn = getClassFile(sootClass, classPath);

		if(filesToReturn.isEmpty()){
			throw new IOException("Cannot find file for class '" +  sootClass.getName() + "'");
		}

		for(File f :filesToReturn) {
			FileUtils.forceDelete(f);
		}
		assert(getClassFile(sootClass, classPath).isEmpty());
	}

	public static void writeClass(SootClass sootClass, Collection<File> classPath) throws IOException{

		List<File> filesToReturn = getClassFile(sootClass, classPath);

		if(filesToReturn.isEmpty()){
			throw new IOException("Cannot find file for class '" + sootClass.getName() + "'");
		}

		for(File f : filesToReturn) {
			writeClass(sootClass, f);
		}
	}

	public static void writeClass(SootClass sootClass, File outputFile) throws IOException {

		//I don't fully understand why, but you need to retrieve the methods before writing to the file
		for (SootMethod sootMethod : sootClass.getMethods()) {
			if(sootMethod.isConcrete()){
				if(!sootMethod.hasActiveBody()) {
					sootMethod.retrieveActiveBody();
				}
			}
		}

		OutputStream streamOut = new JasminOutputStream(new FileOutputStream(outputFile));
		PrintWriter writerOut = new PrintWriter(new OutputStreamWriter(streamOut));

		JasminClass jasminClass = new soot.jimple.JasminClass(sootClass);
		jasminClass.print(writerOut);
		writerOut.flush();
		streamOut.close();
	}

	public static Set<File> extractJars(List<File> classPaths) throws IOException{
		Set<File> decompressedJars = new HashSet<File>();
		for(File file : new HashSet<File>(classPaths)){
			JarFile jarFile = null;
			try {
				jarFile = new JarFile(file);
			} catch (IOException e) {
				continue;
			}

			assert(jarFile != null);

			ClassFileUtils.decompressJar(file);
			decompressedJars.add(file);
		}
		return decompressedJars;
	}

	public static void compressJars(Set<File> decompressedJars) throws IOException {
		for(File file : decompressedJars){
			if(!file.exists()){
				System.out.println("File '" + file.getAbsolutePath() + "' does not exist");
			} else if(!file.isDirectory()){
				System.out.println("File '" + file.getAbsolutePath() + "' is not a directory");
			}
			assert(file.exists() && file.isDirectory());
			ClassFileUtils.compressJar(file);
		}
	}

	public static boolean directoryContains(File dir, File file){
		/* To find if a file is within a directory, we simply keep calling file.getParentFile(), until we find
		a parent directory equal to the directory. We will eventually get to a point where file.getParentFile() == null
		in the case where a file is not within a directory.
		*/

		if(file.getParentFile() == null){
			return false;
		}

		if(file.getParentFile().equals(dir)){
			return true;
		}

		return directoryContains(dir, file.getParentFile());
	}

	public static long getSize(SootClass sootClass){

		for(SootMethod m: sootClass.getMethods()){
			if(m.isConcrete()) {
				m.retrieveActiveBody();
			}
		}

		StringWriter stringWriter = new StringWriter();
		PrintWriter writerOut = new PrintWriter(stringWriter);
		JasminClass jasminClass = new JasminClass(sootClass);
		jasminClass.print(writerOut);
		writerOut.flush();

		return stringWriter.getBuffer().length();
	}
}
