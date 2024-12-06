package org.ui.swingworkers;

import org.entity.FeedbackLogProcessingResult;
import org.service.FeedbackLogAnalyzeService;
import org.ui.tools.ToolsForUI;

import javax.swing.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

public class LogsAnalyzeSwingWorker extends SwingWorker<FeedbackLogProcessingResult, FeedbackLogProcessingResult> {
    Semaphore semaphore;
    Semaphore mutex;
    ConcurrentLinkedQueue<String> queue;
    List<FeedbackLogProcessingResult> results;

    JTable threadsTable;

    long startTime;
    long endTime;

    public LogsAnalyzeSwingWorker(Semaphore semaphore, Semaphore mutex, ConcurrentLinkedQueue<String> queue, List<FeedbackLogProcessingResult> results,
                             JTable threadsTable) {
        this.semaphore = semaphore;
        this.mutex = mutex;
        this.queue = queue;
        this.results = results;
        this.threadsTable = threadsTable;
    }

    @Override
    protected void process(List<FeedbackLogProcessingResult> chunks) {
        FeedbackLogProcessingResult result = chunks.get(chunks.size() - 1);
        ToolsForUI.updateThreadTable(result.getThread(),
                result.getStatus(),
                result.getLogName(),
                "-",
                threadsTable);
    }

    @Override
    protected void done() {
        try {
            FeedbackLogProcessingResult result = get();
            ToolsForUI.updateThreadTable(result.getThread(),
                    result.getStatus(),
                    result.getLogName(),
                    result.getExecutionTime() + " ms",
                    threadsTable);
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
                    publish(result);

                    Thread.sleep(1000);
                    // Start proceed
                    result.setStatus("Proceed started");
                    publish(result);

                    FeedbackLogAnalyzeService service = new FeedbackLogAnalyzeService(logName);
                    mutex.release();
                    Thread.sleep(5000);
                    result = service.analyze();
                    //Proceeded

                    result.setLogName(logName);
                    result.setThread(Thread.currentThread());
                    result.setStatus("Proceeded");
                    publish(result);
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