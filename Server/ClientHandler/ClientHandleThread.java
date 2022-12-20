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

  private int reCount = 0;

  public ClientHandleThread(String nameString) {
    this.name = nameString;
    Initialize();
  }

  public abstract void Initialize();
  public abstract void DisconnectHandel();
  public abstract ClientInfo getClientInfo();

  @Override
  public void run() {
    getClientInfo().isInReading = false;

    Thread handelThread = new Thread(new WaitAndHandelMsg());
    Thread readThread = new Thread(new WatchForEventThread());
    readThread.start();
    handelThread.start();

    while (this.getClientInfo().socket.isConnected() && reCount < 10) {
      if (getClientInfo().sendQueue.size() > 0) {
        String msg = getClientInfo().sendQueue.pop();
        try {
          getClientInfo().sendString(msg);
        } catch (IOException e) {
          reCount += 1;
        }
      }
    }
    System.out.println("Client disconnect");
    DisconnectHandel();
  }

  private void HandleReadFileList(String namesString) {
    try {
      String[] nameArr = namesString.split(",");
      SwingUtilities.invokeAndWait(new UpdateStringList(nameArr));
    } catch (Exception e) {
    }
  }

  private void HandleWatchUpdate(String updateLog) {
    try {
      String[] updateMsgs = updateLog.split(",");
      SwingUtilities.invokeAndWait(new UpdateLogArea(updateMsgs));
    } catch (Exception e) {
    }

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
    private String[] msg;

    UpdateLogArea(String[] watchMsg) {
      this.msg = watchMsg;
    }

    @Override
    public void run() {
      for (String string : this.msg) {
        area.append(String.format("%s \n", string));
      }
    }

  }

  public class WaitAndHandelMsg implements Runnable {
    @Override
    public void run() {
      while (getClientInfo().socket.isConnected() && reCount < 10) {
        if ( getClientInfo().msgQueue.size() > 0) {
          String cmdMsg = getClientInfo().msgQueue.pop();
          String cmd = cmdMsg.split("&&")[0];

          switch (cmd) {
            case Protocol.SV_CMD_FILELIST: {
              HandleReadFileList(cmdMsg.split("&&")[1]);
              break;
            }
            case Protocol.CL_EVENT_MSG: {
              HandleWatchUpdate(cmdMsg.split("&&")[1]);
              break;
            }
            default:
              break;
          }
        }
      }
    }
  }

  public class WatchForEventThread implements Runnable {
    @Override
    public void run() {
      while (getClientInfo().socket.isConnected() && reCount < 10) {
        try {
          String eventMsg = getClientInfo().waitForString();
          getClientInfo().msgQueue.add(eventMsg);
        } catch (IOException e) {
          reCount += 1;
        }
      }

    }

  }
}
