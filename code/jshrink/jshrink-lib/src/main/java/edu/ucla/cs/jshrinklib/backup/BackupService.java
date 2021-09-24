package edu.ucla.cs.jshrinklib.backup;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class BackupService {
	ArrayList<Checkpoint> checkpoints;
	String backupFolder;
	String realPath;
	boolean isVerbose;

	public BackupService(File project, String backupFolder, boolean verbose){
		this.checkpoints = new ArrayList<>();
		this.backupFolder = backupFolder;
		this.isVerbose = verbose;
		this.realPath = project.getAbsolutePath();
	}
	public boolean addCheckpoint(String checkpointName){
		Checkpoint c = new Checkpoint(this.realPath, this.backupFolder, checkpointName, this.isVerbose);
		if(!c.isValid())
			return false;
		this.checkpoints.add(c);
		return true;
	}
	public boolean removeCheckpoint(){
		if(checkpoints.size()==0){
			return false;
		}
		Checkpoint c= checkpoints.get(checkpoints.size()-1);
		if(this.isVerbose){
			System.out.println("Removing checkpoint- "+c.transformation);
		}
		c.delete();
		checkpoints.remove(c);
		return true;
	}
	public boolean validateLastCheckpoint() {
		if(checkpoints.size()==0){
			return false;
		}
		Checkpoint c = checkpoints.get(checkpoints.size()-1);
		return c.isValid() && c.isSafe();
	}

	public boolean revertToLast(){
		Checkpoint c = checkpoints.get(checkpoints.size()-1);
		if(!c.rollBackToBackup()) {
			System.err.println("Failed to copy checkpoint files to original directory for checkpoint - " + c.transformation);
			return false;
		}
		return true;
	}

	public Set<File> resolveFiles(Set<File> files){
		if(checkpoints.size()==0){
			throw new IllegalArgumentException("No checkpoints found to resolve classpaths");
		}
		Checkpoint c = checkpoints.get(checkpoints.size()-1);
		Set<File> resolvedFiles = new HashSet<File>();
		for(File f: files){
			resolvedFiles.add(c.resolveToBackupFile(f));
		}
		return resolvedFiles;
	}
	//public revertToLast

}
