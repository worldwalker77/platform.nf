package cn.worldwalker.game.wyqp.mj.cards;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import java.util.Arrays;

public class MjCardRuleTest {

    @BeforeSuite
    public void loadTable(){
//        TableMgr.getInstance().load();
    }

    @Test
    public void testCheckPeng() throws Exception {
    }

    @Test
    public void testCheckGang() throws Exception {
    }

    @Test
    public void testCheckMingGangByMoPai() throws Exception {
    }

    @Test
    public void testCheckHandCardGang() throws Exception {
    }

    @Test
    public void testCheckHu() throws Exception {
        MjRoomInfo mjRoomInfo =  new MjRoomInfo();
        mjRoomInfo.setIndexLine(34);
        MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
        mjPlayerInfo.setIsTingHu(0);
        mjPlayerInfo.setHandCardList(Arrays.asList(1,2,3,4,5,6,7,8,10,11,12,33,33));
//        mjPlayerInfo.setHandCardList(Arrays.asList(0,0,0,1,1,1,2,2,2,3,3,6,4,5));
//        mjPlayerInfo.setHandCardList(Arrays.asList(0,0,0,1,1,1,2,2,2,3,3));

        //十三烂
        //七星十三烂
        //风,一手牌

        mjRoomInfo.setDetailType(5);
        boolean isHu = MjCardRule.checkHu(mjRoomInfo, mjPlayerInfo,0,0);
        Assert.assertTrue(isHu);
        isHu = MjCardRule.checkHu(mjRoomInfo, mjPlayerInfo,0,2);
        Assert.assertTrue(isHu);
        mjRoomInfo.setDetailType(6);
        isHu = MjCardRule.checkHu(mjRoomInfo, mjPlayerInfo,0,0);
        Assert.assertTrue(isHu);
        isHu = MjCardRule.checkHu(mjRoomInfo, mjPlayerInfo,0,2);
        Assert.assertTrue(!isHu);
    }


    @Test
    public void testCalculateAllPlayerOperations() throws Exception{
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        mjRoomInfo.setLastPlayerId(1);
        MjCardRule.calculateAllPlayerOperations(mjRoomInfo, 1, 1001,1);
    }
}