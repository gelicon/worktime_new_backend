package biz.gelicon.core.security;

import java.io.*;

public abstract class VerificationData {

    public abstract void write(DataOutputStream out) throws IOException;

    public abstract void read(DataInputStream s) throws IOException;

    public byte[] write() {
        try {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(buf);
            write(out);
            out.flush();
            return buf.toByteArray();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public VerificationData read(byte[] data) {
        try {
            DataInputStream s = new DataInputStream(new ByteArrayInputStream(data));
            try {
                read(s);
                return this;
            } finally {
                s.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
