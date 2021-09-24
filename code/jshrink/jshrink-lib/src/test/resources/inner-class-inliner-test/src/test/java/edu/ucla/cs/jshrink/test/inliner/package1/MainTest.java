package edu.ucla.cs.jshrink.test.inliner.package1;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MainTest {

	@Test
	public void MainTest(){
		String[] args ={};
		Main.main(args);
		assertEquals(2, Main.results);
	}
}
