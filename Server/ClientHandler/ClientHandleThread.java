package Server.ClientHandler;

import java.io.IOException;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import Server.ClientInfo.ClientInfo;
import Protocol.Protocol;

public abstract class ClientHandleThread implements Runnable {
  protected String name = null;
  protected JTextArea area = null;
  protected DefaultListModel<String> pathList = null;

  private LinkedList<String> watchMsgStack = new LinkedList<String>();
  private LinkedList<String> cmdStack = new LinkedList<String>();

  private Map<Integer, Thread> watchThreadMap = new HashMap<Integer, Thread>();
  private int threadCounter = 0;

  public ClientHandleThread(String nameString){
    this.name = nameString;
    Initialize();
  } 
  public abstract void Initialize();
  public abstract ClientInfo getClientInfo();

  @Override
  public void run() {
    getClientInfo().isInReading = false;

    Thread readThread = new Thread(new WaitAndReadMsg());
    readThread.start();

    while (this.getClientInfo().socket.isConnected()) {
      if (getClientInfo().sendQueue.size() > 0 && !getClientInfo().isInReading) {
        String msg = getClientInfo().sendQueue.pop();
        try {
          getClientInfo().sendString(msg);
          getClientInfo().isInReading = true;
          this.cmdStack.add(msg);
        } catch (IOException e){}
      }
    }
    ;

  }

  private void HandleReadFileList() {
    try {
      String namesString = getClientInfo().waitForString();
      String[] nameArr = namesString.split(",");
      SwingUtilities.invokeAndWait(new UpdateStringList(nameArr));
    } catch (Exception e) {}
  }

  private void HandleWatchFile() {
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

  public class UpdateLogArea implements Runnable {
    private String msg;

    UpdateLogArea(String watchMsg) {
      this.msg = watchMsg;
    }

    @Override
    public void run() {
      area.append(String.format("%s \n", msg));
    }

  }

  public class WaitAndReadMsg implements Runnable {
    @Override
    public void run() {
      while (getClientInfo().socket.isConnected()) {
        if (cmdStack.size() > 0 && getClientInfo().isInReading) {
          String cmdMsg = cmdStack.pop();
          String cmd = cmdMsg.split("&&")[0];
          switch (cmd) {
            case Protocol.SV_CMD_FILELIST:{
              HandleReadFileList();
              break;
            }

            case Protocol.SV_START_WATCH:
              HandleWatchFile();
              break;
            default:
              break;
          }
          getClientInfo().isInReading = false;
        }
      }
    }
  }

  private class WatchFile implements Runnable {
    private int watchId;

    WatchFile(int id) {
      this.watchId = id;
    }

    @Override
    public void run() {
      while (getClientInfo().socket.isConnected()) {
        if (!getClientInfo().isInReading) {
          try {
            String watchUpdate = getClientInfo().waitForString();
            SwingUtilities.invokeLater(new UpdateLogArea(watchUpdate));
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      }
      ;
    }
  }
}
