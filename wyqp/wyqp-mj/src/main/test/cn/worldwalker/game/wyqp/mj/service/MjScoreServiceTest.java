package cn.worldwalker.game.wyqp.mj.service;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
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
                Arrays.asList(0,0,1,2,2,3,4,5,6,7,7,8,8), Collections.<Integer>emptyList(),
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
            mjScoreService.calHuPlayer(mjPlayerInfo);
            List<MjScoreEnum> mjScoreEnumList = new ArrayList<>(16);
            for (Integer type : mjPlayerInfo.getMjCardTypeList()) {
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
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }
        mjRoomInfo.setLastPlayerId(mjRoomInfo.getPlayerList().get(3).getPlayerId());

        //单个放冲
        mjRoomInfo.getPlayerList().get(0).setHuType(MjHuTypeEnum.zhuaChong.type);
        mjRoomInfo.getPlayerList().get(0).setIsHu(1);
        mjRoomInfo.getPlayerList().get(0).setMjCardTypeList(Collections.singletonList(MjScoreEnum.PENG_PENG_HU.type));
        mjScoreService.calScoreRoom(mjRoomInfo);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getCurScore().intValue(),4);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getCurScore().intValue(),-4);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getZhuaChongCount().intValue(),1);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getDianPaoCount().intValue(),1);


        //一炮2响
        mjRoomInfo.getPlayerList().get(1).setHuType(MjHuTypeEnum.qiangGang.type);
        mjRoomInfo.getPlayerList().get(1).setIsHu(1);
        mjRoomInfo.getPlayerList().get(1).setMjCardTypeList(Collections.singletonList(MjScoreEnum.PENG_PENG_HU.type));
        mjScoreService.calScoreRoom(mjRoomInfo);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getCurScore().intValue(),8);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(1).getCurScore().intValue(),4);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getCurScore().intValue(),-12);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getZhuaChongCount().intValue(),2);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(1).getZhuaChongCount().intValue(),1);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getDianPaoCount().intValue(),2);


        //自摸
        mjRoomInfo.getPlayerList().get(0).setHuType(MjHuTypeEnum.gangKai.type);
        mjRoomInfo.getPlayerList().get(1).setIsHu(0);
        mjScoreService.calScoreRoom(mjRoomInfo);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getCurScore().intValue(),20);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(1).getCurScore().intValue(),0);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(2).getCurScore().intValue(),-4);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getCurScore().intValue(),-16);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getZhuaChongCount().intValue(),2);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(0).getZiMoCount().intValue(),1);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(1).getZhuaChongCount().intValue(),1);
        Assert.assertEquals(mjRoomInfo.getPlayerList().get(3).getDianPaoCount().intValue(),2);


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
        mjRoomInfo.setMaCardList(Arrays.asList(1,1,1,1));

        mjScoreService.calGangScore(mjRoomInfo, gangPlayer,  MjOperationEnum.anGang);

        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            System.out.println("id:" + mjPlayerInfo.getPlayerId() +
                    " ,GangTypeList:"+ mjPlayerInfo.getGangTypeList() +
                    ", GangScore:" + mjPlayerInfo.getGangScore() +
                    ", MaScore:" + mjPlayerInfo.getMaScore());
        }

    }


    @Test
    public void testGetMaPlayerList() throws Exception{
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            mjPlayerInfo.setPlayerId(1000 + i);
            mjRoomInfo.getPlayerList().add(mjPlayerInfo);
        }
        mjRoomInfo.setRoomBankerId(1001);
        mjRoomInfo.setMaCardList(Arrays.asList(3,3,3));


        List<MjPlayerInfo> maPlayerList = mjScoreService.getMaPlayerList(mjRoomInfo);
        for (MjPlayerInfo player : maPlayerList){
            System.out.println(player.getPlayerId());
        }
    }

    @Test
    public void testCalHuPlayer() throws Exception {
        MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
        mjPlayerInfo.setHandCardList(Arrays.asList(23,23,23,17,17,17,18,18,18,19,19));
        mjPlayerInfo.setChiCardList(Collections.<Integer>emptyList());
        mjPlayerInfo.setPengCardList(Arrays.asList(13,13,13));
        mjPlayerInfo.setAnGangCardList(Collections.<Integer>emptyList());
        mjPlayerInfo.setMingGangCardList(Collections.<Integer>emptyList());
        mjScoreService.calHuPlayer(mjPlayerInfo);
        System.out.println(mjPlayerInfo.getMjCardTypeList());
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
}