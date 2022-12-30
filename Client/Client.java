package Client;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.LinkedList;
import Protocol.Protocol;

public class Client {
  BufferedWriter writer;
  BufferedReader reader;
  Path currDir = FileSystems.getDefault().getPath("").toAbsolutePath();
  LinkedList<String> cmdQueue = new LinkedList<String>();

  String myName = "";

  Socket mySocket;
  Boolean isInSending = false;

  ArrayList<FileInfo> watchings = new ArrayList<FileInfo>();
  LinkedList<String> msgQueue = new LinkedList<String>();

  private Thread listenThread = new Thread(new ListenThread());
  private ClientGui gui;

  int errCount = 0;

  public static void main(String[] args) {
    Client newClient = new Client();
  }

  Client() {
    this.gui = new ClientGui();
    gui.SetConnectionBtn(new ConnectBtnHandel());

  }

  private class ConnectBtnHandel implements java.awt.event.ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {

      listenThread.start();

    }
  }

  private class ListenThread implements Runnable {
    @Override
    public void run() {
      String address = gui.getAddress();
      int port = gui.getPortNumber();
      String name = gui.getName();

      try (
          Socket s = new Socket(address, port);
          BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
          BufferedReader buffReader = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

        writer = buffWriter;
        reader = buffReader;
        mySocket = s;


        gui.SetStatus(true);
        writeString(name);

        myName = waitAndRead();
        System.out.println(myName);

        Thread watchThread = new Thread(new FileChangeWatcher());
        watchThread.start();

        while (mySocket.isConnected() && errCount <= 10) {
          try {
            if (!isInSending) {
              System.out.println("Waiting for msg");
              String msg = waitAndRead();
              isInSending = true;
              System.out.println(msg);
              cmdQueue.add(msg);
              HandleCmd();
            }
          } catch (IOException e) {
            // e.printStackTrace();
            errCount += 1;
          }
        }
        System.out.println("Closing socket");
        mySocket.close();
      } catch (IOException e) {
        // e.printStackTrace();
        errCount += 1;
      }
      gui.SetStatus(false);
      errCount = 0;
    }

  }

  private void writeString(String msg) throws IOException {
    this.writer.write(msg);
    this.writer.newLine();
    this.writer.flush();
  }

  private String waitAndRead() throws IOException {
    String msg = this.reader.readLine();
    return msg;
  }

  private String[] getAllFileName() {
    File file = new File(this.currDir.toString());
    if (file.list() == null) {
      return (new String[0]);
    }
    String[] names = file.list();
    return names;
  }

  private void changePath(String path) {
    if (this.currDir.toFile().isFile()) {
      return;
    }

    if (!path.equalsIgnoreCase("./")) {
      this.currDir = this.currDir.resolve(path);
    }
    System.out.println("Path is now " + this.currDir.toString());
  }

  private void handelFileList(String path) {
    changePath(path);
    try {
      String[] file = this.getAllFileName();
      System.out.println(file.length);
      if (file.length != 0) {
        String fileListMsg = String.join(",", file);
        this.writeString(String.format("%s&&%s", Protocol.SV_CMD_FILELIST, fileListMsg));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void handelFileWatch(String path) {
    changePath(path);
    File file = this.currDir.toFile();
    if (!file.exists()) {
      return;
    }
    try {
      this.watchings.add(new FileInfo(file));
    } catch (Exception e) {}

    this.currDir = FileSystems.getDefault().getPath("").toAbsolutePath();
    handelFileList("./");
  }

  public void HandleCmd() {

    if (cmdQueue.size() > 0 && isInSending) {
      String cmdMsg = cmdQueue.pop();
      String cmd = cmdMsg.split("&&")[0];
      switch (cmd) {
        case Protocol.SV_CMD_FILELIST: {
          handelFileList(cmdMsg.split("&&")[1]);
          break;
        }
        case Protocol.SV_START_WATCH: {
          handelFileWatch(cmdMsg.split("&&")[1]);
          break;
        }
        default:
          break;
      }
      isInSending = false;
    }
  }

  public class FileChangeWatcher implements Runnable {
    @Override
    public void run() {
      while (mySocket.isConnected() && errCount <= 10) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
          errCount += 1;
        }

        for (FileInfo file : watchings) {
          String msg = file.makeMsg();
          if (msg != null) {
            msgQueue.add(msg);
            System.out.println(file.makeMsg());
          }
        }
        if (msgQueue.size() > 0 && !isInSending) {
          String msg = String.join(",", msgQueue);
          try {
            writeString(String.format("%s&&%s", Protocol.CL_EVENT_MSG, msg));
            msgQueue.clear();
          } catch (IOException e) {
            e.printStackTrace();
            errCount += 1;
          }
        }
      }
    }

  }

  private class FileInfo {
    private WatchService ws = null;
    private WatchKey wk = null;
    private File file;
    private Path myPath = null;
    private long prevModify;

    public FileInfo(File myFile) throws IOException {
      this.file = myFile;

      if(file.isDirectory()){
        this.ws = FileSystems.getDefault().newWatchService();
        this.myPath = FileSystems.getDefault().getPath(myFile.getAbsolutePath()) ;
        this.wk = this.myPath.register(ws, 
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY);
      }
      
      this.prevModify = this.file.lastModified();
    }

    public boolean hasChange() {
      if (this.file.lastModified() != this.prevModify) {
        this.prevModify = this.file.lastModified();
        return true;
      }
      return false;

    }

    public String makeMsg() {
      if(this.file.isDirectory()){
        for(WatchEvent<?> event : this.wk.pollEvents()){
          String eventMsg = ((Path) event.context()) + " " + event.kind().toString() ;
          if(!eventMsg.isEmpty() && !eventMsg.isBlank()){
            msgQueue.add(String.format("%s,%s", eventMsg , this.prevModify));
          }
        }
        this.prevModify = file.lastModified();
        return null;
      }
      if(hasChange()){
        msgQueue.add(String.format("%s,%s", this.file.getName() , this.prevModify));
        return String.format("%s,%s", this.file.getName() , this.prevModify);
      }
      return null;
    }
  }
}
