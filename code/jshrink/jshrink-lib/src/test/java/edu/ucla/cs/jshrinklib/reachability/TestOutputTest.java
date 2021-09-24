package edu.ucla.cs.jshrinklib.reachability;

import edu.ucla.cs.jshrinklib.util.MavenUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestOutputTest {

	@Test
	public void TestOutputTest(){
		String toProcess = "-------------------------------------------------------\n" +
			" T E S T S\n" +
			"-------------------------------------------------------\n" +
			"\n" +
			"-------------------------------------------------------\n" +
			" T E S T S\n" +
			"-------------------------------------------------------\n" +
			"Running org.I0Itec.zkclient.util.ZkPathUtilTest\n" +
			"Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.375 sec - " +
			"in org.I0Itec.zkclient.util.ZkPathUtilTest\n" +
			"Running org.I0Itec.zkclient.ServerZkClientTest\n" +
			"Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 42.592 sec - " +
			"in org.I0Itec.zkclient.ServerZkClientTest\n" +
			"Running org.I0Itec.zkclient.InMemoryConnectionTest\n" +
			"Tests run: 2, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 0.034 sec - " +
			"in org.I0Itec.zkclient.InMemoryConnectionTest\n" +
			"Running org.I0Itec.zkclient.ContentWatcherTest\n" +
			"Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.57 sec - " +
			"in org.I0Itec.zkclient.ContentWatcherTest\n" +
			"Running org.I0Itec.zkclient.ZkClientSerializationTest\n" +
			"Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.282 sec - " +
			"in org.I0Itec.zkclient.ZkClientSerializationTest\n" +
			"Running org.I0Itec.zkclient.ZkConnectionTest\n" +
			"Tests run: 2, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 1.29 sec - " +
			"in org.I0Itec.zkclient.ZkConnectionTest\n" +
			"Running org.I0Itec.zkclient.DistributedQueueTest\n" +
			"Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.801 sec - " +
			"in org.I0Itec.zkclient.DistributedQueueTest\n" +
			"Running org.I0Itec.zkclient.MemoryZkClientTest\n" +
			"Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 15.965 sec - " +
			"in org.I0Itec.zkclient.MemoryZkClientTest\n" +
			"\n" +
			"Results :\n" +
			"\n" +
			"Tests run: 44, Failures: 0, Errors: 0, Skipped: 2\n";

		TestOutput output = MavenUtils.testOutputFromString(toProcess);
		assertEquals(44, output.getRun());
		assertEquals(0, output.getFailures());
		assertEquals(0, output.getErrors());
		assertEquals(2, output.getSkipped());
		assertEquals(toProcess, output.getTestOutputText());
		assertTrue(output.isTestBuildSuccess());
	}

	@Test
	public void TestOutputTest2(){
		String toProcess = "[main] INFO spark.GenericIntegrationTest - Body was: Fo shizzy\n" +
			"[main] INFO spark.GenericIntegrationTest - Body was: Fo shizzy\n" +
			"Method Override Worked\n" +
			"[qtp335941281-21] ERROR spark.http.matching.GeneralError - \n" +
			"java.lang.RuntimeException\n" +
			"\tat spark.GenericIntegrationTest.lambda$setup$35(GenericIntegrationTest.java:195)\n" +
			"\tat spark.RouteImpl$1.handle(RouteImpl.java:72)\n" +
			"\tat spark.http.matching.Routes.execute(Routes.java:61)\n" +
			"\tat spark.http.matching.MatcherFilter.doFilter(MatcherFilter.java:134)\n" +
			"\tat spark.embeddedserver.jetty.JettyHandler.doHandle(JettyHandler.java:50)\n" +
			"\tat org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:1568)\n" +
			"\tat org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:144)\n" +
			"\tat org.eclipse.jetty.server.handler.HandlerList.handle(HandlerList.java:61)\n" +
			"\tat org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:132)\n" +
			"\tat org.eclipse.jetty.server.Server.handle(Server.java:503)\n" +
			"\tat org.eclipse.jetty.server.HttpChannel.handle(HttpChannel.java:364)\n" +
			"\tat org.eclipse.jetty.server.HttpConnection.onFillable(HttpConnection.java:260)\n" +
			"\tat org.eclipse.jetty.io.AbstractConnection$ReadCallback.succeeded(AbstractConnection.java:305)\n" +
			"\tat org.eclipse.jetty.io.FillInterest.fillable(FillInterest.java:103)\n" +
			"\tat org.eclipse.jetty.io.ChannelEndPoint$2.run(ChannelEndPoint.java:118)\n" +
			"\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.runTask(EatWhatYouKill.java:333)\n" +
			"\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.doProduce(EatWhatYouKill.java:310)\n" +
			"\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.tryProduce(EatWhatYouKill.java:168)\n" +
			"\tat org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.run(EatWhatYouKill.java:126)\n" +
			"\tat org.eclipse.jetty.util.thread.ReservedThreadExecutor$ReservedThread.run(ReservedThreadExecutor.java:366)\n" +
			"\tat org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:765)\n" +
			"\tat org.eclipse.jetty.util.thread.QueuedThreadPool$2.run(QueuedThreadPool.java:683)\n" +
			"\tat java.lang.Thread.run(Thread.java:748)\n" +
			"[INFO] Tests run: 40, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.649 s - in spark.GenericIntegrationTest\n" +
			"[Thread-19] INFO spark.embeddedserver.jetty.EmbeddedJettyServer - >>> Spark shutting down ...\n" +
			"[Thread-19] INFO org.eclipse.jetty.server.AbstractConnector - Stopped ServerConnector@7433a487{HTTP/1.1,[http/1.1]}{0.0.0.0:4567}\n" +
			"[Thread-19] INFO org.eclipse.jetty.server.session - node0 Stopped scavenging\n" +
			"[INFO] \n" +
			"[INFO] Results:\n" +
			"[INFO] \n" +
			"[INFO] Tests run: 305, Failures: 0, Errors: 0, Skipped: 0\n" +
			"[INFO] \n" +
			"[INFO] ------------------------------------------------------------------------\n" +
			"[INFO] BUILD SUCCESS\n" +
			"[INFO] ------------------------------------------------------------------------\n" +
			"[INFO] Total time: 37.475 s\n" +
			"[INFO] Finished at: 2019-03-08T11:29:11-08:00\n" +
			"[INFO] Final Memory: 29M/773M\n" +
			"[INFO] ------------------------------------------------------------------------\n";

		TestOutput output = MavenUtils.testOutputFromString(toProcess);
		assertEquals(305, output.getRun());
		assertEquals(0, output.getFailures());
		assertEquals(0, output.getErrors());
		assertEquals(0, output.getSkipped());
		assertEquals(toProcess, output.getTestOutputText());
		assertTrue(output.isTestBuildSuccess());
	}

	@Test
	public void TestOutputTest3(){
		String toProcess = "[INFO] ------------------------------------------------------------------------\n" +
			"[INFO] BUILD SUCCESS\n" +
			"[INFO] ------------------------------------------------------------------------\n" +
			"[INFO] Total time: 37.475 s\n" +
			"[INFO] Finished at: 2019-03-08T11:29:11-08:00\n" +
			"[INFO] Final Memory: 29M/773M\n" +
			"[INFO] ------------------------------------------------------------------------\n";

		TestOutput output = MavenUtils.testOutputFromString(toProcess);
		assertEquals(0, output.getRun());
		assertEquals(0, output.getFailures());
		assertEquals(0, output.getErrors());
		assertEquals(0, output.getSkipped());
		assertEquals(toProcess, output.getTestOutputText());
		assertTrue(output.isTestBuildSuccess());
	}

	@Test
	public void TestOutputTestClassesTest(){
		HashMap<String, Integer> testNamesOriginal = new HashMap<>();
		testNamesOriginal.put("org.I0Itec.zkclient.util.ZkPathUtilTest", 2);
		testNamesOriginal.put("org.I0Itec.zkclient.ServerZkClientTest", 18);
		testNamesOriginal.put("org.I0Itec.zkclient.InMemoryConnectionTest", 2);
		testNamesOriginal.put("org.I0Itec.zkclient.ContentWatcherTest", 4);
		testNamesOriginal.put("org.I0Itec.zkclient.ZkClientSerializationTest", 2);
		testNamesOriginal.put("org.I0Itec.zkclient.ZkConnectionTest", 2);
		testNamesOriginal.put("org.I0Itec.zkclient.DistributedQueueTest", 3);
		testNamesOriginal.put("org.I0Itec.zkclient.MemoryZkClientTest", 11);

		String toProcess = "-------------------------------------------------------\n" +
				" T E S T S\n" +
				"-------------------------------------------------------\n" +
				"\n" +
				"-------------------------------------------------------\n" +
				" T E S T S\n" +
				"-------------------------------------------------------\n" +
				"Running org.I0Itec.zkclient.util.ZkPathUtilTest\n" +
				"Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.375 sec - " +
				"in org.I0Itec.zkclient.util.ZkPathUtilTest\n" +
				"Running org.I0Itec.zkclient.ServerZkClientTest\n" +
				"Tests run: 18, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 42.592 sec - " +
				"in org.I0Itec.zkclient.ServerZkClientTest\n" +
				"Running org.I0Itec.zkclient.InMemoryConnectionTest\n" +
				"Tests run: 2, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 0.034 sec - " +
				"in org.I0Itec.zkclient.InMemoryConnectionTest\n" +
				"Running org.I0Itec.zkclient.ContentWatcherTest\n" +
				"Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 6.57 sec - " +
				"in org.I0Itec.zkclient.ContentWatcherTest\n" +
				"Running org.I0Itec.zkclient.ZkClientSerializationTest\n" +
				"Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 1.282 sec - " +
				"in org.I0Itec.zkclient.ZkClientSerializationTest\n" +
				"Running org.I0Itec.zkclient.ZkConnectionTest\n" +
				"Tests run: 2, Failures: 0, Errors: 0, Skipped: 1, Time elapsed: 1.29 sec - " +
				"in org.I0Itec.zkclient.ZkConnectionTest\n" +
				"Running org.I0Itec.zkclient.DistributedQueueTest\n" +
				"Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 4.801 sec - " +
				"in org.I0Itec.zkclient.DistributedQueueTest\n" +
				"Running org.I0Itec.zkclient.MemoryZkClientTest\n" +
				"Tests run: 11, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 15.965 sec - " +
				"in org.I0Itec.zkclient.MemoryZkClientTest\n" +
				"\n" +
				"Results :\n" +
				"\n" +
				"Tests run: 44, Failures: 0, Errors: 0, Skipped: 2\n";
		HashMap<String, Integer> testNames = MavenUtils.testClassesFromString(toProcess);
		assertEquals(testNames, testNamesOriginal);
	}
}
