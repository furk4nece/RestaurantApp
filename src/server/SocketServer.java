//Bu kısım şuanlık pasif ve hata bulunmaktadır, işlevi ana bilgisayara veri yollayacak olan tablet veya telefonların LAN bağlnantısını sağlamak

package server;

import javax.swing.*;

import database.DatabaseManager;

import java.io.*;
import java.net.*;

public class SocketServer extends Thread {
    private ServerSocket serverSocket;
    private boolean running = true;
    private ServerApp app;
    private DatabaseManager db;

    public SocketServer(ServerApp app, DatabaseManager db) {
        this.app = app;
        this.db = db;
    }

    public void run() {
        try {
            serverSocket = new ServerSocket(1234);
            System.out.println("Sunucu başlatildi. Port: 1234");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket, app, db).start();
            }
        } catch (IOException e) {
            System.err.println("Sunucu hatasi: " + e.getMessage());
        }
    }

    class ClientHandler extends Thread {
        private Socket socket;
        private ServerApp app;
        private DatabaseManager db;

        public ClientHandler(Socket socket, ServerApp app, DatabaseManager db) {
            this.socket = socket;
            this.app = app;
            this.db = db;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line = in.readLine(); // Örnek format: 1|Pizza,Ayran|45.0
                if (line != null) {
                    String[] parts = line.split("\\|");
                    int masaNo = Integer.parseInt(parts[0]);
                    String siparis = parts[1];
                    double tutar = Double.parseDouble(parts[2]);

                    //app.siparisGuncelle(masaNo, siparis, tutar);
                    //db.masaSiparisGuncelle(masaNo, siparis, tutar);
                }
            } catch (IOException e) {
                System.err.println("İleti alma hatasi: " + e.getMessage());
            }
        }
    }

    public void stopServer() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {
            System.err.println("Sunucu durdurulamadi.");
        }
    }
}
