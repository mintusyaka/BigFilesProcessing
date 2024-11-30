package org.app;

import org.entity.FeedbackLogProcessingResult;
import org.service.FeedbackLogAnalyzeService;
import org.ui.BigFilesProcessing;

import javax.swing.*;

public class BigFileProcessingApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new BigFilesProcessing().show();
            }
        });

        /*String logName = "log_test_2.txt";
        FeedbackLogAnalyzeService service = new FeedbackLogAnalyzeService(logName);

        FeedbackLogProcessingResult result = service.analyze();

        System.out.println(result);*/

    }
}
