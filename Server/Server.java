package Server;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server
 */
public class Server {
  ServerSocket server = null;
  Server() {
    try {

      server = new ServerSocket(9000);

      do {
        Socket client = server.accept();
      } while (true);
    } catch (Exception e) {

    }

  }
}