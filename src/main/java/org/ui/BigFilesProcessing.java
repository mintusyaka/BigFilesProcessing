package org.ui;

import org.entity.FeedbackLogProcessingResult;
import org.service.FeedbackLogAnalyzeService;

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
                fillThreadsTableHeader();

                clearThreadTable();

                int numberOfThreads = 0;
                int numberOfSimulatedThreads = 0;
                try {
                    numberOfThreads = Integer.parseInt(currentMaxThreadsCountLabel.getText());
                    numberOfSimulatedThreads = Integer.parseInt(currentMaxSimulatedThreadsCount.getText());
                } catch (Exception ex) {
                    return;
                }

                ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(numberOfThreads);
                Semaphore semaphore = new Semaphore(numberOfSimulatedThreads);
                Semaphore mutex = new Semaphore(1);
                ConcurrentLinkedQueue<String> queue = createLogQueue();
                int fileCount = queue.size();
                List<FeedbackLogProcessingResult> results = new ArrayList<>();

                Callable<FeedbackLogProcessingResult> analyzeFilesTask = () -> {
                    try {
                        long startTime = System.currentTimeMillis();
                        semaphore.acquire();
                        FeedbackLogProcessingResult result = new FeedbackLogProcessingResult();
                        result.setThread(Thread.currentThread());
                        result.setLogName("-");
                        result.setStatus( "Not processed");

                        mutex.acquire();
                        if(queue.isEmpty()) {

                            mutex.release();
                            return result;
                        }
                        else {
                            try {
                                // Thread status to preparing to proceed
                                String logName = queue.poll();
                                result.setLogName(logName);
                                result.setStatus("Preparing to proceed");

                                Thread.sleep(1000);
                                // Start proceed

                                FeedbackLogAnalyzeService service = new FeedbackLogAnalyzeService(logName);
                                mutex.release();
                                //Proceeded
                                result = service.analyze();
                                result.setLogName(logName);
                                result.setThread(Thread.currentThread());
                                result.setStatus("Proceeded");
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            } finally {
                                semaphore.release();
                            }
                        }

                        if(result.getFeedbacksCount() != 0)
                        {
                            result.setStatus("Proceeded");
                            long endTime = System.currentTimeMillis();
                            result.setExecutionTime(endTime - startTime);
                            results.add(result);
                        }
                        return result;
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                };

                List<SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult>> workers = new ArrayList<>();

                for (int i = 0; i < fileCount; ++i) {
                    workers.add(new DataAnalyzeThread(semaphore, mutex, queue, results));
                    scheduler.schedule(workers.get(i), 10, TimeUnit.MILLISECONDS);
                }

                new SwingWorkersDataGetterThread(workers, results).start();

/*
                List<ScheduledFuture<FeedbackLogProcessingResult>> futures = new ArrayList<>();

                for (int i = 0; i < fileCount; ++i) {
                    futures.add(scheduler.schedule(analyzeFilesTask, 10, TimeUnit.MILLISECONDS));
                }

                finalResult = new FeedbackLogProcessingResult();

                long totalTime = 0;
                long startTime = System.currentTimeMillis();

                int goodPercentage = 0;
                int badPercentage = 0;

                for (ScheduledFuture<FeedbackLogProcessingResult> future : futures) {
                    try {
                        FeedbackLogProcessingResult result = future.get();
                        updateThreadTable(result.getThread(), result.getStatus(), result.getLogName(), result.getExecutionTime() + " ms");

                        finalResult.setFeedbacksCount(finalResult.getFeedbacksCount() + result.getFeedbacksCount());
                        finalResult.setExecutionTime(finalResult.getExecutionTime() + result.getExecutionTime());
                        goodPercentage += result.getPercentageOfGoodFeedbacks();
                        badPercentage += result.getPercentageOfBadFeedbacks();
                        finalResult.setMostFrequentlyUsedWord(result.getMostFrequentlyUsedWord());
                        finalResult.setMoreFeedbacksReceivedFrom(result.getMoreFeedbacksReceivedFrom());

                        totalTime += result.getExecutionTime();
                        System.out.println("=========");
                        System.out.println(result);
                        System.out.println("=========");

                    } catch (InterruptedException | ExecutionException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                long endTime = System.currentTimeMillis();
                finalResult.setExecutionTime(endTime - startTime);


                finalResult.setPercentageOfGoodFeedbacks((int)(goodPercentage * 1.0 / futures.size()));
                finalResult.setPercentageOfBadFeedbacks((int)(badPercentage * 1.0 / futures.size()));

                totalProcessedFeedbacksCountLabel.setText(finalResult.getFeedbacksCount() + "");
                genderLabel.setText(finalResult.getMostFrequentlyUsedWord());
                mostFrequentlyUsedWordLabel.setText(finalResult.getMostFrequentlyUsedWord());
                goodPercentageLabel.setText(finalResult.getPercentageOfGoodFeedbacks() + "%");
                badPercentageLabel.setText(finalResult.getPercentageOfBadFeedbacks() + "%");


                for(int i = 0; i < results.size(); ++i)
                {
                    System.out.println(i + ". " + results.get(i));
                }
                System.out.println("Total execution time: " + totalTime + " ms");
                System.out.println("Execution time: " + (endTime - startTime) + " ms");
*/

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

    private ConcurrentLinkedQueue<String> createLogQueue()
    {
        int rows = logsProcessingProgressTable.getRowCount();

        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();

        for(int i = 0; i < rows; ++i)
        {
            String logName = (String) logsProcessingProgressTable.getValueAt(i, 0);
            queue.add(logName);
        }

        return queue;

    }

    private void fillThreadsTableHeader()
    {
        DefaultTableModel model = (DefaultTableModel) threadsTable.getModel();

        model.setColumnIdentifiers(new String[] { "Thread ID", "Status", "File", "Execution Time"});
    }

    private int isAnyFutureGetResult(List<ScheduledFuture<FeedbackLogProcessingResult>> futures)
    {
        for (int i = 0; i < futures.size(); i++) {
            ScheduledFuture<FeedbackLogProcessingResult> future = futures.get(i);
            if (future.isDone()) {
                return i;
            }
        }
        return -1;
    }

    public class FuturesDataGetterThread extends Thread {
        List<ScheduledFuture<FeedbackLogProcessingResult>> futures;
        List<FeedbackLogProcessingResult> results;

        FeedbackLogProcessingResult finalResult;

        public FuturesDataGetterThread(List<ScheduledFuture<FeedbackLogProcessingResult>> futures,
                                            List<FeedbackLogProcessingResult> results,
                                            FeedbackLogProcessingResult finalResult) {
            this.futures = futures;
            this.results = results;
            this.finalResult = finalResult;
        }

        @Override
        public void run() {
            long totalTime = 0;

            int goodPercentage = 0;
            int badPercentage = 0;

            for (ScheduledFuture<FeedbackLogProcessingResult> future : futures) {
                try {
                    FeedbackLogProcessingResult result = future.get();
                    finalResult.setFeedbacksCount(finalResult.getFeedbacksCount() + result.getFeedbacksCount());
                    finalResult.setExecutionTime(finalResult.getExecutionTime() + result.getExecutionTime());
                    goodPercentage += result.getPercentageOfGoodFeedbacks();
                    badPercentage += result.getPercentageOfBadFeedbacks();
                    finalResult.setMostFrequentlyUsedWord(result.getMostFrequentlyUsedWord());
                    finalResult.setMoreFeedbacksReceivedFrom(result.getMoreFeedbacksReceivedFrom());

                    totalTime += result.getExecutionTime();
                    System.out.println("=========");
                    System.out.println(result);
                    System.out.println("=========");

                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }

            finalResult.setPercentageOfGoodFeedbacks((int)(goodPercentage * 1.0 / futures.size()));
            finalResult.setPercentageOfBadFeedbacks((int)(badPercentage * 1.0 / futures.size()));

            for(int i = 0; i < results.size(); ++i)
            {
                System.out.println(i + ". " + results.get(i));
            }
            System.out.println("Total execution time: " + totalTime);

            System.out.println(finalResult.getFeedbacksCount() + " feedbacks received");
            System.out.println(finalResult.getMoreFeedbacksReceivedFrom());
            System.out.println(finalResult.getMostFrequentlyUsedWord());
            System.out.println(finalResult.getPercentageOfGoodFeedbacks());
            System.out.println(finalResult.getPercentageOfBadFeedbacks());
        }
    }

    public class SwingWorkersDataGetterThread extends Thread {
        List<SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult>> workers;
        List<FeedbackLogProcessingResult> results;

        public SwingWorkersDataGetterThread(List<SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult>> workers,
                                            List<FeedbackLogProcessingResult> results) {
            this.workers = workers;
            this.results = results;
        }


        @Override
        public void run() {
            long totalTime = 0;

            for (SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult> worker : workers) {
                try {
                    FeedbackLogProcessingResult result = worker.get();
                    totalTime += result.getExecutionTime();
                    System.out.println("=========");
                    System.out.println(result);
                    System.out.println("=========");

                } catch (InterruptedException | ExecutionException ex) {
                    throw new RuntimeException(ex);
                }
            }

            for(int i = 0; i < results.size(); ++i)
            {
                System.out.println(i + ". " + results.get(i));
            }
            System.out.println("Total execution time: " + totalTime);

        }
    }

    public class DataAnalyzeThread extends SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult> {
        Semaphore semaphore;
        Semaphore mutex;
        ConcurrentLinkedQueue<String> queue;
        List<FeedbackLogProcessingResult> results;

        long startTime;
        long endTime;

        public DataAnalyzeThread(Semaphore semaphore, Semaphore mutex, ConcurrentLinkedQueue<String> queue, List<FeedbackLogProcessingResult> results) {
            this.semaphore = semaphore;
            this.mutex = mutex;
            this.queue = queue;
            this.results = results;
        }

        @Override
        protected void process(List<FeedbackLogProcessingResult> chunks) {
            FeedbackLogProcessingResult result = chunks.get(chunks.size() - 1);
            updateThreadTable(result.getThread(), result.getStatus(), result.getLogName(), "-");
        }

        @Override
        protected void done() {
            try {
                FeedbackLogProcessingResult result = get();
                updateThreadTable(result.getThread(), result.getStatus(), result.getLogName(), result.getExecutionTime() + " ms");
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected FeedbackLogProcessingResult doInBackground() throws Exception {
            try {
                startTime = System.currentTimeMillis();
                semaphore.acquire();
                FeedbackLogProcessingResult result = new FeedbackLogProcessingResult();
                result.setThread(Thread.currentThread());
                result.setLogName("-");
                result.setStatus( "Not processed");

                publish(result);

                mutex.acquire();
                if(queue.isEmpty()) {

                    mutex.release();
                    return result;
                }
                else {
                    try {
                        // Thread status to preparing to proceed
                        String logName = queue.poll();
                        result.setLogName(logName);
                        result.setStatus("Preparing to proceed");

                        Thread.sleep(1000);
                        // Start proceed

                        FeedbackLogAnalyzeService service = new FeedbackLogAnalyzeService(logName);
                        mutex.release();
                        //Proceeded
                        result = service.analyze();
                        result.setLogName(logName);
                        result.setThread(Thread.currentThread());
                        result.setStatus("Proceeded");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        semaphore.release();
                    }
                }

                if(result.getFeedbacksCount() != 0)
                {
                    result.setStatus("Proceeded");
                    endTime = System.currentTimeMillis();
                    result.setExecutionTime(endTime - startTime);
                    results.add(result);
//                    new DataAnalyzeThread(semaphore, mutex, queue, results).execute();
                }
                return result;
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }


    }

    private void updateThreadTable(Thread thread, String status, String logName, String executionTime)
    {
        DefaultTableModel model = (DefaultTableModel) threadsTable.getModel();
        int rows = threadsTable.getRowCount();

        boolean threadAlreadyAdded = false;
        int threadRow = model.getRowCount() - 1;

        for(int i = 0; i < rows; ++i)
        {
            String threadName = (String) threadsTable.getValueAt(i, 0);
            if(threadName.equals(thread.getName().substring(7))) {
                threadAlreadyAdded = true;
                threadRow = i;
                break;
            }
        }

        if(threadAlreadyAdded)
        {
            model.setValueAt(status, threadRow, 1);
            model.setValueAt(logName, threadRow, 2);
            model.setValueAt(executionTime, threadRow, 3);
        } else {
            Vector<String> row = new Vector<>();
            row.add(thread.getName().substring(7));
            row.add(status);
            row.add(logName);
            row.add(executionTime);

            model.addRow(row);
        }

    }

}
