package org.example;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        String filePath = "C:\\Users\\Админ\\Downloads\\photo_5842558693126813489_y.jpg"; // Шлях до файлу, який треба відправити

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            System.out.println("Підключення до сервера...");

            try (
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    DataInputStream in = new DataInputStream(socket.getInputStream())
            ) {
                // Зчитування файлу
                File file = new File(filePath);
                if (!file.exists()) {
                    System.out.println("Файл не знайдено: " + filePath);
                    return;
                }

                String fileName = file.getName();
                int fileSize = (int) file.length();
                byte[] fileData = new byte[fileSize];

                try (FileInputStream fis = new FileInputStream(file)) {
                    fis.read(fileData);
                }

                // Відправка метаданих на сервер
                out.writeUTF(fileName);
                out.writeInt(fileSize);

                // Перевірка розміру файлу перед відправкою
                if (fileSize <= 1024) {
                    // Відправка файлу на сервер
                    out.write(fileData);

                    // Отримання підтвердження від сервера
                    String serverResponse = in.readUTF();
                    System.out.println("Відповідь сервера: " + serverResponse);

                    if (serverResponse.contains("успішно")) {
                        int receivedSize = in.readInt();
                        byte[] receivedData = new byte[receivedSize];
                        in.readFully(receivedData);

                        // Збереження файлу, отриманого від сервера
                        try (FileOutputStream fos = new FileOutputStream("received_" + fileName)) {
                            fos.write(receivedData);
                        }
                        System.out.println("Файл отримано та збережено як 'received_" + fileName + "'.");
                        saveTransferStatus(fileName, fileSize, "Успішно скачано та збережено");
                    }
                } else {
                    // Якщо файл не задовільняє умовам, зберігаємо статус
                    String serverResponse = in.readUTF();
                    System.out.println("Відповідь сервера: " + serverResponse);
                    saveTransferStatus(fileName, fileSize, "Не задовільняє умовам, файл не скачаний");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для збереження статусу завантаження
    private static void saveTransferStatus(String fileName, int fileSize, String status) {
        String log = "Файл: " + fileName + ", Розмір: " + fileSize + " байт, Статус: " + status;
        System.out.println("Статус завантаження: " + log);

        try (FileWriter writer = new FileWriter("download_status.txt", true)) {
            writer.write(log + System.lineSeparator());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
