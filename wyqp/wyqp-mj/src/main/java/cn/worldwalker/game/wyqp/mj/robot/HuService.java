package cn.worldwalker.game.wyqp.mj.robot;

import cn.worldwalker.game.wyqp.mj.seed.Seed;
import cn.worldwalker.game.wyqp.mj.seed.SeedService;

import java.util.*;

public class HuService {
    private static HuService ourInstance = new HuService();

    public static HuService getInstance() {
        return ourInstance;
    }

    private HuService() {
    }

    /**
     * 万、筒、条、风边界等信息
     */
    enum SubCardEnum {
        wan(0, 8, false),
        tong(9, 17, false),
        tiao(18, 26, false),
        feng(27, 33, true);

        SubCardEnum(int min, int max, boolean isFeng) {
            this.min = min;
            this.max = max;
            this.isFeng = isFeng;
        }

        int min;
        int max;
        boolean isFeng;
    }

    /**
     * 拆分生成 万、筒、条、风的牌
     *
     * @param cardList 所有手牌
     * @return 万->牌，筒->牌，条->牌
     */
    private Map<SubCardEnum, List<Integer>> split(List<Integer> cardList) {
        Map<SubCardEnum, List<Integer>> typeMap = new HashMap<>(4);
        for (SubCardEnum subCardEnum : SubCardEnum.values()) {
            List<Integer> subCardList = new ArrayList<>(16);
            for (Integer v : cardList) {
                if (v >= subCardEnum.min && v <= subCardEnum.max) {
                    subCardList.add(v % 9);
                }
            }
            if (subCardList.size() > 0) {
                typeMap.put(subCardEnum, subCardList);
            }
        }
        return typeMap;
    }

    private int[] convertToSeed(List<Integer> valueList) {
        int[] seed = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (Integer v : valueList) {
            seed[v]++;
        }
        return seed;
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
        return seeds.contains(new Seed(convertToSeed(valueList)));
    }

    private boolean isSubLan(List<Integer> valueList, boolean isFeng) {
        int[] seed = convertToSeed(valueList);
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

    public boolean isHu(List<Integer> cardList) {
        if (isQiDui(cardList) || isShiSanLan(cardList)) {
            return true;
        }
        Map<SubCardEnum, List<Integer>> map = split(cardList);
        for (Map.Entry<SubCardEnum, List<Integer>> entry : map.entrySet()) {
            if (!isSubHu(entry.getValue(), entry.getKey().isFeng))
                return false;
        }
        return true;
    }

    public boolean isQiDui(List<Integer> cardList) {
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

    public boolean isShiSanLan(List<Integer> cardList) {
        if (cardList.size() != 14){
            return false;
        }
        Map<SubCardEnum, List<Integer>> map = split(cardList);
        for (Map.Entry<SubCardEnum, List<Integer>> entry : map.entrySet()) {
            if (!isSubLan(entry.getValue(), entry.getKey().isFeng))
                return false;
        }
        return true;

    }

}


