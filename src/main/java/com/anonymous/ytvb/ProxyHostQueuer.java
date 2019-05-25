package com.anonymous.ytvb;

import java.io.File;
import java.io.FileNotFoundException;

public class ProxyHostQueuer extends ObjectQueuer<ProxyHost> {

    private static final String SEPARATOR = " ";

    public ProxyHostQueuer(File objectFile) throws FileNotFoundException {
        super(objectFile);
    }

    @Override
    public ProxyHost processElement(String element) throws Exception {
        if (!element.contains(SEPARATOR)) throw new Exception();
        String[] parts = element.split(SEPARATOR);
        if (parts.length != 3) throw new Exception();

        return new ProxyHost(parts[0], Integer.parseInt(parts[1]), parts[2].equalsIgnoreCase("y"));
    }

    @Override
    public String parseErrorMessage() {
        return "Error parsing proxy host at line %s";
    }
}
