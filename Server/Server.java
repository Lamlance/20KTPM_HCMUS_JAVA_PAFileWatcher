package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

import Protocol.Protocol;
import Server.ClientHandler.ClientHandleThread;
import Server.ClientInfo.ClientInfo;

/**
 * Server
 */
public class Server {
  ServerSocket server = null;
  ServerGui gui = null;
  HashMap<String, ClientInfo> clientMap = new HashMap<String, ClientInfo>();

  public static void main(String[] args) {
    Server newServer = new Server();
  }

  Server() {
    try {
      server = new ServerSocket(9000);
      this.gui = new ServerGui();

      do {
        System.out.println("Server is listening");
        Socket clienSocket = server.accept();
        ClientInfo clientInfo = new ClientInfo(clienSocket);
        String clientName = AcceptClient(clientInfo);
        clientMap.put(clientName, clientInfo);
        
        Thread newThread = new Thread(
          new ClientHandleThread(clientInfo, clientName,gui.getLogTextArea(),gui.getFileNameModel())
        );
        clientInfo.sendQueue.add(String.format("%s&&./",Protocol.SV_CMD_FILELIST));
        newThread.start();

      } while (true);
    } catch (Exception e) {

    }

  }

  private String AcceptClient(ClientInfo clientInfo) {
    try {
      String name = clientInfo.waitForString();
      String newName = this.ClientNameChecker(name);
      clientInfo.sendString(newName);
      
      return newName;

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String ClientNameChecker(String nameString) {
    if (!this.clientMap.containsKey(nameString)) {
      return nameString;
    }

    Integer counter = 0;
    Set<String> clientName = this.clientMap.keySet();
    for (String name : clientName) {
      counter += (name.equalsIgnoreCase(nameString)) ? 1 : 0;
    }
    nameString = nameString.concat(String.format("$$%d", counter));
    return nameString;
  }
}