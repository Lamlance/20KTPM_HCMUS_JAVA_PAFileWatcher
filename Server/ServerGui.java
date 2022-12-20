package Server;

import java.awt.BorderLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ServerGui {
  private JFrame frame;
  private JTextArea logText = new JTextArea();
  private DefaultListModel<String> fileNameModel = new DefaultListModel<String>();
  public JList<String> fileNameList = new JList<String>(fileNameModel);

  public DefaultComboBoxModel<String> clientNameModel = new DefaultComboBoxModel<String>();

  private JComboBox<String> clienBox = new JComboBox<String>(clientNameModel);

  private JButton fileNavBtn = new JButton("NAV");
  private JButton fileWatchBtn = new JButton("WATCH");

  ServerGui(){
    JFrame.setDefaultLookAndFeelDecorated(true);
    this.frame = new JFrame("File watcher server");
    this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // this.logText.append("SHIT \n");
    
    this.frame.add(new JScrollPane(this.logText),BorderLayout.CENTER);
    
    this.fileNameList.setPreferredSize(new java.awt.Dimension(200,500));
    this.frame.add(new JScrollPane(this.fileNameList) ,BorderLayout.WEST);

    JPanel actionPanel = new JPanel(new java.awt.GridLayout(1,3) );
    
    actionPanel.add(this.clienBox);
    actionPanel.add(this.fileNavBtn);
    actionPanel.add(this.fileWatchBtn);
    this.frame.add(actionPanel,BorderLayout.PAGE_START);

    this.frame.setSize(800, 600);
    this.frame.setVisible(true);
  }
  public JTextArea getLogTextArea(){
    return this.logText;
  }
  public DefaultListModel<String> getFileNameModel(){
    return this.fileNameModel;
  }
  public JList<String> getFileNameJList(){
    return this.fileNameList;
  }
  public String getCurrClientName(){
    return ((String) this.clienBox.getSelectedItem());
  }
  public String getCurrNav(){
    return this.fileNameList.getSelectedValue();
  }
  public void SetNavBtnHandler(java.awt.event.ActionListener ls){
    this.fileNavBtn.addActionListener(ls);
  }
  public void SetWatchBtnHandler(java.awt.event.ActionListener ls){
    this.fileWatchBtn.addActionListener(ls);
  }
  public void SetClientBoxHandler(java.awt.event.ActionListener ls){
    this.clienBox.addActionListener(ls);
  }
}
