package org.ui.swingworkers;

import org.entity.FeedbackLogProcessingResult;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ExecutionException;

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