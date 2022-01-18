package client.security;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class SecurePassword {

    private static byte[] getRandomSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private static String getSaltedHash(String password, byte[] salt) {
        byte[] hash = null;
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = null;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            hash = factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        if (hash != null) {
            return Base64.getEncoder().encodeToString(hash);
        } else {
            return ""; // FIXME: XD
        }
    }

    public static String[] createSaltedHash(String password) {
        byte[] generatedSalt = getRandomSalt();
        String[] result = new String[2];
        result[0] = Base64.getEncoder().encodeToString(generatedSalt);
        result[1] = getSaltedHash(password, generatedSalt);
        return result;
    }

    public static boolean compareHashWithPassword(String password, String salt, String hash) {
        return hash.equals(getSaltedHash(password, Base64.getDecoder().decode(salt)));
    }

}
