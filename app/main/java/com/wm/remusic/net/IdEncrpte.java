package com.wm.remusic.net;

import java.security.MessageDigest;

/**
 * Created by wm on 2016/4/14.
 */
public class IdEncrpte {

    public static void main(String[] args) {
        byte[] g = ("3go8&$8*3*3h0k(2)2").getBytes();
        byte[] id = "30101323".getBytes();
        int gl = g.length;
        int idl = id.length;
        for (int i = 0; i < idl; i++) {
            id[i] = (byte) (id[i] ^ g[i % gl]);
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 输入的字符串转换成字节数组
            messageDigest.update(id);
            // 转换并返回结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            // 字符数组转换成字符串返回
            System.out.println(parseByte2HexStr(resultByteArray));
            System.out.println(Base64Encoder.encode("ÓÙDhﾤBYu]8æÑcè ".getBytes()));
            // System.out.println(Base64Encoder.encode(binary(resultByteArray, 2)));

        } catch (Exception e) {

        }

    }

    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

}
