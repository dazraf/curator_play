package curatorplay.server.session;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

public class SessionOwner extends LeaderSelectorListenerAdapter implements AutoCloseable {
  private static final Logger logger = LoggerFactory.getLogger(SessionOwner.class);
  private final CountDownLatch releaseOwnership = new CountDownLatch(1);
  private final LeaderSelector leaderSelector;
  private final String path;
  private final byte[] leaderData;
  private final CuratorFramework curator;

  public SessionOwner(CuratorFramework curator, String path, byte[] leaderData) {
    logger.info("creating LeaderSelector for " + path);
    this.path = path;
    this.leaderData = leaderData;
    this.curator = curator;
    // the leadership path has to be distinct from the se
    String leaderPath = ZKPaths.makePath("/leaders", ZKPaths.getNodeFromPath(path));
    leaderSelector = new LeaderSelector(curator, leaderPath, this);
    leaderSelector.autoRequeue();
    leaderSelector.start();
  }

  @Override
  public void takeLeadership(CuratorFramework client) throws Exception {
    logger.info("taking ownership for " + path);
    try {
      try {
        client.setData().forPath(path, leaderData);
      } catch (Exception e) {
        // this is pretty serious - if we can't set the
        logger.error("exception setting data for path!", e);
      }
      releaseOwnership.await();
    } finally {
      logger.info("releasing leadership for " + path);
    }
  }

  @Override
  public void close() throws Exception {
    logger.info("closing for " + path);
    releaseOwnership.countDown();
    try {
      curator.setData().forPath(path, null);
    } finally {
      leaderSelector.close();
    }
  }
}
