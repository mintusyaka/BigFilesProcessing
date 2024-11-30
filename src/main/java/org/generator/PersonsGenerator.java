package org.generator;

import org.entity.Person;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PersonsGenerator {
    private final int PERSONS_COUNT = 100;

    public void generate(List<Person> persons)
    {
        List<String> names = new ArrayList<>();
        names.add("John");
        names.add("Ryan");
        names.add("George");
        names.add("Alex");
        names.add("Harry");
        names.add("Frank");

        for(int i = 0; i < PERSONS_COUNT; i++)
        {
            persons.add(new Person(getRandomName(names), getRandomGender()));
        }

    }

    private String getRandomName(List<String> names)
    {
        Random random = new Random();
        return names.get(random.nextInt(names.size() - 1));
    }

    private String getRandomGender()
    {
        Random random = new Random();
        return random.nextBoolean() ? "male" : "female";
    }
}
