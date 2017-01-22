package com.wm.remusic.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class BMA {

    public static final String FORMATE = "json";
    public static final String BASE = "http://tingapi.ting.baidu.com/v1/restserver/ting?from=android&version=5.6.5.6&format=" + FORMATE;

    /**
     * 轮播音乐封面
     *
     * @param num 数量
     * @return
     */
    public static String focusPic(int num) {
        StringBuffer sb = new StringBuffer(BASE);
        sb.append("&method=").append("baidu.ting.plaza.getFocusPic")
                .append("&num=").append(num);
        return sb.toString();
    }

    /**
     * 唱片专辑
     *
     * @author Sanron
     */
    public static class Album {

        /**
         * 推荐唱片
         *
         * @param offset 偏移量
         * @param limmit 获取数量
         * @return
         */
        public static String recommendAlbum(int offset, int limmit) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.plaza.getRecommendAlbum")
                    .append("&offset=").append(offset)
                    .append("&limit=").append(limmit);
            return sb.toString();
        }

        /**
         * 唱片信息
         *
         * @param albumid 唱片id
         * @return
         */
        public static String albumInfo(String albumid) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.album.getAlbumInfo")
                    .append("&album_id=").append(albumid);
            return sb.toString();
        }
    }


    /**
     * 音乐场景
     *
     * @author Sanron
     */
    public static class Scene {

        /**
         * 推荐音乐场景(需要cuid，暂时关闭)
         * @return
         */
//		public static String sugestionScene(){
//			StringBuffer sb = new StringBuffer(BASE);
//			sb.append("&method=").append("baidu.ting.scene.getSugScene");
//			return sb.toString();
//		}

        /**
         * 固定场景
         *
         * @return
         */
        public static String constantScene() {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.scene.getConstantScene");
            return sb.toString();
        }

        /**
         * 所有场景类别
         *
         * @return
         */
        public static String sceneCategories() {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.scene.getCategoryList");
            return sb.toString();
        }

        /**
         * 场景类别下的所有场景
         *
         * @param categoreid 类别id
         * @return
         */
        public static String categoryScenes(String categoreid) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.scene.getCategoryScene")
                    .append("&category_id=").append(categoreid);
            return sb.toString();
        }
    }

    /**
     * 音乐标签
     *
     * @author Sanron
     */
    public static class Tag {
        /**
         * 所有音乐标签
         *
         * @return
         */
        public static String allSongTags() {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.tag.getAllTag");
            return sb.toString();
        }

        /**
         * 热门音乐标签
         *
         * @param num 数量
         * @return
         */
        public static String hotSongTags(int num) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.tag.getHotTag")
                    .append("&nums=").append(num);
            return sb.toString();
        }

        /**
         * 标签为tagname的歌曲
         *
         * @param tagname 标签名
         * @param limit   数量
         * @return
         */
        public static String tagSongs(String tagname, int limit) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.tag.songlist")
                    .append("&tagname=").append(encode(tagname))
                    .append("&limit=").append(limit);
            return sb.toString();
        }
    }

    public static class Song {

        /**
         * 歌曲基本信息
         *
         * @param songid 歌曲id
         * @return
         */
        public static String songBaseInfo(String songid) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.song.baseInfos")
                    .append("&song_id=").append(songid);
            return sb.toString();
        }

        /**
         * 编辑推荐歌曲
         *
         * @param num 数量
         * @return
         */
        public static String recommendSong(int num) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.song.getEditorRecommend")
                    .append("&num=").append(num);
            return sb.toString();
        }

        /**
         * 歌曲信息和下载地址
         *
         * @param songid
         * @return
         */
        public static String songInfo(String songid) {
            StringBuffer sb = new StringBuffer(BASE);
            String str = "songid=" + songid + "&ts=" + System.currentTimeMillis();
            String e = AESTools.encrpty(str);
            sb.append("&method=").append("baidu.ting.song.getInfos")
                    .append("&").append(str)
                    .append("&e=").append(e);
            return sb.toString();
        }

        /**
         * 歌曲伴奏信息
         *
         * @param songid
         * @return
         */
        public static String accompanyInfo(String songid) {
            StringBuffer sb = new StringBuffer(BASE);
            String str = "song_id=" + songid + "&ts=" + System.currentTimeMillis();
            String e = AESTools.encrpty(str);
            sb.append("&method=").append("baidu.ting.learn.down")
                    .append("&").append(str)
                    .append("&e=").append(e);
            return sb.toString();
        }

        /**
         * 相似歌曲
         *
         * @param songid
         * @return
         */
        public static String recommendSongList(String songid, int num) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.song.getRecommandSongList")
                    .append("&song_id=").append(songid)
                    .append("&num=").append(num);
            return sb.toString();
        }
    }

    /**
     * 艺术家
     *
     * @author Sanron
     */
    public static class Artist {

        /**
         * 全部地区
         */
        public static final int AREA_ALL = 0;
        /**
         * 华语
         */
        public static final int AREA_CHINIESE = 6;
        /**
         * 欧美
         */
        public static final int AREA_EU = 3;
        /**
         * 韩国
         */
        public static final int AREA_KOREA = 7;
        /**
         * 日本
         */
        public static final int AREA_JAPAN = 60;
        /**
         * 其他
         */
        public static final int AREA_OTHER = 5;

        /**
         * 无选择
         */
        public static final int SEX_NONE = 0;
        /**
         * 男性
         */
        public static final int SEX_MALE = 1;
        /**
         * 女性
         */
        public static final int SEX_FEMALE = 2;
        /**
         * 组合
         */
        public static final int SEX_GROUP = 3;

        /**
         * 获取艺术家列表
         *
         * @param offset 偏移
         * @param limit  数量
         * @param area   地区：0不分,6华语,3欧美,7韩国,60日本,5其他
         * @param sex    性别：0不分,1男,2女,3组合
         * @param order  排序：1按热门，2按艺术家id
         * @param abc    艺术家名首字母：a-z,other其他
         * @return
         */
        public static String artistList(int offset, int limit, int area, int sex, int order, String abc) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.artist.getList");
            sb.append("&offset=").append(offset);
            sb.append("&limit=").append(limit);
            sb.append("&area=").append(area);
            sb.append("&sex=").append(sex);
            sb.append("&order=").append(order);//暂时不清楚order排序
            if (abc != null && !abc.trim().equals("")) {
                sb.append("&abc=").append(abc);
            }
            return sb.toString();
        }

        /**
         * 热门艺术家
         *
         * @param offset 偏移量
         * @param limit  获取数量
         * @return
         */
        public static String hotArtist(int offset, int limit) {
            return artistList(offset, limit, 0, 0, 1, null);
        }

        /**
         * 艺术家歌曲
         *
         * @param tinguid  tinguid
         * @param artistid 艺术家id
         * @param offset   偏移量
         * @param limit    获取数量
         * @return
         */
        public static String artistSongList(String tinguid, String artistid, int offset, int limit) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.artist.getSongList")
                    .append("&order=2")
                    .append("&tinguid=").append(tinguid)
                    .append("&artistid=").append(artistid)
                    .append("&offset=").append(offset)
                    .append("&limits=").append(limit);
            return sb.toString();
        }

        /**
         * 艺术家信息
         *
         * @param tinguid  tinguid
         * @param artistid 艺术家id
         * @return
         */
        public static String artistInfo(String tinguid, String artistid) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.artist.getinfo")
                    .append("&tinguid=").append(tinguid)
                    .append("&artistid=").append(artistid);
            return sb.toString();
        }
    }

    /**
     * 音乐榜
     */
    public static class Billboard {

        /**
         * 所有音乐榜类别
         *
         * @return
         */
        public static String billCategory() {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.billboard.billCategory")
                    .append("&kflag=1");
            return sb.toString();
        }

        /**
         * 音乐榜歌曲
         *
         * @param type   类型
         * @param offset 偏移
         * @param size   获取数量
         * @return
         */
        public static String billSongList(int type, int offset, int size) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.billboard.billList")
                    .append("&type=").append(type)
                    .append("&offset=").append(offset)
                    .append("&size=").append(size)
                    .append("&fields=").append(encode("song_id,title,author,album_title,pic_big,pic_small,havehigh,all_rate,charge,has_mv_mobile,learn,song_source,korean_bb_song"));
            return sb.toString();
        }
    }

    /**
     * 歌单
     *
     * @author Sanron
     */
    public static class GeDan {

        /**
         * 歌单分类
         *
         * @return
         */
        public static String geDanCategory() {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.diy.gedanCategory");
            return sb.toString();
        }

        /**
         * 热门歌单
         *
         * @param num
         * @return
         */
        public static String hotGeDan(int num) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.diy.getHotGeDanAndOfficial")
                    .append("&num=").append(num);
            return sb.toString();
        }

        /**
         * 歌单
         *
         * @param pageNo   页码
         * @param pageSize 每页数量
         * @return
         */
        public static String geDan(int pageNo, int pageSize) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.diy.gedan")
                    .append("&page_size=").append(pageSize)
                    .append("&page_no=").append(pageNo);
            return sb.toString();
        }


        /**
         * 包含标签的歌单
         *
         * @param tag      标签名
         * @param pageNo   页码
         * @param pageSize 每页数量
         * @return
         */
        public static String geDanByTag(String tag, int pageNo, int pageSize) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.diy.search")
                    .append("&page_size=").append(pageSize)
                    .append("&page_no=").append(pageNo)
                    .append("&query=").append(encode(tag));
            return sb.toString();
        }

        /**
         * 歌单信息和歌曲
         *
         * @param listid 歌单id
         * @return
         */
        public static String geDanInfo(String listid) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.diy.gedanInfo")
                    .append("&listid=").append(listid);
            return sb.toString();
        }
    }

    /**
     * 电台
     *
     * @author Sanron
     */
    public static class Radio {

        /**
         * 录制电台
         *
         * @param pageNo   页数
         * @param pageSize 每页数量，也是返回数量
         * @return
         */
        public static String recChannel(int pageNo, int pageSize) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.radio.getRecChannel")
                    .append("&page_no=").append(pageNo)
                    .append("&page_size=").append(pageSize);
            return sb.toString();
        }

        /**
         * 推荐电台（注意返回的都是乐播节目)
         *
         * @param num
         * @return
         */
        public static String recommendRadioList(int num) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.radio.getRecommendRadioList")
                    .append("&num=").append(num);
            return sb.toString();
        }

        /**
         * 频道歌曲
         *
         * @param channelname 频道名,注意返回的json数据频道有num+1个，但是最后一个是空的
         * @return
         */
        public static String channelSong(String channelname, int num) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.radio.getChannelSong")
                    .append("&channelname=").append(encode(channelname))
                    .append("&pn=0")
                    .append("&rn=").append(num);
            return sb.toString();
        }
    }

    /**
     * 乐播节目
     * 节目相当于一个专辑
     * 每一期相当于专辑里的每首歌
     *
     * @author Sanron
     */
    public static class Lebo {

        /**
         * 频道
         *
         * @param pageNo   页码(暂时无用)
         * @param pageSize 每页数量，也是返回数量(暂时无用)
         * @return
         */
        public static String channelTag(int pageNo, int pageSize) {

            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.lebo.getChannelTag")
                    .append("&page_no=").append(pageNo)
                    .append("&page_size=0").append(pageSize);
            return sb.toString();
        }

        /**
         * 返回频道下的不同节目的几期
         * 包含几个节目，每个节目有一期或多期
         * 比如返回 	节目1第1期，节目1第2期，节目2第1期，节目3第6期
         *
         * @param tagId 频道id
         * @param num   数量
         * @return
         */
        public static String channelSongList(String tagId, int num) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.lebo.channelSongList")
                    .append("&tag_id=").append(tagId)
                    .append("&num=").append(num);
            return sb.toString();
        }

        /**
         * 节目信息
         *
         * @param albumid        节目id
         * @param lastestSongNum 返回最近几期
         * @return
         */
        public static String albumInfo(String albumid, int lastestSongNum) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.lebo.albumInfo")
                    .append("&album_id=").append(albumid)
                    .append("&num=").append(lastestSongNum);
            return sb.toString();
        }
    }

    /**
     * 搜索
     *
     * @author Sanron
     */
    public static class Search {

        /**
         * 热门关键字
         *
         * @return
         */
        public static String hotWord() {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.search.hot");
            return sb.toString();
        }

        /**
         * 搜索建议
         *
         * @param
         * @return
         */
        public static String searchSugestion(String query) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.search.catalogSug")
                    .append("&query=").append(encode(query));
            return sb.toString();
        }

        /**
         * 搜歌词
         *
         * @param songname 歌名
         * @param artist   艺术家
         * @return
         */
        public static String searchLrcPic(String songname, String artist) {
            StringBuffer sb = new StringBuffer(BASE);
            String ts = Long.toString(System.currentTimeMillis());
            String query = encode(songname) + "$$" + encode(artist);
            String e = AESTools.encrpty("query=" + songname + "$$" + artist + "&ts=" + ts);
            sb.append("&method=").append("baidu.ting.search.lrcpic")
                    .append("&query=").append(query)
                    .append("&ts=").append(ts)
                    .append("&type=2")
                    .append("&e=").append(e);
            return sb.toString();
        }

        /**
         * 合并搜索结果，用于搜索建议中的歌曲
         *
         * @param query
         * @return
         */
        public static String searchMerge(String query, int pageNo, int pageSize) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.search.merge")
                    .append("&query=").append(encode(query))
                    .append("&page_no=").append(pageNo)
                    .append("&page_size=").append(pageSize)
                    .append("&type=-1&data_source=0");
            return sb.toString();
        }

        /**
         * 搜索伴奏
         *
         * @param query    关键词
         * @param pageNo   页码
         * @param pageSize 每页数量，也是返回数量
         * @return
         */
        public static String searchAccompany(String query, int pageNo, int pageSize) {
            StringBuffer sb = new StringBuffer(BASE);
            sb.append("&method=").append("baidu.ting.learn.search")
                    .append("&query=").append(encode(query))
                    .append("&page_no=").append(pageNo)
                    .append("&page_size=").append(pageSize);
            return sb.toString();
        }
    }

    public static String encode(String str) {
        if (str == null) return "";

        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }

}
