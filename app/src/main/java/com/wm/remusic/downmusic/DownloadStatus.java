package com.wm.remusic.downmusic;

/**
 * Created by dzc on 15/11/21.
 */
public class DownloadStatus {
    public static final int DOWNLOAD_STATUS_INIT = -1;
    public static final int DOWNLOAD_STATUS_PREPARE = 0;
    public static final int DOWNLOAD_STATUS_START = 1;
    public static final int DOWNLOAD_STATUS_DOWNLOADING = 2;
    public static final int DOWNLOAD_STATUS_CANCEL = 3;
    public static final int DOWNLOAD_STATUS_ERROR = 4;
    public static final int DOWNLOAD_STATUS_COMPLETED = 5;
    public static final int DOWNLOAD_STATUS_PAUSE = 6;
}
