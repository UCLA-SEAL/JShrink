package edu.ucla.cs.jshrinklib;

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;

import java.io.File;
import java.io.IOException;

public class GitGetter {
	private File gitDir = null;
	public GitGetter(){
		try {
			this.gitDir = File.createTempFile("gitDir", "");
			this.gitDir.delete();
			this.gitDir.mkdir();
		}catch(IOException e){
			System.err.println("Failed to create gitdir");
			System.err.println(e.fillInStackTrace());
			System.exit(1);
		}

		assert(this.gitDir != null);
	}

	public File addGitHubProject(String username, String project, String commit, File toCheck){
		if(toCheck.exists()){
			return toCheck;
		}
		return addGitHubProject(username, project, commit);
	}

	public File addGitHubProject(String username, String project, String commit){
		File localRepo = new File(this.gitDir.getAbsolutePath() + File.separator + username + "_" + project);

		if(!localRepo.exists()) {
			String remoteRepo = "https://github.com/" + username + "/" + project + ".git";
			CloneCommand clone = new CloneCommand();
			clone.setDirectory(localRepo);
			clone.setURI(remoteRepo);
			try {
				Git git = clone.call();
				git.checkout().setName(commit).call();
			} catch (Exception e) {
				System.err.println("Failed to get repo \"" + remoteRepo + "\".");
				System.err.println(e.fillInStackTrace());
				System.exit(1);
			}
		}

		return localRepo;
	}

	public void removeGitDir(){
		this.gitDir.delete();
	}

}
