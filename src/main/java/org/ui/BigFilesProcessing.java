package org.ui;

import org.entity.FeedbackLogProcessingResult;
import org.ui.entity.DataForLogsProcessing;
import org.ui.tools.ToolsForUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

public class BigFilesProcessing {
    private JPanel mainPanel;
    private JLabel maxThreadsCountLabel;
    private JLabel simulateLimitationsLabel;
    private JPanel simulateLimitationsPanel;
    private JButton changeLimitationButton;
    private JTable logsProcessingProgressTable;
    private JButton addLogFileButton;
    private JButton startProcessingButton;
    private JPanel logsFIlesManagePanel;
    private JLabel currentMaxThreadsCountLabel;
    private JTextField maxThreadsCountInput;
    private JTextField maxSimmulatedThreadsCountInput;
    private JLabel currentMaxSimulatedThreadsCount;
    private JTable threadsTable;
    private JLabel totalProcessedFeedbacksCountLabel;
    private JLabel genderLabel;
    private JLabel mostFrequentlyUsedWordLabel;
    private JLabel goodPercentageLabel;
    private JLabel badPercentageLabel;

    private final int MAX_THREADS = 12;



    private FeedbackLogProcessingResult finalResult;

    public BigFilesProcessing() {
        addLogFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*JFileChooser fileChooser = new JFileChooser();
                int result = fileChooser.showOpenDialog(null);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    // temporary
                    Vector<String> row = new Vector<>();
                    row.add(file.getName());
                    row.add("Not processed");
                    row.add(Integer.toString(logsProcessingProgressTable.getRowCount()));

                    *//*String[] row = {"dasd", "dasd"};*//*

                    DefaultTableModel model = (DefaultTableModel) logsProcessingProgressTable.getModel();

                    model.setColumnIdentifiers(new String[] { "Log Name", "Status", "Thread ID"});

                    model.addRow(row);

                }*/
                Vector<String> row = new Vector<>();
                row.add("log_test_1.txt");
                row.add("Not processed");
                row.add(Integer.toString(logsProcessingProgressTable.getRowCount()));

                DefaultTableModel model = (DefaultTableModel) logsProcessingProgressTable.getModel();

                model.setColumnIdentifiers(new String[] { "Log Name", "Status", "Thread ID"});

                model.addRow(row);

                row = new Vector<>();
                row.add("log_test_2.txt");
                row.add("Not processed");
                row.add(Integer.toString(logsProcessingProgressTable.getRowCount()));
                model.addRow(row);

                row = new Vector<>();
                row.add("log_test_2024-11-30.txt");
                row.add("Not processed");
                row.add(Integer.toString(logsProcessingProgressTable.getRowCount()));
                model.addRow(row);

                row = new Vector<>();
                row.add("log_test_3.txt");
                row.add("Not processed");
                row.add(Integer.toString(logsProcessingProgressTable.getRowCount()));
                model.addRow(row);


                row = new Vector<>();
                row.add("log_test_4.txt");
                row.add("Not processed");
                row.add(Integer.toString(logsProcessingProgressTable.getRowCount()));
                model.addRow(row);
            }
        });

        // START PROCESSING
        startProcessingButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                DataForLogsProcessing data = ToolsForUI.initProcessing(
                        threadsTable,
                        currentMaxThreadsCountLabel,
                        currentMaxSimulatedThreadsCount,
                        logsProcessingProgressTable
                        );

                ToolsForUI.processingWithSwingWorker(
                        data,
                        threadsTable,
                        totalProcessedFeedbacksCountLabel,
                        genderLabel,
                        mostFrequentlyUsedWordLabel,
                        goodPercentageLabel,
                        badPercentageLabel
                );
            }
        });

        changeLimitationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String maxThreadsCount = maxThreadsCountInput.getText();
                if (!maxThreadsCount.isEmpty()) {
                    try {
                        int newMaxThreadsCount = Integer.parseInt(maxThreadsCount);
                        if (newMaxThreadsCount > 0 && newMaxThreadsCount <= MAX_THREADS) {
                            currentMaxThreadsCountLabel.setText(Integer.toString(newMaxThreadsCount));
                        }
                        else {
                            currentMaxThreadsCountLabel.setText("Invalid number of threads!");
                        }

                        int newMaxSimulatedThreadsCount = Integer.parseInt(maxSimmulatedThreadsCountInput.getText());
                        if (newMaxSimulatedThreadsCount > 0 && newMaxSimulatedThreadsCount <= newMaxThreadsCount) {
                            currentMaxSimulatedThreadsCount.setText(Integer.toString(newMaxSimulatedThreadsCount));
                        }
                        else {
                            currentMaxSimulatedThreadsCount.setText("Invalid number of threads!");
                        }
                    } catch (Exception ex) {
                        currentMaxThreadsCountLabel.setText("Only numbers!");
                    }
                }

            }
        });
    }

    public void show() {
        JFrame frame = new JFrame("Big Files Processing");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(mainPanel);
        frame.pack(); // Adjusts window size based on the preferred size of components
        frame.setLocationRelativeTo(null); // Centers window on the screen
        frame.setVisible(true);

    }
}
