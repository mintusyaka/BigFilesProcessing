package org.ui.entity;

import org.entity.FeedbackLogProcessingResult;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;

public class DataForLogsProcessing {
    int numberOfThreads;
    int numberOfSimulatedThreads;
    ConcurrentLinkedQueue<String> queue;
    List<FeedbackLogProcessingResult> results;
    Semaphore semaphore;
    Semaphore mutex;
    FeedbackLogProcessingResult finalResult;
    ScheduledThreadPoolExecutor scheduler;
    int fileCount;

    public DataForLogsProcessing() {
        this.numberOfThreads = 0;
        this.numberOfSimulatedThreads = 0;
        this.queue = new ConcurrentLinkedQueue<>();
        this.results = new ArrayList<>();
        this.semaphore = new Semaphore(1);
        this.mutex = new Semaphore(1);
        this.finalResult = new FeedbackLogProcessingResult();
    }

    public DataForLogsProcessing(int numberOfThreads, int numberOfSimulatedThreads, ConcurrentLinkedQueue<String> queue, List<FeedbackLogProcessingResult> results, Semaphore semaphore, Semaphore mutex, FeedbackLogProcessingResult finalResult, ScheduledThreadPoolExecutor scheduler) {
        this.numberOfThreads = numberOfThreads;
        this.numberOfSimulatedThreads = numberOfSimulatedThreads;
        this.queue = queue;
        this.results = results;
        this.semaphore = semaphore;
        this.mutex = mutex;
        this.finalResult = finalResult;
        this.scheduler = scheduler;
        fileCount = 0;
    }

    public void setData(JLabel currentMaxThreadsCountLabel, JLabel currentMaxSimulatedThreadsCount, JTable logsProcessingProgressTable)
    {
        int numberOfThreads = 0;
        int numberOfSimulatedThreads = 0;
        try {
            numberOfThreads = Integer.parseInt(currentMaxThreadsCountLabel.getText());
            numberOfSimulatedThreads = Integer.parseInt(currentMaxSimulatedThreadsCount.getText());
        } catch (Exception ex) {
            return;
        }
        setFinalResult(new FeedbackLogProcessingResult());
        setMutex(new Semaphore(1));
        setNumberOfThreads(numberOfThreads);
        setNumberOfSimulatedThreads(numberOfSimulatedThreads);
        setSemaphore(new Semaphore(numberOfSimulatedThreads));
        setQueue(createLogQueue(logsProcessingProgressTable));
        setResults(new ArrayList<>());
        setScheduler(new ScheduledThreadPoolExecutor(numberOfThreads));
        setFileCount();
    }

    private ConcurrentLinkedQueue<String> createLogQueue(JTable logsProcessingProgressTable)
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

    public void setFileCount() {
        fileCount = queue.size();
    }
    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }
    public int getFileCount() {
        return fileCount;
    }

    public void setScheduler(ScheduledThreadPoolExecutor scheduler) {
        this.scheduler = scheduler;
    }
    public ScheduledThreadPoolExecutor getScheduler() {
        return scheduler;
    }

    public void setFinalResult(FeedbackLogProcessingResult finalResult) {
        this.finalResult = finalResult;
    }
    public FeedbackLogProcessingResult getFinalResult() {
        return finalResult;
    }

    public void setMutex(Semaphore mutex) {
        this.mutex = mutex;
    }
    public Semaphore getMutex() {
        return mutex;
    }

    public void setSemaphore(Semaphore semaphore) {
        this.semaphore = semaphore;
    }
    public Semaphore getSemaphore() {
        return semaphore;
    }

    public void setQueue(ConcurrentLinkedQueue<String> queue) {
        this.queue = queue;
    }
    public ConcurrentLinkedQueue<String> getQueue() {
        return queue;
    }

    public void setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;
    }
    public int getNumberOfThreads() {
        return numberOfThreads;
    }

    public void setNumberOfSimulatedThreads(int numberOfSimulatedThreads) {
        this.numberOfSimulatedThreads = numberOfSimulatedThreads;
    }
    public int getNumberOfSimulatedThreads() {
        return numberOfSimulatedThreads;
    }

    public void setResults(List<FeedbackLogProcessingResult> results) {
        this.results = results;
    }
    public List<FeedbackLogProcessingResult> getResults() {
        return results;
    }
}