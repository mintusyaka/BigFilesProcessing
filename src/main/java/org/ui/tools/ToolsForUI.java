package org.ui.tools;

import org.entity.FeedbackLogProcessingResult;
import org.ui.entity.DataForLogsProcessing;
import org.ui.futures.FeedbackLogProcessor;
import org.ui.swingworkers.LogsAnalyzeSwingWorker;
import org.ui.swingworkers.SwingWorkersDataGetterThread;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ToolsForUI {
    public static void fillThreadsTableHeader(JTable threadsTable)
    {
        DefaultTableModel model = (DefaultTableModel) threadsTable.getModel();
        model.setColumnIdentifiers(new String[] { "Thread ID", "Status", "File", "Execution Time"});
    }

    public static void clearThreadTable(JTable threadsTable) {
        DefaultTableModel model = (DefaultTableModel) threadsTable.getModel();
        model.setRowCount(0);
    }

    public static void updateThreadTable(Thread thread, String status, String logName, String executionTime, JTable threadsTable)
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

    public static DataForLogsProcessing initProcessing(JTable threadsTable,
                                      JLabel currentMaxThreadsCountLabel,
                                      JLabel currentMaxSimulatedThreadsCount,
                                      JTable logsProcessingProgressTable
    ) {
        ToolsForUI.fillThreadsTableHeader(threadsTable);
        ToolsForUI.clearThreadTable(threadsTable);

        DataForLogsProcessing data = new DataForLogsProcessing();
        data.setData(currentMaxThreadsCountLabel, currentMaxSimulatedThreadsCount, logsProcessingProgressTable);

        return data;
    }

    public static void processingWithFuture(
            DataForLogsProcessing data,
            JTable threadsTable,
            JLabel totalProcessedFeedbacksCountLabel,
            JLabel genderLabel,
            JLabel mostFrequentlyUsedWordLabel,
            JLabel goodPercentageLabel,
            JLabel badPercentageLabel
    ) {
        List<ScheduledFuture<FeedbackLogProcessingResult>> futures = new ArrayList<>();

        for (int i = 0; i < data.getFileCount(); ++i) {
            futures.add(data.getScheduler().schedule(new FeedbackLogProcessor(data), 10, TimeUnit.MILLISECONDS));
        }

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


        getFinalResult(
                data,
                goodPercentage,
                badPercentage,
                totalProcessedFeedbacksCountLabel,
                genderLabel,
                mostFrequentlyUsedWordLabel,
                goodPercentageLabel,
                badPercentageLabel,
                futures
        );

        for(int i = 0; i < data.getResults().size(); ++i)
        {
            System.out.println(i + ". " + data.getResults().get(i));
        }
        System.out.println("Total execution time: " + totalTime + " ms");
        System.out.println("Execution time: " + (endTime - startTime) + " ms");
    }

    public static void processingWithSwingWorker(
            DataForLogsProcessing data,
            JTable threadsTable,
            JLabel totalProcessedFeedbacksCountLabel,
            JLabel genderLabel,
            JLabel mostFrequentlyUsedWordLabel,
            JLabel goodPercentageLabel,
            JLabel badPercentageLabel
    ) {
        List<SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult>> workers = new ArrayList<>();

        for (int i = 0; i < data.getFileCount(); ++i) {
            workers.add(new LogsAnalyzeSwingWorker(data.getSemaphore(), data.getMutex(), data.getQueue(), data.getResults(), threadsTable));
            data.getScheduler().schedule(workers.get(i), 2, TimeUnit.SECONDS);
        }

        SwingWorkersDataGetterThread getter =
                new SwingWorkersDataGetterThread(
                        workers,
                        data.getResults(),
                        data,
                        totalProcessedFeedbacksCountLabel,
                        genderLabel,
                        mostFrequentlyUsedWordLabel,
                        goodPercentageLabel,
                        badPercentageLabel
                );
        long startTime = System.currentTimeMillis();
        getter.start();
        System.out.println("Parallel execution time: " + (System.currentTimeMillis() - startTime));

    }

    public static void getFinalResult(
            DataForLogsProcessing data,
            int goodPercentage,
            int badPercentage,
            List<SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult>> workers,
            JLabel totalProcessedFeedbacksCountLabel,
            JLabel genderLabel,
            JLabel mostFrequentlyUsedWordLabel,
            JLabel goodPercentageLabel,
            JLabel badPercentageLabel
    ) {
        data.getFinalResult().setPercentageOfGoodFeedbacks((int)(goodPercentage * 1.0 / workers.size()));
        data.getFinalResult().setPercentageOfBadFeedbacks((int)(badPercentage * 1.0 / workers.size()));

        totalProcessedFeedbacksCountLabel.setText(data.getFinalResult().getFeedbacksCount() + "");
        genderLabel.setText(data.getFinalResult().getMostFrequentlyUsedWord());
        mostFrequentlyUsedWordLabel.setText(data.getFinalResult().getMostFrequentlyUsedWord());
        goodPercentageLabel.setText(data.getFinalResult().getPercentageOfGoodFeedbacks() + "%");
        badPercentageLabel.setText(data.getFinalResult().getPercentageOfBadFeedbacks() + "%");
    }

    public static void getFinalResult(
            DataForLogsProcessing data,
            int goodPercentage,
            int badPercentage,
            JLabel totalProcessedFeedbacksCountLabel,
            JLabel genderLabel,
            JLabel mostFrequentlyUsedWordLabel,
            JLabel goodPercentageLabel,
            JLabel badPercentageLabel,
            List<ScheduledFuture<FeedbackLogProcessingResult>> futures
    ) {
        data.getFinalResult().setPercentageOfGoodFeedbacks((int)(goodPercentage * 1.0 / futures.size()));
        data.getFinalResult().setPercentageOfBadFeedbacks((int)(badPercentage * 1.0 / futures.size()));

        totalProcessedFeedbacksCountLabel.setText(data.getFinalResult().getFeedbacksCount() + "");
        genderLabel.setText(data.getFinalResult().getMostFrequentlyUsedWord());
        mostFrequentlyUsedWordLabel.setText(data.getFinalResult().getMostFrequentlyUsedWord());
        goodPercentageLabel.setText(data.getFinalResult().getPercentageOfGoodFeedbacks() + "%");
        badPercentageLabel.setText(data.getFinalResult().getPercentageOfBadFeedbacks() + "%");
    }
}
