package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import Protocol.Protocol;

public class Client {
  BufferedWriter writer;
  BufferedReader reader;
  Path currDir = Paths.get(".");
  LinkedList<String> cmdQueue = new LinkedList<String>();

  Socket mySocket;
  Boolean isInSending = false;

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

      String name = this.waitAndRead();
      System.out.println(name);


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

  private void hadnleFileList(String path) {
    if (path.equalsIgnoreCase("../")) {
      this.currDir = this.currDir.getParent();
    }
    if (!path.equalsIgnoreCase("./")) {
      this.currDir = this.currDir.resolve(path);
    }

    String[] file = this.getAllFileName();
    String fileListMsg = String.join(",", file);
    try {
      this.writeString(fileListMsg);
    } catch (IOException e) {e.printStackTrace();}
  }

  public void HandleCmd() {
    if (cmdQueue.size() > 0 && isInSending) {
      String cmdMsg = cmdQueue.pop();
      System.out.println("Handling " + cmdMsg);
      String cmd = cmdMsg.split("&&")[0];
      switch (cmd) {
        case Protocol.SV_CMD_FILELIST:
          System.out.println("Handle FileList");
          hadnleFileList(cmdMsg.split("&&")[1]);
          System.out.println("Finished Handle FileList");
          break;
        default:
          break;
      }
      isInSending = false;
    }
  }
}
