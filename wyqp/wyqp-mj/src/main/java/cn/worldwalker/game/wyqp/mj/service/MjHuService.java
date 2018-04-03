package cn.worldwalker.game.wyqp.mj.service;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.mj.enums.MjValueEnum;
import cn.worldwalker.game.wyqp.mj.seed.Seed;
import cn.worldwalker.game.wyqp.mj.seed.SeedService;

import java.util.*;

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
    private boolean isSubHu(List<Integer> valueList, boolean isFeng, int laziCnt) {
        if (valueList.isEmpty())
            return true;
        boolean withGen = ((valueList.size() + laziCnt)) % 3 == 2;
        Set<Seed> seeds = isFeng ? SeedService.getInstanceFeng().getSeeds(withGen, laziCnt) :
                SeedService.getInstance().getSeeds(withGen, laziCnt);
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
                    if (seed[i+j] > 0) {
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
        return isQiDui(cardList) || isShiSanLan(cardList) || isNormalHu(cardList);

    }

    /**
     * 判断是否符合胡牌规则(无赖子的情况）
     */
    boolean isNormalHu(List<Integer> cardList) {
        if (cardList.size() % 3 != 2){
            return false;
        }
        Map<MjValueEnum, List<Integer>> map = mjCardService.split(cardList);
        boolean hasGen = false;
        for (Map.Entry<MjValueEnum, List<Integer>> entry : map.entrySet()) {
            if (entry.getValue().size() %3 == 2){
                if (hasGen){
                    return false;
                }
                hasGen = true;
            }
            if (!isSubHu(entry.getValue(), entry.getKey().isFeng, 0))
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
        int[] cards = mjCardService.convertToLongSeed(cardList);
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
        if (cardList.size() != 14) {
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
    public boolean isQingYiSe(List<Integer> cardList) {
        Map<MjValueEnum, List<Integer>> map = mjCardService.split(cardList);
        //杠了的有可能大于14张牌吧
        return cardList.size() > 13 && map.size() == 1;
    }

    /**
     * 一整幅牌（包括万、筒、条、风），带赖子牌胡，一定是有将的
     */
    boolean isHuLaizi(List<Integer> cardList, int laiziCnt) {
        if ((cardList.size() + laiziCnt) % 3 != 2){
           return false;
        }

        Map<MjValueEnum, List<Integer>> map = mjCardService.split(cardList);
        //只有一个花色
        if (map.size() == 1){
            List<Integer> cardValueList = null;
            for (MjValueEnum mjValueEnum: MjValueEnum.values()){
                if (map.get(mjValueEnum) != null){
                    cardValueList = map.get(mjValueEnum);
                    break;
                }
            }
            return isSubHu(cardValueList, cardList.get(0) >= MjValueEnum.feng.min ,laiziCnt);
        }

        Map<Integer, List<Map.Entry<MjValueEnum, List<Integer>>>> mapCntCard = new HashMap<>(4);
        //按照％3值来存入map
        for (Map.Entry<MjValueEnum, List<Integer>> entry : map.entrySet()) {
            Integer cnt = entry.getValue().size() % 3;
            if (mapCntCard.get(cnt) == null) {
                mapCntCard.put(cnt, new ArrayList<Map.Entry<MjValueEnum, List<Integer>>>(4));
            }
            mapCntCard.get(cnt).add(entry);
        }

        //如果是有多个花色，各花色轮流配赖子当将，检查其他花型的是否符合胡牌型
        for (Map.Entry<Integer, List<Map.Entry<MjValueEnum, List<Integer>>>> entry : mapCntCard.entrySet()) {
            int genNeedLaiCnt = 2 - entry.getKey();  //凑够带将的胡牌，需要的赖子数
            if (genNeedLaiCnt <= laiziCnt) {
                for (Map.Entry<MjValueEnum, List<Integer>> entry1 : entry.getValue()) {
                    MjValueEnum genValueEnum = entry1.getKey();
                    List<Integer> genCardList = entry1.getValue();
//                    System.out.println(genCardList);
                    boolean isHu = true;
                    if (isSubHu(genCardList, genValueEnum.isFeng, genNeedLaiCnt)) {
                        int restLaiziCnt = laiziCnt - genNeedLaiCnt; //凑够不带将的胡牌，需要的赖子(有可能需要再加三个赖子，再判断一次, 如果赖子够的话）
                        for (Map.Entry<MjValueEnum, List<Integer>> entry2 : map.entrySet()) {
                            MjValueEnum tangValueEnum = entry2.getKey();
                            List<Integer> tangCardList = entry2.getValue();
                            if (!tangValueEnum.equals(genValueEnum) ) { //判断除了已经用作带将胡的牌，其他的牌是否符合胡牌
                                int needLaiCnt = (3 - (tangCardList.size() % 3)) % 3;
                                if (needLaiCnt <= restLaiziCnt) {
                                    if (isSubHu(tangCardList, tangValueEnum.isFeng, needLaiCnt)) {
                                        restLaiziCnt = restLaiziCnt - needLaiCnt;
                                    }else {
                                       isHu = false;
                                    }
                                }else {
                                    isHu = false;
                                }
                            }
                        }
                    } else {
                        isHu = false;
                    }
                    if (isHu)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean isGang(List<Integer> cardList, Integer card){
        int[] seed = mjCardService.convertToLongSeed(cardList);
        return seed[card] == 3;
    }

    public boolean isTingQiDui(MjPlayerInfo mjPlayerInfo, Integer card){
        List<Integer> cardList = mjPlayerInfo.getHandCardList();
        if (cardList.size() != 13){
            return false;
        }
        int[] cards = mjCardService.convertToLongSeed(mjPlayerInfo.getHandCardList());
        int duiCnt = 0;
        for (int i=0; i<34; i++){
            if (cards[i] % 2 == 0){
                duiCnt = duiCnt + cards[i] / 2;
            }
        }
        return duiCnt == 5 && cards[card] % 2 == 1;
    }

    public boolean isTingQingYiSe(MjPlayerInfo mjPlayerInfo, Integer card){
        List<Integer> cardList = new ArrayList<>(16);
        cardList.addAll(mjPlayerInfo.getHandCardList());
        cardList.addAll(mjPlayerInfo.getPengCardList());
        cardList.addAll(mjPlayerInfo.getAnGangCardList());
        cardList.addAll(mjPlayerInfo.getMingGangCardList());
        Map<MjValueEnum,List<Integer>> map = mjCardService.split(cardList);
        MjValueEnum mjValueEnum = null;
        int maxSize = 0;
        for (Map.Entry<MjValueEnum, List<Integer>> entry : map.entrySet()){
            int size = entry.getValue().size();
            if (size > maxSize){
                maxSize = size;
                mjValueEnum = entry.getKey();
            }
        }
        int restSize = cardList.size() - maxSize;
        return restSize == 1 && mjValueEnum != null && card > mjValueEnum.min && card <= mjValueEnum.max;
    }

    public boolean isNormalTing(MjPlayerInfo mjPlayerInfo, Integer card){
        List<Integer> cardList = new LinkedList<>();
        cardList.addAll(mjPlayerInfo.getHandCardList());
        cardList.add(card);
        int[] seeds = mjCardService.convertToLongSeed(mjPlayerInfo.getHandCardList());
        for (int i=0; i<34; i++){
            if (seeds[i] > 0){
                cardList.remove(Integer.valueOf(i));
                if (isHuLaizi(cardList,1)){
//                    System.out.println("card:" + mjPlayerInfo.getHandCardList() + " ,card:" + card +  " remove:" + i + " and ting");
                    return true;
                }
                cardList.add(i);
            }
        }
        return false;
    }


    /*
    目前只用于输赢控制，所以只判断清一色、碰碰胡、七对的听牌
     */
    public boolean isTing(MjPlayerInfo mjPlayerInfo, Integer card){
        return isTingQiDui(mjPlayerInfo,card) || isTingQingYiSe(mjPlayerInfo, card) || isNormalTing(mjPlayerInfo, card);
    }


    public boolean isGoodCard(List<Integer> cardList, Integer card){
        Map<MjValueEnum, List<Integer>> map = mjCardService.split(cardList);
        MjValueEnum mjValueEnum =  MjValueEnum.getCardEnum(card);
        if (mjValueEnum != null){
            List<Integer> cardValueList = map.get(mjValueEnum);
            if (cardValueList != null){
                int[] seed = mjCardService.convertToSeed(cardValueList);
                int val = card % 9;

                if (mjValueEnum.isFeng ){
                    if (seed[val] > 0) {
                        return true;
                    } else {
                        int min = 0, max = 4, cnt = 0;
                        if (val > 3){
                            min = 4;
                            max = 7;
                        }
                        for (int i=min; i<max; i++){
                            if (i!=val && seed[i] > 0){
                                cnt++;
                            }
                        }
                        if (cnt >= 2){
                            return true;
                        }
                    }
                }else {
                    if ( seed[val] > 0 || (val > 1 && seed[val-2] > 0 && seed[val-1] > 0 ) ||
                            (val > 0 && val < 8 && seed[val-1] > 0 && seed[val+1] > 0 ) ||
                            (val < 7 && seed[val+1] > 0 && seed[val+2] > 0 ) ){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}


