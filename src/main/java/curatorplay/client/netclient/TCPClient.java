package curatorplay.client.netclient;

import com.google.inject.Inject;
import curatorplay.client.session.ClientSessionManager;
import curatorplay.client.session.Session;
import curatorplay.client.session.SessionListener;
import curatorplay.common.SessionLeaderDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.VertxFactory;
import org.vertx.java.core.net.NetClient;
import org.vertx.java.core.net.NetSocket;

public class TCPClient implements AutoCloseable, SessionListener {
  private static final Logger logger = LoggerFactory.getLogger(TCPClient.class);
  private final Vertx vertx;
  private final Session session;
  private NetClient netClient;
  private NetSocket netSocket;
  private SessionLeaderDetails leaderDetails;

  @Inject
  public TCPClient(ClientSessionManager csm) throws Exception {
    this.vertx = VertxFactory.newVertx();
    this.netClient = vertx.createNetClient();
    this.session = csm.createSession(this);
  }

  @Override
  public void onNewLeader(SessionLeaderDetails leaderDetails) {
    logger.info("new leader: {}", leaderDetails);
    try {
      tearDownSocket();
      this.leaderDetails = leaderDetails;
      connect();
    } catch (Exception e) {
      logger.error ("unhandled exception", e);
    }
  }

  private void connect() {
    if (leaderDetails == null)
      return;

    netClient.connect(leaderDetails.getPort(), leaderDetails.getHost(), ar -> {
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

  private void tearDownSocket() {
    if (netSocket != null) {
      logger.info("disconnecting from {}", netSocket.remoteAddress());
      netSocket.closeHandler(null);
      netSocket.close();
      netSocket = null;
    }
  }

  @Override
  public void close() throws Exception {
    tearDownSocket();
    netClient.close();
    vertx.stop();
    session.close();
  }
}
