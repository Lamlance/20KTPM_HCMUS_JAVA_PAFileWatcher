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
  private int reCount = 0;

  public ClientHandleThread(String nameString) {
    this.name = nameString;
  }

  public abstract void DisconnectHandel();
  public abstract ClientInfo getClientInfo();
  public abstract void LogEvent(String msg);
  public abstract void UpdateNavList(String[] files);

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
      UpdateNavList(nameArr);
      // SwingUtilities.invokeAndWait(new UpdateStringList(nameArr));
    } catch (Exception e) {
    }
  }

  private void HandleWatchUpdate(String updateLog) {
    try {
      // String[] updateMsgs = updateLog.split(",");
      LogEvent(updateLog);
      // SwingUtilities.invokeAndWait(new UpdateLogArea(updateMsgs));
    } catch (Exception e) {
    }

  }


  public class WaitAndHandelMsg implements Runnable {
    @Override
    public void run() {
      while (getClientInfo().socket.isConnected() && reCount < 10) {
        if ( getClientInfo().msgQueue.size() > 0) {
          String cmdMsg = getClientInfo().msgQueue.pop();
          
          String[] msgs = cmdMsg.split("&&");
          if(msgs.length < 2){
            continue;
          }


          switch (msgs[0]) {
            case Protocol.SV_CMD_FILELIST: {
              HandleReadFileList(msgs[1]);
              break;
            }
            case Protocol.CL_EVENT_MSG: {
              HandleWatchUpdate(msgs[1]);
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
