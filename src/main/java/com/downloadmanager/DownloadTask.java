package com.downloadmanager;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.atomic.AtomicBoolean;

public class DownloadTask implements Runnable {
    private final String fileUrl;
    private final String savePath;
    private final AtomicBoolean isPaused;
    private final AtomicBoolean isStopped;
    private final DownloadController controller;

    public DownloadTask(String fileUrl, String savePath, DownloadController controller) {
        this.fileUrl = fileUrl;
        this.savePath = savePath;
        this.isPaused = new AtomicBoolean(false);
        this.isStopped = new AtomicBoolean(false);
        this.controller = controller;
    }

    public void pause() {
        isPaused.set(true);
    }

    public void resume() {
        isPaused.set(false);
        synchronized (this) {
            notify();
        }
    }

    public void stop() {
        isStopped.set(true);
    }

    @Override
    public void run() {
        RandomAccessFile raf = null;
        InputStream in = null;

        try {
            System.out.println("Connecting to URL: " + fileUrl);
            URL url = new URL(fileUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            File file = new File(savePath);

            // Перевірка та створення директорії
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                if (!parentDir.mkdirs()) {
                    System.out.println("Не вдалося створити директорію: " + parentDir.getAbsolutePath());
                    return;
                }
            }

            long downloadedBytes = file.exists() ? file.length() : 0;
            connection.setRequestProperty("Range", "bytes=" + downloadedBytes + "-");
            connection.connect();

            // Перевірка коду відповіді сервера
            int status = connection.getResponseCode();
            if (status != HttpURLConnection.HTTP_PARTIAL && status != HttpURLConnection.HTTP_OK) {
                System.out.println("Server response: " + status + " " + connection.getResponseMessage());
                return;
            }

            long totalBytes = connection.getContentLengthLong() + downloadedBytes;
            in = connection.getInputStream();
            raf = new RandomAccessFile(file, "rw");
            raf.seek(downloadedBytes);

            byte[] buffer = new byte[4096];
            int bytesRead;
            long currentBytes = downloadedBytes;

            while ((bytesRead = in.read(buffer)) != -1) {
                // Перевірка на зупинку завантаження
                if (isStopped.get()) {
                    System.out.println("Download stopped: " + fileUrl);
                    break;
                }

                // Перевірка на паузу
                synchronized (this) {
                    while (isPaused.get()) {
                        System.out.println("Download paused: " + fileUrl);
                        wait(); // Очікуємо, поки завдання буде відновлено
                    }
                }

                raf.write(buffer, 0, bytesRead);
                currentBytes += bytesRead;

                // Оновлення прогресу
                int progress = (int) ((currentBytes * 100) / totalBytes);
                controller.updateProgress(fileUrl, progress);
            }

            if (currentBytes == totalBytes) {
                System.out.println("Download completed: " + fileUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (raf != null) raf.close();
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Метод відновлення завантаження
    public void resumeDownloadTask() {
        isPaused.set(false);
        synchronized (this) {
            notify();
        }
    }

}


