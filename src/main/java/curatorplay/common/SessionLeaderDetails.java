package curatorplay.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionLeaderDetails {
  private String host;
  private int port;

  public SessionLeaderDetails() {}

  public SessionLeaderDetails(String host, int port) {
    this.host = host;
    this.port = port;
  }
  public int getPort() {
    return port;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setPort(int port) {
    this.port = port;
  }

  @Override
  public String toString() {
    return host + ":" + port;
  }
}
