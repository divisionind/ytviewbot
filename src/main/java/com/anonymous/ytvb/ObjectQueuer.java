package com.anonymous.ytvb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used for multi-thread-safe retrieving of strings from a pool
 */
public abstract class ObjectQueuer<T> {

    public abstract T processElement(String element) throws Exception;

    public abstract String parseErrorMessage();

    private List<T> objects;
    private AtomicInteger ai;

    public ObjectQueuer(File objectFile) throws FileNotFoundException {
        ai = new AtomicInteger(0);
        objects = new ArrayList<>();

        try (Scanner s = new Scanner(new FileReader(objectFile))) {
            int i = 0;
            NumberFormat numberFormatter = NumberFormat.getNumberInstance();
            while (s.hasNext()) {
                i++;
                try {
                    objects.add(processElement(s.nextLine()));
                } catch (Exception e) {
                    YTViewBot.log.severe(String.format(parseErrorMessage(), numberFormatter.format(i)));
                }
            }
        }
    }

    public T getObject() {
        int on = ai.getAndIncrement();
        if (on >= objects.size()) {
            ai.set(0);
            on = 0;
        }
        return objects.get(on);
    }
}
