package org.ui;

import org.entity.FeedbackLogProcessingResult;
import org.service.FeedbackLogAnalyzeService;
import org.ui.entity.DataForLogsProcessing;
import org.ui.swingworkers.LogsAnalyzeSwingWorker;
import org.ui.swingworkers.SwingWorkersDataGetterThread;
import org.ui.tools.ToolsForUI;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.concurrent.*;

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
                ToolsForUI.fillThreadsTableHeader(threadsTable);
                ToolsForUI.clearThreadTable(threadsTable);

                DataForLogsProcessing data = new DataForLogsProcessing();
                data.setData(currentMaxThreadsCountLabel, currentMaxSimulatedThreadsCount, logsProcessingProgressTable);

                Callable<FeedbackLogProcessingResult> analyzeFilesTask = () -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        data.getSemaphore().acquire();
                        FeedbackLogProcessingResult result = new FeedbackLogProcessingResult();
                        result.setThread(Thread.currentThread());
                        result.setLogName("-");
                        result.setStatus( "Not processed");

                        data.getMutex().acquire();
                        if(data.getQueue().isEmpty()) {

                            data.getMutex().release();
                            return result;
                        }
                        else {
                            try {
                                // Thread status to preparing to proceed
                                String logName = data.getQueue().poll();
                                result.setLogName(logName);
                                result.setStatus("Preparing to proceed");

                                Thread.sleep(1000);
                                // Start proceed

                                FeedbackLogAnalyzeService service = new FeedbackLogAnalyzeService(logName);
                                data.getMutex().release();
                                //Proceeded
                                result = service.analyze();
                                result.setLogName(logName);
                                result.setThread(Thread.currentThread());
                                result.setStatus("Proceeded");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            } finally {
                                data.getSemaphore().release();
                            }
                        }

                        if(result.getFeedbacksCount() != 0)
                        {
                            result.setStatus("Proceeded");
                            long endTime = System.currentTimeMillis();
                            result.setExecutionTime(endTime - startTime);
                            data.getResults().add(result);
                        }
                        return result;
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                };

                /*List<SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult>> workers = new ArrayList<>();

                for (int i = 0; i < data.getFileCount(); ++i) {
                    workers.add(new LogsAnalyzeSwingWorker(data.getSemaphore(), data.getMutex(), data.getQueue(), data.getResults(), threadsTable));
                    data.getScheduler().schedule(workers.get(i), 10, TimeUnit.MILLISECONDS);
                }

                new SwingWorkersDataGetterThread(workers, data.getResults()).start();*/


                List<ScheduledFuture<FeedbackLogProcessingResult>> futures = new ArrayList<>();

                for (int i = 0; i < data.getFileCount(); ++i) {
                    futures.add(data.getScheduler().schedule(analyzeFilesTask, 10, TimeUnit.MILLISECONDS));
                }

                finalResult = new FeedbackLogProcessingResult();

                long totalTime = 0;
                long startTime = System.currentTimeMillis();

                int goodPercentage = 0;
                int badPercentage = 0;

                for (ScheduledFuture<FeedbackLogProcessingResult> future : futures) {
                    try {
                        FeedbackLogProcessingResult result = future.get();
                        ToolsForUI.updateThreadTable(result.getThread(),
                                result.getStatus(),
                                result.getLogName(),
                                result.getExecutionTime() + " ms",
                                threadsTable);

                        data.getFinalResult().setFeedbacksCount(data.getFinalResult().getFeedbacksCount() + result.getFeedbacksCount());
                        data.getFinalResult().setExecutionTime(data.getFinalResult().getExecutionTime() + result.getExecutionTime());
                        goodPercentage += result.getPercentageOfGoodFeedbacks();
                        badPercentage += result.getPercentageOfBadFeedbacks();
                        data.getFinalResult().setMostFrequentlyUsedWord(result.getMostFrequentlyUsedWord());
                        data.getFinalResult().setMoreFeedbacksReceivedFrom(result.getMoreFeedbacksReceivedFrom());

                        totalTime += result.getExecutionTime();
                        System.out.println("=========");
                        System.out.println(result);
                        System.out.println("=========");

                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                long endTime = System.currentTimeMillis();
                data.getFinalResult().setExecutionTime(endTime - startTime);


                data.getFinalResult().setPercentageOfGoodFeedbacks((int)(goodPercentage * 1.0 / futures.size()));
                data.getFinalResult().setPercentageOfBadFeedbacks((int)(badPercentage * 1.0 / futures.size()));

                totalProcessedFeedbacksCountLabel.setText(data.getFinalResult().getFeedbacksCount() + "");
                genderLabel.setText(data.getFinalResult().getMostFrequentlyUsedWord());
                mostFrequentlyUsedWordLabel.setText(data.getFinalResult().getMostFrequentlyUsedWord());
                goodPercentageLabel.setText(data.getFinalResult().getPercentageOfGoodFeedbacks() + "%");
                badPercentageLabel.setText(data.getFinalResult().getPercentageOfBadFeedbacks() + "%");


                for(int i = 0; i < data.getResults().size(); ++i)
                {
                    System.out.println(i + ". " + data.getResults().get(i));
                }
                System.out.println("Total execution time: " + totalTime + " ms");
                System.out.println("Execution time: " + (endTime - startTime) + " ms");

            }

            private void clearThreadTable() {
                DefaultTableModel model = (DefaultTableModel) threadsTable.getModel();
                model.setRowCount(0);
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
