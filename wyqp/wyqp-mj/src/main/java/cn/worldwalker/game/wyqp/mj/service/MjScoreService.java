package cn.worldwalker.game.wyqp.mj.service;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.mj.cards.MjCardRule;
import cn.worldwalker.game.wyqp.mj.enums.MjHuTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjScoreEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjValueEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MjScoreService {
    private static MjScoreService ourInstance = new MjScoreService();

    private MjCardService mjCardService = MjCardService.getInstance();
    private MjHuService mjHuService = MjHuService.getInstance();

    public static MjScoreService getInstance() {
        return ourInstance;
    }

    private MjScoreService() {
    }


    private boolean isMengQing(MjPlayerInfo mjPlayerInfo){
        return mjPlayerInfo.getPengCardList().size() == 0 &&
                mjPlayerInfo.getChiCardList().size() == 0 &&
                mjPlayerInfo.getMingGangCardList().size() == 0 &&
                mjPlayerInfo.getAnGangCardList().size() == 0;
    }

    private void checkPengPeng(MjPlayerInfo mjPlayerInfo){
        List<Integer> cardList = new ArrayList<>(16);
        cardList.addAll(mjPlayerInfo.getHandCardList());
        cardList.addAll(mjPlayerInfo.getChiCardList());
        cardList.addAll(mjPlayerInfo.getPengCardList());
        cardList.addAll(mjPlayerInfo.getMingGangCardList());
        cardList.addAll(mjPlayerInfo.getAnGangCardList());

        int[] cards = mjCardService.convertToLongSeed(cardList);
        int duiCnt = 0, pengCnt = 0;
        for (int v : cards){
            if (v == 2){
                duiCnt ++;
            }
            if (v>2){
                pengCnt++;
            }
        }
        if (duiCnt ==1 && pengCnt == 4){
            if (isMengQing(mjPlayerInfo)){
                mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.MENG_QING_PENG_PENG_HU.type);
            } else {
                mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.PENG_PENG_HU.type);
            }
        }
    }

    private void checkQingYiSe(MjPlayerInfo mjPlayerInfo){
        List<Integer> cardList = new ArrayList<>(16);
        cardList.addAll(mjPlayerInfo.getHandCardList());
        cardList.addAll(mjPlayerInfo.getChiCardList());
        cardList.addAll(mjPlayerInfo.getPengCardList());
        cardList.addAll(mjPlayerInfo.getMingGangCardList());
        cardList.addAll(mjPlayerInfo.getAnGangCardList());

        if (mjHuService.isQingYiSe(cardList)){
            if (mjHuService.isNormalHu(cardList)){
                mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.HU_QING_YI_SE.type);
            }else if (cardList.get(0) > 26){
                mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.ZI_QING_YI_SE.type);
            }else if (isMengQing(mjPlayerInfo)){
                mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.MENG_QING_QING_YI_SE.type);
            } else {
                mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.LUAN_QING_YI_SE.type);
            }
        }

    }

    private void checkQiDui(MjPlayerInfo mjPlayerInfo){
        if (mjHuService.isQiDui(mjPlayerInfo.getHandCardList())){
            mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.QI_DUI.type);
        }
    }

    private void checkShiSanLan(MjPlayerInfo mjPlayerInfo){
        if (mjHuService.isShiSanLan(mjPlayerInfo.getHandCardList())){
            Map<MjValueEnum, List<Integer>> map = mjCardService.split(mjPlayerInfo.getHandCardList());
            if (map.get(MjValueEnum.feng).size() == 7){
                mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.QI_XING.type);
            } else {
                mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.SHI_SHI_SAN_LAN.type);
            }
        }
    }

    private void checkDanDiao(MjPlayerInfo mjPlayerInfo){
        if (mjPlayerInfo.getHandCardList().size() == 2){
            mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.DAN_DIAO.type);
        }
    }

    public void calHuPlayer(MjPlayerInfo mjPlayerInfo){
        checkPengPeng(mjPlayerInfo);
        checkQingYiSe(mjPlayerInfo);
        checkQiDui(mjPlayerInfo);
        checkShiSanLan(mjPlayerInfo);
        checkDanDiao(mjPlayerInfo);
        if (mjPlayerInfo.getMjCardTypeList().isEmpty()){
            mjPlayerInfo.getMjCardTypeList().add(MjScoreEnum.PING_HU.type);
        }
    }

    public void calHuRoom(MjRoomInfo mjRoomInfo){
        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            if (mjPlayerInfo.getIsHu().equals(1)){
                calHuPlayer(mjPlayerInfo);
            }
        }

    }
    @SuppressWarnings("ConstantConditions")
    private void assignScore(List<MjPlayerInfo> winPlayList, List<MjPlayerInfo> losePlayList){
        for (MjPlayerInfo winPlayerInfo : winPlayList){
            int score = 0;
            //天胡地胡放一起了
            if (winPlayerInfo.getDiscardCardList().isEmpty()){
                score = 8;
            } else {
                //南丰其实只会同时存在一种胡牌类型
                for (Integer type : winPlayerInfo.getMjCardTypeList()){
                    score = score + MjScoreEnum.getByType(type).score;
                }
            }
            for (MjPlayerInfo losePlayerInfo : losePlayList){
                if (!winPlayerInfo.getPlayerId().equals(losePlayerInfo.getPlayerId())){
                    winPlayerInfo.setCurScore(winPlayerInfo.getCurScore() + score);
                    losePlayerInfo.setCurScore(losePlayerInfo.getCurScore() - score);
                }
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void calScoreRoom(MjRoomInfo roomInfo){
        //找到所有胡牌的人,判断天地胡
        List<MjPlayerInfo> huPlayerList = new ArrayList<>(4);
        for (MjPlayerInfo player : roomInfo.getPlayerList()) {
            if (player.getIsHu().equals(1)) {
                huPlayerList.add(player);
                if (player.getDiscardCardList().isEmpty()) {
                    if (roomInfo.getRoomBankerId().equals(player.getPlayerId())) {
                        player.setHuType(MjHuTypeEnum.diHu.type);
                    }
                    //天胡在发牌的时候就判断了，这里就不判断了
                }
            }
        }
        //计算各个玩家的加分和减分
        switch (MjHuTypeEnum.getMjHuTypeEnum(huPlayerList.get(0).getHuType())){
            case zhuaChong:
            case qiangGang:
            case diHu:
                MjPlayerInfo dianPaoPlayer = MjCardRule.getLastPlayer(roomInfo);
                assignScore(huPlayerList, Collections.singletonList(dianPaoPlayer));
                for (MjPlayerInfo mjPlayerInfo: huPlayerList){
                    mjPlayerInfo.setZhuaChongCount( mjPlayerInfo.getZhuaChongCount() + 1);
                }
                dianPaoPlayer.setDianPaoCount( dianPaoPlayer.getDianPaoCount() + 1 );
                break;
            case ziMo:
            case gangKai:
            case tianHu:
                assignScore(huPlayerList, roomInfo.getPlayerList());
                //实际上也只会有一个自摸
                for (MjPlayerInfo mjPlayerInfo : huPlayerList){
                    mjPlayerInfo.setZiMoCount(mjPlayerInfo.getZiMoCount() + 1);
                }
                break;
        }

        //计算每个玩家总得分及设置房间的总赢家
        Integer totalWinnerId = roomInfo.getPlayerList().get(0).getPlayerId();
        Integer maxTotalScore = roomInfo.getPlayerList().get(0).getTotalScore();
        for (MjPlayerInfo player : roomInfo.getPlayerList()) {
            player.setTotalScore(player.getTotalScore() + player.getCurScore());
            if (player.getTotalScore() > maxTotalScore) {
                maxTotalScore = player.getTotalScore();
                totalWinnerId = player.getPlayerId();
            }
        }
        roomInfo.setTotalWinnerId(totalWinnerId);
    }
}
