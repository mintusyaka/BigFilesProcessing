package org.generator.config;

public class FeedbackLogGeneratorBadConfig extends FeedbackGeneratorLogConfig {
    public FeedbackLogGeneratorBadConfig() {
        feedbacksCount = 100000;
        goodRateChance = 0.2;
    }
}
