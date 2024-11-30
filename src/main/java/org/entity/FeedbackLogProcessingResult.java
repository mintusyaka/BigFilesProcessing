package org.entity;

public class FeedbackLogProcessingResult {
    long feedbacksCount;
    String moreFeedbacksReceivedFrom;
    String mostFrequentlyUsedWord;
    int percentageOfGoodFeedbacks;
    int percentageOfBadFeedbacks;

    long executionTime;

    public long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    Thread thread;
    String logName;
    String status;

    public FeedbackLogProcessingResult() {
        feedbacksCount = 0;
        moreFeedbacksReceivedFrom = "";
        mostFrequentlyUsedWord = "";
        percentageOfGoodFeedbacks = 0;
        percentageOfBadFeedbacks = 0;
    }

    public int getPercentageOfBadFeedbacks() {
        return percentageOfBadFeedbacks;
    }
    public int getPercentageOfGoodFeedbacks() {
        return percentageOfGoodFeedbacks;
    }
    public String getMostFrequentlyUsedWord() {
        return mostFrequentlyUsedWord;
    }
    public String getMoreFeedbacksReceivedFrom() {
        return moreFeedbacksReceivedFrom;
    }
    public long getFeedbacksCount() {
        return feedbacksCount;
    }

    public void setFeedbacksCount(long feedbacksCount) {
        this.feedbacksCount = feedbacksCount;
    }
    public void setMoreFeedbacksReceivedFrom(String moreFeedbacksReceivedFrom) {
        this.moreFeedbacksReceivedFrom = moreFeedbacksReceivedFrom;
    }
    public void setMostFrequentlyUsedWord(String mostFrequentlyUsedWord) {
        this.mostFrequentlyUsedWord = mostFrequentlyUsedWord;
    }
    public void setPercentageOfBadFeedbacks(int percentageOfBadFeedbacks) {
        this.percentageOfBadFeedbacks = percentageOfBadFeedbacks;
    }
    public void setPercentageOfGoodFeedbacks(int percentageOfGoodFeedbacks) {
        this.percentageOfGoodFeedbacks = percentageOfGoodFeedbacks;
    }

    public void setThread(Thread thread) {
        this.thread = thread;
    }
    public Thread getThread() {
        return thread;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }
    public String getLogName() {
        return logName;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "FeedbackLogProcessingResult{" +
                "feedbacksCount=" + feedbacksCount +
                ", moreFeedbacksReceivedFrom='" + moreFeedbacksReceivedFrom + '\'' +
                ", mostFrequentlyUsedWord='" + mostFrequentlyUsedWord + '\'' +
                ", percentageOfGoodFeedbacks=" + percentageOfGoodFeedbacks +
                ", percentageOfBadFeedbacks=" + percentageOfBadFeedbacks +
                '}';
    }
}
