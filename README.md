# curator_play

## Introduction

Contains 3 processes under packages zk, server and client.

To demonstrate:

1. start up ZKApp to initialise the ZooKeeper node
2. start up 1 or more ServerApp instances.
3. start up 1 or more ClientApp instances.

## servers

* Each server creates a TCP service that will send "data" results to it's connections.
* Each server also watches for sessions posted in ZK by clients and runs a continuous leadership election for these sessions.
* If it gains leadership, it posts the end point of its TCP server to the session ZK node's data.

## clients

Each client creates a session and waits for leader connection data.
When it receives one (its session having been won by one leader), it attempts to maintain a connection to the leader, receiving "data" packets.

Manual tests passed

1. 2 servers, 2 clients. Both clients start to receive data from their respective session leader
2. Gracefully shutting down a server, transfers ownership of the session to the other server. Respective clients reconnect and continue to receive data.
3. Crashing a server, transfers ownership of the session to the other server, after ZK timeout (30s). Respective clients reconnect and continue to receive data.
