package ru.saidgadjiev.prototype.core.http;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by said on 22.09.2018.
 */
public abstract class FilePart {

    private final String filename;

    public FilePart(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public abstract InputStream getInputStream() throws IOException;
}
