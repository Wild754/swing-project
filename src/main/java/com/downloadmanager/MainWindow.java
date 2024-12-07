package com.downloadmanager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {
    private static MainWindow instance; // Синглтон
    private JTable downloadTable;
    private JButton startButton, stopButton, resumeButton;
    private JTextField urlField;
    private final DownloadController controller;

    public MainWindow() {
        instance = this; // Ініціалізація синглтону
        controller = new DownloadController(5); // Максимум 5 потоків

        setTitle("Download Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());

        // Верхня панель
        JPanel topPanel = new JPanel(new BorderLayout());
        urlField = new JTextField();
        startButton = new JButton("Start");
        stopButton = new JButton("Stop");
        resumeButton = new JButton("Resume");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        buttonPanel.add(resumeButton);

        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        // Таблиця завантажень
        String[] columnNames = {"URL", "Status", "Progress"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        downloadTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(downloadTable);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        setupListeners(); // Додаємо обробники подій
    }

    public static MainWindow getInstance() {
        return instance;
    }

    // Додаємо обробники подій
    private void setupListeners() {
        // Кнопка "Start"
        startButton.addActionListener(e -> {
            String url = urlField.getText().trim();
            if (!url.isEmpty()) {
                String savePath = "downloads/" + url.substring(url.lastIndexOf('/') + 1);

                // Додаємо завантаження через контролер
                controller.addDownload(url, savePath);

                // Додаємо запис до таблиці
                DefaultTableModel model = (DefaultTableModel) downloadTable.getModel();
                model.addRow(new Object[]{url, "In Progress", "0%"});
            }
        });

        // Кнопка "Stop"
        stopButton.addActionListener(e -> {
            int selectedRow = downloadTable.getSelectedRow();
            if (selectedRow >= 0) {
                String url = (String) downloadTable.getValueAt(selectedRow, 0);
                controller.stopDownload(url);

                // Оновлюємо статус в таблиці
                downloadTable.setValueAt("Stopped", selectedRow, 1);
            }
        });

        // Кнопка "Resume"
        resumeButton.addActionListener(e -> {
            int selectedRow = downloadTable.getSelectedRow();
            if (selectedRow >= 0) {
                String url = (String) downloadTable.getValueAt(selectedRow, 0);
                controller.resumeDownload(url);

                // Оновлюємо статус в таблиці
                downloadTable.setValueAt("In Progress", selectedRow, 1);
            }
        });
    }

    // Оновлення прогресу в таблиці
    public void updateDownloadProgress(String url, int progress) {
        SwingUtilities.invokeLater(() -> {
            DefaultTableModel model = (DefaultTableModel) downloadTable.getModel();
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 0).equals(url)) {
                    model.setValueAt(progress + "%", i, 2);
                    break;
                }
            }
        });
    }
}
