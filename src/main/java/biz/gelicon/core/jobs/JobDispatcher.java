package biz.gelicon.core.jobs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.BackpressureOverflow;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class JobDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(JobDispatcher.class);

    private PublishSubject<Runnable> jobs;

    public JobDispatcher() {
        jobs = PublishSubject.create();
        jobs.observeOn(Schedulers.computation()) // включаем пул thread
                .subscribe(runnable -> {
                    logger.info("pull job {}",runnable.hashCode());
                    try {
                        runnable.run();
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(),ex);
                    }
                });
    }

    /**
     * Асинхронные задачи с временным буфером
     * @param bufferTimeSpan - время накопления буфера
     * @param action - функция обработки
     */
    public JobDispatcher(long bufferTimeSpan, Action1<List<Runnable>> action) {
        jobs = PublishSubject.create();
        jobs.observeOn(Schedulers.computation()) // включаем пул thread
                .buffer(bufferTimeSpan, TimeUnit.MILLISECONDS)
                .subscribe(action);
    }

    /**
     * Асинхронные задачи с временным буфером и стандартной обработкой (запуск run)
     * @param bufferTimeSpan - время накопления буфера
     */
    public JobDispatcher(long bufferTimeSpan) {
        this(bufferTimeSpan,buf -> {
            // буфера может быь пустым
            if(!buf.isEmpty()) {
                Runnable last = buf.get(buf.size() - 1);
                logger.info("pull job {}",last.hashCode());
                try {
                    last.run();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(),ex);
                }
            } else {
                logger.info("Buffered job is empty");
            }
        });
    }

    public JobDispatcher(int bufferSize, BackpressureOverflow.Strategy strategy) {
        jobs = PublishSubject.create();
        jobs.onBackpressureBuffer(bufferSize, () -> {}, strategy)
                .observeOn(Schedulers.computation()) // включаем пул thread
                .subscribe(runnable -> {
                    logger.info("pull job {}",runnable.hashCode());
                    try {
                        runnable.run();
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(),ex);
                    }
                });
    }

    public void pushJob(Runnable job) {
        logger.info("push job {}",job.hashCode());
        jobs.onNext(job);
    }


}
