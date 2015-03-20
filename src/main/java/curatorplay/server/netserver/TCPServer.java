package curatorplay.server.netserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.NetSocket;

public class TCPServer implements AutoCloseable {
  private static Logger logger = LoggerFactory.getLogger(TCPServer.class);
  private Vertx vertx;
  private NetServer server;

  /**
   * Returns the connection data as session data - too leaky?
   * @return
   */
  public byte[] start() {
    this.vertx = VertxFactory.newVertx();
    this.server = vertx.createNetServer()
      .connectHandler(ns -> {
        sendData(ns);
        logger.info("connection from " + ns.remoteAddress());
        ns.closeHandler((v) -> {
          logger.info("disconnected from " + ns.remoteAddress());
        });
        ns.dataHandler(buffer -> logger.info("received from " + ns.remoteAddress() + ": " + buffer.getString(0, buffer.length())));
      })
    .listen(0, "localhost");
    logger.info("started on " + server.host() + ":" + server.port());
    return new JsonObject().putString("host", server.host()).putNumber("port", server.port()).encode().getBytes();
  }

  private void sendData(NetSocket ns) {
    try {
      ns.write("data");
      vertx.setTimer(3000, l -> sendData(ns));
    } catch (Exception e) {
      logger.error("failed to write data", e);
    }
  }


  @Override
  public void close() throws Exception {
    server.close();
    vertx.stop();
  }
}
