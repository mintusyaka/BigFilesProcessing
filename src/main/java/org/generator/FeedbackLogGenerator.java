package org.generator;

import org.entity.Feedback;
import org.entity.Person;
import org.generator.config.FeedbackGeneratorLogConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FeedbackLogGenerator {
    private FeedbackGeneratorLogConfig config;

    public FeedbackLogGenerator(FeedbackGeneratorLogConfig config)
    {
        this.config = config;
    }

    public List<Feedback> generate()
    {
        List<Feedback> log = new ArrayList<>();

        List<String> messages = new ArrayList<>();
        messages.add("Very good");
        messages.add("Very bad");
        messages.add("Very nice");
        messages.add("I don't like it");
        messages.add("I don't want to tell anything");
        messages.add("Hmm.. Maybe I go here again");
        messages.add("\"Carolina\" is a very good place to eat and spent time with friends. I think I'm go here again. Maybe.");

        List<Person> persons = new ArrayList<>();
        PersonsGenerator personsGenerator = new PersonsGenerator();
        personsGenerator.generate(persons);

        for(int i = 0; i < config.feedbacksCount; i++)
        {
            log.add(new Feedback(getRandomMessage(messages), getRandomPerson(persons), getRandomRate()));
        }

        return log;
    }

    public Person getRandomPerson(List<Person> persons)
    {
        Random random = new Random();
        return persons.get(random.nextInt(persons.size() - 1));
    }

    public String getRandomMessage(List<String> messages)
    {
        Random random = new Random();
        return messages.get(random.nextInt(messages.size() - 1));
    }

    public Feedback.FeedbackRate getRandomRate()
    {
        Random random = new Random();

        if(config.goodRateChance < 0)
        {
            return random.nextDouble() <= random.nextDouble() + random.nextDouble() - random.nextDouble() ? Feedback.FeedbackRate.Good : Feedback.FeedbackRate.Bad;
        }

        return random.nextDouble() <= config.goodRateChance ? Feedback.FeedbackRate.Good : Feedback.FeedbackRate.Bad;
    }
}
