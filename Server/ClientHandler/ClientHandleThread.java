package Server.ClientHandler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import Server.ClientInfo.ClientInfo;

public class ClientHandleThread implements Runnable {
  ClientInfo client = null;
  String name = null;
  JTextArea area = null;
  DefaultListModel<String> pathList;

  public ClientHandleThread(ClientInfo info, String name, JTextArea area, DefaultListModel<String> pathList) {
    this.client = info;
    this.name = name;
    this.area = area;
    this.pathList = pathList;
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

      for (String fileName : this.nameList) {
        pathList.addElement(fileName);
      }
    }

  }

  @Override
  public void run() {
    try {
      do {
        System.out.println("Waiting for input");
        String readString = client.waitForString();
        System.out.println(readString);

        if (readString.equalsIgnoreCase("$$PATHLIST")) {
          ArrayList<String> fileNames = new ArrayList<>();
          do {
            String name = client.waitForString();
            if (name.equalsIgnoreCase("$$END")) {
              break;
            }
            System.out.println(name);
            fileNames.add(name);
          } while (true);
          SwingUtilities.invokeLater(new UpdateStringList(fileNames.toArray(new String[0])));
        }

      } while (true);

    } catch (Exception e) {

    }
  }
}
