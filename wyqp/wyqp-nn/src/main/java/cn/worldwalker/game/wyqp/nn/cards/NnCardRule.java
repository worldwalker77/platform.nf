package cn.worldwalker.game.wyqp.nn.cards;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import cn.worldwalker.game.wyqp.common.domain.base.BasePlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.base.Card;
import cn.worldwalker.game.wyqp.common.exception.BusinessException;
import cn.worldwalker.game.wyqp.common.exception.ExceptionEnum;
import cn.worldwalker.game.wyqp.nn.enums.NnCardTypeEnum;

public class NnCardRule {
	
	public static void main(String[] args) {
		List<Card> cardList = new ArrayList<Card>();
		Card card1 = new Card();
		card1.setCardValue(2);
		Card card2 = new Card();
		card2.setCardValue(4);
		Card card3 = new Card();
		card3.setCardValue(5);
		Card card4 = new Card();
		card4.setCardValue(5);
		Card card5 = new Card();
		card5.setCardValue(5);
		cardList.add(card1);
		cardList.add(card2);
		cardList.add(card3);
		cardList.add(card4);
		cardList.add(card5);
		List<Card> nnCardList = new ArrayList<Card>();
	}
	
	/**
	 * 计算牌型
	 * @param cardList
	 * @return
	 */
	public static Integer calculateCardType(List<Card> cardList, List<Card> nnCardList, List<Card> robFourCardList, Card fifthCard){
		for(int i = 0; i < 4; i++){
			Card card = new Card();
			cardCopy(cardList.get(i), card);
			robFourCardList.add(card);
		}
		cardCopy(cardList.get(4), fifthCard);
		/**排序*/
		rankSinglePlayerCards(cardList);
		/**炸弹牛*/
		if (isBombNiu(cardList)) {
			return NnCardTypeEnum.BOMB_NIU.cardType;
		}
		/**五小*/
		if (isFiveSmallNiu(cardList)) {
			return NnCardTypeEnum.FIVE_SMALL_NIU.cardType;
		}
		/**五花牛*/
		if (isGoldNiu(cardList)) {
			return NnCardTypeEnum.GOLD_NIU.cardType;
		}
		
		/**有牛牌型*/
		Integer value0 = 0;
		Integer value1 = 0;
		Integer value2 = 0;
		for(int i = 0; i < 3; i++){
			value0 = getRealValue(cardList.get(i));
			for(int j = i + 1; j < 4; j++){
				value1 = getRealValue(cardList.get(j));
				for(int k = j + 1; k < 5; k++){
					value2 = getRealValue(cardList.get(k));
					if ((value0 + value1 + value2)%10 == 0) {
						nnCardList.add(cardList.get(i));
						nnCardList.add(cardList.get(j));
						nnCardList.add(cardList.get(k));
						/**有牛的情况下，如果是牛牛*/
						if (isNiuNiu(cardList)) {
							return NnCardTypeEnum.NIU_NIU.cardType;
						}
						return getNiuNum(cardList, i, j, k);
					}
				}
			}
		}
		/**无牛*/
		return NnCardTypeEnum.NO_NIU.cardType;
	}
	
	public static void cardCopy(Card srcCard, Card tarCard){
		tarCard.setCardIndex(srcCard.getCardIndex());
		tarCard.setCardSuit(srcCard.getCardSuit());
		tarCard.setCardSuitName(srcCard.getCardSuitName());
		tarCard.setCardValue(srcCard.getCardValue());
	}
	
	public static Integer getNiuNum(List<Card> cardList, int i, int j, int k){
		Integer cartType = null;
		int size = cardList.size();
		int subSum = 0;
		for(int m = 0; m < size; m++){
			if (m != i && m != j && m != k) {
				subSum += getRealValue(cardList.get(m));
			}
		}
		int remainder = subSum%10;
		switch (remainder) {
		case 1:
			cartType = NnCardTypeEnum.NIU_1.cardType;
			break;
		case 2:
			cartType = NnCardTypeEnum.NIU_2.cardType;
			break;
		case 3:
			cartType = NnCardTypeEnum.NIU_3.cardType;
			break;
		case 4:
			cartType = NnCardTypeEnum.NIU_4.cardType;
			break;
		case 5:
			cartType = NnCardTypeEnum.NIU_5.cardType;
			break;
		case 6:
			cartType = NnCardTypeEnum.NIU_6.cardType;
			break;
		case 7:
			cartType = NnCardTypeEnum.NIU_7.cardType;
			break;
		case 8:
			cartType = NnCardTypeEnum.NIU_8.cardType;
			break;
		case 9:
			cartType = NnCardTypeEnum.NIU_9.cardType;
			break;

		default:
			break;
		}
		return cartType;
	}
	
	public static Integer getRealValue(Card card){
		int realValue = 0;
		if (card.getCardValue() > 10) {
			realValue = 10;
		}else{
			realValue = card.getCardValue();
		}
		return realValue;
	}
	
	public static boolean isBombNiu(List<Card> cardList){
		for(int i = 0; i < 2; i++){
			if (cardList.get(i).getCardValue().equals(cardList.get(i + 1).getCardValue())
				&&cardList.get(i + 1).getCardValue().equals(cardList.get(i + 2).getCardValue())
				&&cardList.get(i + 2).getCardValue().equals(cardList.get(i + 3).getCardValue())){
				return true;
			}
		}
		return false;
	}
	
	public static boolean isFiveSmallNiu(List<Card> cardList){
		int size = cardList.size();
		for(int i = 0; i < size; i++){
			if (cardList.get(i).getCardValue() >= 5){
				return false;
			}
		}
		return true;
	}
	
	public static boolean isGoldNiu(List<Card> cardList){
		int size = cardList.size();
		for(int i = 0; i < size; i++){
			if (cardList.get(i).getCardValue() <= 10){
				return false;
			}
		}
		return true;
	}
	
	public static boolean isNiuNiu(List<Card> cardList){
		int size = cardList.size();
		int sum = 0;
		int realValue = 0;
		for(int i = 0; i < size; i++){
			if (cardList.get(i).getCardValue() > 10) {
				realValue = 10;
			}else{
				realValue = cardList.get(i).getCardValue();
			}
			sum += realValue;
		}
		if (sum%10 == 0) {
			return true;
		}
		return false;
	}
	
	/**
	 * 对玩家的牌进行排序，从小到大
	 * @param playerCards
	 */
	public static void rankCards(List<List<Card>> playerCards){
		for(List<Card> cards : playerCards){
			int size = cards.size();
			for(int i = 0; i< size - 1; i++){
				for(int j = 0; j < size - 1 - i; j++){
					if (cards.get(j).getCardValue() > cards.get(j + 1).getCardValue()) {
						Card tempCard = cards.get(j);
						cards.set(j, cards.get(j + 1));
						cards.set(j + 1, tempCard);
					}else if(cards.get(j).getCardValue().equals(cards.get(j + 1).getCardValue())){
						if (cards.get(j).getCardSuit() >  cards.get(j + 1).getCardSuit()) {
							Card tempCard = cards.get(j);
							cards.set(j, cards.get(j + 1));
							cards.set(j + 1, tempCard);
						}
					}
				}
			}
		}
	}
	
	/**
	 * 对单个玩家的牌进行排序，从小到大
	 * @param playerCards
	 */
	public static void rankSinglePlayerCards(List<Card> cards){
		int size = cards.size();
		for(int i = 0; i< size - 1; i++){
			for(int j = 0; j < size - 1 - i; j++){
				if (cards.get(j).getCardValue() > cards.get(j + 1).getCardValue()) {
					Card tempCard = cards.get(j);
					cards.set(j, cards.get(j + 1));
					cards.set(j + 1, tempCard);
				}else if(cards.get(j).getCardValue().equals(cards.get(j + 1).getCardValue())){
					if (cards.get(j).getCardSuit() >  cards.get(j + 1).getCardSuit()) {
						Card tempCard = cards.get(j);
						cards.set(j, cards.get(j + 1));
						cards.set(j + 1, tempCard);
					}
				}
			}
		}
	}
	
	public static int cardTypeCompare(BasePlayerInfo player1, BasePlayerInfo player2){
		if (player1.getCardType() > player2.getCardType()) {
			return 1;
		}else if(player1.getCardType() < player2.getCardType()){
			return -1;
		}else{/**如果牌型相同，则比较最大牌的牌值（除炸弹牛外）*/
			if(NnCardTypeEnum.BOMB_NIU.cardType.equals(player1.getCardType())){
				if (player1.getCardList().get(1).getCardValue() > player2.getCardList().get(1).getCardValue()) {
					return 1;
				}else{
					return -1;
				}
			}else{
				if (compareTwoCardWithCardValueAndSuit(player1.getCardList().get(4), player2.getCardList().get(4)) == 1) {
					return 1;
				}else{
					return -1;
				}
			}
		}
	}
	
	/**
	 * 比较两张牌的大小(牌值和花色综合比较)
	 * @param card1
	 * @param card2
	 * @return
	 */
	public static int compareTwoCardWithCardValueAndSuit(Card card1, Card card2){
		if (card1.getCardValue() > card2.getCardValue()) {
			return 1;
		}else if(card1.getCardValue().equals(card2.getCardValue())){//如果牌值相等，则继续比较花色
			/**一副牌中牌值相同，花色肯定不同*/
			if (card1.getCardSuit() > card2.getCardSuit()) {
				return 1;
			}else{
				return -1;
			}
		}else{
			return -1;
		}
	}
	
	/**
	 * 比较两张牌的牌值大小
	 * @param card1
	 * @param card2
	 * @return
	 */
	public static int compareTwoCardWithCardValue(Card card1, Card card2){
		if (card1.getCardValue() > card2.getCardValue()) {
			return 1;
		}else if(card1.getCardValue().equals(card2.getCardValue())){//如果牌值相等，则继续比较花色
			return 0;
		}else{
			return -1;
		}
	}
	
	/**
	 * 比较两张牌的花色大小
	 * @param card1
	 * @param card2
	 * @return
	 */
	public static int compareTwoCardWithCardSuit(Card card1, Card card2){
		if (card1.getCardSuit() > card2.getCardSuit()) {
			return 1;
		}else{
			return -1;
		}
	}
	
}
