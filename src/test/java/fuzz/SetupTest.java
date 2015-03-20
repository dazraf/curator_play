package fuzz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class SetupTest {
  private TestingServer zkServer;

  @Before
  public void before() throws Exception {
    zkServer = new TestingServer(2181);
  }

  @After
  public void after() throws IOException {
    zkServer.stop();
  }

  @Test
  public void test() throws Exception {
    CuratorFramework server = CuratorFrameworkFactory.newClient(zkServer.getConnectString(), new RetryOneTime(2000));
    server.start();

    server.create().withMode(CreateMode.PERSISTENT).forPath("/sessions");
    PathChildrenCache cache = new PathChildrenCache(server, "/sessions", true);
    CountDownLatch latch = new CountDownLatch(2);
    cache.getListenable().addListener((c, event) -> {
      System.out.println(event.getData().getPath());
      latch.countDown();
    });
    cache.start();

    CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new RetryOneTime(2000));
    client.start();
    String result = client.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/sessions/session");

    latch.await();
    client.close();
    latch.await();
    cache.close();
    server.close();
  }
}
