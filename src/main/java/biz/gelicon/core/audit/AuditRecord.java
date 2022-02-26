package biz.gelicon.core.audit;

import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.utils.OrmUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Document(collection = "audit",language = "russian")
public class AuditRecord {
    private static final Logger logger = LoggerFactory.getLogger(AuditRecord.class);

    @Id
    String logId;

    @Indexed
    private long datetime;

    private ProguserInfo proguser;

    @Indexed
    private int kind;

    private String path;

    @Indexed
    private String entity;   //nulleable

    private List<Integer> idValue; //nulleable

    private Object inputObject;

    private Object outputObject; //nulleable

    private String faultInfo; //nulleable

    private Long duration;

    public AuditRecord() {
        this.datetime = System.currentTimeMillis();
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
        this.kind = kind;
    }

    public String getKindName() {
        return AuditKind.values()[kind].name();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getFaultInfo() {
        return faultInfo;
    }

    public void setFaultInfo(String faultInfo) {
        this.faultInfo = faultInfo;
    }

    public Object getInputObject() {
        return inputObject;
    }

    public void setInputObject(Object inputObject) {
        this.inputObject = inputObject;
        autoRecognitionIdValue(inputObject);
    }

    public List<Integer> getIdValue() {
        return idValue;
    }

    public void setIdValue(List<Integer> idValue) {
        this.idValue = idValue;
    }

    private void autoRecognitionIdValue(Object inputObject) {
        if(inputObject==null) {
            this.idValue = null;
            return;
        }
        switch (AuditKind.values()[kind]) {
            case CALL_FOR_DELETE:
                this.idValue = Arrays.stream((int[]) inputObject)
                                            .mapToObj(Integer::valueOf)
                        .collect(Collectors.toList());
                break;
            case CALL_FOR_EDIT:
                this.idValue = Arrays.asList(new Integer[]{(Integer) inputObject});
                break;
            case CALL_FOR_SAVE_UPDATE:
                Field idField = OrmUtils.getIdField(inputObject.getClass(),false);
                idField.setAccessible(true);
                try {
                    Integer idValue = (Integer) idField.get(inputObject);
                    this.idValue = Arrays.asList(new Integer[]{idValue});
                } catch (IllegalAccessException e) {
                    logger.warn(String
                            .format("Audit threat! No access for %s field of % object", idField.toString(), inputObject.toString()));
                }
                break;
        }
    }

    public Object getOutputObject() {
        return outputObject;
    }

    public void setOutputObject(Object outputObject) {
        this.outputObject = outputObject;
    }

    public ProguserInfo getProguser() {
        return proguser;
    }

    public void setProguser(ProguserInfo proguser) {
        this.proguser = proguser;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public long getDatetime() {
        return datetime;
    }

    public void setDatetime(long datetime) {
        this.datetime = datetime;
    }

    @Override
    public String toString() {
        return "AuditRecord{" +
                "pu=" + proguser +
                ", kind=" + kind +
                ", path='" + path + '\'' +
                ", idValue='" + idValue + '\'' +
                ", entity='" + entity + '\'' +
                ", faultInfo='" + faultInfo + '\'' +
                ", inputObject=" + inputObject +
                ", outputObject=" + outputObject +
                '}';
    }

    @Document(language = "russian")
    public static class ProguserInfo {
        @Indexed
        private Integer proguserId;
        private String proguserName;

        public ProguserInfo() {
        }

        public ProguserInfo(Proguser pu) {
            this.proguserId = pu.getProguserId();
            this.proguserName = pu.getProguserName();
        }

        public Integer getProguserId() {
            return proguserId;
        }

        public void setProguserId(Integer proguserId) {
            this.proguserId = proguserId;
        }

        public String getProguserName() {
            return proguserName;
        }

        public void setProguserName(String proguserName) {
            this.proguserName = proguserName;
        }
    }

}
