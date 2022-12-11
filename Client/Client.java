package Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Client {
  public static void main(String[] args) {
    Client newClient =  new Client();
  }
  Client() {
    try (
        Socket s = new Socket("localhost", 9000);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
      do {

        writer.write("LAM");
        writer.newLine();
        writer.flush();

        String myName = reader.readLine();
        System.out.println(myName);

        writer.write("$$PATHLIST");
        writer.newLine();
        writer.flush();

        String[] fileName = this.getAllFileName("./");
        for (String name : fileName) {
          writer.write(name);
          writer.newLine();
          writer.flush();
        }

        writer.write("$$END");
        writer.newLine();
        writer.flush();

      } while (true);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String[] getAllFileName(String path) {
    File file = new File(path);
    String[] names = file.list();
    return names;
  }
}
