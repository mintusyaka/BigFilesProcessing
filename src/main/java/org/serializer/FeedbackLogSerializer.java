package org.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.entity.Feedback;
import org.entity.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.http.WebSocketHandshakeException;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class FeedbackLogSerializer {
    public static void serialize(Log<Feedback> log, String filename) throws Exception {
        String[] split = filename.split("\\.");
        if(split.length < 2)
        {
            throw new Exception("Incorrect filename");
        }

        String newFilename = split[0] + "_" + log.getDate().toString() + "." + split[1];

        File file = new File(newFilename);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(FileWriter fw = new FileWriter(newFilename)) {
            gson.toJson(log, fw);
        } catch (IOException e) {
            throw new Exception("Cannot serialize feedback log");
        }
    }

    public static void serialize(Log<Feedback> log, String filename, boolean isTest) throws Exception {
        String newFilename;
        if(isTest)
        {
            newFilename = filename;
        }
        else {
            String[] split = filename.split("\\.");
            if (split.length < 2) {
                throw new Exception("Incorrect filename");
            }

            newFilename = split[0] + "_" + log.getDate().toString() + "." + split[1];
        }


        File file = new File(newFilename);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try(FileWriter fw = new FileWriter(newFilename)) {
            gson.toJson(log, fw);
        } catch (IOException e) {
            throw new Exception("Cannot serialize feedback log");
        }
    }

    public static Log<Feedback> deserialize(String filename) throws Exception {
        try(FileReader fr = new FileReader(filename)) {
            Type listOfOrderType = new TypeToken<Log<Feedback>>() {}.getType();
            Gson gson = new Gson();
            Log<Feedback> log = gson.fromJson(fr, listOfOrderType);

            return log;
        } catch (IOException e) {
            throw new Exception("Cannot deserialize feedback log");
        }
    }
}
