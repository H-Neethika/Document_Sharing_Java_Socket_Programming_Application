import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.awt.geom.RoundRectangle2D;

public class Server {
    static ArrayList<MyFile> myFiles = new ArrayList<>();

    static JFrame jFrame;
    static JPanel jPanel;
    static JPanel emptyPanel;
    static JLabel emptyLabel;



    public static void main(String[] args) throws IOException {
        jFrame = new JFrame("DSS Server");
        jFrame.setSize(1000, 650);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setLocationRelativeTo(null);
        jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        Color lightBlue = new Color(173, 216, 230);
        jPanel.setBackground(lightBlue);

        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JLabel jlTitle = new JLabel("Effortlessly receive files securely. Experience seamless file sharing now!");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20, 0, 10, 0));
        jlTitle.setOpaque(true);
        jlTitle.setBackground(new Color(93, 145, 211));
        jlTitle.setHorizontalAlignment(SwingConstants.CENTER);
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        emptyPanel = new JPanel();
        emptyPanel.setLayout(new BorderLayout());
        emptyPanel.setBackground(new Color(167, 199, 231));

        // Text label to be displayed above the image
        JLabel textLabel = new JLabel("Waiting for files...");
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textLabel.setFont(new Font("Arial", Font.BOLD, 16));
        textLabel.setBorder(new EmptyBorder(40, 0, 10, 0));

        // Load the image and check for null to ensure it loaded
        ImageIcon imageIcon = new ImageIcon("Images/placeholder.png"); // Adjust path as needed
        if (imageIcon.getIconWidth() > 0 && imageIcon.getIconHeight() > 0) {
            Image originalImage = imageIcon.getImage();
            Image scaledImage = originalImage.getScaledInstance(
                    550, 400, Image.SCALE_SMOOTH);
            emptyLabel = new JLabel(new ImageIcon(scaledImage));

            emptyPanel.add(textLabel, BorderLayout.NORTH);
            emptyPanel.add(emptyLabel, BorderLayout.CENTER);
        } else {
            System.err.println("Image not found or invalid: placeholder.jpg");
        }

        jFrame.add(jlTitle, BorderLayout.NORTH);
        jFrame.add(jScrollPane, BorderLayout.CENTER);

        updateEmptyState();

        jFrame.setVisible(true);

        ServerSocket serverSocket = new ServerSocket(1234);

        Thread serverThread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    String senderIp = socket.getInetAddress().getHostAddress();  // Get the IP address

                    handleClient(socket);
                } catch (IOException error) {
                    error.printStackTrace();
                }
            }
        });
        serverThread.start();
    }

    private static void handleClient(Socket socket) {
        // Print connection established message
        String senderIp = socket.getInetAddress().getHostAddress();  // Get the IP address
        System.out.println("Connection established with client: " + senderIp);

        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            int fileNameLength = dataInputStream.readInt(); // the length of the file name

            if (fileNameLength > 0) {
                byte[] fileNameBytes = new byte[fileNameLength];
                dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length); //reads the bytes of the file name from the input stream and stores them in the fileNameBytes array.
                String fileName = new String(fileNameBytes);

                int fileContentLength = dataInputStream.readInt();

                if (fileContentLength > 0) {
                    byte[] fileContentBytes = new byte[fileContentLength];
                    dataInputStream.readFully(fileContentBytes, 0, fileContentLength);

                    // Print file details
                    System.out.println("Processing file: " + fileName + " from client: " + senderIp);

                    // Add the file to the GUI
                    addFileRow(fileName, fileContentBytes, senderIp);

                    myFiles.add(new MyFile(Thread.currentThread().getId(), fileName, fileContentBytes, getFileExtension(fileName)));

                    if (myFiles.size() == 1) {
                        updateEmptyState();
                    }
                }
            }
        } catch (IOException error) {
            error.printStackTrace();
        } finally {
            try {
                socket.close();
                // Print when connection is closed
                System.out.println("Connection closed with client: " + senderIp + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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

        // Get file extension
        String extension = getFileExtension(fileName).toLowerCase();

        // Return the corresponding icon, or the default if none found
        return fileIcons.getOrDefault(extension, defaultIcon);
    }

    private static void addFileRow(String fileName, byte[] fileContentBytes, String senderIp) {
        // Custom panel with rounded corners and fixed size
        RoundedPanel fileRowPanel = new RoundedPanel(50); // 10-pixel corner radius
        Color bg = new Color(167, 199, 231);
        fileRowPanel.setBackground(bg); // Background color for differentiation

        Dimension fixedSize = new Dimension(550, 80); // Desired width and height
        fileRowPanel.setPreferredSize(fixedSize);
        fileRowPanel.setMaximumSize(fixedSize); // Ensure it does not grow
        fileRowPanel.setMinimumSize(fixedSize); // Ensure it does not shrink

        // Add borders and padding
        EmptyBorder paddingBorder = new EmptyBorder(10, 10, 10, 10);
        CompoundBorder combinedBorder = new CompoundBorder(new LineBorder(Color.GRAY, 0), paddingBorder);
        fileRowPanel.setBorder(combinedBorder);

        // Add a label for the sender IP address
        JLabel ipLabel = new JLabel("Sender IP: " + senderIp);
        ipLabel.setFont(new Font("Arial", Font.BOLD, 14));

        // Add a label for the file name with an icon
        Icon fileIcon = getFileIcon(fileName);
        JLabel fileNameLabel = new JLabel(fileName, fileIcon, SwingConstants.LEFT);
        fileNameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        fileNameLabel.setIconTextGap(10);

        // Panel to hold the file name horizontally
        JPanel fileNamePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        fileNamePanel.setOpaque(false); // Make it transparent
        fileNamePanel.add(fileNameLabel);

        // Add download button
        JButton downloadButton = new JButton(new ImageIcon("Images/download_icon.png"));
        downloadButton.setBorderPainted(false);
        downloadButton.setContentAreaFilled(false);
        downloadButton.setFocusPainted(false);
        downloadButton.setOpaque(false);
        downloadButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(fileRowPanel, "Do you want to download the file \"" + fileName + "\"?", "Confirm Download", JOptionPane.YES_NO_OPTION);

            if (option == JOptionPane.YES_OPTION) {
                downloadFile(fileName, fileContentBytes);
            }
        });

        // Add components to the custom rounded panel
        fileRowPanel.setLayout(new BorderLayout());
        fileRowPanel.add(ipLabel, BorderLayout.NORTH); // IP label at the top
        fileRowPanel.add(fileNamePanel, BorderLayout.CENTER); // File name in the center

        // Add download button to the right side
        fileRowPanel.add(downloadButton, BorderLayout.EAST); // Download button on the right

        // Add vertical spacing between file rows
        jPanel.add(Box.createVerticalStrut(10), 0); // Add a gap between files
        jPanel.add(fileRowPanel, 1); // Add the custom rounded panel to the main panel

        jPanel.revalidate();
        jPanel.repaint();
    }


    private static void updateEmptyState() {
        if (myFiles.isEmpty()) {
            if (jPanel.getComponentCount() == 0) { // Ensure the emptyPanel is not already there
                jPanel.add(emptyPanel);
            }
        } else {
            jPanel.remove(emptyPanel);
        }
        jPanel.revalidate();
        jPanel.repaint();
    }


    // Custom output stream to redirect System.out
    static class CustomOutputStream extends OutputStream {
        private JTextArea textArea;

        public CustomOutputStream(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public void write(int b) throws IOException {
            // Redirect the output to the text area
            textArea.append(String.valueOf((char) b));
            // Scroll the text area to the end
            textArea.setCaretPosition(textArea.getDocument().getLength());
        }
    }

    public static JFrame createFrame(String fileName, byte[] fileData) {
        JFrame frame = new JFrame("Download File");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center the frame on the screen

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("Download " + fileName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton downloadButton = new JButton(new ImageIcon("download_icon.png"));
        downloadButton.setFont(new Font("Arial", Font.BOLD, 16));
        downloadButton.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(frame, "Are you sure you want to download this file?", "Confirm Download", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                downloadFile(fileName, fileData);
                frame.dispose();
            }
        });

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(downloadButton, BorderLayout.CENTER);

        frame.add(panel);

        return frame;
    }

    private static void downloadFile(String fileName, byte[] fileData) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (FileOutputStream fos = new FileOutputStream(fileToSave)) {
                fos.write(fileData);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf(".");
        if (i > 0) {
            return fileName.substring(i + 1);
        } else {
            return "No extension found";
        }
    }

    static class RoundedPanel extends JPanel {
        private int cornerRadius;

        public RoundedPanel(int cornerRadius) {
            this.cornerRadius = cornerRadius;
            this.setOpaque(false); // Required for custom painting
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); // Set the background color
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius));
        }
    }
}
