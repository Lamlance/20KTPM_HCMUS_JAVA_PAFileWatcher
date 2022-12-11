package Server;

import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ServerGui {
  private JFrame frame;
  private JTextArea logText = new JTextArea();
  private DefaultListModel<String> fileNameModel = new DefaultListModel<String>();
  private JList<String> fileNameList = new JList<String>(fileNameModel);

  ServerGui(){
    JFrame.setDefaultLookAndFeelDecorated(true);
    this.frame = new JFrame("File watcher server");
    this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    this.logText.append("SHIT \n");
    this.frame.add(this.logText,BorderLayout.CENTER);
    this.frame.add( new JScrollPane(this.fileNameList),BorderLayout.WEST);

    this.frame.setSize(800, 600);
    this.frame.setVisible(true);
  }
  public JTextArea getLogTextArea(){
    return this.logText;
  }
  public DefaultListModel<String> getFileNameModel(){
    return this.fileNameModel;
  }
}
