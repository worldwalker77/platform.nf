package cn.worldwalker.game.wyqp.mj.mock;

import java.util.List;

public class MockPlayer {

    private Integer playerId;
    private Integer curMoCard;
    private List<Integer> handCardList;
    private List<Integer> pengCardList;
    private List<Integer> minGangCardList;
    private List<Integer> anGangCardList;

    public Integer getCurMoCard() {
        return curMoCard;
    }

    public void setCurMoCard(Integer curMoCard) {
        this.curMoCard = curMoCard;
    }

    public Integer getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Integer playerId) {
        this.playerId = playerId;
    }

    public List<Integer> getHandCardList() {
        return handCardList;
    }

    public void setHandCardList(List<Integer> handCardList) {
        this.handCardList = handCardList;
    }

    public List<Integer> getPengCardList() {
        return pengCardList;
    }

    public void setPengCardList(List<Integer> pengCardList) {
        this.pengCardList = pengCardList;
    }

    public List<Integer> getMinGangCardList() {
        return minGangCardList;
    }

    public void setMinGangCardList(List<Integer> minGangCardList) {
        this.minGangCardList = minGangCardList;
    }

    public List<Integer> getAnGangCardList() {
        return anGangCardList;
    }

    public void setAnGangCardList(List<Integer> anGangCardList) {
        this.anGangCardList = anGangCardList;
    }
}
