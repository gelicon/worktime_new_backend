package biz.gelicon.core.utils;

import biz.gelicon.core.security.VerificationData;
import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;
import org.springframework.security.crypto.codec.Hex;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public class SecurityUtils {
    private static final String HASH_ALG = "SHA-256";
    private static final int SALT_SIZE = 16;
    private static final int HASH_STR_LIMIT = 128;

    private static MessageDigest messageDigest;
    private static final Random randomizer = new SecureRandom();

    static {
        try {
            messageDigest = MessageDigest.getInstance(HASH_ALG);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean checkPassword(String password, String hash) {
        byte salt[];
        BaseEncoding baseEncoding = BaseEncoding.base16().lowerCase();
        salt = baseEncoding.decode(hash.substring(0, SALT_SIZE * 2));
        String passwordHash = hashPassword(salt, password);
        return passwordHash.equals(hash);
    }

    public static String encodePassword(String password) {
        byte salt[] = new byte[SALT_SIZE];
        synchronized (randomizer) {
            randomizer.nextBytes(salt);
        }
        return hashPassword(salt, password);
    }

    private static String hashPassword(byte[] salt, String password) {
        byte digest[];
        messageDigest.update(salt);
        digest = messageDigest.digest(password.getBytes(Charsets.UTF_8));
        BaseEncoding baseEncoding = BaseEncoding.base16().lowerCase();
        int hashStringSize = Math.min(HASH_STR_LIMIT - SALT_SIZE * 2, messageDigest.getDigestLength() * 2);
        return baseEncoding.encode(salt) + baseEncoding.encode(digest).substring(0, hashStringSize);
    }

    private static final String keyAlg = "AES";
    private static final String cipher = "AES/ECB/PKCS5Padding";

    public static byte[] cipher(String key, byte[] data, int mode)
            throws Exception {
        byte[] skey = Hex.decode(key);
        Cipher c = Cipher.getInstance(cipher);
        c.init(mode, new SecretKeySpec(skey, keyAlg));
        return c.doFinal(data);
    }

    public static String getVerificationCode(String key, VerificationData data) {
        try {
            return new String(Hex.encode(cipher(key, data.write(), Cipher.ENCRYPT_MODE)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static <T extends VerificationData> T readVerificationData(String key,
                                                                      String verificationCode, T data) {
        try {
            return (T) data.read(
                    cipher(key, Hex.decode(verificationCode), Cipher.DECRYPT_MODE));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
