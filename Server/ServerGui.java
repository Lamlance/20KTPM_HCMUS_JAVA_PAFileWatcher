package Server;

import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class ServerGui {
  private JFrame frame;
  private final String[] logTableCol = { "Client", "File name", "Date" };

  // private JTextArea logText = new JTextArea();
  private DefaultTableModel logTableModel = new DefaultTableModel(logTableCol, 0);
  private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssZ");

  private DefaultListModel<String> fileNameModel = new DefaultListModel<String>();
  public JList<String> fileNameList = new JList<String>(fileNameModel);

  public DefaultComboBoxModel<String> clientNameModel = new DefaultComboBoxModel<String>();

  private JComboBox<String> clienBox = new JComboBox<String>(clientNameModel);

  private JButton fileNavBtn = new JButton("NAV");
  private JButton fileWatchBtn = new JButton("WATCH");

  ServerGui() {
    JFrame.setDefaultLookAndFeelDecorated(true);
    this.frame = new JFrame("File watcher server");
    this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // this.logText.append("SHIT \n");

    this.frame.add(new JScrollPane(new JTable(this.logTableModel)), BorderLayout.CENTER);

    this.fileNameList.setPreferredSize(new java.awt.Dimension(200, 500));
    this.frame.add(new JScrollPane(this.fileNameList), BorderLayout.WEST);

    JPanel actionPanel = new JPanel(new java.awt.GridLayout(1, 3));

    actionPanel.add(this.clienBox);
    actionPanel.add(this.fileNavBtn);
    actionPanel.add(this.fileWatchBtn);
    this.frame.add(actionPanel, BorderLayout.PAGE_START);

    this.frame.setSize(800, 600);
    this.frame.setVisible(true);
  }

  public DefaultListModel<String> getFileNameModel() {
    return this.fileNameModel;
  }

  public JList<String> getFileNameJList() {
    return this.fileNameList;
  }

  public String getCurrClientName() {
    return ((String) this.clienBox.getSelectedItem());
  }

  public String getCurrNav() {
    return this.fileNameList.getSelectedValue();
  }

  public void SetNavBtnHandler(java.awt.event.ActionListener ls) {
    this.fileNavBtn.addActionListener(ls);
  }

  public void SetWatchBtnHandler(java.awt.event.ActionListener ls) {
    this.fileWatchBtn.addActionListener(ls);
  }

  public void SetClientBoxHandler(java.awt.event.ActionListener ls) {
    this.clienBox.addActionListener(ls);
  }

  private class UpdateLogThread implements Runnable {
    String name;
    String file;
    String date;

    UpdateLogThread(String name, String fileName, String dateString) {
      super();
      this.name = name;
      this.file = fileName;
      this.date = dateString;
    }

    @Override
    public void run() {
      try {
        long dateNum = Long.parseLong(date);
        Date dateObj = new Date(dateNum);
        logTableModel.addRow(new String[] { name, file, simpleDateFormat.format(dateObj) });
      } catch (Exception e) {
        logTableModel.addRow(new String[] { name, file, date });
      }
    }
  }

  private class UpdateNavThread implements Runnable {
    String[] names;

    UpdateNavThread(String[] names) {
      super();
      this.names = names;
    }

    @Override
    public void run() {
      fileNameList.clearSelection();
      fileNameModel.clear();
      fileNameModel.addElement("./");
      fileNameModel.addElement("../");
      for (String name : this.names) {
        fileNameModel.addElement(name);
      }
    }

  }

  public void logToTable(String name, String fileName, String dateString) {
    SwingUtilities.invokeLater(new UpdateLogThread(name, fileName, dateString));
  }

  public void updateNavList(String[] files) {
    SwingUtilities.invokeLater(new UpdateNavThread(files));
  }
}
