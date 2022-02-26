package biz.gelicon.core.security;

import biz.gelicon.core.model.Proguser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RecoveryData extends VerificationData {
    private Integer proguserId;
    private long timestamp;

    public RecoveryData() {
    }

    public RecoveryData(Proguser pu) {
        this.proguserId = pu.getProguserId();
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void write(DataOutputStream out) throws IOException {
        out.writeInt(proguserId);
        out.writeLong(timestamp);
    }

    @Override
    public void read(DataInputStream s) throws IOException {
        proguserId = s.readInt();
        timestamp = s.readLong();
    }

    public Integer getProguserId() {
        return proguserId;
    }

    public void setProguserId(Integer proguserId) {
        this.proguserId = proguserId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
