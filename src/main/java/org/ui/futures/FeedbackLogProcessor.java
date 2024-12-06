package org.ui.futures;

import org.entity.FeedbackLogProcessingResult;
import org.service.FeedbackLogAnalyzeService;
import org.ui.entity.DataForLogsProcessing;

import java.util.concurrent.Callable;

public class FeedbackLogProcessor implements Callable<FeedbackLogProcessingResult> {
    private final DataForLogsProcessing data;

    public FeedbackLogProcessor(DataForLogsProcessing data) {
        this.data = data;
    }

    @Override
    public FeedbackLogProcessingResult call() throws Exception {
            try {
                long startTime = System.currentTimeMillis();
                data.getSemaphore().acquire();
                FeedbackLogProcessingResult result = new FeedbackLogProcessingResult();
                result.setThread(Thread.currentThread());
                result.setLogName("-");
                result.setStatus( "Not processed");

                data.getMutex().acquire();
                if(data.getQueue().isEmpty()) {

                    data.getMutex().release();
                    return result;
                }
                else {
                    try {
                        // Thread status to preparing to proceed
                        String logName = data.getQueue().poll();
                        result.setLogName(logName);
                        result.setStatus("Preparing to proceed");

                        Thread.sleep(1000);
                        // Start proceed

                        FeedbackLogAnalyzeService service = new FeedbackLogAnalyzeService(logName);
                        data.getMutex().release();
                        //Proceeded
                        result = service.analyze();
                        result.setLogName(logName);
                        result.setThread(Thread.currentThread());
                        result.setStatus("Proceeded");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        data.getSemaphore().release();
                    }
                }

                if(result.getFeedbacksCount() != 0)
                {
                    result.setStatus("Proceeded");
                    long endTime = System.currentTimeMillis();
                    result.setExecutionTime(endTime - startTime);
                    data.getResults().add(result);
                }
                return result;
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
    }
}
