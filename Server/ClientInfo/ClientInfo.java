package Server.ClientInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientInfo {
  public Socket socket;
  public BufferedReader reader;
  public BufferedWriter writer;

  public ClientInfo(Socket client) throws IOException {
    this.socket = client;
    this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
    this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
  }

  public String waitForString() throws IOException {
    return this.reader.readLine();
  }

  public void sendString(String msg) throws IOException {
    this.writer.write(msg);
    this.writer.newLine();
    this.writer.flush();
  }
}
