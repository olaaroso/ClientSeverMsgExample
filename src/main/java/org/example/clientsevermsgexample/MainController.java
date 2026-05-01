package org.example.clientsevermsgexample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private ComboBox<String> dropdownPort;
    @FXML private Button clearBtn;
    @FXML private TextArea resultArea;
    @FXML private Label server_lbl;
    @FXML private Button testBtn;
    @FXML private Label test_lbl;
    @FXML private TextField urlName;

    // Server Variables
    private TextArea serverChatArea;
    private TextField serverInput;
    private DataOutputStream serverOut;

    // Client Variables
    private TextArea clientChatArea;
    private TextField clientInput;
    private DataOutputStream clientOut;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dropdownPort.getItems().addAll("7", "13", "21", "23", "71", "80", "119", "161");
    }

    @FXML
    void checkConnection(ActionEvent event) {
        String host = urlName.getText();
        int port = Integer.parseInt(dropdownPort.getValue());

        try {
            Socket sock = new Socket(host, port);
            resultArea.appendText(host + " listening on port " + port + "\n");
            sock.close();
        } catch (UnknownHostException e) {
            resultArea.appendText(e + "\n");
        } catch (Exception e) {
            resultArea.appendText(host + " not listening on port " + port + "\n");
        }
    }

    @FXML
    void clearBtn(ActionEvent event) {
        resultArea.setText("");
        urlName.setText("");
    }


    // SERVER LOGIC
    @FXML
    void startServer(ActionEvent event) {
        Stage stage = new Stage();
        Group root = new Group();

        serverChatArea = new TextArea();
        serverChatArea.setLayoutX(20);
        serverChatArea.setLayoutY(20);
        serverChatArea.setPrefSize(560, 200);
        serverChatArea.setEditable(false);

        serverInput = new TextField();
        serverInput.setLayoutX(20);
        serverInput.setLayoutY(240);
        serverInput.setPrefSize(450, 30);
        serverInput.setPromptText("Type a message to the client...");

        Button sendBtn = new Button("Send");
        sendBtn.setLayoutX(490);
        sendBtn.setLayoutY(240);
        sendBtn.setPrefSize(90, 30);
        sendBtn.setOnAction(e -> sendServerMessage());

        serverInput.setOnAction(e -> sendServerMessage());

        root.getChildren().addAll(serverChatArea, serverInput, sendBtn);
        Scene scene = new Scene(root, 600, 350);
        stage.setScene(scene);
        stage.setTitle("Server Chat");
        stage.show();

        new Thread(this::runServer).start();
    }

    private void runServer() {
        try (ServerSocket serverSocket = new ServerSocket(6666)) {
            updateServerChat("Server started on port 6666. Waiting for a client...");

            Socket clientSocket = serverSocket.accept();
            updateServerChat("Client connected!");

            DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
            serverOut = new DataOutputStream(clientSocket.getOutputStream());

            while (true) {
                String message = dis.readUTF();
                updateServerChat("Client: " + message);
            }
        } catch (IOException e) {
            updateServerChat("Server disconnected or error: " + e.getMessage());
        }
    }

    private void sendServerMessage() {
        try {
            String msg = serverInput.getText();
            if (!msg.isEmpty() && serverOut != null) {
                serverOut.writeUTF(msg);
                serverOut.flush();
                updateServerChat("Server: " + msg);
                serverInput.clear();
            }
        } catch (IOException e) {
            updateServerChat("Error sending message: " + e.getMessage());
        }
    }

    private void updateServerChat(String message) {
        Platform.runLater(() -> serverChatArea.appendText(message + "\n"));
    }


    // CLIENT LOGIC
    @FXML
    void startClient(ActionEvent event) {
        Stage stage = new Stage();
        Group root = new Group();

        clientChatArea = new TextArea();
        clientChatArea.setLayoutX(20);
        clientChatArea.setLayoutY(20);
        clientChatArea.setPrefSize(560, 200);
        clientChatArea.setEditable(false);

        clientInput = new TextField();
        clientInput.setLayoutX(20);
        clientInput.setLayoutY(240);
        clientInput.setPrefSize(450, 30);
        clientInput.setPromptText("Type a message to the server...");

        Button sendBtn = new Button("Send");
        sendBtn.setLayoutX(490);
        sendBtn.setLayoutY(240);
        sendBtn.setPrefSize(90, 30);
        sendBtn.setOnAction(e -> sendClientMessage());

        clientInput.setOnAction(e -> sendClientMessage());

        root.getChildren().addAll(clientChatArea, clientInput, sendBtn);
        Scene scene = new Scene(root, 600, 350);
        stage.setScene(scene);
        stage.setTitle("Client Chat");
        stage.show();

        new Thread(this::runClient).start();
    }

    private void runClient() {
        try {
            Socket socket = new Socket("localhost", 6666);
            updateClientChat("Connected to server at localhost:6666!");

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            clientOut = new DataOutputStream(socket.getOutputStream());

            while (true) {
                String message = dis.readUTF();
                updateClientChat("Server: " + message);
            }
        } catch (IOException e) {
            updateClientChat("Client disconnected or error: " + e.getMessage());
        }
    }

    private void sendClientMessage() {
        try {
            String msg = clientInput.getText();
            if (!msg.isEmpty() && clientOut != null) {
                clientOut.writeUTF(msg);
                clientOut.flush();
                updateClientChat("Client: " + msg);
                clientInput.clear();
            }
        } catch (IOException e) {
            updateClientChat("Error sending message: " + e.getMessage());
        }
    }

    private void updateClientChat(String message) {
        Platform.runLater(() -> clientChatArea.appendText(message + "\n"));
    }
}