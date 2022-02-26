package biz.gelicon.core.controllers;

import biz.gelicon.core.dto.PasswordAndKeyDTO;
import biz.gelicon.core.dto.RecoveryPasswordDTO;
import biz.gelicon.core.security.UserCredential;
import biz.gelicon.core.utils.GridDataOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.subethamail.wiser.Wiser;
import org.subethamail.wiser.WiserMessage;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.AssertTrue;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RecoveryTest extends IntergatedTest {

    private static Wiser wiser;

    @BeforeAll
    public static void setup() {
        // старт почтового сервиса
        wiser = new Wiser();
        wiser.setPort(2625); // 25 порт может быть занят
        wiser.setHostname("localhost");
        wiser.start();
    }
    @AfterAll
    public static void destroy() {
        wiser.stop();
    }


    @Test
    @Transactional
    @Rollback
    public void requestRecovery() throws Exception {
        RecoveryPasswordDTO dto = new RecoveryPasswordDTO("test@test.com");
        this.mockMvc.perform(post("/security/recovery/request")
                .content(new ObjectMapper().writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));
        Assert.assertEquals(1,wiser.getMessages().size());
        MimeMessage email = wiser.getMessages().get(0).getMimeMessage();
        MimeMultipart multipart = (MimeMultipart) email.getContent();
        String content = getTextFromMimeMultipart(multipart);
        Assert.assertTrue(content.indexOf("<title>Восстановление пароля</title>")>=0);

        WiserMessage mail = wiser.getMessages().get(0);
        String recoveryKey = mail.getMimeMessage().getHeader("recoveryKey")[0];
        PasswordAndKeyDTO recovery = new PasswordAndKeyDTO(recoveryKey,"pswd1");
        this.mockMvc.perform(post("/security/recovery/save")
                .content(new ObjectMapper().writeValueAsString(recovery))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("\"errorMessage\":"))));

        // проверяем вход
        UserCredential user = new UserCredential("root", "pswd1");
        this.mockMvc.perform(post("/security/gettoken")
                .content(new ObjectMapper().writeValueAsString(user))
                .contentType(MediaType.APPLICATION_JSON))
                //.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("\"token\":")));

    }

    private String getTextFromMimeMultipart(
            MimeMultipart mimeMultipart) throws MessagingException, IOException {
        StringBuilder result = new StringBuilder();
        int count = mimeMultipart.getCount();
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i);
            if (bodyPart.isMimeType("text/plain")) {
                result.append("\n").append(bodyPart.getContent());
                break; // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                result.append("\n").append(bodyPart.getContent());
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
            }
        }
        return result.toString();
    }

}
