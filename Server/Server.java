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
      this.gui.SetWatchBtnHandler(new WatchBtnHandler());
      this.gui.SetClientBoxHandler(new ClientBoxHandler());

      do {
        System.out.println("Server is listening");
        Socket clienSocket = server.accept();
        ClientInfo clientInfo = new ClientInfo(clienSocket);
        String clientName = AcceptClient(clientInfo);
        clientMap.put(clientName, clientInfo);

        gui.clientNameModel.addElement(clientName);
        gui.logToTable(clientName,"Connected","");

        Thread newThread = new Thread(new ClientThread(clientName));
        newThread.start();

      } while (true);
    } catch (Exception e) {

    }

  }

  private String AcceptClient(ClientInfo clientInfo) {
    try {
      String name = clientInfo.waitForString();
      String newName = String.format("%s:%d", name,clientInfo.socket.getPort());
      clientInfo.sendString(newName);
      
      return newName;

    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
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

  class WatchBtnHandler implements java.awt.event.ActionListener{
    @Override
    public void actionPerformed(ActionEvent e) { 
      String clientName = gui.getCurrClientName();
      String nav = gui.getCurrNav();
      if(nav == null){
        return;
      }
      String cmd = String.format("%s&&%s",Protocol.SV_START_WATCH,nav);
      clientMap.get(clientName).sendQueue.add(cmd);
    }    
  }

  class ClientBoxHandler implements java.awt.event.ActionListener{
    @Override
    public void actionPerformed(ActionEvent e) {
      String clientName = gui.getCurrClientName();
      if(clientName == null){
        return;
      }
      clientMap.get(clientName).sendQueue.add(String.format("%s&&./",Protocol.SV_CMD_FILELIST));
    }
    
  }

  class ClientThread extends ClientHandleThread{
    public ClientThread(String nameString) {
      super(nameString);
    }
    @Override
    public ClientInfo getClientInfo() {
      return clientMap.get(this.name);
    }
    @Override
    public void DisconnectHandel() {
      clientMap.remove(this.name);
      gui.clientNameModel.removeElement(this.name);
      gui.fileNameList.clearSelection();
      gui.getFileNameModel().clear();
      System.out.println("Client disconnect");
      gui.logToTable(this.name,"Disconnected","");
    }
    @Override
    public void LogEvent(String msg) {
      String[] datas = msg.split(",");
      if(datas.length >= 2){
        gui.logToTable(this.name,datas[0],datas[1]);
      }
      return;
    }
    @Override
    public void UpdateNavList(String[] files) {
      gui.updateNavList(files);
    }
    
  }

}