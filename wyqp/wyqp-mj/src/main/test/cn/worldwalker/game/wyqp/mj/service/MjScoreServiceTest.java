package cn.worldwalker.game.wyqp.mj.service;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.mj.cards.MjCardRule;
import cn.worldwalker.game.wyqp.mj.enums.MjHuTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjOperationEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjScoreEnum;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

import static cn.worldwalker.game.wyqp.mj.enums.MjScoreEnum.*;

public class MjScoreServiceTest {

    private MjScoreService mjScoreService = MjScoreService.getInstance();

    @Test
    public void testCalScore() throws Exception {

        Map<List<List<Integer>>, List<MjScoreEnum>> caseMap = new HashMap<>(16);
        //平胡
        caseMap.put(Arrays.asList(
                Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8,  10, 10), Arrays.asList(9,9,9),
                Collections.<Integer>emptyList(), Collections.<Integer>emptyList()),
                Arrays.asList(PING_HU));
        //碰碰胡
        caseMap.put(Arrays.asList(
                Arrays.asList(0, 0, 0, 3,3,3, 8,8,8, 10, 10), Arrays.asList(9,9,9),
                Collections.<Integer>emptyList(), Collections.<Integer>emptyList()),
                Arrays.asList(PENG_PENG_HU));

        //乱清一色
        caseMap.put(Arrays.asList(
                Arrays.asList(0,0,1,2,2,3,4,5,6,8,8), Arrays.asList(7,7,7),
                Collections.<Integer>emptyList(), Collections.<Integer>emptyList()),
                Arrays.asList(LUAN_QING_YI_SE));

        //门清清一色
        caseMap.put(Arrays.asList(
                Arrays.asList(0,0,1,2,2,3,4,5,6,7,7,8,8,8), Collections.<Integer>emptyList(),
                Collections.<Integer>emptyList(), Collections.<Integer>emptyList()),
                Arrays.asList(MENG_QING_QING_YI_SE));

        //胡清一色
        caseMap.put(Arrays.asList(
                Arrays.asList(0,0,0,1,2,3,4,5,6,8,8), Arrays.asList(7,7,7),
                Collections.<Integer>emptyList(), Collections.<Integer>emptyList()),
                Arrays.asList(HU_QING_YI_SE));

        //字清一色
        caseMap.put(Arrays.asList(
                Arrays.asList(27,27,28,28,28,28,29,29,30,30,31), Arrays.asList(33,33,33),
                Collections.<Integer>emptyList(), Collections.<Integer>emptyList()),
                Arrays.asList(ZI_QING_YI_SE));


        //七对
        caseMap.put(Arrays.asList(
                Arrays.asList(0, 0, 1, 1, 8, 8, 9, 9, 11, 11, 18, 18, 33, 33), Collections.<Integer>emptyList(),
                Collections.<Integer>emptyList(), Collections.<Integer>emptyList()),
                Arrays.asList(QI_DUI ));
        //十三烂
        caseMap.put(Arrays.asList(
                Arrays.asList(0, 3, 6, 9, 12, 15, 18, 21, 24, 29, 30, 31, 32, 33), Collections.<Integer>emptyList(),
                Collections.<Integer>emptyList(), Collections.<Integer>emptyList()),
                Arrays.asList(SHI_SHI_SAN_LAN ));
        //七星
        caseMap.put(Arrays.asList(
                Arrays.asList(0, 3, 6, 9, 12, 15, 18, 27,28,29, 30, 31, 32, 33), Collections.<Integer>emptyList(),
                Collections.<Integer>emptyList(), Collections.<Integer>emptyList()),
                Arrays.asList(QI_XING));
        //单吊
        caseMap.put(Arrays.asList(
                Arrays.asList(0, 0) , Arrays.asList(1,1,1,3,3,3,7,7,7,33,33,33),
                Collections.<Integer>emptyList(), Collections.<Integer>emptyList()),
                Arrays.asList(DAN_DIAO,PENG_PENG_HU));


        for (Map.Entry<List<List<Integer>>, List<MjScoreEnum>> entry : caseMap.entrySet()) {
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setHandCardList(entry.getKey().get(0));
            mjPlayerInfo.setPengCardList(entry.getKey().get(1));
            mjPlayerInfo.setAnGangCardList(entry.getKey().get(2));
            mjPlayerInfo.setMingGangCardList(entry.getKey().get(3));
            List<Integer> typeList = mjScoreService.calHuPlayer(mjPlayerInfo, null);
            List<MjScoreEnum> mjScoreEnumList = new ArrayList<>(16);
            for (Integer type : typeList) {
                mjScoreEnumList.add(MjScoreEnum.getByType(type));
            }

            if (!isListEqual(mjScoreEnumList, entry.getValue())){
                System.out.println( mjScoreEnumList + ", " + entry);
            }
            Assert.assertTrue(isListEqual(mjScoreEnumList, entry.getValue()));
        }
    }

    @Test
    public void testCalScoreAll() throws Exception{
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(1000 + i);
            mjPlayerInfo.setDiscardCardList(Arrays.asList(0,1,2));
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }
        mjRoomInfo.setLastPlayerId(mjRoomInfo.getPlayerList().get(3).getPlayerId());

        //单个放冲
        mjRoomInfo.getPlayerList().get(0).setHuType(MjHuTypeEnum.zhuaChong.type);
        mjRoomInfo.getPlayerList().get(0).setIsHu(1);
        mjRoomInfo.getPlayerList().get(0).setMjCardTypeList(Collections.singletonList(MjScoreEnum.PENG_PENG_HU.type));
        mjScoreService.calScoreRoom(mjRoomInfo);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getHuScore().intValue(),4);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getHuScore().intValue(),-4);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getZhuaChongCount().intValue(),1);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getDianPaoCount().intValue(),1);


        //一炮2响
        mjRoomInfo.getPlayerList().get(1).setHuType(MjHuTypeEnum.zhuaChong.type);
        mjRoomInfo.getPlayerList().get(1).setIsHu(1);
        mjRoomInfo.getPlayerList().get(1).setMjCardTypeList(Collections.singletonList(MjScoreEnum.PENG_PENG_HU.type));
        mjScoreService.calScoreRoom(mjRoomInfo);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getHuScore().intValue(),8);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(1).getHuScore().intValue(),4);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getHuScore().intValue(),-12);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getZhuaChongCount().intValue(),2);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(1).getZhuaChongCount().intValue(),1);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getDianPaoCount().intValue(),2);

        //自摸
        mjRoomInfo.getPlayerList().get(0).setHuType(MjHuTypeEnum.ziMo.type);
        mjRoomInfo.getPlayerList().get(1).setIsHu(0);
        mjScoreService.calScoreRoom(mjRoomInfo);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getHuScore().intValue(),20);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(1).getHuScore().intValue(),0);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(2).getHuScore().intValue(),-4);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getHuScore().intValue(),-16);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getZhuaChongCount().intValue(),2);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getZiMoCount().intValue(),1);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(1).getZhuaChongCount().intValue(),1);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getDianPaoCount().intValue(),2);

    }

    @Test
    public void testQiangGangScore() throws Exception{
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(2000 + i);
            mjPlayerInfo.setDiscardCardList(Arrays.asList(0,1,2));
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }
        MjPlayerInfo dianPaoPlayer = mjRoomInfo.getPlayerList().get(3);
        MjPlayerInfo winPlayer = mjRoomInfo.getPlayerList().get(0);
        MjPlayerInfo zhuangPlayer = mjRoomInfo.getPlayerList().get(1);

        mjRoomInfo.setLastPlayerId(dianPaoPlayer.getPlayerId());
        mjRoomInfo.setRoomBankerId(zhuangPlayer.getPlayerId());
        winPlayer.setIsHu(1);
        winPlayer.setHuType(MjHuTypeEnum.qiangGang.type);
        winPlayer.setMjCardTypeList(Collections.singletonList(MjScoreEnum.PING_HU.type));



        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            mjPlayerInfo.setHuScore(0);
            mjPlayerInfo.setMaScore(0);
        }
        mjRoomInfo.setMaCardList(Arrays.asList(1,2));
        mjScoreService.calScoreRoom(mjRoomInfo);
        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            System.out.println(mjPlayerInfo.getPlayerId() + " ,huScore:" + mjPlayerInfo.getHuScore());
            System.out.println(mjPlayerInfo.getPlayerId() + " ,maScore:" + mjPlayerInfo.getMaScore());
        }
        Assert.assertEquals(winPlayer.getHuScore(),Integer.valueOf(4));
        Assert.assertEquals(dianPaoPlayer.getHuScore(),Integer.valueOf(-4));
        Assert.assertEquals(zhuangPlayer.getMaScore(),Integer.valueOf(-4));
        Assert.assertEquals(winPlayer.getMaScore(),Integer.valueOf(4));


        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            mjPlayerInfo.setHuScore(0);
            mjPlayerInfo.setMaScore(0);
        }
        mjRoomInfo.setMaCardList(Arrays.asList(1,3));
        mjScoreService.calScoreRoom(mjRoomInfo);
        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            System.out.println(mjPlayerInfo.getPlayerId() + " ,huScore:" + mjPlayerInfo.getHuScore());
            System.out.println(mjPlayerInfo.getPlayerId() + " ,maScore:" + mjPlayerInfo.getMaScore());
        }
        Assert.assertEquals(winPlayer.getHuScore(),Integer.valueOf(4));
        Assert.assertEquals(dianPaoPlayer.getHuScore(),Integer.valueOf(-4));
        Assert.assertEquals(zhuangPlayer.getMaScore(),Integer.valueOf(4));
        Assert.assertEquals(dianPaoPlayer.getMaScore(),Integer.valueOf(-4));



        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            mjPlayerInfo.setHuScore(0);
            mjPlayerInfo.setMaScore(0);
        }
        mjRoomInfo.setMaCardList(Arrays.asList(0,2));
        mjScoreService.calScoreRoom(mjRoomInfo);
        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            System.out.println(mjPlayerInfo.getPlayerId() + " ,huScore:" + mjPlayerInfo.getHuScore());
            System.out.println(mjPlayerInfo.getPlayerId() + " ,maScore:" + mjPlayerInfo.getMaScore());
        }
        Assert.assertEquals(winPlayer.getHuScore(),Integer.valueOf(4));
        Assert.assertEquals(dianPaoPlayer.getHuScore(),Integer.valueOf(-4));
        Assert.assertEquals(zhuangPlayer.getMaScore(),Integer.valueOf(-4));
        Assert.assertEquals(winPlayer.getMaScore(),Integer.valueOf(4));

    }


    @Test
    public void testGangKaiScore() throws Exception{
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(3000 + i);
            mjPlayerInfo.setDiscardCardList(Arrays.asList(0,1,2));
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }
        MjPlayerInfo dianPaoPlayer = mjRoomInfo.getPlayerList().get(3);
        MjPlayerInfo winPlayer = mjRoomInfo.getPlayerList().get(0);
        MjPlayerInfo zhuangPlayer = mjRoomInfo.getPlayerList().get(1);

//        mjRoomInfo.setLastPlayerId(dianPaoPlayer.getPlayerId());
//        mjRoomInfo.setRoomBankerId(zhuangPlayer.getPlayerId());
        winPlayer.setIsHu(1);
        winPlayer.setHuType(MjHuTypeEnum.gangKai.type);
        winPlayer.setMjCardTypeList(Collections.singletonList(MjScoreEnum.PENG_PENG_HU.type));


//        mjRoomInfo.setMaCardList(Arrays.asList(0,2));
        mjScoreService.calScoreRoom(mjRoomInfo);
        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            System.out.println(mjPlayerInfo.getPlayerId() + " ,huScore:" + mjPlayerInfo.getHuScore());
            System.out.println(mjPlayerInfo.getPlayerId() + " ,maScore:" + mjPlayerInfo.getMaScore());
        }
        Assert.assertEquals(winPlayer.getHuScore(),Integer.valueOf(24));
        Assert.assertEquals(dianPaoPlayer.getHuScore(),Integer.valueOf(-8));
        Assert.assertEquals(zhuangPlayer.getHuScore(),Integer.valueOf(-8));

    }


    private boolean isListEqual(List list1, List list2) {
        return (list1 == null && list2 == null)
                || (list1 != null && list1.size() == list2.size() && list1.containsAll(list2));
    }

    @Test
    public void testCalGangScore() throws Exception {
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(1000 + i);
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }

        mjRoomInfo.setLastPlayerId(mjRoomInfo.getPlayerList().get(3).getPlayerId());

        MjPlayerInfo gangPlayer = mjRoomInfo.getPlayerList().get(0);
        gangPlayer.setHandCardList(Arrays.asList(1,2,3,4));
        mjRoomInfo.setRoomBankerId(1001);
        MjPlayerInfo bankerPlayer = MjCardRule.getBankerPlayer(mjRoomInfo);
        mjRoomInfo.setMaCardList(Arrays.asList(4-1));

        mjScoreService.calGangScore(mjRoomInfo, gangPlayer,  MjOperationEnum.anGang,0);

        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            System.out.println("id:" + mjPlayerInfo.getPlayerId() +
                    " ,GangTypeList:"+ mjPlayerInfo.getGangTypeList() +
                    ", GangScore:" + mjPlayerInfo.getGangScore() +
                    ", MaScore:" + mjPlayerInfo.getMaScore());
        }
        Assert.assertEquals(gangPlayer.getGangScore().intValue(),6);
        Assert.assertEquals(bankerPlayer.getGangScore().intValue(),2);

    }

    @Test
    public void tetCalGangScoreRoom() throws Exception{
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(1000 + i);
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }

        MjPlayerInfo gangPlayer = mjRoomInfo.getPlayerList().get(0);
        gangPlayer.setMingGangCardList(Arrays.asList(1,1,1,1,2,2,2,2));
        gangPlayer.setAnGangCardList(Arrays.asList(0,0,0,0));
        gangPlayer.setHandCardList(Arrays.asList(1,2,3,4));
        MjPlayerInfo fangGangPlayer = mjRoomInfo.getPlayerList().get(1);
        mjRoomInfo.getOpMap().put(1, String.valueOf(fangGangPlayer.getPlayerId()));
        mjRoomInfo.getOpMap().put(2, String.valueOf(gangPlayer.getPlayerId()));

        MjPlayerInfo otherPlayer1 = mjRoomInfo.getPlayerList().get(2);
        MjPlayerInfo otherPlayer2 = mjRoomInfo.getPlayerList().get(3);


        mjScoreService.calGangScoreRoom(mjRoomInfo);

        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            System.out.println(mjPlayerInfo.getGangScore());
        }

        Assert.assertEquals(gangPlayer.getGangScore(),Integer.valueOf(13));
        Assert.assertEquals(fangGangPlayer.getGangScore(),Integer.valueOf(-5));
        Assert.assertEquals(otherPlayer1.getGangScore(),Integer.valueOf(-4));
        Assert.assertEquals(otherPlayer2.getGangScore(),Integer.valueOf(-4));
    }


    @Test
    public void testGangScoreRoom2() throws Exception {
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(1000 + i);
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }

        MjPlayerInfo gangPlayer1 = mjRoomInfo.getPlayerList().get(0);
        gangPlayer1.setMingGangCardList(Arrays.asList(1,1,1,1));
        MjPlayerInfo gangPlayer2 = mjRoomInfo.getPlayerList().get(3);
        gangPlayer2.setMingGangCardList(Arrays.asList(2,2,2,2));
//        MjPlayerInfo fangGangPlayer = mjRoomInfo.getPlayerList().get(1);
        mjRoomInfo.setRoomBankerId(gangPlayer1.getPlayerId());
        mjRoomInfo.getOpMap().put(1, String.valueOf(gangPlayer1.getPlayerId()));
        mjRoomInfo.getOpMap().put(2, String.valueOf(gangPlayer2.getPlayerId()));

//        MjPlayerInfo otherPlayer1 = mjRoomInfo.getPlayerList().get(2);
//        MjPlayerInfo otherPlayer2 = mjRoomInfo.getPlayerList().get(3);

        mjRoomInfo.setMaCardList(Arrays.asList(1,4,17,28));

        mjScoreService.calGangScoreRoom(mjRoomInfo);

        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            System.out.println(mjPlayerInfo.getGangScore());
        }

//        Assert.assertEquals(gangPlayer.getGangScore(),Integer.valueOf(13));
//        Assert.assertEquals(fangGangPlayer.getGangScore(),Integer.valueOf(-5));
//        Assert.assertEquals(otherPlayer1.getGangScore(),Integer.valueOf(-4));
//        Assert.assertEquals(otherPlayer2.getGangScore(),Integer.valueOf(-4));
    }

    @Test
    public void testControlMa() throws Exception{
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(1000 + i);
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }
        MjPlayerInfo bankPlayer = mjRoomInfo.getPlayerList().get(0);
        mjRoomInfo.setRoomBankerId(bankPlayer.getPlayerId());
        mjRoomInfo.getControlPlayer().add(bankPlayer.getPlayerId());

        MjPlayerInfo huPlayer = mjRoomInfo.getPlayerList().get(0);
        huPlayer.setIsHu(1);
        int val = mjScoreService.getWinPos(mjRoomInfo);
        Assert.assertEquals(val,1);
        huPlayer.setIsHu(0);

        huPlayer = mjRoomInfo.getPlayerList().get(1);
        huPlayer.setIsHu(1);
        val = mjScoreService.getWinPos(mjRoomInfo);
        Assert.assertEquals(val,2);
        huPlayer.setIsHu(0);

        huPlayer = mjRoomInfo.getPlayerList().get(2);
        huPlayer.setIsHu(1);
        val = mjScoreService.getWinPos(mjRoomInfo);
        Assert.assertEquals(val,3);
        huPlayer.setIsHu(0);

        huPlayer = mjRoomInfo.getPlayerList().get(3);
        huPlayer.setIsHu(1);
        val = mjScoreService.getWinPos(mjRoomInfo);
        Assert.assertEquals(val,4);
    }


    @Test
    public void testGenerateMaCard() throws Exception{
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(1000 + i);
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }
        mjRoomInfo.setMaiMaCount(2);
        MjPlayerInfo bankPlayer = mjRoomInfo.getPlayerList().get(0);
        mjRoomInfo.setRoomBankerId(bankPlayer.getPlayerId());
        mjRoomInfo.getControlPlayer().add(bankPlayer.getPlayerId());
        MjPlayerInfo huPlayer = mjRoomInfo.getPlayerList().get(0);
        huPlayer.setIsHu(1);


        mjRoomInfo.setTableRemainderCardList(Arrays.asList(8,10,10,28,29,29,29));
        mjScoreService.generateMaCard(mjRoomInfo);
        System.out.println(mjRoomInfo.getMaCardList());
        Assert.assertTrue(mjRoomInfo.getMaCardList().contains(8));
        Assert.assertEquals(mjRoomInfo.getMaCardList().size(), 2);

        mjRoomInfo.setTableRemainderCardList(Arrays.asList(10,28,28,31));
        mjScoreService.generateMaCard(mjRoomInfo);
        System.out.println(mjRoomInfo.getMaCardList());
        Assert.assertTrue(mjRoomInfo.getMaCardList().contains(31));
        Assert.assertEquals(mjRoomInfo.getMaCardList().size(), 2);

    }

    @Test
    public void testGetMaPlayerList() throws Exception{
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(1000 + i);
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }

        mjRoomInfo.setRoomBankerId(1000);
        int val = 0;
        for (int i = 0; i < 9; i++) {
            val = val + 1;
            mjRoomInfo.setMaCardList(Arrays.asList(i));
            List<MjPlayerInfo> maPlayerList = mjScoreService.getMaPlayerList(mjRoomInfo);
            for (MjPlayerInfo player : maPlayerList) {
                if (!player.getPlayerId().equals(1000 + (val - 1) % 4)){
                    System.out.println("i:" + i + " ,val:" + val);
                }
                Assert.assertEquals(player.getPlayerId(), Integer.valueOf(1000 + (val-1)%4 ));
            }
        }
        val = 0;
        for (int i=9; i<18; i++){
            val = val + 1;
            mjRoomInfo.setMaCardList(Arrays.asList(i));
            List<MjPlayerInfo> maPlayerList = mjScoreService.getMaPlayerList(mjRoomInfo);
            for (MjPlayerInfo player : maPlayerList){
                if (!player.getPlayerId().equals(1000 + (val - 1) % 4)){
                    System.out.println("i:" + i + " ,val:" + val);
                }
                Assert.assertEquals(player.getPlayerId(), Integer.valueOf(1000 + (val-1)%4 ));
            }
        }
        val = 0;
        for (int i=18; i<27; i++){
            val = val + 1;
            mjRoomInfo.setMaCardList(Arrays.asList(i));
            List<MjPlayerInfo> maPlayerList = mjScoreService.getMaPlayerList(mjRoomInfo);
            for (MjPlayerInfo player : maPlayerList){
                if (!player.getPlayerId().equals(1000 + (val - 1) % 4)){
                    System.out.println("i:" + i + " ,val:" + val);
                }
                Assert.assertEquals(player.getPlayerId(), Integer.valueOf(1000 + (val-1)%4 ));
            }
        }
        val = 0;
        for (int i=27; i<34; i++){
            val = val + 1;
            mjRoomInfo.setMaCardList(Arrays.asList(i));
            List<MjPlayerInfo> maPlayerList = mjScoreService.getMaPlayerList(mjRoomInfo);
            for (MjPlayerInfo player : maPlayerList){
                if (!player.getPlayerId().equals(1000 + (val - 1) % 4)){
                    System.out.println("i:" + i + " ,val:" + val);
                }
                Assert.assertEquals(player.getPlayerId(), Integer.valueOf(1000 + (val-1)%4 ));
            }
        }
        /*
        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            mjRoomInfo.setRoomBankerId(mjPlayerInfo.getPlayerId());
            List<Integer> allCard = new ArrayList<>(64);
            for (int i=0; i<34; i++){
                allCard.add(i);
            }
            mjRoomInfo.setMaCardList(allCard);


            List<MjPlayerInfo> maPlayerList = mjScoreService.getMaPlayerList(mjRoomInfo);
            for (MjPlayerInfo player : maPlayerList){
                System.out.println(player.getPlayerId());
            }
        }
     */

    }

    @Test
    public void testCalHuPlayer() throws Exception {
        MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
        mjPlayerInfo.setHandCardList(Arrays.asList(9,10,11,14,15,16,16));
        mjPlayerInfo.setChiCardList(Collections.<Integer>emptyList());
        mjPlayerInfo.setPengCardList(Arrays.asList(13,13,13,17,17,17));
        mjPlayerInfo.setAnGangCardList(Collections.<Integer>emptyList());
        mjPlayerInfo.setMingGangCardList(Collections.<Integer>emptyList());
        List<Integer> allCardList = new ArrayList<>(16);
        allCardList.addAll(mjPlayerInfo.getHandCardList());
        allCardList.addAll(mjPlayerInfo.getPengCardList());
        allCardList.addAll(mjPlayerInfo.getMingGangCardList());
        allCardList.addAll(mjPlayerInfo.getAnGangCardList());
        allCardList.add(16);
        boolean isHu = MjHuService.getInstance().isHu(allCardList);
        System.out.println(isHu);
        List<Integer> typeList = mjScoreService.calHuPlayer(mjPlayerInfo,16);
        System.out.println(typeList);
    }

    @Test
    public void testCalGangScore1() throws Exception {

        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(1000 + i);
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }
        mjRoomInfo.setRoomBankerId(1001);
        mjRoomInfo.setMaCardList(Arrays.asList(3,3,3));
    }

    @Test
    public void testTemp() throws Exception{
//        System.out.println(new HashSet<>(Arrays.asList(1,1,1,2,2,3,3,3,3)));
        for (int i=0; i<10 && false; i++){
            System.out.println(i);
        }

    }

}