package com.wm.remusic.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public final class AESTools {

    private static final String INPUT = "2012171402992850";
    private static final String IV = "2012061402992850";
    private static final char[] CHARS = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};

    public static String encrpty(String paramString) {

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.update(INPUT.getBytes());
        byte[] stringBytes = messageDigest.digest();
        StringBuilder stringBuilder = new StringBuilder(stringBytes.length * 2);
        for (int i = 0; i < stringBytes.length; i++) {
            stringBuilder.append(CHARS[((stringBytes[i] & 0xF0) >>> 4)]);
            stringBuilder.append(CHARS[(stringBytes[i] & 0xF)]);
        }
        String str = stringBuilder.toString();
        SecretKeySpec localSecretKeySpec = new SecretKeySpec(
                str.substring(str.length() / 2)
                        .getBytes(), "AES");
        Cipher localCipher;
        try {
            localCipher = Cipher
                    .getInstance("AES/CBC/PKCS5Padding");
            localCipher.init(1, localSecretKeySpec,
                    new IvParameterSpec(IV.getBytes()));
            return URLEncoder.encode(
                    new String(BytesHandler.getChars(localCipher
                            .doFinal(paramString.getBytes()))),
                    "utf-8");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return "";
    }

}