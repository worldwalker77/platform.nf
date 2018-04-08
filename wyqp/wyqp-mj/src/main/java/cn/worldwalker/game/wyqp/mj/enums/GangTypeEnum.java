package cn.worldwalker.game.wyqp.mj.enums;

public enum GangTypeEnum {
    AN_GANG(1,"暗杠"),
    MING_GANG(2,"明杠"),
    ZI_MO_MING_GANG(3,"自摸明杠");

    GangTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer type;
    public String desc;
}
