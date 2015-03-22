package curatorplay.client.session;

import curatorplay.common.SessionLeaderDetails;

@FunctionalInterface
public interface SessionListener {
  void onNewLeader(SessionLeaderDetails sessionLeaderDetails);
}
