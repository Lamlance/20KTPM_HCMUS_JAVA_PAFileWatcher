package Server;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JTextArea;

import Protocol.Protocol;
import Server.ClientHandler.ClientHandleThread;
import Server.ClientInfo.ClientInfo;

/**
 * Server
 */
public class Server {
  ServerSocket server = null;
  ServerGui gui = null;
  public HashMap<String, ClientInfo> clientMap = new HashMap<String, ClientInfo>();

  public static void main(String[] args) {
    Server newServer = new Server();
  }

  Server() {
    try {
      server = new ServerSocket(9000);
      this.gui = new ServerGui();
      this.gui.SetNavBtnHandler(new NavBtnHandler());

      do {
        System.out.println("Server is listening");
        Socket clienSocket = server.accept();
        ClientInfo clientInfo = new ClientInfo(clienSocket);
        String clientName = AcceptClient(clientInfo);
        clientMap.put(clientName, clientInfo);

        gui.clientNameModel.addElement(clientName);

        Thread newThread = new Thread(new ClientThread(clientName));
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

  class NavBtnHandler implements java.awt.event.ActionListener{
    @Override
    public void actionPerformed(ActionEvent e) {
      String clientName = gui.getCurrClientName();
      String nav = gui.getCurrNav();
      if(nav == null){
        return;
      }
      String cmd = String.format("%s&&%s",Protocol.SV_CMD_FILELIST,nav);
      clientMap.get(clientName).sendQueue.add(cmd);
    }
    
  }

  class ClientThread extends ClientHandleThread{
    public ClientThread(String nameString) {
      super(nameString);
    }
    @Override
    public void Initialize() {
     this.area = gui.getLogTextArea();
     this.pathList = gui.getFileNameModel();
    }
    @Override
    public ClientInfo getClientInfo() {
      return clientMap.get(this.name);
    }
    
  }
}