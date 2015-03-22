package curatorplay;

import org.apache.curator.test.TestingServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class SetupTest {
  private TestingServer zkServer;

  @Before
  public void before() throws Exception {
  }

  @After
  public void after() throws IOException {
    if (zkServer != null) {
      zkServer.stop();
      zkServer = null;
    }
  }

  @Test
  public void sessionCreation() throws Exception {
    givenZookeeper();
    givenCurator(1);
    givenClient(1);
  }

  private void givenCurator(Object key) {
  }

  private void givenClient(int id) {

  }

  private void givenZookeeper() throws Exception {
    this.zkServer = new TestingServer(2181);
    zkServer.start();
  }
}
