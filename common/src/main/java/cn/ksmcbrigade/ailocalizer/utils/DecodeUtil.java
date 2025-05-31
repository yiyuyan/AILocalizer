package cn.ksmcbrigade.ailocalizer.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.HexFormat;

public class DecodeUtil { private static String decrypt(String encryptedHex, String secretKey) throws Exception {byte[] encryptedBytes = HexFormat.of().parseHex(encryptedHex);SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), "AES");Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);byte[] decryptedBytes = cipher.doFinal(encryptedBytes);return new String(decryptedBytes);}































































    public static String randomStrings() throws Exception {
        Base64.Decoder decoder = Base64.getDecoder();
        return new String(decoder.decode(decrypt(new String(decoder.decode("ZGViN2M2ZDc2NDU0MDgzYmY0ZThlNDY0ZmYwMzA5YWVhZmE0MGUxNGI1ODU2ODY1Y2NhNjc0MTk5NWYzMTk2ZWQ0ZmY5NzZiMDcwMmI1NjFmNDY1OWVmNGYwZjMwYmJhODQwYmI2MDI5ZWM3NDQ3ZTIyMzczNTY1N2UzYTllYTRiZTI0YmZiZjg3MjYwN2Q1ODYzNDg3ODgzMTRmNzc5NQ==")),new String(decoder.decode(decoder.decode("TVRJek5EVTJOemc1TUdGaVkyUmxabWRvYVdwcmJHMXViM0J4Y25OMGRYWT0="))))));
    }
}
