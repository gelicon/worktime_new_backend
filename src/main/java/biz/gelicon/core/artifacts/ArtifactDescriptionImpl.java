package biz.gelicon.core.artifacts;

import biz.gelicon.artifacts.ArtifactDescription;
import biz.gelicon.artifacts.ArtifactService;

import java.util.HashMap;
import java.util.Map;

public class ArtifactDescriptionImpl implements ArtifactDescription {

    private ArtifactService holder;
    private  ArtifactKinds kind;
    private  String code;
    private  String name;
    private String unitName;
    private String entityName;
    private Map<String, Object> props = new HashMap<>();

    public ArtifactDescriptionImpl(ArtifactKinds kind, String code, String name) {
        this.kind = kind;
        this.code = code;
        this.name = name;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getKind() {
        return kind.ordinal();
    }

    @Override
    public void forUnit(String unitName) {
        this.unitName = unitName;
    }

    @Override
    public void forEntity(String entityName) {
        this.entityName = entityName;

    }

    @Override
    public void declareProperty(String propName, Object value) {
        this.props.put(propName,value);
    }

    public void setHolder(ArtifactService currService) {
        this.holder = currService;
    }

    public ArtifactService getHolder() {
        return holder;
    }

    public String getUnitName() {
        return unitName;
    }

    public String getEntityName() {
        return entityName;
    }

    public Map<String, Object> getProps() {
        return props;
    }


}
