package CombinedPackage;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.*;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;

public class Client extends JPanel {

	File fileToSend;
	JFrame clientFrame;
	JLabel fileName, hostName;
	JButton sendBtn, chooseBtn;
	String selectedFile;
	int key;
	int serverOnlineFlag;
	PublicKey receivedPublicKey;
	InetAddress clientAddress, serverAddress;

	public Client() {
		key = -1;
		serverOnlineFlag = -1;

		setSize(600, 580);
		setLayout(null);
		setBackground(new Color(0x0F1C2E));

		setLocation(60, 140);
		setVisible(true);

		fileName = new JLabel("Choose a file to send");
		fileName.setFont(new Font("Arial", Font.BOLD, 25));
		fileName.setBounds(170, 0, 500, 100);
		fileName.setForeground(new Color(0xacc2ef));
		add(fileName);

		hostName = new JLabel("Receiver : ");
		hostName.setFont(new Font("Arial", Font.BOLD, 16));
		hostName.setBounds(30, 80, 550, 100);
		hostName.setForeground(new Color(0xacc2ef));
		add(hostName);

		sendBtn = new JButton("Send File");
		sendBtn.setFont(new Font("Arial", Font.BOLD, 20));
		sendBtn.setBounds(50, 180, 150, 100);
		sendBtn.setForeground(new Color(0xe0e0e0));
		sendBtn.setBackground(new Color(0X1F3A5F));
		add(sendBtn);

		chooseBtn = new JButton("Choose File");
		chooseBtn.setFont(new Font("Arial", Font.BOLD, 20));
		chooseBtn.setBounds(380, 180, 150, 100);
		chooseBtn.setForeground(new Color(0xe0e0e0));
		chooseBtn.setBackground(new Color(0X1F3A5F));
		add(chooseBtn);
		validate();

		int timeout = 1000;
		ExecutorService executorService = Executors.newFixedThreadPool(350); // Adjust the thread pool size as needed

		JPanel hostNamePanel = new JPanel();

		hostNamePanel.setBounds(130, 350, 320, 180);
		DefaultListModel<String> hostListModel = new DefaultListModel<>();
		JList<String> hostList = new JList<>(hostListModel);
		JScrollPane hostScrollPane = new JScrollPane(hostList);
		hostList.setFont(new Font("Arial", Font.BOLD, 14));
		hostList.setBackground(new Color(0x1f2b3e));
		hostList.setForeground(new Color(0xcee8ff));
		hostScrollPane.setPreferredSize(new Dimension(320, 200));
		hostScrollPane.validate();
		hostNamePanel.add(hostScrollPane);
		add(hostNamePanel);

		hostList.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				JList<String> list = (JList<String>) evt.getSource();
				if (evt.getClickCount() == 1) {
					int index = list.locationToIndex(evt.getPoint());
					String selectedIP = hostListModel.getElementAt(index);
					// JOptionPane.showMessageDialog(clientFrame, "You selected IP: " + selectedIP);
					hostName.setText("Receiver : " + selectedIP);
					key = -1;
					fileName.setText("Choose a file to send");
					selectedFile = null;
					fileToSend=null;
					try {
						serverAddress = InetAddress.getByName(selectedIP.substring(0, selectedIP.indexOf(' ')));
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
			}
		});

		try {
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
				while (inetAddresses.hasMoreElements()) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
						// Assuming IPv4 address here
						String baseIpAddress = inetAddress.getHostAddress().substring(0,
								inetAddress.getHostAddress().lastIndexOf('.') + 1);

						for (int i = 1; i <= 254; i++) {
							final int index = i;
							executorService.execute(() -> {
								String ipAddress = baseIpAddress + index;
								try {
									InetAddress addressToCheck = InetAddress.getByName(ipAddress);
									if (addressToCheck.isReachable(timeout)) {
										String hostName = addressToCheck.getHostName();
										hostListModel.addElement(ipAddress + " (" + hostName + ")");
										validate();
									}
								} catch (IOException e) {
									// Handle exceptions
								}
							});
						}
					}
				}
			}
		} catch (SocketException e) {
			// Handle exceptions
		}
		validate();
		executorService.shutdown();
		chooseBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (serverAddress == null)
					JOptionPane.showMessageDialog(null, "No Receiver selected", "Message",
							JOptionPane.INFORMATION_MESSAGE);
				else {
					if (key == -1) {
						try (Socket socket = new Socket(serverAddress, 12345)) {
							DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
							dataOutputStream.writeInt(1111);
							serverOnlineFlag = 1;
						} catch (IOException ioe) {
							// ioe.printStackTrace();
							// System.out.println("Server is not online to do handshake");
							JOptionPane.showMessageDialog(null, "Server is not online to do a handshake", "Message",
									JOptionPane.INFORMATION_MESSAGE);
							serverOnlineFlag = 0;
						}
						if (serverOnlineFlag == 1) {
							try (ServerSocket serverSocket = new ServerSocket(1235)) {
								while (true) {
									Socket socket = serverSocket.accept();
									key = -1;
									DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

									key = dataInputStream.readInt();
									if (key == 2222) {
										ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
										receivedPublicKey = (PublicKey) ois.readObject();
										// System.out.println("Key received" + key);
										break;
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							}
						}
					}
				}

				if (key != -1) {
					FileDialog fileDialog = new FileDialog(new Frame(), "Choose a file to send", FileDialog.LOAD);

					fileDialog.setVisible(true);

					selectedFile = fileDialog.getFile();
					if (selectedFile != null) {
						int i = selectedFile.lastIndexOf('.');
						if (i > 0) {
							if (selectedFile.substring(i + 1).equalsIgnoreCase("txt")) {
								fileToSend = new File(fileDialog.getDirectory(), selectedFile);
								fileName.setText("<html>The file to send is: <br>" + fileToSend.getName() + "</html>");
							} else {
								fileName.setText("The file selected is not a text file!!");
								JOptionPane.showMessageDialog(null, "The selected file is not a text file", "Message",
										JOptionPane.INFORMATION_MESSAGE);
							}
						} else {
							fileName.setText("The file does not have an extension");
							JOptionPane.showMessageDialog(null, "The file does not have an extension", "Message",
									JOptionPane.INFORMATION_MESSAGE);
						}
					}
				}
			}
		});

		sendBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				if (fileToSend == null) {
					fileName.setText("Please choose a file to send");
					JOptionPane.showMessageDialog(null, "Kindly choose a file to send", "Message",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					try {
						// Encrypt the file content using RSA
						Cipher cipher = Cipher.getInstance("RSA");
						cipher.init(Cipher.ENCRYPT_MODE, receivedPublicKey);

						FileInputStream fileInputStream = new FileInputStream(fileToSend.getAbsolutePath());
						Socket socket = new Socket(serverAddress, 1234);
						DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

						String fNameString = fileToSend.getName();
						byte[] fNameBytes = fNameString.getBytes();

						// Send the file name first
						dataOutputStream.writeInt(fNameBytes.length);
						dataOutputStream.write(fNameBytes);

						// Define a buffer for reading and encrypting chunks of data
						byte[] buffer = new byte[245]; // Adjust the size according to your key size

						int bytesRead;
						while ((bytesRead = fileInputStream.read(buffer)) != -1) {
							// Encrypt the chunk of data and send it
							byte[] encryptedChunk = cipher.doFinal(buffer, 0, bytesRead);
							dataOutputStream.writeInt(encryptedChunk.length);
							dataOutputStream.write(encryptedChunk);
							fileName.setText("<html>The file to send is: <br>" + fileToSend.getName() + "</html>");
						}

						// Signal the end of file transmission
						dataOutputStream.writeInt(0);

						fileInputStream.close();
						dataOutputStream.close();
						socket.close();
					} catch (IOException ioe) {
						// ioe.printStackTrace();
						fileName.setText("Receiver not Online");
						JOptionPane.showMessageDialog(null, "Receiver not online", "Message",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException
							| IllegalBlockSizeException | BadPaddingException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}
}
