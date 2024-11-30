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

    private final int MAX_THREADS = 12;

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
                row.add("log_test_2024-11-30.txt");
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
                List<FeedbackLogProcessingResult> resultsInFutures = new ArrayList<>();

                List<ScheduledFuture<FeedbackLogProcessingResult>> futures = new ArrayList<>();



                Callable<FeedbackLogProcessingResult> analyzeFilesTask = () -> {
                    try {
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
                        return result;
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                };

                for(int i = 0; i < numberOfThreads; ++i)
                {
                    futures.add(scheduler.schedule(analyzeFilesTask, 10, TimeUnit.MILLISECONDS));
                    resultsInFutures.add(null);
                }

                //listener on new results in array of ScheduledFuture
                /*while(true)
                {
                    int index = isAnyFutureGetResult(futures);
                    if(index != -1)
                    {
                        try {
                            FeedbackLogProcessingResult result = futures.get(index).get();

                            results.add(futures.get(index).get());
                            futures.set(index, scheduler.schedule(analyzeFilesTask, 10, TimeUnit.MILLISECONDS));

                        } catch (InterruptedException | ExecutionException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                    if(results.contains(null))
                        break;
                }*/

                /*SwingWorker<FeedbackLogProcessingResult, Void> worker = new SwingWorker<>() {
                    @Override
                    protected FeedbackLogProcessingResult doInBackground() throws Exception {
                        // Wait for the result of the scheduled task (this is blocking until completion)
                        for(int i = 0; i < futures.size(); ++i) {
                            if(futures.get(i).isDone()) {
                                return futures.get(i).get();
                            }
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            // Get the result from the background task and update the UI
                            FeedbackLogProcessingResult result = get();
                            updateThreadTable(result.getThread(), result.getThread().getState().toString(), result.getLogName());
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        } finally {
                            if(results.size() == fileCount) {
                                System.out.println("All files processed!");

                                System.out.println("Scheduler shot down STARTED!");
                                scheduler.shutdownNow();
                                System.out.println("Scheduler shot down FINISHED!");
                                System.out.println("==========================");
                            }
                            else {
                                System.out.println("Some files are not processed!");
                            }
                        }
                    }
                };*/

                boolean isProceed = true;

                while(isProceed) {
                    isProceed = false;
                    for(int i = 0; i < numberOfThreads; ++i) {
                        if(futures.get(i).isDone())
                        {

                            try {
                                FeedbackLogProcessingResult result = futures.get(i).get();
                                if(result != null) {
                                    if(result.getFeedbacksCount() != 0) {
                                        isProceed = true;
                                        results.add(result);
                                        futures.remove(i);
                                        futures.add(scheduler.schedule(analyzeFilesTask, 10, TimeUnit.MILLISECONDS));
                                        updateThreadTable(result.getThread(), result.getThread().getState().toString(), result.getLogName());
                                    }
                                    else {
                                        updateThreadTable(result.getThread(), result.getThread().getState().toString(), result.getLogName());
                                    }
                                }
                            } catch (InterruptedException | ExecutionException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                        else {
                            isProceed = true;
                        }
                        if(results.size() == fileCount) {
                            isProceed = false;
                            break;
                        }
                    }
/*
                    System.out.println("=========");
                    for(int i = 0; i < results.size(); ++i)
                    {
                        System.out.println(results.get(i));
                    }
                    System.out.println("-----------");
                    System.out.println(queue.size());
                    System.out.println("=========");*/

                }


                /*while(!queue.isEmpty())
                {
                    int index = isAnyFutureGetResult(futures);
                    if(index != -1)
                    {
                        try {
                            FeedbackLogProcessingResult result = futures.get(index).get();

                            results.add(futures.get(index).get());
                            futures.set(index, scheduler.schedule(analyzeFilesTask, 10, TimeUnit.MILLISECONDS));

                        } catch (InterruptedException | ExecutionException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }*/

/*

                try {
                    if(scheduler.awaitTermination(10, TimeUnit.SECONDS))
                        System.out.println("\nTermination done\n");
                    else
                        scheduler.shutdownNow();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
*/
                System.out.println("Scheduler shot down STARTED!");

                try {
                    if(scheduler.awaitTermination(10, TimeUnit.SECONDS))
                    {
                        System.out.println("\nTermination done\n");
                    }
                    else {
                        scheduler.shutdownNow();
                        System.out.println("\nTermination failed\n");
                    }
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }

                System.out.println("Scheduler shot down FINISHED!");

                for(int i = 0; i < results.size(); ++i)
                {
                    System.out.println(results.get(i));
                }

                /*for(int i = 0; i < futures.size(); ++i)
                {
                    try {
                        System.out.println(futures.get(i).get());
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                        throw new RuntimeException(ex);
                    }
                }*/
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

        model.setColumnIdentifiers(new String[] { "Thread ID", "Status", "File"});
    }

    private void updateThreadTable(Thread thread, String status, String logName)
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
        } else {
            Vector<String> row = new Vector<>();
            row.add(thread.getName().substring(7));
            row.add(status);
            row.add(logName);

            model.addRow(row);
        }

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
}
