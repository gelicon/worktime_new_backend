package biz.gelicon.core.service;

import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.response.exceptions.ReportResultNotFoundException;
import biz.gelicon.core.utils.MimeMap;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class DownloaderCenter {

    public static class PendingTask {
        private final String fileName;
        private final Proguser owner;
        private final String shortName;
        private final String mimeType;

        public PendingTask(String fileName, Proguser user) {
            this.fileName = fileName;
            this.owner = user;
            this.shortName = new File(fileName).getName();
            this.mimeType =  MimeMap.getContentTypeFor(this.shortName);
        }

        public String getFileName() {
            return fileName;
        }

        public Proguser getOwner() {
            return owner;
        }

        public String getShortName() {
            return shortName;
        }

        public String getMimeType() {
            return mimeType;
        }
    }

    private Map<Integer,PendingTask> tasks = new HashMap<>();

    synchronized public Integer pushTask(String fullNameFile, Proguser pu) {
        Integer correlationId = Integer.valueOf(ThreadLocalRandom.current().nextInt(0,Integer.MAX_VALUE));
        tasks.put(correlationId,new PendingTask(fullNameFile,pu));
        return correlationId;
    }


    synchronized public PendingTask getTask(Integer correlationId,Proguser pu) {
        PendingTask task =  tasks.get(correlationId);
        if(task==null) {
            throw new ReportResultNotFoundException();
        }
        if(task.owner.getProguserId()!=pu.getProguserId()) {
            throw new AccessDeniedException("Пользователь не является владельцем задачи");
        }
        return task;
    };

    public InputStream getInputStream(PendingTask task) {
        try {
            return new FileInputStream(task.getFileName());
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void finish(Integer correlationId) {
        tasks.remove(correlationId);
    }

}
