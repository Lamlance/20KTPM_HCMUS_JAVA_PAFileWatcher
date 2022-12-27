package Client;

import java.awt.AWTEvent;
import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ClientGui {
  private JFrame frame;
  
  private JTextField adressField = new JTextField("localhost");
  private JTextField portField = new JTextField("9000");
  private JTextField nameField = new JTextField("LAM");

  private JButton connecBtn = new JButton("Connect");

  ClientGui(){

    JFrame.setDefaultLookAndFeelDecorated(true);
    this.frame = new JFrame("File watcher client");
    this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    JPanel actionPanel = new JPanel(new java.awt.GridLayout(1,4));

    JPanel addressPanel = new JPanel(new BorderLayout());
    addressPanel.add(new JLabel("URL: "),BorderLayout.WEST);
    addressPanel.add(this.adressField,BorderLayout.CENTER);

    JPanel portPanel = new JPanel(new BorderLayout());
    portPanel.add(new JLabel("PORT: "),BorderLayout.WEST);
    portPanel.add(this.portField,BorderLayout.CENTER);

    JPanel namePanel = new JPanel(new BorderLayout());
    namePanel.add(new JLabel("User name: "),BorderLayout.WEST);
    namePanel.add(this.nameField,BorderLayout.CENTER);

    actionPanel.add(addressPanel);
    actionPanel.add(portPanel);
    actionPanel.add(namePanel);

    this.frame.add(actionPanel,BorderLayout.PAGE_START);
    
    this.frame.add(this.connecBtn, BorderLayout.PAGE_END);

    // this.frame.setSize(600, 200);
    this.frame.pack();
    this.frame.setVisible(true);
  }

  public String getAddress(){
    return this.adressField.getText();
  }
  public int getPortNumber(){
    try {
      return Integer.parseInt(this.portField.getText());
    } catch (Exception e) {
      return 9000;
    }
  }

  public void disableActionTab(){
    this.adressField.setEditable(false);
    this.portField.setEditable(false);
    this.connecBtn.setEnabled(false);
  }

  public void SetConnectionBtn(java.awt.event.ActionListener ls) {
    connecBtn.addActionListener(ls);
  }

  public void CloseFrame(){
    frame.dispose();
  }
}
