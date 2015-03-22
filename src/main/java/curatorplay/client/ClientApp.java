package curatorplay.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import curatorplay.client.netclient.TCPClient;
import curatorplay.client.session.ClientSessionManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientApp extends AbstractModule {
  public static void main(String [] args) throws Exception {
    Injector injector = Guice.createInjector(new ClientApp());
    // we do this to shutdown the client gracefully where possible
    try (ClientSessionManager csm = injector.getInstance(ClientSessionManager.class)) {
      try (TCPClient client = injector.getInstance(TCPClient.class)) {
        waitForNewLine();
      }
    }
  }

  private static void waitForNewLine() throws IOException {
    new BufferedReader(new InputStreamReader(System.in)).readLine();
  }

  @Override
  protected void configure() {
    bind(String.class).annotatedWith(Names.named(ClientSessionManager.ZOOKEEPER_CONNECTION_STRING)).toInstance("127.0.0.1:2181");
    bind(ObjectMapper.class).asEagerSingleton();
    bind(ClientSessionManager.class).asEagerSingleton();
    bind(TCPClient.class);
  }
}
