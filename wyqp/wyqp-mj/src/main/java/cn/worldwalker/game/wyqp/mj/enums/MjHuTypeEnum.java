package cn.worldwalker.game.wyqp.mj.enums;

public enum MjHuTypeEnum {
	
	zhuaChong(0, 1, "抓冲"),
	ziMo(1, 1, "自摸"),
	tianHu(2, 1, "天胡"),
	qiangGang(3, 3, "抢杠"),
	gangKai(4, 2, "杠开"),
    diHu(5, 2, "地胡");

	public Integer type;
	/**倍数*/
	public Integer multiple;
	public String desc;
	
	private MjHuTypeEnum(Integer type, Integer multiple, String desc){
		this.type = type;
		this.multiple = multiple;
		this.desc = desc;
	}
	
	public static MjHuTypeEnum getMjHuTypeEnum(Integer type){
		for(MjHuTypeEnum cardType : MjHuTypeEnum.values()){
			if (cardType.type.equals(type)) {
				return cardType;
			}
		}
		return null;
		
	}
}
