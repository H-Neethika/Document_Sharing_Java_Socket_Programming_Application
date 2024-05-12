import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import javax.swing.border.EmptyBorder;
import java.util.ArrayList;
import java.util.List;

public class Client {
  private static List<File> selectedFiles = new ArrayList<>();

  public static void main(String[] args) {
    JFrame jFrame = new JFrame("DSS Client");
    jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Creating a JPanel with a background image
    JPanel backgroundPanel = new JPanel(new BorderLayout()) {
      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ImageIcon backgroundImage = new ImageIcon("Images/SenderBackground.jpg"); // Change the file path accordingly
        g.drawImage(backgroundImage.getImage(), 0, 0, getWidth(), getHeight(), this);
      }

      @Override
      public Dimension getPreferredSize() {
        return new Dimension(1000, 650); // Adjust the dimensions here as needed
      }
    };

    JLabel jlTitle = new JLabel("Effortlessly share files securely. Experience seamless sharing now!");
    jlTitle.setFont(new Font("Arial", Font.BOLD, 28));
    jlTitle.setForeground(new Color(96, 82, 157));
    jlTitle.setBorder(new EmptyBorder(20, 0, 10, 0));
    jlTitle.setHorizontalAlignment(SwingConstants.CENTER);
    backgroundPanel.add(jlTitle, BorderLayout.NORTH);

    JPanel contentPanel = new JPanel(new GridBagLayout());
    contentPanel.setOpaque(false);

    JLabel jlFileName = new JLabel("Choose files to send");
    jlFileName.setFont(new Font("Arial", Font.BOLD, 25));
    jlFileName.setForeground(new Color(36, 26, 97));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    contentPanel.add(jlFileName, gbc);

    JTextArea selectedFilesTextArea = new JTextArea();
    selectedFilesTextArea.setEditable(false);
    selectedFilesTextArea.setLineWrap(true);
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.gridwidth = 2;
    gbc.insets = new Insets(10, 10, 10, 10);
    JScrollPane scrollPane = new JScrollPane(selectedFilesTextArea);
    scrollPane.setPreferredSize(new Dimension(500, 100));
    contentPanel.add(scrollPane, gbc);

    JButton jbChooseFile = new JButton("<html><font color='white'>Choose File</font></html>");
    jbChooseFile.setFont(new Font("Arial", Font.BOLD, 20));
    jbChooseFile.setBackground(new Color(96, 82, 157)); // Set background color
    jbChooseFile.setForeground(Color.WHITE); // Set text color
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    gbc.insets = new Insets(350, 80, 0, 10);
    contentPanel.add(jbChooseFile, gbc);

    JButton jbSendFiles = new JButton("<html><font color='white'>Send Files</font></html>");
    jbSendFiles.setFont(new Font("Arial", Font.BOLD, 20));
    jbSendFiles.setBackground(new Color(255, 144, 141)); // Set background color
    jbSendFiles.setForeground(Color.WHITE); // Set text color
    gbc.gridx = 1;
    gbc.gridy = 2;
    gbc.gridwidth = 1;
    gbc.insets = new Insets(350, 0, 0, 0);
    contentPanel.add(jbSendFiles, gbc);

    jbChooseFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setDialogTitle("Choose files to send.");
        jFileChooser.setMultiSelectionEnabled(true); // Enable selecting multiple files

        if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          File[] files = jFileChooser.getSelectedFiles(); // Get selected files
          selectedFiles.clear(); // Clear previous selections
          selectedFilesTextArea.setText(""); // Clear previous file names
          for (File file : files) {
            selectedFiles.add(file);
            selectedFilesTextArea.append(file.getName() + "\n"); // Display selected file names
          }
        }
      }
    });

    jbSendFiles.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (!selectedFiles.isEmpty()) {
          for (File file : selectedFiles) {
            sendFile(file); // Send each selected file
          }
        } else {
          jlFileName.setText("Please choose files to send first.");
        }
      }
    });

    backgroundPanel.add(contentPanel, BorderLayout.CENTER);

    // Set the JPanel as the content pane of the JFrame
    jFrame.setContentPane(backgroundPanel);
    jFrame.pack(); // Pack the frame to adjust its size
    jFrame.setVisible(true);
    jFrame.setLocationRelativeTo(null);
  }

  private static void sendFile(File fileToSend) {
    if (fileToSend == null) {
      return;
    }

    try {
      FileInputStream fileInputStream = new FileInputStream(fileToSend.getAbsolutePath());
      Socket socket = new Socket("localhost", 1234); // Change to server IP and port
      DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

      String fileName = fileToSend.getName();
      byte[] fileNameBytes = fileName.getBytes();

      byte[] fileContentBytes = new byte[(int) fileToSend.length()];
      fileInputStream.read(fileContentBytes);

      dataOutputStream.writeInt(fileNameBytes.length);
      dataOutputStream.write(fileNameBytes);

      dataOutputStream.writeInt(fileContentBytes.length);
      dataOutputStream.write(fileContentBytes);

      // Close resources
      fileInputStream.close();
      dataOutputStream.close();
      socket.close();
    } catch (IOException error) {
      error.printStackTrace();
    }
  }
}
