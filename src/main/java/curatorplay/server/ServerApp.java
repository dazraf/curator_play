package curatorplay.server;

import curatorplay.server.netserver.TCPServer;
import curatorplay.server.session.SessionOwners;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerApp {
  private static Logger logger = LoggerFactory.getLogger(ServerApp.class);

  public static void main(String [] args) throws Exception {
    new ServerApp().run("127.0.0.1:2181");
  }

  private void run(String connectionString) throws Exception {
    CuratorFramework curator = setupCurator(connectionString);
    TCPServer tcpServer = new TCPServer();
    SessionOwners owners = new SessionOwners(curator, tcpServer.start());
    waitForNewLine();
    owners.close();
    tcpServer.close();
    curator.close();
  }

  private CuratorFramework setupCurator(String connectionString) {
    CuratorFramework curator = CuratorFrameworkFactory.newClient(connectionString, new RetryOneTime(2000));
    curator.start();
    return curator;
  }

  private void waitForNewLine() throws IOException {
    new BufferedReader(new InputStreamReader(System.in)).readLine();
  }
}
