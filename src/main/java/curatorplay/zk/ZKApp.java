package curatorplay.zk;

import org.apache.curator.test.TestingServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ZKApp {
  public static void main(String [] args) throws Exception {
    TestingServer server = new TestingServer(2181);
    server.start();
    System.out.println(server.getConnectString());
    new BufferedReader(new InputStreamReader(System.in)).readLine();
    server.stop();
  }
}
