package com.downloadmanager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashMap;
import java.util.Map;
import javax.swing.SwingUtilities;

public class DownloadController {
    private final ExecutorService executor;
    private final Map<String, DownloadTask> tasks;
    private final Map<String, Integer> downloadProgress;

    public DownloadController(int maxThreads) {
        executor = Executors.newFixedThreadPool(maxThreads);
        tasks = new HashMap<>();
        downloadProgress = new HashMap<>();
    }

    // Додаємо завантаження в чергу
    public void addDownload(String url, String savePath) {
        if (!tasks.containsKey(url)) {
            DownloadTask task = new DownloadTask(url, savePath, this);
            tasks.put(url, task);
            executor.submit(task);
            downloadProgress.put(url, 0); // Ініціалізація прогресу
            System.out.println("Завантаження додано: " + url);
        }
    }

    // Відновлення завантаження
    public void resumeDownload(String url) {
        if (tasks.containsKey(url)) {
            tasks.get(url).resumeDownloadTask(); // Виклик оновленого методу
            System.out.println("Завантаження відновлено: " + url);
        }
    }

    // Зупинка завантаження
    public void stopDownload(String url) {
        if (tasks.containsKey(url)) {
            tasks.get(url).stop();
            tasks.remove(url);
            downloadProgress.remove(url); // Видаляємо прогрес
            System.out.println("Завантаження зупинено: " + url);
        }
    }

    // Оновлення прогресу завантаження
    public void updateProgress(String url, int progress) {
        downloadProgress.put(url, progress);

        // Оновлення GUI
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = MainWindow.getInstance(); // Якщо MainWindow є синглтоном
            if (mainWindow != null) {
                mainWindow.updateDownloadProgress(url, progress);
            }

        });
    }
}
