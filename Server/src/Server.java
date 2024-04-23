import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    static ArrayList<MyFile> myFiles = new ArrayList<>();
    static JFrame jFrame; // Declare JFrame globally
    static JPanel jPanel; // Declare JPanel globally

    public static void main(String[] args) throws IOException {
        jFrame = new JFrame("DDS's Server"); // Initialize JFrame here
        jFrame.setSize(500, 500);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Panel to hold file rows
        jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));

        JScrollPane jScrollPane = new JScrollPane(jPanel);
        jScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Title label
        JLabel jlTitle = new JLabel("DDS's File Receiver");
        jlTitle.setFont(new Font("Arial", Font.BOLD, 25));
        jlTitle.setBorder(new EmptyBorder(20, 0, 10, 0));
        jlTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add components to frame
        jFrame.add(jlTitle, BorderLayout.NORTH);
        jFrame.add(jScrollPane, BorderLayout.CENTER);

        jFrame.setVisible(true);

        ServerSocket serverSocket = new ServerSocket(1234);

        Thread serverThread = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    handleClient(socket);
                } catch (IOException error) {
                    error.printStackTrace();
                }
            }
        });
        serverThread.start();
    }

    private static void handleClient(Socket socket) {
        try {
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

            int fileNameLength = dataInputStream.readInt();

            if (fileNameLength > 0) {
                byte[] fileNameBytes = new byte[fileNameLength];
                dataInputStream.readFully(fileNameBytes, 0, fileNameBytes.length);
                String fileName = new String(fileNameBytes);

                int fileContentLength = dataInputStream.readInt();

                if (fileContentLength > 0) {
                    byte[] fileContentBytes = new byte[fileContentLength];
                    dataInputStream.readFully(fileContentBytes, 0, fileContentLength);

                    addFileRow(fileName, fileContentBytes);
                    myFiles.add(new MyFile(Thread.currentThread().getId(), fileName, fileContentBytes, getFileExtension(fileName)));
                }
            }
        } catch (IOException error) {
            error.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void addFileRow(String fileName, byte[] fileContentBytes) {
        JPanel fileRowPanel = new JPanel(new BorderLayout());
        fileRowPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel fileNameLabel = new JLabel(fileName);
        fileNameLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        fileRowPanel.add(fileNameLabel, BorderLayout.CENTER);

        jPanel.add(fileRowPanel);
        jPanel.revalidate();
        jPanel.repaint();

        fileRowPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                createFrame(fileName, fileContentBytes).setVisible(true);
            }
        });
    }

    public static JFrame createFrame(String fileName, byte[] fileData) {
        JFrame frame = new JFrame("Download File");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null); // Center the frame on the screen

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Download " + fileName);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton downloadButton = new JButton("Download");
        downloadButton.setFont(new Font("Arial", Font.BOLD, 16));
        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
}
