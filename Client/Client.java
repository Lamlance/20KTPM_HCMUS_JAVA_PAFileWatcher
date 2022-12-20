package Client;

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

  public static void main(String[] args) {
    Client newClient = new Client();
  }

  Client() {
    try (
        Socket s = new Socket("localhost", 9000);
        BufferedWriter buffWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        BufferedReader buffReader = new BufferedReader(new InputStreamReader(s.getInputStream()))) {

      this.writer = buffWriter;
      this.reader = buffReader;
      this.mySocket = s;

      this.writeString("LAM");

      this.myName = this.waitAndRead();
      System.out.println(myName);

      Thread watchThread = new Thread(new FileChangeWatcher());
      watchThread.start();

      while (mySocket.isConnected()) {
        if (!this.isInSending) {
          System.out.println("Waiting for msg");
          String msg = this.waitAndRead();
          this.isInSending = true;
          System.out.println(msg);
          cmdQueue.add(msg);
          HandleCmd();
        }
      }
      ;

    } catch (IOException e) {
      e.printStackTrace();
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
    String[] names = file.list();
    return names;
  }

  private void changePath(String path){
    if (!path.equalsIgnoreCase("./") ) {
      this.currDir = this.currDir.resolve(path);
    }
    System.out.println("Path is now " + this.currDir.toString());
  }

  private void handelFileList(String path) {
    changePath(path);

    String[] file = this.getAllFileName();
    String fileListMsg = String.join(",", file);
    try {
      this.writeString(String.format("%s&&%s",Protocol.SV_CMD_FILELIST,fileListMsg));
    } catch (IOException e) {e.printStackTrace();}
  }

  private void handelFileWatch(String path){
    changePath(path);
    Path filePath = this.currDir;
    File file = new File(filePath.toString());
    if(!file.exists()){
      return;
    }
    this.watchings.add(new FileInfo(file));
  }

  public void HandleCmd() {
    
    if (cmdQueue.size() > 0 && isInSending) {
      String cmdMsg = cmdQueue.pop();
      String cmd = cmdMsg.split("&&")[0];
      switch (cmd) {
        case Protocol.SV_CMD_FILELIST:{
          handelFileList(cmdMsg.split("&&")[1]);
          break;
        }
        case Protocol.SV_START_WATCH:{
          handelFileWatch(cmdMsg.split("&&")[1]);
          break;
        }
        default:
          break;
      }
      isInSending = false;
    }
  }

  public class FileChangeWatcher implements Runnable{
    @Override
    public void run() {
      while(mySocket.isConnected()){
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {e.printStackTrace();}

        for (FileInfo file : watchings) {
          if(file.hasChange()){
            msgQueue.add(file.makeMsg());
            System.out.println(file.makeMsg());
          }
        }
        if(msgQueue.size() > 0 && !isInSending){
          String msg = String.join(",", msgQueue);
          try {
            writeString(String.format("%s&&%s",Protocol.CL_EVENT_MSG,msg));
            msgQueue.clear();
          } catch (IOException e) {e.printStackTrace();}
        }
      }      
    }

  }

  private class FileInfo{
    private File file;
    private long prevModify;

    public FileInfo(File myFile){
      this.file = myFile;
      this.prevModify = this.file.lastModified();
    }
    public boolean hasChange(){
      if(this.file.lastModified() != this.prevModify){
        this.prevModify = this.file.lastModified();
        return true;
      }
      return false;
    }
    public String makeMsg(){
      return String.format("%s: %s has changed", myName,this.file.getName());
    }
  }
}
