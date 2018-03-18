package cn.worldwalker.game.wyqp.mj.enums;

public enum MjValueEnum {
    wan(0, 8, false),
    tong(9, 17, false),
    tiao(18, 26, false),
    feng(27, 33, true);

    MjValueEnum(int min, int max, boolean isFeng) {
        this.min = min;
        this.max = max;
        this.isFeng = isFeng;
    }

    public int min;
    public int max;
    public boolean isFeng;

}
