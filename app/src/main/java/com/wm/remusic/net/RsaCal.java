package com.wm.remusic.net;

import android.support.annotation.NonNull;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;

/**
 * Created by wm on 2016/4/13.
 */
public class RsaCal {
    public static String reverse(String str) {
        return new StringBuilder(str).reverse().toString();
    }


    public static String rsaEncode(String key) {
        String pubKey = "010001";
        String m = "157794750267131502212476817800345498121872783333389747424011531025366277535262539913701806290766479189477533597854989606803194253978660329941980786072432806427833685472618792592200595694346872951301770580765135349259590167490536138082469680638514416594216629258349130257685001248172188325316586707301643237607";
        try {
            String k = reverse(key);
            String keyTo16 = toHex(k, "GBK");
            // System.out.println(new BigInteger(keyTo16, 16));
            // new BigInteger(keyTo16, 16) 字符串转为16进制数字，
            // pow(Integer.valueOf(pubKey, 16)) 得到次方的值
            //remainder 取余数
            String c = (new BigInteger(keyTo16, 16).pow(Integer.valueOf(pubKey, 16))).remainder(new BigInteger(m)) + "";

            //转为16进制表示
            return (new BigInteger(c).toString(16));
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
        return "-1";
    }


    public static void main3(String[] args) {
        String key = "7b104953fb112826";
        String pubKey = "010001";
        String m = "157794750267131502212476817800345498121872783333389747424011531025366277535262539913701806290766479189477533597854989606803194253978660329941980786072432806427833685472618792592200595694346872951301770580765135349259590167490536138082469680638514416594216629258349130257685001248172188325316586707301643237607";
        try {
            String k = reverse(key);
            String keyTo16 = toHex(k, "GBK");
            // System.out.println(new BigInteger(keyTo16, 16));
            // new BigInteger(keyTo16, 16) 字符串转为16进制数字，
            // pow(Integer.valueOf(pubKey, 16)) 得到次方的值
            //remainder 取余数
            String c = (new BigInteger(keyTo16, 16).pow(Integer.valueOf(pubKey, 16))).remainder(new BigInteger(m)) + "";

            //转为16进制表示
            System.out.println(new BigInteger(c).toString(16));
        } catch (UnsupportedEncodingException e) {

            e.printStackTrace();
        }
    }

    @NonNull
    static public String toHex(String text, String enc) throws UnsupportedEncodingException {
        byte B[] = text.getBytes(enc);
        StringBuilder buf = new StringBuilder();
        for (byte b : B) {
            buf.append(Integer.toHexString(b & 0xff));
        }
        return buf.toString();
    }

    public static void main1(String[] args) throws Exception {
        // TODO Auto-generated method stub
        HashMap<String, Object> map = RSAUtils.getKeys();
        //生成公钥和私钥
        //     RSAPublicKey publicKey = (RSAPublicKey) map.get("public");
        //     RSAPrivateKey privateKey = (RSAPrivateKey) map.get("private");
        String big1 = "00e0b509f6259df8642dbc35662901477df22677ec152b5ff68ace615bb7b725152b3ab17a876aea8a5aa76d2e417629ec4ee341f56135fccf695280104e0312ecbda92557c93870114af6c9d05c4f7f0c3685b7a46bee255932575cce10b424d813cfe4875d3e82047b97ddef52741d546b8e289dc6935b3ece0462db0a22b8e7";
        String big2 = "010001";
        //  System.out.println(b1.toString());
        // System.out.println(b2.toString());
        RSAPublicKeySpec rsaPubKS = new RSAPublicKeySpec(new BigInteger(big1, 16), new BigInteger(big2, 16));
        KeyFactory kf = KeyFactory.getInstance("RSA");
        RSAPublicKey publicKey = (RSAPublicKey) kf.generatePublic(rsaPubKS);

        //模
        String modulus = publicKey.getModulus().toString();
        //公钥指数
        String public_exponent = publicKey.getPublicExponent().toString();

        System.err.println(modulus);
        System.err.println(public_exponent);
        //私钥指数
        //  String private_exponent = privateKey.getPrivateExponent().toString();
        //明文
        String ming = "628211bf359401b7";
        //使用模和指数生成公钥和私钥
        RSAPublicKey pubKey = RSAUtils.getPublicKey(new BigInteger(big1, 16) + "", new BigInteger(big2, 16) + "");
        // RSAPrivateKey priKey = RSAUtils.getPrivateKey(modulus, private_exponent);
        //加密后的密文
        String mi = RSAUtils.encryptByPublicKey(ming, pubKey);
        System.err.println(mi);
        //解密后的明文
        //  ming = RSAUtils.decryptByPrivateKey(mi, priKey);
        //  System.err.println(ming);
    }
}