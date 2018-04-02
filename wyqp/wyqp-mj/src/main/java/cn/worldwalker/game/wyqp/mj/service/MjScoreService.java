package cn.worldwalker.game.wyqp.mj.service;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.mj.cards.MjCardRule;
import cn.worldwalker.game.wyqp.mj.enums.GangTypeEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjOperationEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjScoreEnum;
import cn.worldwalker.game.wyqp.mj.enums.MjValueEnum;
import org.apache.log4j.Logger;

import java.util.*;

import static cn.worldwalker.game.wyqp.mj.enums.MjHuTypeEnum.getMjHuTypeEnum;

public class MjScoreService {
    private static MjScoreService ourInstance = new MjScoreService();

    private MjCardService mjCardService = MjCardService.getInstance();
    private MjHuService mjHuService = MjHuService.getInstance();

    public static MjScoreService getInstance() {
        return ourInstance;
    }

    private static final Logger log = Logger.getLogger(MjScoreService.class);

    private MjScoreService() {
    }



    private boolean isMengQing(MjPlayerInfo mjPlayerInfo){
        return mjPlayerInfo.getPengCardList().size() == 0 &&
                mjPlayerInfo.getChiCardList().size() == 0 &&
                mjPlayerInfo.getMingGangCardList().size() == 0 &&
                mjPlayerInfo.getAnGangCardList().size() == 0;
    }

    private void checkPengPeng(MjPlayerInfo mjPlayerInfo, Integer huCard, List<Integer> typeList){
        List<Integer> cardList = new ArrayList<>(16);
        cardList.addAll(mjPlayerInfo.getHandCardList());
        if (huCard != null) cardList.add(huCard);
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
                typeList.add(MjScoreEnum.MENG_QING_PENG_PENG_HU.type);
            } else {
                typeList.add(MjScoreEnum.PENG_PENG_HU.type);
            }
        }
    }

    private void checkQingYiSe(MjPlayerInfo mjPlayerInfo, Integer huCard, List<Integer> typeList){
        List<Integer> cardList = new ArrayList<>(16);
        cardList.addAll(mjPlayerInfo.getHandCardList());
        if (huCard != null) cardList.add(huCard);
        cardList.addAll(mjPlayerInfo.getChiCardList());
        cardList.addAll(mjPlayerInfo.getPengCardList());
        cardList.addAll(mjPlayerInfo.getMingGangCardList());
        cardList.addAll(mjPlayerInfo.getAnGangCardList());

        if (mjHuService.isQingYiSe(cardList)){
            List<Integer> handAndMoList = new ArrayList<>(16);
            handAndMoList.addAll(mjPlayerInfo.getHandCardList());
            if (huCard != null){
                handAndMoList.add(huCard);
            }
            if (mjHuService.isNormalHu(handAndMoList)){
                typeList.add(MjScoreEnum.HU_QING_YI_SE.type);
            }else if (cardList.get(0) > 26){
                typeList.add(MjScoreEnum.ZI_QING_YI_SE.type);
            }else if (isMengQing(mjPlayerInfo)){
                typeList.add(MjScoreEnum.MENG_QING_QING_YI_SE.type);
            } else {
                typeList.add(MjScoreEnum.LUAN_QING_YI_SE.type);
            }
        }
    }

    private void checkQiDui(MjPlayerInfo mjPlayerInfo, Integer huCard, List<Integer> typeList){
        List<Integer> cardList = new ArrayList<>(16);
        cardList.addAll(mjPlayerInfo.getHandCardList());
        if (huCard != null) cardList.add(huCard);
        if (mjHuService.isQiDui(cardList)){
            typeList.add(MjScoreEnum.QI_DUI.type);
        }
    }

    private void checkShiSanLan(MjPlayerInfo mjPlayerInfo, Integer huCard, List<Integer> typeList){
        List<Integer> cardList = new ArrayList<>(16);
        cardList.addAll(mjPlayerInfo.getHandCardList());
        if (huCard != null) cardList.add(huCard);
        if (mjHuService.isShiSanLan(cardList)){
            Map<MjValueEnum, List<Integer>> map = mjCardService.split(mjPlayerInfo.getHandCardList());
            if (map.get(MjValueEnum.feng).size() == 7){
                typeList.add(MjScoreEnum.QI_XING.type);
            } else {
                typeList.add(MjScoreEnum.SHI_SHI_SAN_LAN.type);
            }
        }
    }

    private void checkDanDiao(MjPlayerInfo mjPlayerInfo, Integer huCard, List<Integer> typeList){
        List<Integer> cardList = new ArrayList<>(16);
        cardList.addAll(mjPlayerInfo.getHandCardList());
        if (huCard != null) cardList.add(huCard);
        if (cardList.size() == 2){
            typeList.add(MjScoreEnum.DAN_DIAO.type);
        }
    }

    /*
    计算单个玩家胡牌牌型
     */
    public List<Integer> calHuPlayer(MjPlayerInfo mjPlayerInfo, Integer huCard){
        List<Integer> typeList = new ArrayList<>(16);
        checkPengPeng(mjPlayerInfo, huCard, typeList);
        checkQingYiSe(mjPlayerInfo, huCard, typeList);
        checkQiDui(mjPlayerInfo, huCard, typeList);
        checkShiSanLan(mjPlayerInfo, huCard, typeList);
        checkDanDiao(mjPlayerInfo, huCard, typeList);
        if (typeList.isEmpty()){
            typeList.add(MjScoreEnum.PING_HU.type);
        }
        return typeList;
    }

    @SuppressWarnings("ConstantConditions")
    private Integer getHuCard(MjRoomInfo mjRoomInfo, MjPlayerInfo mjPlayerInfo){
        Integer huCard = null; //计算各个玩家的加分和减分
        switch (getMjHuTypeEnum(mjPlayerInfo.getHuType())){
            case zhuaChong:
            case qiangGang:
            case diHu:
                huCard = mjRoomInfo.getLastCardIndex();
                break;
            case ziMo:
            case gangKai:
            case tianHu:
                huCard = mjPlayerInfo.getCurMoPaiCardIndex();
                break;
        }
        return huCard;
    }
    /*
    计算整个房间胡牌牌型
     */
    @SuppressWarnings("ConstantConditions")
    void calHuRoom(MjRoomInfo mjRoomInfo){
        for (MjPlayerInfo mjPlayerInfo : mjRoomInfo.getPlayerList()){
            if (mjPlayerInfo.getIsHu().equals(1)){
                Integer huCard = getHuCard(mjRoomInfo, mjPlayerInfo);
//                mjPlayerInfo.getMjCardTypeList().clear();
                mjPlayerInfo.getMjCardTypeList().addAll(calHuPlayer(mjPlayerInfo, huCard));
            }
        }

    }

    /**
     * 列表中有个几个，主要用来计算马中了几次
     */
    private int getCntInList(List<MjPlayerInfo> playerInfoList, MjPlayerInfo playerInfo){
        int cnt = 0;
        for (MjPlayerInfo mjPlayerInfo : playerInfoList){
            if (mjPlayerInfo.getPlayerId().equals(playerInfo.getPlayerId())){
                cnt = cnt + 1;
            }
        }
        return cnt;

    }

    /*
    为输和赢的玩家算分
     */
    @SuppressWarnings("ConstantConditions")
    private void assignScore(List<MjPlayerInfo> winPlayList, List<MjPlayerInfo> losePlayList, MjRoomInfo roomInfo){
        //庄家和中了马的
        MjPlayerInfo bankerPlayer = MjCardRule.getBankerPlayer(roomInfo);
        List<MjPlayerInfo> maPlayerList = getMaPlayerList(roomInfo);
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
                score = score > 8 ? 8 : score;
            }

            for (MjPlayerInfo losePlayerInfo : losePlayList) {
                if (!winPlayerInfo.getPlayerId().equals(losePlayerInfo.getPlayerId())) {
                    winPlayerInfo.setHuScore(winPlayerInfo.getHuScore() + score);
                    losePlayerInfo.setHuScore(losePlayerInfo.getHuScore() - score);
                    //输了马了哦
                    int loseMaCnt = getCntInList(maPlayerList, losePlayerInfo);
                    if (loseMaCnt > 0){
                        assignHuMaScore(Collections.singletonList(winPlayerInfo),
                                Collections.singletonList(bankerPlayer), winPlayerInfo,score * loseMaCnt );
                    }
                }
            }
            //赢了马
            int winMaCnt = getCntInList(maPlayerList, winPlayerInfo);
            if (winMaCnt > 0){
                assignHuMaScore(Collections.singletonList(bankerPlayer),losePlayList, winPlayerInfo,score * winMaCnt );
            }


        }
    }

    /*
    计算整个房间玩家的牌面得分
     */
    @SuppressWarnings("ConstantConditions")
    void calScoreRoom(MjRoomInfo roomInfo){
        //找到所有胡牌的人,判断天地胡
        List<MjPlayerInfo> huPlayerList = new ArrayList<>(4);
        for (MjPlayerInfo player : roomInfo.getPlayerList()) {
            if (player.getIsHu().equals(1)) {
                huPlayerList.add(player);
            }
        }
        //计算各个玩家的加分和减分
        switch (getMjHuTypeEnum(huPlayerList.get(0).getHuType())){
            case zhuaChong:
            case diHu:
                MjPlayerInfo dianPaoPlayer = MjCardRule.getLastPlayer(roomInfo);
                assignScore(huPlayerList, Collections.singletonList(dianPaoPlayer), roomInfo);
                for (MjPlayerInfo mjPlayerInfo: huPlayerList){
                    mjPlayerInfo.setZhuaChongCount( mjPlayerInfo.getZhuaChongCount() + 1);
                }
                dianPaoPlayer.setDianPaoCount( dianPaoPlayer.getDianPaoCount() + 1 );
                break;
            case qiangGang:
                MjPlayerInfo qiangGangpPlayer = MjCardRule.getLastPlayer(roomInfo);
                assignScore(huPlayerList, Arrays.asList(qiangGangpPlayer,qiangGangpPlayer,qiangGangpPlayer), roomInfo);
                for (MjPlayerInfo mjPlayerInfo: huPlayerList){
                    mjPlayerInfo.setZhuaChongCount( mjPlayerInfo.getZhuaChongCount() + 1);
                }
                qiangGangpPlayer.setDianPaoCount( qiangGangpPlayer.getDianPaoCount() + 1 );
                break;
            case ziMo:
            case gangKai:
            case tianHu:
                assignScore(huPlayerList, roomInfo.getPlayerList(), roomInfo);
                //实际上也只会有一个自摸
                for (MjPlayerInfo mjPlayerInfo : huPlayerList){
                    mjPlayerInfo.setZiMoCount(mjPlayerInfo.getZiMoCount() + 1);
                }
                break;
        }


    }

    /**
     * 计算总赢家，马分、杠分，算到总分中
     */
    public void calTotalWin(MjRoomInfo roomInfo){
        //计算每个玩家总得分及设置房间的总赢家
        Integer totalWinnerId = roomInfo.getPlayerList().get(0).getPlayerId();
        Integer maxTotalScore = roomInfo.getPlayerList().get(0).getTotalScore();
        for (MjPlayerInfo player : roomInfo.getPlayerList()) {
            //算上胡分
            player.setCurScore(player.getCurScore() + player.getHuScore());
            //算上杠分
            player.setCurScore(player.getCurScore() + player.getGangScore());
            //算上马分
            player.setCurScore(player.getCurScore() + player.getMaScore());
            //更新总分
            player.setTotalScore(player.getTotalScore() + player.getCurScore());
            if (player.getTotalScore() > maxTotalScore) {
                maxTotalScore = player.getTotalScore();
                totalWinnerId = player.getPlayerId();
            }
        }
        roomInfo.setTotalWinnerId(totalWinnerId);
    }

    /*
    为杠牌玩家算分
     */
    private void assignGangScore(MjRoomInfo roomInfo, List<MjPlayerInfo> winPlayList, List<MjPlayerInfo> losePlayList, int score ){
        //庄家和中了马的
        MjPlayerInfo bankerPlayer = MjCardRule.getBankerPlayer(roomInfo);
        List<MjPlayerInfo> maPlayerList = getMaPlayerList(roomInfo);
        for (MjPlayerInfo winPlayerInfo : winPlayList){
            for (MjPlayerInfo losePlayerInfo : losePlayList){
                if (!winPlayerInfo.getPlayerId().equals(losePlayerInfo.getPlayerId())){
                    winPlayerInfo.setGangScore(winPlayerInfo.getGangScore() + score);
                    losePlayerInfo.setGangScore(losePlayerInfo.getGangScore() - score);
                    //输了马
                    int loseCnt = getCntInList(maPlayerList, losePlayerInfo);
                    if (loseCnt > 0){
                        assignGangMaScore(Collections.singletonList(winPlayerInfo),
                                Collections.singletonList(bankerPlayer), winPlayerInfo,loseCnt * score);
                    }
                }
            }
            //赢了马
            int winCnt = getCntInList(maPlayerList, winPlayerInfo);
            if (winCnt > 0) {
                assignGangMaScore(Collections.singletonList(bankerPlayer), losePlayList, winPlayerInfo, winCnt * score);
            }
        }
    }

    /*
    计算杠的分数
     */
    public void calGangScore(MjRoomInfo roomInfo, MjPlayerInfo player, MjOperationEnum operationType){
        switch (operationType) {
            case mingGang:
                //如果是摸牌后的明杠
                if (MjCardRule.isHandCard3n2(player)) {
                    player.getGangTypeList().add(GangTypeEnum.ZI_MO_MING_GANG.type);
                    assignGangScore(roomInfo,Collections.singletonList(player),roomInfo.getPlayerList(),1);
                }else{//如果是别人打的牌的明杠
                    player.getGangTypeList().add(GangTypeEnum.MING_GANG.type);
                    MjPlayerInfo lastPlayer = MjCardRule.getLastPlayer(roomInfo);
                    assignGangScore(roomInfo,Collections.singletonList(player),roomInfo.getPlayerList(),1);
                    //放杠2分，补1分
                    assignGangScore(roomInfo,Collections.singletonList(player),Collections.singletonList(lastPlayer),1);
                }
                player.setMinGangCount(player.getMinGangCount() + 1);
                break;
            case anGang:
                player.getGangTypeList().add(GangTypeEnum.AN_GANG.type);
                assignGangScore(roomInfo,Collections.singletonList(player),roomInfo.getPlayerList(),2);
                player.setAnGangCount(player.getAnGangCount() + 1);
                break;
            default:
                break;
        }
    }


    /**
     ** 计算马分
     */
    private void assignHuMaScore(List<MjPlayerInfo> maPlayerList, List<MjPlayerInfo> losePlayerList,
                                  MjPlayerInfo winPlayer, int score){
        for (MjPlayerInfo maPlayer: maPlayerList){
            for (MjPlayerInfo losePlayer: losePlayerList){
                if (!losePlayer.getPlayerId().equals(maPlayer.getPlayerId()) &&
                        !losePlayer.getPlayerId().equals(winPlayer.getPlayerId())){
                    maPlayer.setMaScore( maPlayer.getMaScore() + score);
                    losePlayer.setMaScore( losePlayer.getMaScore() - score);
                }
            }
        }
    }

    private void assignGangMaScore(List<MjPlayerInfo> maPlayerList, List<MjPlayerInfo> losePlayerList,
                                  MjPlayerInfo winPlayer, int score){
        for (MjPlayerInfo maPlayer: maPlayerList){
            for (MjPlayerInfo losePlayer: losePlayerList){
                if (!losePlayer.getPlayerId().equals(maPlayer.getPlayerId()) &&
                        !losePlayer.getPlayerId().equals(winPlayer.getPlayerId())){
                    maPlayer.setGangScore( maPlayer.getGangScore() + score);
                    losePlayer.setGangScore( losePlayer.getGangScore() - score);
                }
            }
        }
    }


    /**
     * 获取中马的玩家
     */
    List<MjPlayerInfo> getMaPlayerList(MjRoomInfo roomInfo){
        List<MjPlayerInfo> maPlayerList = new ArrayList<>(4);
        for (int i=0; i<roomInfo.getPlayerList().size(); i++){
            MjPlayerInfo mjPlayerInfo = roomInfo.getPlayerList().get(i);
            if (mjPlayerInfo.getPlayerId().equals(roomInfo.getRoomBankerId())){
                for (Integer maCard : roomInfo.getMaCardList()){
                    int pos = (maCard +1) %9 == 0 ? 9 :(maCard + 1) % 9;
                    maPlayerList.add(roomInfo.getPlayerList().get( (i + 3 + pos ) % 4));
                }
                break;
            }
        }
        return maPlayerList;
    }


}
