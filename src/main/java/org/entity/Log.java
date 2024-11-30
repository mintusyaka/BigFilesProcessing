package org.entity;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class Log<T> {
    LocalDate date;
    List<T> logRecords;

    public Log(List<T> logRecords, LocalDate date) {
        this.logRecords = logRecords;
        this.date = date;
    }

    public List<T> getLogRecords() {
        return logRecords;
    }

    public void setLogRecords(List<T> logRecords) {
        this.logRecords = logRecords;
    }

    public void addLogRecord(T logRecord) {
        logRecords.add(logRecord);
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
