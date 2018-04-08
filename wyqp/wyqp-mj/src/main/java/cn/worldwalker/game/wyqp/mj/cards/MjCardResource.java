package cn.worldwalker.game.wyqp.mj.cards;

import cn.worldwalker.game.wyqp.common.utils.JsonUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MjCardResource {
	
	private static List<Integer> orderCardList = new ArrayList<Integer>();
	static{
	    //先改成支持江西南丰
		for(int i = 0; i < 34; i++){
			if (i > 33) {
				orderCardList.add(i);
			}else{
				for(int j = 0; j < 4; j++){
					orderCardList.add(i);
				}
			}
		}
	}
	/**
	 * 生成桌牌列表，乱序
	 * @return
	 */
	public static List<Integer> genTableOutOrderCardList(){
		List<Integer> cardList = new ArrayList<Integer>();
		cardList.addAll(orderCardList);
		Collections.shuffle(cardList);
		return cardList;
	}
	
	public static List<Integer> sortCardList(List<Integer> cardList){
		Collections.sort(cardList);
		return cardList;
	}
	/**
	 * 生成玩家手牌列表
	 * @param tableRemainderCardList
	 * @param cardNum 需要生成牌的数量
	 * @return
	 */
	public static List<Integer> genHandCardList(List<Integer> tableRemainderCardList, int cardNum){
		List<Integer> handCardList = new ArrayList<Integer>();
		/**循环摸cardNum张牌*/
		for(int i = 0; i < cardNum; i++){
			int tempCardIndex = tableRemainderCardList.remove(0);
			handCardList.add(tempCardIndex);
		}
		/**排序*/
		Collections.sort(handCardList);
		return handCardList;
	}
	public static Integer mopai(List<Integer> tableRemainderCardList){
		return tableRemainderCardList.remove(0);
	}
	
	public static Integer genPiZiCardInex(List<Integer> tableRemainderCardList, Integer indexLine){
		int beginIndex = 53;
		Integer piZiCardIndex = 0;
		while(true){
			piZiCardIndex = tableRemainderCardList.get(beginIndex);
			if (piZiCardIndex < indexLine) {
				tableRemainderCardList.remove(beginIndex);
				break;
			}
			beginIndex++;
		}
		return piZiCardIndex;
	}
	
	public static Integer genBaiDaCardIndex(Integer piziCardIndex){
		Integer baiDaCardIndex = piziCardIndex + 1;
		if (piziCardIndex == 8) {
			baiDaCardIndex = 0;
		}else if(piziCardIndex == 17){
			baiDaCardIndex = 9;
		}else if(piziCardIndex == 26){
			baiDaCardIndex = 18;
		}else if(piziCardIndex == 30){/**如果痞子是北风，则百搭是东风*/
			baiDaCardIndex = 27;
		}else if(piziCardIndex == 33){/**如果痞子是白，则百搭是中*/
			baiDaCardIndex = 31;
		}
		return baiDaCardIndex;
	}
	
	public static void main(String[] args) {
		List<Integer> cardList = genTableOutOrderCardList();
		System.out.println(JsonUtil.toJson(genHandCardList(cardList, 13)));
	}
}
