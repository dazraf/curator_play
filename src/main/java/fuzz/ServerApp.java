package fuzz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ServerApp {
  public static void main(String [] args) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new RetryOneTime(2000));
    client.start();

    if (client.checkExists().forPath("/sessions") == null) {
      try {
        client.create().withMode(CreateMode.PERSISTENT).forPath("/sessions");
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    PathChildrenCache cache = new PathChildrenCache(client, "/sessions", true);
    cache.getListenable().addListener((c, event) -> {
      StringBuilder sb = new StringBuilder();
      sb.append(event.getType().name()).append(" ").append(event.getData().getPath());
      System.out.println(sb);
    });
    cache.start();
    new BufferedReader(new InputStreamReader(System.in)).readLine();
    cache.close();
    client.close();
  }
}
