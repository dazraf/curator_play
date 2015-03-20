package fuzz;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClientApp {
  public static void main(String [] args) throws Exception {
    CuratorFramework client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new RetryOneTime(2000));
    client.start();
    String path = client.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/sessions/session");
    System.out.println(path);
    new BufferedReader(new InputStreamReader(System.in)).readLine();
    client.close();
  }
}
