package cn.worldwalker.game.wyqp.mj.service;

import cn.worldwalker.game.wyqp.mj.enums.MjValueEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MjCardService {
    private static MjCardService ourInstance = new MjCardService();

    public static MjCardService getInstance() {
        return ourInstance;
    }

    private MjCardService() {
    }

    /**
     * 拆分生成 万、筒、条、风的牌
     */
    public Map<MjValueEnum, List<Integer>> split(List<Integer> cardList) {
        Map<MjValueEnum, List<Integer>> typeMap = new HashMap<>(4);
        for (MjValueEnum subCardEnum : MjValueEnum.values()) {
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

    /**
     * 9位的seed
     */
    int[] convertToSeed(List<Integer> valueList) {
        int[] seed = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int v : valueList) {
            seed[v]++;
        }
        return seed;
    }

    /**
     * 34位的seed
     */
    public int[] convertToLongSeed(List<Integer> cardList){
        int[] seed = new int[34];
        for (int v : cardList) {
            seed[v]++;
        }
        return seed;
    }


}
