package org.entity;

public class Feedback {
    private String message;
    private Person person;
    private FeedbackRate rate;

    public Feedback(String message, Person person, FeedbackRate rate) {
        this.message = message;
        this.person = person;
        this.rate = rate;
    }

    public String getMessage() {
        return message;
    }

    public Person getPerson() {
        return person;
    }

    public FeedbackRate getRate() {
        return rate;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setRate(FeedbackRate rate) {
        this.rate = rate;
    }

    static public enum FeedbackRate {
        Good,
        Bad
    }

    @Override
    public String toString() {
        return "Feedback{" +
                "message='" + message + '\'' +
                ", person=" + person +
                ", rate=" + rate +
                '}';
    }
}
