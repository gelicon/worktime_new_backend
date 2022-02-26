package biz.gelicon.core.controllers;

import biz.gelicon.core.annotations.Audit;
import biz.gelicon.core.annotations.MethodPermission;
import biz.gelicon.core.annotations.RestrictStoreToAudit;
import biz.gelicon.core.audit.AuditKind;
import biz.gelicon.core.dto.PasswordAndKeyDTO;
import biz.gelicon.core.dto.PasswordAndUserDTO;
import biz.gelicon.core.dto.RecoveryPasswordDTO;
import biz.gelicon.core.dto.TokenDTO;
import biz.gelicon.core.model.Proguser;
import biz.gelicon.core.model.ProguserAuth;
import biz.gelicon.core.model.ProguserChannel;
import biz.gelicon.core.repository.ProgUserAuthRepository;
import biz.gelicon.core.repository.ProgUserRepository;
import biz.gelicon.core.repository.ProguserChannelRepository;
import biz.gelicon.core.response.StandardResponse;
import biz.gelicon.core.response.TokenResponse;
import biz.gelicon.core.response.exceptions.*;
import biz.gelicon.core.security.*;
import biz.gelicon.core.service.MailService;
import biz.gelicon.core.utils.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@Tag(name = "Безопасность", description = "Контроллер для управления токенами и функциями, не требующими авторизации  " +
        "Контроллер находится в неавторизированной зоне.")
@RequestMapping(value = "/security",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Transactional
@MethodPermission(noStore = true)
public class SecurityController {
    private static final Logger logger = LoggerFactory.getLogger(SecurityController.class);

    @Autowired
    private ProgUserRepository progUserRepository;
    @Autowired
    private ProgUserAuthRepository progUserAuthRepository;
    @Autowired
    private ProguserChannelRepository proguserChannelRepository;
    @Autowired
    private CredentialProviderFactory credentialProviderFactory;
    @Autowired
    AuthenticationCashe authenticationCashe;
    @Autowired
    private MailService emailService;

    @Value("${gelicon.core.aeskey}")
    private String aeskey;
    @Value("${gelicon.core.frontend}")
    private String frontendBaseAddress;

    @Autowired
    private BruteForceProtection bruteForceProtection;




    @Operation(summary = "Получение токена",
            description = "Возвращает токен или ошибку авторизации. Если возвращается ошибка с кодом 131, то пароль правильный, но временный. " +
                    "Для входа в систему его нужно заменить на постоянный (вызов changepswd) ")
    @RequestMapping(value = "gettoken", method = RequestMethod.POST)
    @Audit(kinds={AuditKind.SECURITY_SYSTEM},noAuthentication=true)
    public TokenResponse getToken(@RestrictStoreToAudit @RequestBody UserCredential credential) {
        Proguser pu = progUserRepository.findByUserName(credential.getUserName());
        if(pu==null) {
            throw new IncorrectUserOrPasswordException();
        }
        // сбросим кэш токенов для пользователя
        authenticationCashe.clearByUserName(pu.getProguserName());
        // проверка на блокировку от brute force
        if(bruteForceProtection.nextAttemptIsBruteForceAttack(pu.getProguserName())) {
            bruteForceProtection.startProtectPeriod(pu.getProguserName());
            throw new UserBruteForceLockException(bruteForceProtection.getMaxAttempCount());
        }
        // проверка на блокировку
        if(!pu.toUserDetail().isAccountNonLocked()) {
            throw new UserLockException();
        }
        // получаем провайдера
        CredentialProvider<String> crprovider = credentialProviderFactory.getProvider(CredentialProvider.CredentialProviderType.AuthByPassword);
        // надо проверить есть ли временный пароль
        if(crprovider.hasTempAuthentication(pu.getProguserId())) {
            // проверим временный пароль
            if(!crprovider.checkTempAuthentication(pu.getProguserId(),credential.getPassword())) {
                bruteForceProtection.onAuthenticationFailure(pu.getProguserName());
                throw new IncorrectUserOrPasswordException();
            }
            // и все равно не пустим. Но выкинем другую ошибку
            throw new HasTemporaryAuthenticationException();
        }

        // проверяем наличие правильного временного или постоянного пароля
        Integer proguserId = pu.getProguserId();
        String password = credential.getPassword();
        if(!crprovider.checkAuthentication(proguserId, password)) {
            bruteForceProtection.onAuthenticationFailure(pu.getProguserName());
            throw new IncorrectUserOrPasswordException();
        }

        ProguserAuth auth = progUserAuthRepository.findActiveLastByDate(pu.getProguserId());
        if(auth==null) {
            auth = progUserAuthRepository.reNew(pu.getProguserId());
        }

        bruteForceProtection.onAuthenticationSuccess(pu.getProguserName());

        return new TokenResponse(auth.getProguserAuthToken(),pu.getProguserFullName(),pu.getProguserName());
    }


    @Operation(summary = "Создание нового токена, взамен устаревшего",
            description = "Возвращает новый токен вместо устаревшего или ошибку авторизации. Устаревший токен должен быть передан")
    @RequestMapping(value = "renew", method = RequestMethod.POST)
    @Audit(kinds={AuditKind.SECURITY_SYSTEM},noAuthentication=true)
    public TokenResponse renewToken(@RequestBody
                                        @Parameter(description = "Устаревший токен") TokenDTO oldToken) {
        Date now = new Date();
        Proguser pu = progUserRepository.findByToken(oldToken.getToken());
        if(pu==null) {
            throw new IncorrectTokenException();
        }
        // сбросим кэш токенов для пользователя
        authenticationCashe.clearByUserName(pu.getProguserName());
        // проверка на блокировку
        if(!pu.toUserDetail().isAccountNonLocked()) {
            throw new UserLockException();
        }
        // проверим протух ли токен
        ProguserAuth storeAuth = progUserAuthRepository.findByValue(oldToken.getToken());
        storeAuth.checkExpired();

        ProguserAuth auth = progUserAuthRepository.reNew(pu.getProguserId());

        return new TokenResponse(auth.getProguserAuthToken(),pu.getProguserFullName(),pu.getProguserName());
    }

    @Operation(summary = "Смена пароля с временного на постоянный. Необходимо знать старый пароль",
            description = "Позволяет поменять собственный пароль пользователя с временного на постоянный")
    @RequestMapping(value = "changepswd", method = RequestMethod.POST)
    @ResponseBody
    @Audit(kinds={AuditKind.SECURITY_SYSTEM},noAuthentication=true)
    public String changePassword(@RequestBody
                               @Parameter(description = "Новый и старый пароли") PasswordAndUserDTO dto) {
        Proguser pu =progUserRepository.findByUserName(dto.getUserName());
        if(pu==null) {
            throw new IncorrectUserOrPasswordException();
        }
        // проверка на блокировку от brute force
        if(bruteForceProtection.nextAttemptIsBruteForceAttack(pu.getProguserName())) {
            bruteForceProtection.startProtectPeriod(pu.getProguserName());
            throw new UserBruteForceLockException(bruteForceProtection.getMaxAttempCount());
        }
        // проверка на блокировку
        if(!pu.toUserDetail().isAccountNonLocked()) {
            throw new UserLockException();
        }
        // получаем провайдера
        CredentialProvider<String> crprovider = credentialProviderFactory.getProvider(CredentialProvider.CredentialProviderType.AuthByPassword);
        if(!crprovider.checkTempAuthentication(pu.getProguserId(),dto.getOldPassword())) {
            bruteForceProtection.onAuthenticationFailure(pu.getProguserName());
            throw new IncorrectUserOrPasswordException();
        }
        bruteForceProtection.onAuthenticationSuccess(pu.getProguserName());
        crprovider.updateAuthentication(pu.getProguserId(),dto.getNewPassword(),false);
        return StandardResponse.SUCCESS;
    }

    @Operation(summary = "Запрос на восстановление пароля",
            description = "Позволяет отправить запрос на восстановление пароля. Отправит письмо по зарегистрированному email адресу")
    @RequestMapping(value = "recovery/request", method = RequestMethod.POST)
    @ResponseBody
    @Audit(kinds={AuditKind.SECURITY_SYSTEM},noAuthentication=true)
    public String recoveryRequest(@RequestBody
                           @Parameter(description = "Данные для восстановления") RecoveryPasswordDTO dto) {
        Proguser pu;
        if(dto.getEmailOrLogin().indexOf("@")>0) {
            pu =progUserRepository.findByEmail(dto.getEmailOrLogin());
        } else {
            pu =progUserRepository.findByUserName(dto.getEmailOrLogin());
        }
        if(pu==null) {
            throw new IncorrectUserOrEmailException();
        }
        // проверка на блокировку
        if(!pu.toUserDetail().isAccountNonLocked()) {
            throw new UserLockException();
        }
        // провека на присутствие адреса email
        ProguserChannel puChannel = proguserChannelRepository.getlEmail(pu.getProguserId());
        if(puChannel.getProguserChannelAddress()==null || puChannel.getProguserChannelAddress().isEmpty()) {
            throw new RuntimeException("При регистрации не был указан адрес электронной почты для восстановления пароля");
        }
        // отправка почтового сообщения
        RecoveryData data = new RecoveryData(pu);
        String code = SecurityUtils.getVerificationCode(aeskey, data);
        Map<String,String> headers = new HashMap<>();
        headers.put("recoveryKey",code);
        emailService.sendEmail("Восстановление пароля",
                buildMessageText(pu,data,code),
                puChannel.getProguserChannelAddress(),
                headers);

        return StandardResponse.SUCCESS;
    }

    private String buildMessageText(Proguser pu,RecoveryData data, String code) {
        String url = frontendBaseAddress+"recovery/"+code;
        InputStream input = this.getClass().getClassLoader().getResourceAsStream("mail/template-recovery.html");
        String template = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n"));
        return String.format(template,pu.getProguserFullName(),url);
    }

    private static long KEY_ALIVE = 60 * 60 * 1000; // 1 час

    @Operation(summary = "Восстановление пароля",
            description = "Позволяет восстановить пароля. Отправит письмо по зарегистрированному email адресу")
    @RequestMapping(value = "recovery/save", method = RequestMethod.POST)
    @ResponseBody
    @Audit(kinds={AuditKind.SECURITY_SYSTEM},noAuthentication=true)
    public TokenResponse.UserInfo recoveryProcess(@RequestBody
                                  @Parameter(description = "Данные для восстановления") PasswordAndKeyDTO dto) {

        RecoveryData data = SecurityUtils.readVerificationData(aeskey,dto.getKey(),new RecoveryData());
        // проверка на то что ключ устарел
        if ((System.currentTimeMillis() - data.getTimestamp()) > KEY_ALIVE)
            throw new RuntimeException("Истекло время, отведенное для восстановления пароля");

        Proguser pu =progUserRepository.findById(data.getProguserId());
        if(pu==null) {
            throw new IncorrectUserOrPasswordException();
        }

        // сброс
        authenticationCashe.clearByUserName(pu.getProguserName());

        // проверка на блокировку
        if(!pu.toUserDetail().isAccountNonLocked()) {
            throw new UserLockException();
        }

        // получаем провайдера
        CredentialProvider<String> crprovider = credentialProviderFactory.getProvider(CredentialProvider.CredentialProviderType.AuthByPassword);
        crprovider.updateAuthentication(pu.getProguserId(),dto.getNewPassword(),false);

        return new TokenResponse.UserInfo(pu.getProguserName(),pu.getProguserFullName());
    }

}
