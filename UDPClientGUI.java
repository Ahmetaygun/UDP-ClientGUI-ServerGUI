import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class UDPClientGUI extends JFrame {
    private JLabel nameLabel;
    private JTextField nameLabelField;
    private JLabel serverLabel;
    private JTextField serverField;
    private JLabel messageLabel;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> errorList;
    private DefaultListModel<String> errorListModel;

    private final int MAX_RETRY = 3;

    public UDPClientGUI() {
        setTitle("UDP Client");
        setSize(500, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        nameLabel = new JLabel("Your Name:");
        nameLabelField = new JTextField();
        serverLabel = new JLabel("Server Port:");
        serverField = new JTextField();
        messageLabel = new JLabel("Message:");
        messageField = new JTextField();
        sendButton = new JButton("Send");

        add(nameLabel);
        add(nameLabelField);
        add(serverLabel);
        add(serverField);
        add(messageLabel);
        add(messageField);
        add(sendButton);

        errorListModel = new DefaultListModel<>();
        errorList = new JList<>(errorListModel);
        JScrollPane errorScrollPane = new JScrollPane(errorList);
        add(errorScrollPane);

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    String serverAddress = "localhost";
                    int serverPort = Integer.parseInt(serverField.getText());
                    String name = nameLabelField.getText();
                    String message = messageField.getText();

                    DatagramSocket socket = new DatagramSocket();
                    InetAddress server = InetAddress.getByName(serverAddress);
                    DatagramPacket receivePacket = null;

                    int retryCount = 0;

                    while (retryCount < MAX_RETRY) {
                        socket.setSoTimeout(3000);
                        DatagramPacket sendPacket = new DatagramPacket((name + ": " + message).getBytes(), (name + ": " + message).length(), server, serverPort);
                        socket.send(sendPacket);

                        byte[] receiveBuf = new byte[256];
                        receivePacket = new DatagramPacket(receiveBuf, receiveBuf.length);
                        try {
                            socket.receive(receivePacket);
                            break;
                        } catch (SocketTimeoutException ex) {
                            retryCount++;
                            addErrorToList("Connection timeout. Retrying... (Attempt " + retryCount + ")");
                            continue;
                        } finally {
                            if (receivePacket == null) {
                                addErrorToList("Server not responding. Retrying... (Attempt " + retryCount + ")");
                            } else {
                                addErrorToList("Response received from server.");
                            }
                        }
                    }

                    if (retryCount == MAX_RETRY) {
                        JOptionPane.showMessageDialog(null, "Error: Failed to connect after " + MAX_RETRY + " attempts.");
                        addErrorToList("Failed to connect after " + MAX_RETRY + " attempts.");
                    } else {
                        String receivedMessage = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        JOptionPane.showMessageDialog(null, "Server Response: " + receivedMessage);
                        addErrorToList("Server Response: " + receivedMessage);
                    }
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new UDPClientGUI().setVisible(true);
            }
        });
    }
}