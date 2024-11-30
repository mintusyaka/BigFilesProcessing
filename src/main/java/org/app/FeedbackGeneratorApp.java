package org.app;

import org.entity.Feedback;
import org.entity.Log;
import org.generator.FeedbackLogGenerator;
import org.generator.config.FeedbackLogGeneratorRandConfig;
import org.serializer.FeedbackLogSerializer;

import java.time.LocalDate;

public class FeedbackGeneratorApp {
    public static void main(String[] args) {
        String filename = "log_test_4.txt";

        FeedbackLogGenerator generator = new FeedbackLogGenerator(new FeedbackLogGeneratorRandConfig());

        Log<Feedback> log = new Log<Feedback>(null, LocalDate.now());
        log.setLogRecords(generator.generate());

        try {
            FeedbackLogSerializer.serialize(log, filename, true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Log<Feedback> newLog = null;
        try {
            newLog = FeedbackLogSerializer.deserialize(filename);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(newLog != null)
        for(Feedback feedback : newLog.getLogRecords())
        {
            System.out.println(feedback);
        }


    }
}
