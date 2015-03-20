package curatorplay.client;

import curatorplay.client.netclient.TCPClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientApp {
  private static final Logger logger = LoggerFactory.getLogger(ClientApp.class);

  public static void main(String [] args) throws Exception {
    TCPClient tcpClient = new TCPClient();
    CuratorFramework curator = setupCurator();
    NodeCache nodeCache = setUpSession(tcpClient, curator);
    waitForNewLine();
    nodeCache.close();
    curator.close();
    tcpClient.close();
  }

  private static NodeCache setUpSession(TCPClient tcpClient, CuratorFramework curator) throws Exception {
    String path = curator.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/sessions/session", null);
    NodeCache nodeCache = new NodeCache(curator, path);
    nodeCache.getListenable().addListener(() -> tcpClient.connect(nodeCache.getCurrentData().getData()));
    nodeCache.start();
    System.out.println(path);
    return nodeCache;
  }

  private static CuratorFramework setupCurator() {
    CuratorFramework curator = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new RetryOneTime(2000));
    curator.start();
    return curator;
  }

  private static void waitForNewLine() throws IOException {
    new BufferedReader(new InputStreamReader(System.in)).readLine();
  }
}
