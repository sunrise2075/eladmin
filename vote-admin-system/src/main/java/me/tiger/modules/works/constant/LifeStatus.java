package me.tiger.modules.works.constant;

/**
 * 作品状态：1. 提交  2. 审核中  3. 审核通过  4. 审核拒绝 5. 获奖
 *
 * */
public enum LifeStatus {
    SUBMIT(1,"提交"),
    REVIEWING(2,"审核中"),
    APPROVED(3,"审核通过"),
    REJECTED(4,"审核拒绝"),
    WIN(5,"已获奖");


    ;
    private Integer code;
    private String name;

    LifeStatus(Integer code, String name) {
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
