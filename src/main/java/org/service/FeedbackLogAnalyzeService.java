package org.service;

import org.entity.Feedback;
import org.entity.FeedbackLogProcessingResult;
import org.entity.Log;
import org.serializer.FeedbackLogSerializer;

import java.util.HashMap;
import java.util.Map;

public class FeedbackLogAnalyzeService {
    String logName;
    Log<Feedback> log;

    public FeedbackLogAnalyzeService(String logName) {
        this.logName = logName;
        this.log = null;
    }

    public FeedbackLogProcessingResult analyze()
    {
        FeedbackLogProcessingResult result = new FeedbackLogProcessingResult();

        getLog();

        result.setFeedbacksCount(log.getLogRecords().size());

        result.setMoreFeedbacksReceivedFrom(whoSentMoreFeedbacks());

        result.setMostFrequentlyUsedWord(getMostFrequentlyUsedWord());

        result.setPercentageOfGoodFeedbacks(getCountOfGoodFeedbacks() * 100 / log.getLogRecords().size());

        result.setPercentageOfBadFeedbacks(getCountOfBadFeedbacks() * 100 / log.getLogRecords().size());

        return result;
    }

    private void getLog() {
        try {
            log = FeedbackLogSerializer.deserialize(logName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String whoSentMoreFeedbacks()
    {
        Map<String, Integer> feedbackCount = new HashMap<>();

        // Count the feedbacks per sender
        for (Feedback feedback : log.getLogRecords()) {
            String sender = feedback.getPerson().getGender();
            feedbackCount.put(sender, feedbackCount.getOrDefault(sender, 0) + 1);
        }

        // Find the sender with the maximum feedback count
        String personWithMostFeedbacks = null;
        int maxFeedbackCount = 0;

        for (Map.Entry<String, Integer> entry : feedbackCount.entrySet()) {
            if (entry.getValue() > maxFeedbackCount) {
                maxFeedbackCount = entry.getValue();
                personWithMostFeedbacks = entry.getKey();
            }
        }

        return personWithMostFeedbacks;
    }

    private int getCountOfGoodFeedbacks() {
        int goodFeedbacksCount = 0;

        for(Feedback feedback : log.getLogRecords())
        {
            if(feedback.getRate() == Feedback.FeedbackRate.Good)
                goodFeedbacksCount++;
        }

        return goodFeedbacksCount;
    }

    private int getCountOfBadFeedbacks() {
        int badFeedbacksCount = 0;
        for(Feedback feedback : log.getLogRecords())
        {
            if(feedback.getRate() == Feedback.FeedbackRate.Bad)
                badFeedbacksCount++;
        }

        return badFeedbacksCount;
    }

    private String getMostFrequentlyUsedWord() {
        Map<String, Integer> wordCount = new HashMap<>();

        // Process each feedback's message
        for (Feedback feedback : log.getLogRecords()) {
            String message = feedback.getMessage();
            // Split the message into words, normalize by lowercasing and removing non-alphabetic characters
            String[] words = message.split("\\s+");

            for (String word : words) {
                // Remove non-alphabetical characters and convert to lowercase
                word = word.replaceAll("[^a-zA-Z]", "").toLowerCase();

                if (!word.isEmpty() && !word.equals("i")) {
                    // Count the frequency of each word
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }
        }

        // Find the word with the highest count
        String mostFrequentWord = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentWord = entry.getKey();
            }
        }

        return mostFrequentWord;
    }

}