package curatorplay.server;

import curatorplay.server.netserver.TCPServer;
import curatorplay.server.session.SessionOwners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ServerApp {

  public static void main(String [] args) throws Exception {
    new ServerApp().run("127.0.0.1:2181");
  }

  private void run(String connectionString) throws Exception {
    TCPServer tcpServer = new TCPServer();
    SessionOwners owners = new SessionOwners(connectionString, tcpServer.start());
    waitForNewLine();
    owners.close();
    tcpServer.close();
  }

  private void waitForNewLine() throws IOException {
    new BufferedReader(new InputStreamReader(System.in)).readLine();
  }
}
