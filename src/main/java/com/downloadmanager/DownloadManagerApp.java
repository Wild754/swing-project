package com.downloadmanager;

import com.downloadmanager.MainWindow;

public class DownloadManagerApp {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true);
        });
    }
}
