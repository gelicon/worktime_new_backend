package biz.gelicon.core.artifacts;

import biz.gelicon.artifacts.ArtifactDescription;
import biz.gelicon.artifacts.ArtifactManager;
import biz.gelicon.artifacts.ArtifactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ArtifactManagerImpl implements ArtifactManager {
    private static final Logger logger = LoggerFactory.getLogger(ArtifactManagerImpl.class);
    private ArtifactService currService;
    private boolean overlappingFlag = true;
    List<Map<String,ArtifactDescription>> artifacts;

    public ArtifactManagerImpl() {
        ArtifactKinds[] allkinds = ArtifactKinds.values();
        ArtifactKinds max = allkinds[allkinds.length-1];
        artifacts = IntStream.range(0, max.ordinal()).boxed()
                .map(k->new HashMap<String,ArtifactDescription>())
                .collect(Collectors.toList());
    }

    // загрузка всех связанных модулей
    public void loadArtifacts() {
        logger.info("Load artifacts...");
        List<ArtifactService> sevices = ArtifactService.getInstances();
        sevices.forEach(s-> {
            currService = s;
            s.registerManager(this);
        });
        logger.info("Load artifacts [Ok]");
    }

    @Override
    public ArtifactDescription registerArtifact(int kind, String code, String name) {
        if(code==null || code.isEmpty())
            throw new RuntimeException("Code Artifact is empty");
        if(name==null || name.isEmpty())
            throw new RuntimeException("Name Artifact is empty");
        logger.info("\tRegister Artifact {}:{}",code,name);
        ArtifactKinds akind = ArtifactKinds.values()[kind-1];
        ArtifactDescriptionImpl artifactDesc = new ArtifactDescriptionImpl(akind, code, name);
        artifactDesc.setHolder(currService);
        // override печатных форм
        ArtifactDescription regDesc = findByCode(akind,code);
        if(regDesc!=null) {
            // если запрет перекрытия
            if(!overlappingFlag) {
                logger.warn("-- Duplicate code Artifact  {}:{}",regDesc.getCode(),regDesc.getName());
                // не регистрируем, но объект возвращаем, чтобы не ломала загрузку приложения
                return artifactDesc;
            }
            artifacts.get(akind.ordinal()).remove(code,regDesc);
            logger.warn("-- Was remove overlapping Artifact {}:{}",regDesc.getCode(),regDesc.getName());
        }
        artifacts.get(akind.ordinal()).put(code,artifactDesc);
        return artifactDesc;
    }

    @Override
    public Object run(int kind, String code, Map<String, Object> params) {
        ArtifactDescriptionImpl artifactDesc = (ArtifactDescriptionImpl)findByCode(ArtifactKinds.values()[kind-1],code);
        if(artifactDesc==null)
            throw new RuntimeException(String.format("Artifact %s not found",code));
        return artifactDesc.getHolder().run(kind,code,params);
    }


    public ArtifactDescription findByCode(ArtifactKinds akind, String code) {
        return artifacts.get(akind.ordinal()).get(code);
    }

    public Collection<ArtifactDescription> getArtifacts(ArtifactKinds akind) {
        List<ArtifactDescription> values = new ArrayList(artifacts.get(akind.ordinal()).values());
        return Collections.unmodifiableList(values);
    }


    public void setOverlappingFlag(boolean overlappingFlag) {
        this.overlappingFlag = overlappingFlag;
    }
}
