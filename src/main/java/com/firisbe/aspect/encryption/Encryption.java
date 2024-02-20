package com.firisbe.aspect.encryption;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;


import javax.crypto.spec.SecretKeySpec;

import java.util.Base64;



@Service
public class Encryption {
    private static final String SECRET_KEY = "buBirOrnekAnahtar1234567";

    // Veriyi şifreleyen method
    public String encrypt(String strToEncrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes()));
        } catch (Exception e) {
            System.out.println("Şifreleme hatası: " + e.toString());
        }
        return null;
    }

    // Şifrelenmiş veriyi çözen method
    public String decrypt(String strToDecrypt) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Çözme hatası: " + e.toString());
        }
        return null;
    }
}
