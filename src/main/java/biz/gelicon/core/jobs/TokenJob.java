package biz.gelicon.core.jobs;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class TokenJob {
    public static final int TOKENUPDATE_BUFFER_TIME_SPAN = 60 * 1000; // 1 min

    private Map<String,JobDispatcher> tokenUpdateJobs = new HashMap<>();

    public JobDispatcher getDispatcher(String token) {
        JobDispatcher job = tokenUpdateJobs.get(token);
        if(job==null) {
            job = new JobDispatcher(TOKENUPDATE_BUFFER_TIME_SPAN);
            tokenUpdateJobs.put(token,job);
        }
        return job;
    }
}
