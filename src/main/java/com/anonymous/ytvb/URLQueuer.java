package com.anonymous.ytvb;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;

public class URLQueuer extends ObjectQueuer<URL> {

    public URLQueuer(File stringFile) throws FileNotFoundException {
        super(stringFile);
    }

    @Override
    public URL processElement(String element) throws MalformedURLException {
        return new URL(element);
    }

    @Override
    public String parseErrorMessage() {
        return "Error parsing url at line %s";
    }
}
