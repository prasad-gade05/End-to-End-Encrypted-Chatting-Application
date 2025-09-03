package ServerPackage;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import javax.crypto.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileSystemView;

public class Server {

  static ArrayList<MyFile> myFiles = new ArrayList<>();
  int fileId;
  JFrame serverFrame;
  JLabel fileTitle;
  JScrollPane scrl;
  JPanel jpl;
  KeyPair keyPair;
  PublicKey publicKey;
  PrivateKey privateKey;
  InetAddress clientAddress, serverAddress;

  public Server() {
    try {
      keyPair = generateKeyPair();
      publicKey = keyPair.getPublic();
      privateKey = keyPair.getPrivate();
    } catch (Exception e) {
      e.printStackTrace();
    }

    serverFrame = new JFrame("Receive Files");
    serverFrame.setSize(600, 580);
    serverFrame.setLayout(null);
    serverFrame.setLocation(860, 140);
    serverFrame.getContentPane().setBackground(new Color(0x0F1C2E));
    serverFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    jpl = new JPanel();
    jpl.setLayout(new BoxLayout(jpl, BoxLayout.Y_AXIS));
    scrl = new JScrollPane(jpl);
    jpl.setBackground(new Color(0x1f2b3e));
    jpl.setBounds(10, 160, 560, 300);
    scrl.setBounds(10, 160, 560, 300);
    //		jpl.setBackground(Color.BLACK);
    //		serverFrame.add(jpl);
    serverFrame.add(scrl);

    fileTitle = new JLabel("Received Files");
    fileTitle.setForeground(new Color(0xacc2ef));
    fileTitle.setFont(new Font("Arial", Font.BOLD, 25));
    fileTitle.setBounds(200, 0, 500, 100);
    serverFrame.add(fileTitle);

    JLabel clientName = new JLabel("Receiving from : ");
    clientName.setFont(new Font("Arial", Font.BOLD, 14));
    clientName.setForeground(new Color(0xacc2ef));
    clientName.setBounds(0, 50, 500, 100);
    serverFrame.add(clientName);

    serverFrame.setVisible(true);

    Thread dataReaderThread = new Thread(() -> {
      try (ServerSocket serverSocket = new ServerSocket(12345)) {
        while (true) {
          Socket socket = serverSocket.accept();

          DataInputStream dataInputStream = new DataInputStream(
            socket.getInputStream()
          );
          clientAddress = socket.getInetAddress();

          clientName.setText(
            "Receiving from : " +
            clientAddress
              .toString()
              .substring(1, clientAddress.toString().length()) +
            " (" +
            clientAddress.getHostName() +
            " )"
          );

          int handShake = dataInputStream.readInt();
          if (handShake == 1111) {
            try (Socket sockett = new Socket(clientAddress, 1235)) {
              DataOutputStream dataOutputStream = new DataOutputStream(
                sockett.getOutputStream()
              );
              dataOutputStream.writeInt(2222);
              ObjectOutputStream oos = new ObjectOutputStream(
                sockett.getOutputStream()
              );
              oos.writeObject(publicKey);
            } catch (IOException ioe) {
              //	 						ioe.printStackTrace();
              //						System.out.println("Client is not online to accept key");
              JOptionPane.showMessageDialog(
                null,
                "Client is not online to accept key",
                "Message",
                JOptionPane.INFORMATION_MESSAGE
              );
            }
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    });

    dataReaderThread.start();

    try (ServerSocket serverSocket = new ServerSocket(1234)) {
      while (true) {
        Socket socket = serverSocket.accept();

        DataInputStream dataInputStream = new DataInputStream(
          socket.getInputStream()
        );
        int fNameLength = dataInputStream.readInt();

        if (fNameLength > 0) {
          byte[] fNameBytes = new byte[fNameLength];
          dataInputStream.readFully(fNameBytes, 0, fNameLength);
          String fName = new String(fNameBytes);

          ByteArrayOutputStream decryptedDataBuffer = new ByteArrayOutputStream();
          ByteArrayOutputStream unDecryptedDataBuffer = new ByteArrayOutputStream();

          int fContentLength;

          while ((fContentLength = dataInputStream.readInt()) > 0) {
            byte[] fContentBytes = new byte[fContentLength];
            dataInputStream.readFully(fContentBytes, 0, fContentLength);

            // Decrypt the chunk using RSA
            Cipher rsaDecryptCipher = Cipher.getInstance("RSA");
            rsaDecryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedChunk = rsaDecryptCipher.doFinal(fContentBytes);

            // Write the decrypted chunk to the ByteArrayOutputStream
            decryptedDataBuffer.write(decryptedChunk);

            unDecryptedDataBuffer.write(fContentBytes);
          }

          // Access the complete decrypted data as a byte array
          byte[] decryptedData = decryptedDataBuffer.toByteArray();
          byte[] unDecryptedData = unDecryptedDataBuffer.toByteArray();
          // Close the ByteArrayOutputStream, socket, and input stream
          decryptedDataBuffer.close();
          unDecryptedDataBuffer.close();

          JPanel jplRows = new JPanel(new GridLayout(1, 3, 10, 10));
          jplRows.setBackground(new Color(0x1f2b3e));
          jplRows.setBorder(
            BorderFactory.createCompoundBorder(
              new LineBorder(new Color(0x0D6E6E)),
              new EmptyBorder(10, 0, 10, 0)
            )
          );

          //					jplRows.setLayout(new BoxLayout(jplRows, BoxLayout.Y_AXIS));

          JLabel fNameLabel = new JLabel(fName);
          fNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
          fNameLabel.setForeground(new Color(0x55ccc9));
          fNameLabel.setFont(new Font("Arial", Font.BOLD, 15));
          fNameLabel.setBorder(new EmptyBorder(10, 0, 10, 0));

          JButton view = new JButton("View");
          view.setFont(new Font("Arial", Font.BOLD, 15));
          view.setForeground(new Color(0xe0e0e0));
          view.setBackground(new Color(0X1F3A5F));
          view.setBorder(new LineBorder(new Color(0xffbfab)));

          JButton viewDecrypted = new JButton("View Decrypted");
          viewDecrypted.setFont(new Font("Arial", Font.BOLD, 15));
          viewDecrypted.setForeground(new Color(0xe0e0e0));
          viewDecrypted.setBackground(new Color(0X1F3A5F));
          viewDecrypted.setBorder(new LineBorder(new Color(0xffbfab)));

          if (getFileExtension(fName).equalsIgnoreCase("txt")) {
            jplRows.setName(String.valueOf(fileId));
            jplRows.add(fNameLabel);
            jplRows.add(view);
            jplRows.add(viewDecrypted);
            jpl.add(jplRows);
            view.addMouseListener(getMyMouseListener());
            viewDecrypted.addMouseListener(getMyMouseListener());
            serverFrame.validate();
          }
          myFiles.add(
            new MyFile(
              fileId,
              fName,
              decryptedData,
              getFileExtension(fName),
              unDecryptedData
            )
          );
          fileId++;
        }
      }
    } catch (
      IOException
      | NoSuchAlgorithmException
      | InvalidKeyException
      | IllegalBlockSizeException
      | BadPaddingException
      | NoSuchPaddingException e
    ) {
      e.printStackTrace();
    }
  }

  private KeyPair generateKeyPair() throws Exception {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048); // You can adjust the key size as needed
    return keyPairGenerator.generateKeyPair();
  }

  public String getFileExtension(String FName) {
    int i = FName.lastIndexOf('.');
    if (i > 0) {
      return FName.substring(i + 1);
    } else {
      return "No Extension Found!!!";
    }
  }

  public MouseListener getMyMouseListener() {
    return new MouseListener() {
      @Override
      public void mouseClicked(MouseEvent me) {
        Object source = me.getSource();

        if (source instanceof JButton) {
          JButton clickedButton = (JButton) source;
          if (clickedButton.getText() == "View") {
            //						System.out.println(clickedButton.getParent().getName());
            //						System.out.println("view clicked");

            int fileId = Integer.parseInt(clickedButton.getParent().getName());

            for (MyFile myFile : myFiles) {
              if (myFile.getId() == fileId) {
                JFrame jPreviewFrame = createFrame(
                  myFile.getName(),
                  myFile.getUnDecryptedData(),
                  myFile.getFileExtension()
                );

                jPreviewFrame.setVisible(true);
              }
            }
          }
          if (clickedButton.getText() == "View Decrypted") {
            int fileId = Integer.parseInt(clickedButton.getParent().getName());

            for (MyFile myFile : myFiles) {
              if (myFile.getId() == fileId) {
                JFrame jPreviewFrame = createFrame(
                  myFile.getName(),
                  myFile.getData(),
                  myFile.getFileExtension()
                );

                jPreviewFrame.setVisible(true);
              }
            }
          }
        }
      }

      private JFrame createFrame(
        String fileName,
        byte[] fileData,
        String fileExtension
      ) {
        JFrame jFrame = new JFrame("File Downloader");
        jFrame.getContentPane().setBackground(new Color(0x0F1C2E));

        jFrame.setSize(400, 400);
        jFrame.setLocation(580, 200);

        jFrame.validate();
        JPanel jPanel = new JPanel();
        jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
        jPanel.setBackground(new Color(0x0F1C2E));

        JLabel jTitle = new JLabel("File Downloader");
        jTitle.setForeground(new Color(0xacc2ef));
        jTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        jTitle.setFont(new Font("Arial", Font.BOLD, 20));
        jTitle.setBorder(new EmptyBorder(20, 0, 10, 0));

        JLabel jPrompt = new JLabel(
          "<html>Are you sure you want to download <br>" + fileName + "</html>"
        );
        jPrompt.setForeground(new Color(0x55ccc9));
        jPrompt.setAlignmentX(Component.CENTER_ALIGNMENT);
        jPrompt.setFont(new Font("Arial", Font.BOLD, 20));
        jPrompt.setBorder(new EmptyBorder(20, 0, 10, 0));

        JButton yesBtn = new JButton("YES");
        yesBtn.setPreferredSize(new Dimension(150, 40));
        yesBtn.setFont(new Font("Arial", Font.BOLD, 20));
        yesBtn.setForeground(new Color(0xe0e0e0));
        yesBtn.setBackground(new Color(0X1F3A5F));

        JButton noBtn = new JButton("NO");
        noBtn.setPreferredSize(new Dimension(150, 40));
        noBtn.setFont(new Font("Arial", Font.BOLD, 20));
        noBtn.setForeground(new Color(0xe0e0e0));
        noBtn.setBackground(new Color(0X1F3A5F));

        JTextArea fileContents = new JTextArea(100, 100);
        fileContents.setEditable(false);
        fileContents.setBackground(new Color(0x1f2b3e));
        fileContents.setForeground(new Color(0xcee8ff));
        JScrollPane scrl = new JScrollPane(fileContents);
        fileContents.setAlignmentX(Component.CENTER_ALIGNMENT);
        scrl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel jpBtn = new JPanel();
        jpBtn.setBackground(new Color(0x0F1C2E));
        jpBtn.setBorder(new EmptyBorder(20, 0, 10, 0));
        jpBtn.add(yesBtn);
        jpBtn.add(noBtn);

        if (fileExtension.equalsIgnoreCase("txt")) {
          fileContents.setText(new String(fileData));
        }

        yesBtn.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
              FileSystemView fileSystemView = FileSystemView.getFileSystemView();
              File defaultDownloadFolder = fileSystemView.getDefaultDirectory();

              File fileToDownload = new File(defaultDownloadFolder, fileName);
              try {
                FileOutputStream fileOutputStream = new FileOutputStream(
                  fileToDownload
                );
                fileOutputStream.write(fileData);
                fileOutputStream.close();
                jFrame.dispose();

                JFrame fileInfo = new JFrame("File Information");
                fileInfo.getContentPane().setBackground(new Color(0x0F1C2E));
                fileInfo.setSize(400, 360);
                fileInfo.setLocation(580, 200);
                fileInfo.setLayout(null);
                JTextArea fIleInfoJLabel = new JTextArea(
                  "\nDownloaded : " +
                  fileName +
                  "\n\nAt Location:" +
                  fileToDownload.getAbsolutePath()
                );

                fIleInfoJLabel.setBackground(new Color(0x1f2b3e));
                fIleInfoJLabel.setForeground(new Color(0xcee8ff));
                fIleInfoJLabel.setEditable(false);
                fIleInfoJLabel.setBounds(10, 60, 350, 100);
                fIleInfoJLabel.setFont(new Font("Arial", Font.BOLD, 14));
                JScrollPane scrl = new JScrollPane(fIleInfoJLabel);
                scrl.setBounds(10, 40, 350, 100);
                fileInfo.add(scrl);

                JButton fileExplorerBtnButton = new JButton(
                  "<html>View in <br>File Explorer</html>"
                );
                fileExplorerBtnButton.setFont(new Font("Arial", Font.BOLD, 14));
                fileExplorerBtnButton.setBounds(10, 200, 150, 40);
                fileExplorerBtnButton.setForeground(new Color(0xe0e0e0));
                fileExplorerBtnButton.setBackground(new Color(0X1F3A5F));
                fileInfo.add(fileExplorerBtnButton);
                fileExplorerBtnButton.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      try {
                        Desktop.getDesktop().open(defaultDownloadFolder);
                      } catch (IOException e1) {
                        e1.printStackTrace();
                      }
                    }
                  }
                );

                JButton openFileBtn = new JButton("Open File");
                openFileBtn.setFont(new Font("Arial", Font.BOLD, 14));
                openFileBtn.setBounds(210, 200, 150, 40);
                openFileBtn.setForeground(new Color(0xe0e0e0));
                openFileBtn.setBackground(new Color(0X1F3A5F));
                fileInfo.add(openFileBtn);
                openFileBtn.addActionListener(
                  new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                      File file = new File(fileToDownload.getAbsolutePath());
                      try {
                        Desktop.getDesktop().open(file);
                      } catch (IOException e1) {
                        e1.printStackTrace();
                      }
                    }
                  }
                );

                fileInfo.setVisible(true);
                // System.out.println("File Downloaded!!");
                // System.out.println("At location : " + fileToDownload.getAbsolutePath());
              } catch (IOException e) {
                e.printStackTrace();
              }
            }
          }
        );

        noBtn.addActionListener(
          new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
              jFrame.dispose();
            }
          }
        );

        jPanel.add(jTitle);
        jPanel.add(jPrompt);
        //				jPanel.add(fileContents);
        jPanel.add(scrl);
        jPanel.add(jpBtn);
        jFrame.add(jPanel);
        return jFrame;
      }

      @Override
      public void mousePressed(MouseEvent me) {}

      @Override
      public void mouseReleased(MouseEvent me) {}

      @Override
      public void mouseEntered(MouseEvent e) {}

      @Override
      public void mouseExited(MouseEvent e) {}
    };
  }

  public static void main(String[] args) {
    new Server();
  }
}
