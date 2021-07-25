package me.tiger.modules.works.constant;

public enum Type {

    ARTICLE(0,"文字类作品"),
    IMAGES(1,"图片类作品"),
    VIDEO(2,"视频类作品");

    private Integer code;
    private String name;

    Type(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
