package biz.gelicon.core.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class BruteForceProtection {
    private static final Logger logger = LoggerFactory.getLogger(BruteForceProtection.class);

    @Value("${gelicon.core.login.maxlimitattemp:5}")
    private int MAXLIMITATTEMP;
    @Value("${gelicon.core.login.bruteforcelockduration:10}")
    private int PROTECTPERIODLOCK_DURATION;

    Map<String,ProtectionRecord> bruteForceCounter =  new ConcurrentHashMap<>();

    class ProtectionRecord {
        int attempCount = 0;
        Timer timer;

        void resetTimer() {
            if(timer!=null) {
                timer.cancel();
            }
        }
    }


    public BruteForceProtection() {
    }

    /**
     * Проверяет будет ли следующая попытка brute-force атакой
     * @param username
     * @return
     */
    public boolean nextAttemptIsBruteForceAttack(String username) {
        ProtectionRecord pr = findProtectionRecord(username);
        return pr.attempCount >= MAXLIMITATTEMP;
    }

    /**
     * Сбрасывает счетчик brute-force
     * @param username
     */
    public void resetBruteForceCounter(String username) {
        ProtectionRecord pr = bruteForceCounter.get(username);
        if(pr!=null) {
            pr.resetTimer();
        }
        bruteForceCounter.remove(username);
        logger.info("BruteForce: counter reset user={}",username);
    }


    /**
     * Событие - пароль указан неверно
     * @param username
     */
    public void onAuthenticationFailure(String username) {
        ProtectionRecord pr = findProtectionRecord(username);
        logger.info("BruteForce: Authentication failure user={} bruteforcecount={}",username,pr.attempCount);
        pr.attempCount++;
    }

    /**
     * Событие - пароль указан верно. Счетчик сбрасывается
     * @param username
     */
    public void onAuthenticationSuccess(String username) {
        ProtectionRecord pr = findProtectionRecord(username);
        logger.info("BruteForce: Authentication success user={} bruteforcecount={}",username,pr.attempCount);
        resetBruteForceCounter(username);
    }

    public int getMaxAttempCount() {
        return MAXLIMITATTEMP;
    }

    /**
     * Старт таймера блокировки. По окончании периода счетчик сбрасывается
     * @param username
     */
    public void startProtectPeriod(final String username) {
        ProtectionRecord pr = findProtectionRecord(username);
        if(pr.timer==null) {
            logger.info("BruteForce: Start protection period for user={} bruteforcecount={}",username,pr.attempCount);
            pr.timer =  new Timer();
            pr.timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    resetBruteForceCounter(username);
                }
            }, PROTECTPERIODLOCK_DURATION * 60 * 1000l);
        }
    }

    private ProtectionRecord findProtectionRecord(String username) {
        ProtectionRecord pr = bruteForceCounter.get(username);
        if(pr==null) {
            pr = new ProtectionRecord();
            bruteForceCounter.put(username,pr);
        }
        return pr;
    }

}
