package cn.worldwalker.game.wyqp.mj.service;

import cn.worldwalker.game.wyqp.mj.enums.MjValueEnum;
import cn.worldwalker.game.wyqp.mj.seed.Seed;
import cn.worldwalker.game.wyqp.mj.seed.SeedService;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MjHuService {
    private static MjHuService ourInstance = new MjHuService();

    private MjCardService mjCardService = MjCardService.getInstance();

    public static MjHuService getInstance() {
        return ourInstance;
    }

    private MjHuService() {
    }

    /**
     * @param valueList value的范围为0-8
     * @return 是否符合胡牌
     */
    private boolean isSubHu(List<Integer> valueList, boolean isFeng) {
        if (valueList.isEmpty())
            return true;
        boolean withGen = valueList.size() % 3 == 2;
        Set<Seed> seeds = isFeng ? SeedService.getInstanceFeng().getSeeds(withGen, 0) :
                SeedService.getInstance().getSeeds(withGen, 0);
        return seeds.contains(new Seed(mjCardService.convertToSeed(valueList)));
    }

    /**
    * 单花牌是否符合十三烂规则
    */
    private boolean isSubLan(List<Integer> valueList, boolean isFeng) {
        int[] seed = mjCardService.convertToSeed(valueList);
        for (int i = 0; i < 9; i++) {
            if (seed[i] > 1)
                return false;
            if (!isFeng && seed[i] == 1) {
                for (int j = 1; j < 3 && i + j < 9; j++) {
                    if (seed[j] > 0) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 判胡,包括符合胡牌规则、十三烂、七对、清一色
     */
    public boolean isHu(List<Integer> cardList) {
        return isQingYiSe(cardList) || isQiDui(cardList) || isShiSanLan(cardList) || isNormalHu(cardList);

    }

    /**
     * 判断是否符合胡牌规则
     */
    boolean isNormalHu(List<Integer> cardList){
        Map<MjValueEnum, List<Integer>> map = mjCardService.split(cardList);
        for (Map.Entry<MjValueEnum, List<Integer>> entry : map.entrySet()) {
            if (!isSubHu(entry.getValue(), entry.getKey().isFeng))
                return false;
        }
        return true;
    }

    /**
     * 七对
     */
    boolean isQiDui(List<Integer> cardList) {
        //如果手牌数量小于13则说明吃过牌，不能胡七对
        if (cardList.size() != 14) {
            return false;
        }
        //将手牌进行格式化*/
        int[] cards = new int[34];
        for (Integer aCardList : cardList) {
            cards[aCardList]++;
        }
        for (int i = 0; i < 34; ++i) {
            if (cards[i] % 2 != 0)
                return false;
        }
        return true;
    }

    /**
     * 十三烂
     */
    boolean isShiSanLan(List<Integer> cardList) {
        if (cardList.size() != 14){
            return false;
        }
        Map<MjValueEnum, List<Integer>> map = mjCardService.split(cardList);
        for (Map.Entry<MjValueEnum, List<Integer>> entry : map.entrySet()) {
            if (!isSubLan(entry.getValue(), entry.getKey().isFeng))
                return false;
        }
        return true;

    }

    /**
     * 清一色
     */
    boolean isQingYiSe(List<Integer> cardList){
        Map<MjValueEnum, List<Integer>> map = mjCardService.split(cardList);
        return map.size() == 1;
    }
}


