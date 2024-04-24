import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class UDPServerGUI extends JFrame {
    private JLabel portLabel;
    private JTextField portField;
    private JButton startButton;
    private JTextArea messageArea;
    private JList<String> errorList;
    private DefaultListModel<String> errorListModel;
    private DatagramSocket socket;
    private boolean serverRunning;

    public UDPServerGUI() {
        setTitle("UDP Server");
        setSize(500, 300);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel(new GridLayout(2, 2));
        portLabel = new JLabel("Port:");
        portField = new JTextField();
        startButton = new JButton("Start Server");

        topPanel.add(portLabel);
        topPanel.add(portField);
        topPanel.add(startButton);

        add(topPanel, BorderLayout.NORTH);

        messageArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(messageArea);
        add(scrollPane, BorderLayout.CENTER);

        errorListModel = new DefaultListModel<>();
        errorList = new JList<>(errorListModel);
        JScrollPane errorScrollPane = new JScrollPane(errorList);
        add(errorScrollPane, BorderLayout.EAST);

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int port = Integer.parseInt(portField.getText());
                    if (serverRunning) {
                        socket.close();
                        serverRunning = false;
                        startButton.setText("Start Server");
                        return;
                    }
                    socket = new DatagramSocket(port);
                    serverRunning = true;
                    startButton.setText("Stop Server");

                    Thread serverThread = new Thread(new Runnable() {
                        public void run() {
                            try {
                                while (serverRunning) {
                                    byte[] buf = new byte[256];
                                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                                    socket.receive(packet);

                                    InetAddress clientAddress = packet.getAddress();
                                    int clientPort = packet.getPort();

                                    String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                                    messageArea.append("From " + clientAddress.getHostAddress() + ":" + clientPort + ": " + receivedMessage + "\n");

                                    byte[] sendData = receivedMessage.getBytes();
                                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientAddress, clientPort);
                                    socket.send(sendPacket);

                                    addMessageToList("Port: " + port);
                                }
                            } catch (Exception ex) {
                                if (serverRunning) {
                                    addErrorToList("Error: " + ex.getMessage());
                                }
                            }
                        }
                    });
                    serverThread.start();
                } catch (Exception ex) {
                    addErrorToList("Error: " + ex.getMessage());
                }
            }
        });
    }

    private void addErrorToList(String error) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        errorListModel.addElement(timestamp + " - " + error);
    }

    private void addMessageToList(String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        messageArea.append(timestamp + " - " + message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new UDPServerGUI().setVisible(true);
            }
        });
    }
}