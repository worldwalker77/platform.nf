package cn.worldwalker.game.wyqp.mj.enums;

public enum MjScoreEnum {
    PING_HU(1, 1,"平胡"),
    PENG_PENG_HU(2,4,"碰碰胡"),
    MENG_QING_PENG_PENG_HU(5,8,"门清碰碰胡"),
    QI_DUI(7,8,"七对"),
    SHI_SHI_SAN_LAN(8,2,"十三烂"),
    QI_XING(9,4,"七星"),
    DAN_DIAO(10,8,"单吊"),
    LUAN_QING_YI_SE(11,4,"乱清一色"),
    HU_QING_YI_SE(12,8,"胡清一色"),
    MENG_QING_QING_YI_SE(13,8,"门清清一色"),
    ZI_QING_YI_SE(14,8,"字清一色");


    MjScoreEnum(Integer type, Integer score, String desc) {
        this.type = type;
        this.score = score;
        this.desc = desc;
    }
    public Integer type;
    public Integer score;
    String desc;

    public static MjScoreEnum getByType(Integer type){
        for (MjScoreEnum mjScoreEnum: MjScoreEnum.values()){
            if (mjScoreEnum.type.equals(type)){
                return mjScoreEnum;
            }
        }
        return null;
    }
}
