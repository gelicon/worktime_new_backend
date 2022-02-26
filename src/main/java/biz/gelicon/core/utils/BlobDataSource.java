package biz.gelicon.core.utils;

import javax.activation.DataSource;
import java.io.*;

public class BlobDataSource implements DataSource {

    private final InputStream data;
    private final String name;

    public BlobDataSource(InputStream data, String name) throws IOException {
        if (data == null)
            throw new IOException(String.format("Вложение \"%s\" пустое", name));
        this.data = data;
        this.name = name;
    }

    public String getContentType() {
        return MimeMap.getContentTypeFor(name, "application/octet-stream");
    }

    public InputStream getInputStream() throws IOException {
        return new BufferedInputStream(data);
    }

    public String getName() {
        return name;
    }

    public OutputStream getOutputStream() throws IOException {
        throw new IOException();
    }

}