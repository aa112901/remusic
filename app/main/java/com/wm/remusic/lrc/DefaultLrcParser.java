package com.wm.remusic.lrc;

import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 默认的歌词解析器
 *
 * @author Ligang  2014/8/19
 */
public class DefaultLrcParser implements ILrcParser {
    private static final DefaultLrcParser istance = new DefaultLrcParser();

    public static final DefaultLrcParser getIstance() {
        return istance;
    }

    private DefaultLrcParser() {
    }

    /***
     * 将歌词文件里面的字符串 解析成一个List<LrcRow>
     */
    @Override
    public List<LrcRow> getLrcRows(String str) {

        if (TextUtils.isEmpty(str)) {
            return null;
        }
        BufferedReader br = new BufferedReader(new StringReader(str));

        List<LrcRow> lrcRows = new ArrayList<LrcRow>();
        String lrcLine;
        try {
            while ((lrcLine = br.readLine()) != null) {
                List<LrcRow> rows = LrcRow.createRows(lrcLine);
                if (rows != null && rows.size() > 0) {
                    lrcRows.addAll(rows);
                }
            }
            Collections.sort(lrcRows);
            int len = lrcRows.size();
            for (int i = 0; i < len - 1; i++) {
                lrcRows.get(i).setTotalTime(lrcRows.get(i + 1).getTime() - lrcRows.get(i).getTime());
            }
            lrcRows.get(len - 1).setTotalTime(5000);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return lrcRows;
    }

}
