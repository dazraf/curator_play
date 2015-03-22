package curatorplay.server.netserver;

import curatorplay.common.SessionLeaderDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.net.NetServer;
import org.vertx.java.core.net.NetSocket;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class TCPServer implements AutoCloseable {
  private static Logger logger = LoggerFactory.getLogger(TCPServer.class);
  private Vertx vertx;
  private NetServer server;

  /**
   * Returns the connection data as session data - too leaky?
   * @return
   */
  public SessionLeaderDetails start() {
    AtomicReference<SessionLeaderDetails> ref = new AtomicReference<>();
    CountDownLatch latch = new CountDownLatch(1);
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
    .listen(0, "localhost", ar -> {
      logger.info("started on " + server.host() + ":" + server.port());
      ref.set(new SessionLeaderDetails(server.host(), server.port()));
    });
    while (ref.get() == null); // BAD - but quick
    return ref.get();
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
