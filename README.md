# Encrypted File Sharing Application

This project is a secure file sharing application that allows users to transfer files over a network with the assurance that their data is protected. It uses RSA encryption to ensure the confidentiality and integrity of the files during transfer. The application features a simple and intuitive graphical user interface (GUI) built with Java Swing.

## Features

- **Secure File Transfer:** All files are encrypted using RSA encryption before being sent over the network. This ensures that only the intended recipient can decrypt and access the file.
- **Client-Server Architecture:** The application is built using a client-server model, which allows for easy and efficient file sharing between two computers.
- **Handshake Mechanism:** A simple handshake mechanism is used to exchange the public key between the server and the client, establishing a secure communication channel.
- **User-Friendly GUI:** The application provides a simple and easy-to-use graphical user interface for both the client and the server.

## How It Works

1.  **Server Setup:** The server application is started, and it generates a public-private key pair using the RSA algorithm.
2.  **Client Connection:** The client application connects to the server.
3.  **Public Key Exchange:** The server sends its public key to the client.
4.  **File Encryption:** The client selects a file to send and encrypts it using the server's public key.
5.  **File Transfer:** The encrypted file is sent to the server.
6.  **File Decryption:** The server receives the encrypted file and decrypts it using its private key.
7.  **File Saved:** The decrypted file is saved to the server's local disk.

## Technologies Used

- **Java:** The core programming language used for developing the application.
- **Java Swing:** Used for creating the graphical user interface for both the client and the server.
- **RSA Encryption:** The encryption algorithm used to secure the file transfers.
- **Sockets:** Used for network communication between the client and the server.

## How to Run

1.  **Compile the code:** Compile all the `.java` files in the `CombinedSendnReceive/src/CombinedPackage` directory.
2.  **Run the Server:** Run the `Server.java` file. This will start the server and open the server GUI.
3.  **Run the Client:** Run the `Client.java` file. This will start the client and open the client GUI.
4.  **Connect and Send:** In the client GUI, connect to the server (the default is `localhost`). Then, select a file and click the "Send" button. The file will be encrypted and sent to the server.

**Note:** The client is currently hardcoded to connect to `localhost`. If you want to use it over a network, you will need to change the IP address in the `Client.java` file.

## Future Improvements

- **Configurable IP Address and Port:** Allow the user to specify the IP address and port of the server to connect to.
- **Improved Error Handling:** Provide more informative error messages to the user in case of connection failures or other issues.
- **File Transfer Progress:** Show a progress bar to indicate the status of the file transfer.
- **Support for Larger Files:** The current implementation might have issues with very large files. The file is read into memory before being encrypted, which could lead to memory issues. A streaming approach could be used to handle larger files more efficiently.
