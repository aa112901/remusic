package com.wm.remusic.net;

public class BytesHandler {
    private static char[] chars;
    private static byte[] bytes;

    static {
        chars = new char[64];
        int j = 0;
        for (int i = 65; i <= 90; i++, j++) {
            chars[j] = (char) i;
        }
        for (int i = 97; i <= 122; i++, j++) {
            chars[j] = (char) i;
        }
        for (int i = 48; i <= 57; i++, j++) {
            chars[j] = (char) i;
        }
        chars[j] = 43;
        j++;
        chars[j] = 47;

        bytes = new byte[128];

        for (int i = 0; i < 128; i++) {
            bytes[i] = -1;
        }
        for (int i = 0; i < 64; i++) {
            bytes[chars[i]] = (byte) i;
        }

    }

    public static char[] getChars(byte[] bytes) {
        return getChars(bytes, 0, bytes.length);
    }

    public static char[] getChars(byte[] bytes, int start, int length) {

        int num0 = (length * 4 + 2) / 3;
        final char CHAR = 61;
        char[] result = new char[(length + 2) / 3 * 4];
        int max = start + length;
        int bytesIndex = start;
        int resultIndex = 0;

        for (; bytesIndex < max; ) {

            int n0 = bytes[bytesIndex++] & 0xFF;

            int n1 = 0;
            if (bytesIndex < max) {
                n1 = bytes[bytesIndex++] & 0xFF;
            }

            int n2 = 0;
            if (bytesIndex < max) {
                n2 = bytes[bytesIndex++] & 0xFF;
            }

            int i1 = n0 >>> 2;
            int i2 = ((n0 & 0x3) << 4) | (n1 >>> 4);
            int i3 = ((n1 & 0xF) << 2) | (n2 >>> 6);
            int i4 = n2 & 0x3F;

            result[resultIndex++] = chars[i1];
            result[resultIndex++] = chars[i2];

            char c;
            c = resultIndex < num0 ? chars[i3] : CHAR;
            result[resultIndex++] = c;

            c = resultIndex < num0 ? chars[i4] : CHAR;
            result[resultIndex++] = c;
        }
        return result;
    }
}
