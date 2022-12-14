package Server.ClientHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import Server.ClientInfo.ClientInfo;
import Protocol.Protocol;

public class ClientHandleThread implements Runnable {
  ClientInfo client = null;
  String name = null;
  JTextArea area = null;
  DefaultListModel<String> pathList;

  private Boolean isAlive = true;
  private LinkedList<String> watchMsgStack = new LinkedList<String>();
  private Map<Integer,Thread> watchThreadMap = new HashMap<Integer,Thread>();
  private int threadCounter = 0;

  public ClientHandleThread(ClientInfo info, String name, JTextArea area, DefaultListModel<String> pathList) {
    this.client = info;
    this.name = name;
    this.area = area;
    this.pathList = pathList;
  }

  private void HandleReadFileList() {
    ArrayList<String> fileNames = new ArrayList<>();
    try {
      do {
        String name = client.waitForString();
        if (name.equalsIgnoreCase("END&&END")) {
          break;
        }
        fileNames.add(name);
      } while (true);
    } catch (Exception e) {
    }
    SwingUtilities.invokeLater(new UpdateStringList(fileNames.toArray(new String[0])));
  }

  private void HandleWatchFile(){
    this.threadCounter += 1;
    Thread newWatch = new Thread(new WatchFile(this.threadCounter));
    this.watchThreadMap.put(this.threadCounter, newWatch);
    newWatch.start();
  }

  public class UpdateStringList implements Runnable {
    String[] nameList;

    UpdateStringList(String[] nameList) {
      this.nameList = nameList;
    }

    @Override
    public void run() {
      pathList.clear();
      pathList.addElement("../");
      pathList.addElement("./");
      for (String fileName : this.nameList) {
        pathList.addElement(fileName);
      }
    }
  }

  public class UpdateLogArea implements Runnable{
    private String msg;
    UpdateLogArea(String watchMsg){this.msg = watchMsg;}
    @Override
    public void run() {
      area.append(String.format("%s \n", msg));
    }
    
  }

  public class WaitAndSendMsg implements Runnable {
    @Override
    public void run() {
      do {
        if (client.sendQueue.size() > 0 && !client.isInReading) {
          String msg = client.sendQueue.pop();
          try {
            client.sendString(msg);
            client.curCmd = msg;
            client.isInReading = true;
          } catch (IOException e) {}
        }
      } while (isAlive);
    }
  }

  public class WaitAndReadMsg implements Runnable {
    @Override
    public void run() {
      do {
        isAlive = !client.socket.isClosed();
        if (!client.isInReading) {
          continue;
        }
        String cmd = client.curCmd.split("&&")[0];

        switch (cmd) {
          case Protocol.SV_CMD_FILELIST:
            client.isInReading = true;
            HandleReadFileList();
            client.isInReading = false;
            break;
          case Protocol.SV_START_WATCH:
            HandleWatchFile();
            break;
          default:
            break;
        }

      } while (isAlive);
    }
  }

  private class WatchFile implements Runnable{
    private int watchId;
    WatchFile(int id){this.watchId = id;}
    @Override
    public void run() {
      do {
        if(!client.isInReading){
          try {
            String watchUpdate = client.waitForString();
            SwingUtilities.invokeLater(new UpdateLogArea(watchUpdate));
          } catch (IOException e) {e.printStackTrace();}
        }
      } while (isAlive);
    }
    
  }

  @Override
  public void run() {
    Thread sendThread = new Thread(new WaitAndSendMsg());
    Thread readThread = new Thread(new WaitAndReadMsg());

    sendThread.start();
    readThread.start();
  }
}
