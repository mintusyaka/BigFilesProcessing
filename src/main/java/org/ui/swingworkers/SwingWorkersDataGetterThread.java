package org.ui.swingworkers;

import org.entity.FeedbackLogProcessingResult;
import org.ui.entity.DataForLogsProcessing;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SwingWorkersDataGetterThread extends Thread {
    List<SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult>> workers;
    List<FeedbackLogProcessingResult> results;

    DataForLogsProcessing data;
    JLabel totalProcessedFeedbacksCountLabel;
    JLabel genderLabel;
    JLabel mostFrequentlyUsedWordLabel;
    JLabel goodPercentageLabel;
    JLabel badPercentageLabel;

    int goodPercentage = 0;
    int badPercentage = 0;


    public SwingWorkersDataGetterThread(List<SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult>> workers,
                                        List<FeedbackLogProcessingResult> results,
                                        DataForLogsProcessing data,
                                        JLabel totalProcessedFeedbacksCountLabel,
                                        JLabel genderLabel,
                                        JLabel mostFrequentlyUsedWordLabel,
                                        JLabel goodPercentageLabel,
                                        JLabel badPercentageLabel) {
        this.workers = workers;
        this.results = results;
        this.data = data;
        this.totalProcessedFeedbacksCountLabel = totalProcessedFeedbacksCountLabel;
        this.genderLabel = genderLabel;
        this.goodPercentageLabel = goodPercentageLabel;
        this.badPercentageLabel = badPercentageLabel;
        this.mostFrequentlyUsedWordLabel = mostFrequentlyUsedWordLabel;
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

                data.getFinalResult().setFeedbacksCount(data.getFinalResult().getFeedbacksCount() + result.getFeedbacksCount());
                data.getFinalResult().setExecutionTime(data.getFinalResult().getExecutionTime() + result.getExecutionTime());
                goodPercentage += result.getPercentageOfGoodFeedbacks();
                badPercentage += result.getPercentageOfBadFeedbacks();
                data.getFinalResult().setMostFrequentlyUsedWord(result.getMostFrequentlyUsedWord());
                data.getFinalResult().setMoreFeedbacksReceivedFrom(result.getMoreFeedbacksReceivedFrom());

            } catch (InterruptedException | ExecutionException ex) {
                throw new RuntimeException(ex);
            }
        }

        for(int i = 0; i < results.size(); ++i)
        {
            System.out.println(i + ". " + results.get(i));
        }
        System.out.println("Total execution time: " + totalTime);

        getFinalResult();

    }

    public void getFinalResult() {
        data.getFinalResult().setPercentageOfGoodFeedbacks((int)(goodPercentage * 1.0 / workers.size()));
        data.getFinalResult().setPercentageOfBadFeedbacks((int)(badPercentage * 1.0 / workers.size()));

        totalProcessedFeedbacksCountLabel.setText(data.getFinalResult().getFeedbacksCount() + "");
        genderLabel.setText(data.getFinalResult().getMostFrequentlyUsedWord());
        mostFrequentlyUsedWordLabel.setText(data.getFinalResult().getMostFrequentlyUsedWord());
        goodPercentageLabel.setText(data.getFinalResult().getPercentageOfGoodFeedbacks() + "%");
        badPercentageLabel.setText(data.getFinalResult().getPercentageOfBadFeedbacks() + "%");
    }
}