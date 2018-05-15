package cn.worldwalker.game.wyqp.mj.service;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.mj.seed.SeedService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MjHuServiceTest {
    private MjHuService mjHuService = MjHuService.getInstance();

    @Test
    public void testIsHu() throws Exception {
//        TableMgr.getInstance().load();
        Map<List<Integer>,Boolean> caseMap = new HashMap<>(16);
        caseMap.put(Arrays.asList(0,1,2,3,4,5,6,7,8,33,33), true);
        //常规牌型
        caseMap.put(Arrays.asList(0,1,2,3,4,5,6,7,8,10,11,12,33,33), true);
        caseMap.put(Arrays.asList(0,1,2,3,4,5,6,7,8,10,11,12,13,13), true);
        caseMap.put(Arrays.asList(1,1,1,3,4,5,6,7,8,10,11,12,33,33), true);
        caseMap.put(Arrays.asList(1,1,1,3,4,5,6,7,8,10,11,12,13,13), true);
        //万与筒交接处
        caseMap.put(Arrays.asList(8,9,10,33,33), false);
        //筒与条交接处
        caseMap.put(Arrays.asList(16,17,18,33,33), false);
        //条与风交接处
        caseMap.put(Arrays.asList(25,26,27,33,33), false);
        //东南西北任意三个
        caseMap.put(Arrays.asList(1,1,27,28,29), true);
        caseMap.put(Arrays.asList(0,0,28,29,30), true);
        caseMap.put(Arrays.asList(2,2,27,29,30), true);
        caseMap.put(Arrays.asList(3,3,27,28,30), true);
        caseMap.put(Arrays.asList(3,3,29,30,31), false);
        //中发白
        caseMap.put(Arrays.asList(3,3,31,32,33), true);
        //风 碰子
        caseMap.put(Arrays.asList(0,0,27,27,27), true);
        //十三烂
        caseMap.put(Arrays.asList(0,3,6,9,12,15,18,21,24,29,30,31,32,33), true);
        caseMap.put(Arrays.asList(0,2,6,9,12,15,18,21,24,29,30,31,32,33), false);
        caseMap.put(Arrays.asList(0,3,8,9,12,15,18,21,24,29,30,31,32,33), true);
        caseMap.put(Arrays.asList(0,3,8,9,12,15,18,21,24,29,30,30,32,33), false);
        //七对
        caseMap.put(Arrays.asList(0,0,1,1,8,8,9,9,11,11,18,18,33,33), true);
        caseMap.put(Arrays.asList(0,1,1,1,8,8,9,9,11,11,18,18,33,33), false);
        caseMap.put(Arrays.asList(0,0,1,3,8,8,9,9,11,11,18,18,33,33), false);
        caseMap.put(Arrays.asList(0,0,1,3,8,8,9,9,11,11,18,18,33), false);
        caseMap.put(Arrays.asList(0,4,8,12,17,22,27,28,29,30,31,32,33,19), true);
        //清一色
//        caseMap.put(Arrays.asList(0,0,1,1,2,3,4,5,7,7,8,8), true);




        for (Map.Entry<List<Integer>,Boolean> entry : caseMap.entrySet()){
            //无赖子
            boolean isHuNew = mjHuService.isHu(entry.getKey());

            /*有赖子的判胡测试
            boolean isNormalHu = mjHuService.isNormalHu(entry.getKey());
            for (int i=0; i<4; i++){
                List<Integer> cardList = entry.getKey();
                if (cardList.size()-i-1 > 0){
                    boolean isHuLaiZi = mjHuService.isHuLaizi(cardList.subList(0,cardList.size()-i-1),i+1);
                    if (isHuLaiZi != isNormalHu &&  isNormalHu){
                        System.out.println("laizi 失败 + [" + i + "]" + entry);
                        isHuLaiZi = mjHuService.isHuLaizi(cardList.subList(0,cardList.size()-i-1),i+1);
                    }
                }
            }
            */
            if (!entry.getValue().equals(isHuNew)){
                mjHuService.isHu(entry.getKey());
                System.out.println(entry.getKey());
            }
            Assert.assertTrue(entry.getValue().equals(isHuNew));
        }


        //清一色单调判断，必须14张
        Map<List<Integer>,Boolean> caseMapQingYiSe = new HashMap<>(16);
        caseMapQingYiSe.put(Arrays.asList(0,0,0,0,1,2,3,4,5,7,7,7,8,8), true);
        caseMapQingYiSe.put(Arrays.asList(0,0,0,0,1,2,3,4,5,7,7,7,8), false);
        caseMapQingYiSe.put(Arrays.asList(0,0,0,0,1,2,3,4,5,7,7,7,8,9), false);
        caseMapQingYiSe.put(Arrays.asList(10,10,11,11,11,12,13,13,14,15,16,16,17,17), true);

        for (Map.Entry<List<Integer>, Boolean> entry : caseMapQingYiSe.entrySet()){
           Boolean isQingYiSe = mjHuService.isQingYiSe(entry.getKey());
            if (!entry.getValue().equals(isQingYiSe)){
                mjHuService.isHu(entry.getKey());
                System.out.println(entry.getKey());
            }
            Assert.assertTrue(entry.getValue().equals(isQingYiSe));

        }
    }


    @Test
    public void  testHu() throws Exception{
        SeedService.getInstanceFeng();
        SeedService.getInstance();
//        boolean isHu = mjHuService.isHu(Arrays.asList(7,7,12,12,24,24,32,32));
//        boolean isHu = mjHuService.isHu(Arrays.asList(10,10,10,8,8,8,5,6,7,23,23,23,24,26));

//        boolean isHu = mjHuService.isShiSanLan(Arrays.asList(0,3,6,9,12,15,18,21,24,29,30,31,27,33));
//        System.out.println(isHu);
//        isHu = mjHuService.isHu(Arrays.asList(5,6,7,14,16,22,23,24,26,27,29,31,32,33));
        boolean isHu = mjHuService.isHu(Arrays.asList(1,3,4,5,6,7,8,9,9,9,33,33,33,1));
        System.out.println(isHu);
//        isHu = mjHuService.isHuLaizi(Arrays.asList(7,7,12,12,24,24,32,32),0);
//        System.out.println(isHu);
    }

    @Test
    public void testTing() throws Exception{

        MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
        mjPlayerInfo.setHandCardList(Arrays.asList(0,0,1,1,2,2,3,3,4,4,8,9,10));
        System.out.println(mjHuService.isTingQiDui(mjPlayerInfo,11));
        mjPlayerInfo.setHandCardList(Arrays.asList(0,0,1,1,2,2,3,3,4,4,5,5,10));
        System.out.println(mjHuService.isTingQingYiSe(mjPlayerInfo,5));
        SeedService.getInstance();
        SeedService.getInstanceFeng();
        mjPlayerInfo.setHandCardList(Arrays.asList(0,0,0,1,1,1,2,2,2,6,7,9,10));
        boolean isNormalTing = mjHuService.isNormalTing(mjPlayerInfo,15);
        System.out.println(isNormalTing);
    }


    @Test
    public void testIsGoodCard() throws Exception{
        boolean isGood = mjHuService.isGoodCard(Arrays.asList(2,3,22,23,24,27,28),0);
        Assert.assertEquals(isGood,false);
        isGood = mjHuService.isGoodCard(Arrays.asList(1,2),0);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(2,2),0);
        Assert.assertEquals(isGood,false);
        isGood = mjHuService.isGoodCard(Arrays.asList(0,2,3),0);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(0,0,3),0);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(0,0,0,3),0);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(0,0,0,3),0);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(0,3),1);
        Assert.assertEquals(isGood,false);
        isGood = mjHuService.isGoodCard(Arrays.asList(0,2),1);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(2,3),1);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(1,3),2);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(3,4),2);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(7,9),8);
        Assert.assertEquals(isGood,false);
        isGood = mjHuService.isGoodCard(Arrays.asList(9,10),8);
        Assert.assertEquals(isGood,false);
        isGood = mjHuService.isGoodCard(Arrays.asList(27,28),30);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(27,29),30);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(29,30),30);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(29,31),30);
        Assert.assertEquals(isGood,false);
        isGood = mjHuService.isGoodCard(Arrays.asList(32,33),31);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(0,3,8,9,12,15,18,27,28,29,30,30,33),24);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(3,8,9,12,15,18,24,27,28,29,30,30,33),0);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(3,8,9,12,15,18,24,27,28,29,30,30,32),33);
        Assert.assertEquals(isGood,true);
        isGood = mjHuService.isGoodCard(Arrays.asList(3,8,9,12,15,18,24,27,28,29,30,30,32),30);
        Assert.assertEquals(isGood,false);
        isGood = mjHuService.isGoodCard(Arrays.asList(0,3,9,12,15,18,24,27,28,29,30,30,31),8);
        Assert.assertEquals(isGood,true);

    }

}