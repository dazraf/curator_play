package curatorplay.client.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import curatorplay.common.SessionLeaderDetails;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ClientSessionManager implements AutoCloseable {
  private final static Logger logger = LoggerFactory.getLogger(ClientSessionManager.class);
  public static final String ZOOKEEPER_CONNECTION_STRING = "zookeeper-connection-string";
  private final CuratorFramework curator;
  private final ObjectMapper mapper;

  @Inject
  public ClientSessionManager(@Named(ZOOKEEPER_CONNECTION_STRING)String connectionString, ObjectMapper mapper) {
    this.mapper = mapper;
    this.curator = CuratorFrameworkFactory.newClient(connectionString, new RetryOneTime(2000));
    curator.start();
  }

  public Session createSession(SessionListener listener) throws Exception {
    String path = curator.create().creatingParentsIfNeeded().withProtection().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath("/sessions/session", null);
    final NodeCache nodeCache = new NodeCache(curator, path);
    nodeCache.getListenable().addListener(() -> {
      logger.info("receiving data for {}", path);
      SessionLeaderDetails sld = null;
      byte[] bytes = nodeCache.getCurrentData().getData();
      if (bytes != null) {
        sld = mapper.readValue(bytes, SessionLeaderDetails.class);
        logger.info("got leader details {}", sld);
      }
      listener.onNewLeader(sld);
    });
    nodeCache.start();
    return () -> nodeCache.close();
  }

  @Override
  public void close() throws Exception {
    curator.close();
  }
}
