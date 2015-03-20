package curatorplay.server.session;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.apache.curator.framework.imps.CuratorFrameworkState.STARTED;

/**
 * Watches the space of sessions
 * Sets up leader selection on all sessions
 * By deference, sets up session leader connection details for the client to connect to
 */
public class SessionOwners implements Closeable {
  private static final Logger logger = LoggerFactory.getLogger(SessionOwners.class);
  public static final String SESSIONS_ROOT = "/sessions";

  private final ConcurrentHashMap<String, SessionOwner> owners = new ConcurrentHashMap<>();
  private final CuratorFramework curator;
  private final byte[] leaderData;
  private volatile boolean closed = false;
  private ReadWriteLock lock = new ReentrantReadWriteLock();
  private final PathChildrenCache cache;

  /**
   * Sets up everything given the curator.
   * Please note: curatorFramework must be started
   * @param curatorFramework the curator instance - must be in a started state
   * @throws Exception
   */
  public SessionOwners(CuratorFramework curatorFramework, byte[] leaderData) throws Exception {
    this.curator = curatorFramework;
    this.leaderData = leaderData;
    if (curator.getState() != STARTED)
      throw new Exception ("curator must be started");

    ensureRootIsCreated();
    this.cache = new PathChildrenCache(curator, SESSIONS_ROOT, true);
    setupSessionsWatcher();
    cache.start();
  }

  private void ensureRootIsCreated() throws Exception {
    if (curator.checkExists().forPath(SESSIONS_ROOT) == null) {
      try {
        curator.create().withMode(CreateMode.PERSISTENT).forPath(SESSIONS_ROOT);
      } catch (Exception e) {
        logger.error("failed to create sessions root", e);
      }
    }
  }

  private void setupSessionsWatcher() throws Exception {
    cache.getListenable().addListener((c1, event) -> {
      switch (event.getType()) {
        case CHILD_ADDED:
          try {
            create(event.getData().getPath());
          } catch (Exception e) {
            logger.error("failed to create leader selector for " + event.getData().getPath(), e);
          }
          break;
        case CHILD_REMOVED:
          try {
            remove(event.getData().getPath());
          } catch (Exception e) {
            logger.error("failed to remove leader selector for " + event.getData().getPath(), e);
          }
          break;
        default:
          logger.info("unhandled event: " + event.getType());
          // ignore for now
          //        case CHILD_UPDATED:
          //        case CONNECTION_LOST:
          //        case CONNECTION_RECONNECTED:
          //        case CONNECTION_SUSPENDED:
          //        case INITIALIZED:
      }
    });
  }

  private void remove(String path) throws Exception {
    lock.readLock().lock();
    try {
      checkNotClosed();
      SessionOwner owner = owners.remove(path);
      owner.close();
    } finally {
      lock.readLock().lock();
    }
  }


  private SessionOwner create(final String path) throws SessionOwnersClosed {
    logger.info("creating for " + path);
    lock.readLock().lock();
    try {
      checkNotClosed();
      return owners.computeIfAbsent(path, k -> new SessionOwner(curator, k, leaderData));
    } finally {
      lock.readLock().unlock();
    }
  }

  @Override
  public void close() throws IOException {
    lock.writeLock().lock();
    try {
      if (!closed) {
        closed = true;
        reallyClose();
      }
    } finally {
      lock.writeLock().unlock();
    }
  }

  private void reallyClose() throws IOException {
    cache.close();
    owners.forEach((k, v) -> {
      try {
        v.close();
      } catch (Exception  e) {
        logger.error("failed to close session owner", e);
      }
    });
    owners.clear();
  }

  private void checkNotClosed() throws SessionOwnersClosed {
    if (closed)
      throw new SessionOwnersClosed();
  }

}
