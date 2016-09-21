/**
 * Copyright (lrc_arrow) www.longdw.com
 */
package com.wm.remusic.uitl;

import android.text.TextUtils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.text.DecimalFormat;

public class StringHelper {
    /**
     * 判断输入char类型变量的字符类型
     *
     * @param c char类型变量
     * @return CharType 字符类型
     */
    public static CharType checkType(char c) {
        CharType ct = null;

        // 中文，编码区间0x4e00-0x9fbb
        if ((c >= 0x4e00) && (c <= 0x9fbb)) {
            ct = CharType.CHINESE;
        }

        // Halfwidth and Fullwidth Forms， 编码区间0xff00-0xffef
        else if ((c >= 0xff00) && (c <= 0xffef)) { // 2字节英文字
            if (((c >= 0xff21) && (c <= 0xff3a))
                    || ((c >= 0xff41) && (c <= 0xff5a))) {
                ct = CharType.LETTER;
            }

            // 2字节数字
            else if ((c >= 0xff10) && (c <= 0xff19)) {
                ct = CharType.NUM;
            }

            // 其他字符，可以认为是标点符号
            else
                ct = CharType.DELIMITER;
        }

        // basic latin，编码区间 0000-007f
        else if ((c >= 0x0021) && (c <= 0x007e)) { // 1字节数字
            if ((c >= 0x0030) && (c <= 0x0039)) {
                ct = CharType.NUM;
            } // 1字节字符
            else if (((c >= 0x0041) && (c <= 0x005a))
                    || ((c >= 0x0061) && (c <= 0x007a))) {
                ct = CharType.LETTER;
            }
            // 其他字符，可以认为是标点符号
            else
                ct = CharType.DELIMITER;
        }

        // latin-1，编码区间0080-00ff
        else if ((c >= 0x00a1) && (c <= 0x00ff)) {
            if ((c >= 0x00c0) && (c <= 0x00ff)) {
                ct = CharType.LETTER;
            } else
                ct = CharType.DELIMITER;
        } else
            ct = CharType.OTHER;

        return ct;
    }

    /**
     * 获取给定汉字的拼音的首字母
     *
     * @param c 一个汉字
     * @return 如果c不是汉字，返回0；否则返回该汉字的拼音的首字母
     */
    public static char getPinyinFirstLetter(char c) {
        String[] pinyin = null;
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);// 不输出声调
        try {
            // 获取该汉字的拼音（可能是多音字，所以用数组存放结果）
            pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }

        // 如果c不是汉字，toHanyuPinyinStringArray会返回null
        if (pinyin == null) {
            return 0;
        }

        // 只取一个发音，如果是多音字，仅取第一个发音
        return pinyin[0].charAt(0);
    }

    public static String bytesToMB(long bytes) {
        float size = (float) (bytes * 1.0 / 1024 / 1024);
        String result = null;
        if (bytes >= (1024 * 1024)) {
            result = new DecimalFormat("###.00").format(size) + "MB";
        } else {
            result = new DecimalFormat("0.00").format(size) + "MB";
        }
        return result;
    }

    /**
     * 将字符串中的中文转化为拼音,其他字符不变
     *
     * @param inputString
     * @return
     */
    public static String getPingYin(String inputString) {
        if (TextUtils.isEmpty(inputString)) {
            return "";
        }
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] input = inputString.trim().toCharArray();
        String output = "";

        try {
            for (int i = 0; i < input.length; i++) {
                if (Character.toString(input[i]).matches(
                        "[\\u4E00-\\u9FA5]+")) {
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(
                            input[i], format);
                    if (temp == null || TextUtils.isEmpty(temp[0])) {
                        continue;
                    }
                    output += temp[0].replaceFirst(temp[0].substring(0, 1),
                            temp[0].substring(0, 1).toUpperCase());
                } else
                    output += Character.toString(input[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return output;
    }

    public static enum CharType {
        DELIMITER, // 非字母截止字符，例如，．）（　等等　（ 包含U0000-U0080）
        NUM, // 2字节数字１２３４
        LETTER, // gb2312中的，例如:ＡＢＣ，2字节字符同时包含 1字节能表示的 basic latin and latin-1
        OTHER, // 其他字符
        CHINESE;// 中文字
    }
}
