package com.wm.remusic.json;

/**
 * Created by wm on 2016/7/28.
 */
public class RecommendListNewAlbumInfo {


    /**
     * desc : 祁隆
     * pic : http://business.cdn.qianqian.com/qianqian/pic/bos_client_1469672285aa1239d0e00543d967a138198f6ab19c.jpg
     * type_id : 268081987
     * type : 2
     * title : 惦记
     * tip_type : 3
     * author : 祁隆
     */

    private String desc;
    private String pic;
    private String type_id;
    private int type;
    private String title;
    private int tip_type;
    private String author;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getType_id() {
        return type_id;
    }

    public void setType_id(String type_id) {
        this.type_id = type_id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getTip_type() {
        return tip_type;
    }

    public void setTip_type(int tip_type) {
        this.tip_type = tip_type;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
