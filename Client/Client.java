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
  Boolean isRunningCmd = false;

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

        this.writeString("LAM");

        String name = this.waitAndRead();
        System.out.println(name);
        
        Thread listenThread = new Thread(new WaitForCmdThread());
        listenThread.start();

      do {
        String msg = this.waitAndRead();
        System.out.println(msg);
        cmdQueue.add(msg);
      } while (true);

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
    System.out.println("Handle FileList");

    if (path.equalsIgnoreCase("../")) {
      this.currDir = this.currDir.getParent();
    }
    if (!path.equalsIgnoreCase("./")) {
      this.currDir = this.currDir.resolve(path);
    }

    String[] file = this.getAllFileName();
    try {
      for (String nameString : file) {
        this.writeString(nameString);
        System.out.println(nameString);
      }
      this.writeString("END&&END");
    } catch (IOException e) {
      e.printStackTrace();
    }
    isRunningCmd = false;
  }

  private class WaitForCmdThread implements Runnable {
    @Override
    public void run() {
      do {
        if (isRunningCmd || (cmdQueue.size() < 1)) {
          continue;
        }
        String cmdMsg = cmdQueue.pop();
        String cmd = cmdMsg.split("&&")[0];
        switch (cmd) {
          case Protocol.SV_CMD_FILELIST:
            hadnleFileList(cmdMsg.split("&&")[1]);
            isRunningCmd = true;
            break;

          default:
            break;
        }
      } while (true);
    }

  }
}
