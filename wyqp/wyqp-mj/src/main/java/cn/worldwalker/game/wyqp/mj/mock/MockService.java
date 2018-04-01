package cn.worldwalker.game.wyqp.mj.mock;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.mj.cards.MjCardResource;
import cn.worldwalker.game.wyqp.mj.cards.MjCardRule;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MockService {
    private static MockService ourInstance = new MockService();

    public static MockService getInstance() {
        return ourInstance;
    }

    private MockService() {
    }

    public MjRoomInfo convertToRoom(MockRoom mockRoom){
        MjRoomInfo mjRoomInfo = new MjRoomInfo();
        mjRoomInfo.setDetailType(5);
        mjRoomInfo.setCurGame(mockRoom.getCurGame());
        mjRoomInfo.setRoomBankerId(mockRoom.getRoomBankerId());
        mjRoomInfo.setCurPlayerId(mockRoom.getCurPlayerId());
        mjRoomInfo.setMaiMaCount(mockRoom.getMaiMaCount());
        mjRoomInfo.setMaCardList(mockRoom.getMaCardList());
        mjRoomInfo.setStatus(2);
        mjRoomInfo.setGameType(2);
        mjRoomInfo.setIsChiPai(0);
        mjRoomInfo.setUpdateTime(new Date());
        mjRoomInfo.setCurGame(1);
        mjRoomInfo.setTotalGames(8);
        List<MockPlayer> mockPlayerList = mockRoom.getMockPlayerList();
        List<MjPlayerInfo> mjPlayerInfoList = new ArrayList<>(4);
        for (int i=0; i<4; i++){
            MjPlayerInfo mjPlayerInfo = new MjPlayerInfo();
            MockPlayer mockPlayer = mockPlayerList.get(i);
            mjPlayerInfo.setPlayerId(mockPlayer.getPlayerId());
//            if (mockPlayer.getPlayerId().equals(mockRoom.getCurPlayerId())){
                mjPlayerInfo.setCurMoPaiCardIndex(mockPlayer.getCurMoCard());
//            }
            mjPlayerInfo.setHandCardList(mockPlayer.getHandCardList());
            mjPlayerInfo.setPengCardList(mockPlayer.getPengCardList());
            mjPlayerInfo.setMingGangCardList(mockPlayer.getMinGangCardList());
            mjPlayerInfo.setAnGangCardList(mockPlayer.getAnGangCardList());
            mjPlayerInfoList.add(mjPlayerInfo);
        }
        mjRoomInfo.setPlayerList(mjPlayerInfoList);
        List<Integer> allCardList = MjCardResource.genTableOutOrderCardList();
        for (MjPlayerInfo mjPlayerInfo : mjPlayerInfoList){
            MjCardRule.removeCard(allCardList,mjPlayerInfo.getHandCardList());
            MjCardRule.removeCard(allCardList,mjPlayerInfo.getPengCardList());
            MjCardRule.removeCard(allCardList,mjPlayerInfo.getAnGangCardList());
            MjCardRule.removeCard(allCardList,mjPlayerInfo.getMingGangCardList());
        }
        MjCardRule.removeCard(allCardList, mockRoom.getDiscardList());
        mjRoomInfo.setTableRemainderCardList(allCardList);

        return mjRoomInfo;

    }


    public MjRoomInfo refreshRoom(MjRoomInfo mjRoomInfoOld, MjRoomInfo mjRoomInfoNew ) {

//        MjRoomInfo mjRoomInfoOld = JSON.parseObject(oldData, MjRoomInfo.class);
//        MjRoomInfo mjRoomInfoNew = JSON.parseObject(newData, MjRoomInfo.class);

        //一些唯一性的字段，还是旧的
        mjRoomInfoNew.setRoomId(mjRoomInfoOld.getRoomId());
        mjRoomInfoNew.setCurGameUuid(mjRoomInfoOld.getCurGameUuid());
        mjRoomInfoNew.setRoomUuid(mjRoomInfoOld.getRoomUuid());

        List<MjPlayerInfo> oldPlayerList = mjRoomInfoOld.getPlayerList();
        List<MjPlayerInfo> newPlayerList = mjRoomInfoNew.getPlayerList();
        for (int i = 0; i < 4; i++) {
            MjPlayerInfo newPlayer = newPlayerList.get(i);
            MjPlayerInfo oldPlayer = oldPlayerList.get(i);
            newPlayer.setPlayerId(oldPlayer.getPlayerId());
            newPlayer.setNickName(oldPlayer.getNickName());
            //房主
            if (oldPlayer.getPlayerId().equals(mjRoomInfoOld.getRoomOwnerId())) {
                mjRoomInfoNew.setRoomOwnerId(newPlayer.getPlayerId());
            }
            //庄家
            if (oldPlayer.getPlayerId().equals(mjRoomInfoOld.getRoomBankerId())) {
                mjRoomInfoNew.setRoomBankerId(newPlayer.getPlayerId());
            }
            //当前玩家
            if (oldPlayer.getPlayerId().equals(mjRoomInfoOld.getCurPlayerId())) {
                mjRoomInfoNew.setCurPlayerId(newPlayer.getPlayerId());
            }
            //最近操作玩家
            if (oldPlayer.getPlayerId().equals(mjRoomInfoOld.getLastPlayerId())) {
                mjRoomInfoNew.setLastPlayerId(newPlayer.getPlayerId());
            }
        }

        return mjRoomInfoNew;
    }



    public boolean setNextCard(List<Integer> cardList, Integer newCard){
        Iterator<Integer> it = cardList.iterator();
        while(it.hasNext()){
            Integer value = it.next();
            if (value.equals(newCard)){
                it.remove();
                cardList.add(0, newCard);
                return true;
            }
        }
        return false;
    }

}
