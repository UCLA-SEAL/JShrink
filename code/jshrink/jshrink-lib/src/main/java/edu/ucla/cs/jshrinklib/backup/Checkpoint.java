package edu.ucla.cs.jshrinklib.backup;

import edu.ucla.cs.jshrinklib.reachability.TestOutput;
import edu.ucla.cs.jshrinklib.util.MavenUtils;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Checkpoint {
	private java.time.Instant timestamp;
	private Path backupPath;
	private Path oldPath;
	public String transformation;
	private boolean testsPassed;
	private boolean isVerbose;
	public boolean rollBack;
	private boolean isValid;

	private boolean runTests(){
		if (this.isVerbose) {
			System.out.println("Running project tests for checkpoint "+this.transformation+" ...");
		}
		this.testsPassed = false;
		String[] cmd;
		ProcessBuilder processBuilder;
		Process process;
		InputStream stdout;
		InputStreamReader isr;
		BufferedReader br;
		String line;
		int exitValue;
		String maven_log = "";
		try{
			File pomFile = new File(this.backupPath + File.separator + "pom.xml");
			File libsDir = new File(this.backupPath + File.separator + "libs");
			cmd = new String[]{"mvn", "-f", pomFile.getAbsolutePath(), "surefire:test",
					"-Dmaven.repo.local=" + libsDir.getAbsolutePath(), "--batch-mode", "-fn"};
			processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			process = processBuilder.start();
			stdout = process.getInputStream();
			isr = new InputStreamReader(stdout);
			br = new BufferedReader(isr);
			while ((line = br.readLine()) != null) {
				maven_log += line + System.lineSeparator();
			}
			br.close();
			exitValue = process.waitFor();
		}
		catch(Exception e){
			System.out.println(e.getStackTrace());
			return false;
		}

		// still get test output even in case of test failure
		TestOutput out = MavenUtils.testOutputFromString(maven_log);
		if(out.isTestBuildSuccess() && out.getFailures() == 0 && out.getErrors() == 0){
			this.testsPassed = true;
		}

		if (this.isVerbose) {
			//System.out.println(maven_log);
			System.out.println("Done running project tests for "+transformation+" !");
		}

		return this.testsPassed;
	}

	private void copyFiles(Path oldPath, Path newPath) throws IOException {
		FileUtils.copyDirectory(oldPath.toFile(), newPath.toFile());
//		for(Path source: Files.walk(oldPath).collect(Collectors.toSet())){
//			Path target = newPath.resolve(oldPath.getParent().relativize(source));
//			if(source.toFile().isDirectory()){
//				target.toFile().mkdirs();
//			}
//			else{
//				if(!target.getParent().toFile().exists())
//					target.getParent().toFile().mkdirs();
//				Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
//			}
//
//		}
	}
	private boolean backup(String realPath, String backupPath){
		try{
			this.oldPath = Paths.get(realPath);
			File backupFolder = new File(backupPath+File.separator+"backup_"+transformation);
			backupFolder.mkdirs();
			if (!this.oldPath.toFile().isDirectory() ||  (!backupFolder.isDirectory()))
				throw new IllegalArgumentException("Input for backup is not a folder");

			this.backupPath = Paths.get(backupFolder.getAbsolutePath()+File.separator+this.oldPath.getFileName());
			copyFiles(this.oldPath, this.backupPath);
		}
		catch(Exception e){
			System.err.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Checkpoint(String realPath, String backupFolder, String transformation, boolean isVerbose){
		this.transformation = transformation;
		this.isVerbose = isVerbose;
		this.rollBack = false;
		if(!this.backup(realPath, backupFolder))
		{
			throw new IllegalArgumentException("Checkpoint creation failed "+this.transformation);
		}
		this.timestamp = java.time.Instant.now();
		this.isValid = true;
		if(this.isVerbose){
			System.out.println("Created checkpoint "+transformation+" - "+this.getBackupPath()+" for "+this.getRealPath()+" at "+this.timestamp);
		}
	}

	public Checkpoint(String realPath, String backupPath, String transformation){
		this(realPath, backupPath, transformation, false);
	}

	public boolean rollBackToBackup(){
		try {
			this.copyFiles(this.backupPath, this.oldPath);
			this.rollBack = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(this.isVerbose){
			System.out.println("Rolled back "+this.getBackupPath()+" to "+this.getRealPath()+" at "+java.time.Instant.now());
		}
		return this.rollBack;
	}

	public boolean delete(){
		try {
			FileUtils.deleteDirectory(this.backupPath.toFile());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		this.isValid = false;
		return true;
	}

	public File resolveToBackupFile(File realPath){
		return (this.getBackupPath().resolve(this.getRealPath().relativize(realPath.toPath()))).toFile();
	}
	public boolean isSafe(){
		return this.testsPassed || this.runTests();
	}

	public boolean isValid(){return this.isValid;}

	public void exit(){
		System.exit(0);
	}

	public Path getRealPath(){
		return this.oldPath;
	}

	public Path getBackupPath(){
		return this.backupPath;
	}
}
