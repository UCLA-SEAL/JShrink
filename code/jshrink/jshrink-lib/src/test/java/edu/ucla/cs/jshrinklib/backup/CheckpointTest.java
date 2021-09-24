package edu.ucla.cs.jshrinklib.backup;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CheckpointTest {
	public static void buildProject(Path dir){
		String[] cmd;
		ProcessBuilder processBuilder;
		Process process;
		InputStream stdout;
		InputStreamReader isr;
		BufferedReader br;
		String line;
		int exitValue;
		try{
			File pomFile = new File(dir + File.separator + "pom.xml");
			File libsDir = new File(dir + File.separator + "libs");
			cmd = new String[]{"mvn", "-f", pomFile.getAbsolutePath(), "install",
					"-Dmaven.repo.local=" + libsDir.getAbsolutePath(),
					"--quiet",
					"--batch-mode",
					"-DskipTests=true"};
			processBuilder = new ProcessBuilder(cmd);
			processBuilder.redirectErrorStream(true);
			process = processBuilder.start();
			stdout = process.getInputStream();
			isr = new InputStreamReader(stdout);
			br = new BufferedReader(isr);

			String output = "";
			while ((line = br.readLine()) != null) {
				output += line + System.lineSeparator();
			}
			br.close();

			exitValue = process.waitFor();

			if (exitValue != 0) {
				throw new IOException("Build failed! Output the following " + System.lineSeparator() + output);
			}
		}
		catch(Exception e){
			System.out.println("Could not build project "+dir.toString());
			System.out.println(e.getStackTrace());
		}
	}
	public static boolean compareDir(Path dir1, Path dir2){
		Set<File> files1 = new HashSet<File>(FileUtils.listFilesAndDirs(dir1.toFile(), TrueFileFilter.TRUE, TrueFileFilter.TRUE));
		Set<File> files2 = new HashSet<File>(FileUtils.listFilesAndDirs(dir2.toFile(), TrueFileFilter.TRUE, TrueFileFilter.TRUE));
		return files2.stream().map(x->dir2.relativize(x.toPath())).collect(Collectors.toSet())
					.containsAll(files1.stream().map(x->dir1.relativize(x.toPath())).collect(Collectors.toSet()));
	}
	@Test
	public void testFolderBackup(){
		String realPath = CheckpointTest.class.getClassLoader().getResource("simple-test-project2").getPath();
		String backupPath = "/tmp/checkpoint-test";
		Checkpoint c = new Checkpoint(realPath, backupPath, "class-collapse", true);
		assertTrue(compareDir(c.getRealPath(), c.getBackupPath()));
	}

	@Test
	public void rollbackTest(){
		String realPath = CheckpointTest.class.getClassLoader().getResource("simple-test-project2").getPath();
		String backupPath = "/tmp/checkpoint-test";
		//back up real project to backup directory
		Checkpoint c1 = new Checkpoint(realPath, backupPath, "class-collapse", true);

		//create checkpoint for backup directory
		Checkpoint c = new Checkpoint(c1.getBackupPath().toString(), backupPath+"/test1", "class-collapse", true);
		assertTrue(compareDir(c1.getBackupPath(), c.getBackupPath()));
		assertTrue(c1.getBackupPath().equals(c.getRealPath()));

		//delete file from backup directory
		File toDelete = ((LinkedList<File>)FileUtils.listFiles(c.getRealPath().toFile(), TrueFileFilter.TRUE, TrueFileFilter.TRUE)).get(0).getParentFile();
		try {
			FileUtils.deleteDirectory(toDelete);
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertFalse(compareDir(c.getBackupPath(), c.getRealPath()));

		//rollback changes and verify
		c.rollBackToBackup();
		assertTrue(c.rollBack);
		assertTrue(compareDir(c.getBackupPath(), c.getRealPath()));
	}

	@Test
	public void checkpointTest(){
		String realPath = CheckpointTest.class.getClassLoader().getResource("simple-test-project2").getPath();
		String backupPath = "/tmp/checkpoint-test";
		Checkpoint c = new Checkpoint(realPath, backupPath, "class-collapse", true);
		assertTrue(c.isSafe());
		assertTrue(c.isSafe());
	}

	@Test
	public void checkpointTest2(){
		String realPath = CheckpointTest.class.getClassLoader().getResource("junit4").getPath();
		String backupPath = "/tmp/checkpoint-test";
		Checkpoint c = new Checkpoint(realPath, backupPath, "class-collapse", true);
		if(!new File(c.getBackupPath().toFile().getAbsolutePath()+File.separator+"target").exists()){
			CheckpointTest.buildProject(c.getBackupPath());
		}
		assertTrue(c.isSafe());
		assertTrue(c.isSafe());
	}
	@Test
	public void checkpointTestMultipleModules(){
		String realPath = CheckpointTest.class.getClassLoader().getResource("module-test-project").getPath();
		String backupPath = "/tmp/checkpoint-test";
		Checkpoint c = new Checkpoint(realPath, backupPath, "class-collapse", true);
		if(!new File(c.getBackupPath().toFile().getAbsolutePath()+File.separator+"target").exists()){
			CheckpointTest.buildProject(c.getBackupPath());
		}
		assertTrue(c.isSafe());
	}
	@Test
	public void testPathResolution(){
		String realPath = CheckpointTest.class.getClassLoader().getResource("simple-test-project2").getPath();
		String backupPath = "/tmp/checkpoint-test";
		Checkpoint c = new Checkpoint(realPath, backupPath, "class-collapse", true);
		String p = File.separator+"src"+File.separator+"main"+File.separator+"java";
		assertEquals(c.getBackupPath().toAbsolutePath()+p,c.resolveToBackupFile(new File(realPath+p)).getAbsolutePath());
	}
}
