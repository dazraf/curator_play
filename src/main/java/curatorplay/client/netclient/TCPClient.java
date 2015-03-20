package curatorplay.client.netclient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;

public class TCPClient implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(TCPClient.class);
  private final Vertx vertx;
  private NetClient netClient;
  private NetSocket netSocket;
  private JsonObject connectionConfig = null;

  public TCPClient() {
    this.vertx = VertxFactory.newVertx();
    this.netClient = vertx.createNetClient();
  }

  public void connect(byte[] data) {
    try {
      tearDown();
      if (data != null) {
        this.connectionConfig = new JsonObject(new String(data, "UTF-8"));
        connect();
      }
    } catch (Exception e) {
      logger.error ("unhandled exception", e);
    }
  }

  private void connect() {
    netClient.connect(connectionConfig.getInteger("port"), connectionConfig.getString("host"), ar -> {
      if (ar.failed()) {
        logger.error("connection failed", ar.cause());
      } else {
        netSocket = ar.result();
        logger.info("connected to: " + netSocket.remoteAddress());
        ar.result().closeHandler((Void) -> connect()); // world's simplest retry
        ar.result().dataHandler(buffer -> logger.info("received: {}", buffer.getString(0, buffer.length())));
        ar.result().write("hello, i'm that client!");
      }
    });
  }

  private void tearDown() {
    if (netSocket != null) {
      logger.info("disconnecting from {}", netSocket.remoteAddress());
      netSocket.closeHandler(null);
      netSocket.close();
      netSocket = null;
    }
  }

  @Override
  public void close() throws Exception {
    tearDown();
    netClient.close();
    vertx.stop();
  }
}
