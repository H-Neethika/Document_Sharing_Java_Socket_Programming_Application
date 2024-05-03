import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.border.EmptyBorder;
import java.util.HashMap;
import java.util.Map;

public class Client {
  public static void main(String[] args) {
    final File[] fileToSend = new File[1];

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
    jlTitle.setBorder(new EmptyBorder(20,0,10,0));
    jlTitle.setHorizontalAlignment(SwingConstants.CENTER);
    backgroundPanel.add(jlTitle, BorderLayout.NORTH);

    JPanel contentPanel = new JPanel(new GridBagLayout());
    contentPanel.setOpaque(false);

    JLabel jlFileName = new JLabel("Choose a file to send");
    jlFileName.setFont(new Font("Arial", Font.BOLD, 25));
    jlFileName.setForeground(new Color(36, 26, 97));
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.gridwidth = 2;
    gbc.anchor = GridBagConstraints.CENTER;
    contentPanel.add(jlFileName, gbc);

    JButton jbChooseFile = new JButton("<html><font color='white'>Choose File</font></html>");
    jbChooseFile.setFont(new Font("Arial", Font.BOLD, 20));
    jbChooseFile.setBackground(new Color(96, 82, 157)); // Set background color
    jbChooseFile.setForeground(Color.WHITE); // Set text color
    gbc.gridx = 0;
    gbc.gridy = 8;
    gbc.gridwidth = 1;
    gbc.insets = new Insets(450, 0, 0, 10);
    contentPanel.add(jbChooseFile, gbc);

    JButton jbSendFile = new JButton("<html><font color='white'>Send File</font></html>");
    jbSendFile.setFont(new Font("Arial", Font.BOLD, 20));
    jbSendFile.setBackground(new Color(255, 144, 141)); // Set background color
    jbSendFile.setForeground(Color.WHITE); // Set text color
    gbc.gridx = 1;
    gbc.gridy = 8;
    gbc.gridwidth = 1;
    gbc.insets = new Insets(450, 10, 0, 0);
    contentPanel.add(jbSendFile, gbc);

    jbChooseFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser jFileChooser = new JFileChooser();
        jFileChooser.setDialogTitle("Choose a file to send .");

        if (jFileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
          fileToSend[0] = jFileChooser.getSelectedFile();
          jlFileName.setText(fileToSend[0].getName());
        }

      }
    });
    jbSendFile.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (fileToSend[0] == null) {
          jlFileName.setText("Please choose a file first.");
        } else {
          try {
            FileInputStream fileInputStream = new FileInputStream(fileToSend[0].getAbsolutePath());
            Socket socket = new Socket("192.168.56.1", 1234);

            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            String fileName = fileToSend[0].getName();
            byte[] fileNameBytes = fileName.getBytes();

            byte[] fileContentBytes = new byte[(int) fileToSend[0].length()];
            fileInputStream.read(fileContentBytes);

            dataOutputStream.writeInt(fileNameBytes.length);
            dataOutputStream.write(fileNameBytes);

            dataOutputStream.writeInt(fileContentBytes.length);
            dataOutputStream.write(fileContentBytes);
          } catch (IOException error) {
            error.printStackTrace();
          }
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

  private static Icon getFileIcon(String fileName) {
    // You can map file extensions to specific icons
    Map<String, Icon> fileIcons = new HashMap<>();
    fileIcons.put("pdf", new ImageIcon("Images/pdf.png"));
    fileIcons.put("txt", new ImageIcon("Images/text.png"));
    fileIcons.put("jpg", new ImageIcon("Images/jpg-file.png"));
    fileIcons.put("png", new ImageIcon("Images/png-file.png"));
    fileIcons.put("docx", new ImageIcon("Images/docx.png"));
    fileIcons.put("pptx", new ImageIcon("Images/pptx.png"));
    fileIcons.put("xlsx", new ImageIcon("Images/xlsx.png"));
    // Default icon if none is found
    Icon defaultIcon = new ImageIcon("Images/folder.png");
    // Add more file types as needed



    // Get file extension
    String extension = getFileExtension(fileName).toLowerCase();

    // Return the corresponding icon, or the default if none found
    return fileIcons.getOrDefault(extension, defaultIcon);
  }

  private static String getFileExtension(String fileName) {
    int i = fileName.lastIndexOf(".");
    if (i > 0) {
      return fileName.substring(i + 1);
    } else {
      return ""; // No extension found
    }
  }
}
