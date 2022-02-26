package biz.gelicon.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MailService {
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    @Autowired
    private JavaMailSender emailSender;

    @Value("${gelicon.core.mail.from}")
    private String mailFrom;
    @Value("${spring.mail.host}")
    private String mailHost;
    @Value("${spring.mail.port}")
    private Integer mailPort;

    /**
     * Отправка HTML сообщения на почту
     * @param subj - Тема письма
     * @param text - Тело письма
     * @param email - адрес
     */
    public void sendEmail(String subj, String text, String email) {
        List<MimeBodyPart> listAppendix = new ArrayList<>();
        sendEmail(subj,text,email,new HashMap<>(),listAppendix);
    }
    public void sendEmail(String subj, String text, String email, List<MimeBodyPart> appendixs) {
        sendEmail(subj,text,email,new HashMap<>(), appendixs);
    }
    public void sendEmail(String subj, String text, String email,Map<String,String> headers) {
        List<MimeBodyPart> listAppendix = new ArrayList<>();
        sendEmail(subj,text,email,headers, listAppendix);
    }

    /**
     * Отправка HTML сообщения на почту
     * @param subj - Тема письма
     * @param text - Тело письма
     * @param email - адрес
     * @param headers - пользовательские заголовки в письме
     * @param appendixs
     */
    public void sendEmail(String subj, String text, String email, Map<String,String> headers, List<MimeBodyPart> appendixs) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setText(text,true);

            headers.entrySet().forEach(entry-> {
                try {
                    message.setHeader(entry.getKey(),entry.getValue());
                } catch (MessagingException ex) {
                    throw new RuntimeException(ex);
                }
            });
            helper.setFrom(mailFrom);
            helper.setTo(email);
            helper.setSubject(subj);

            for (int i = 0; i < appendixs.size(); i++) {
                MimeBodyPart a = appendixs.get(0);
                helper.addAttachment(a.getFileName(),a.getDataHandler().getDataSource());
            }

            this.emailSender.send(message);
            logger.info(String.format("Send mail. To: %s, From:%s. Server: %s:%d",email,mailFrom,mailHost,mailPort));

        } catch (MessagingException ex) {
            throw new RuntimeException(ex);
        }
    }
}
