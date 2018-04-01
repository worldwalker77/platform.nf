package cn.worldwalker.game.wyqp.mj.mock;

import java.util.ArrayList;
import java.util.List;

public class MockPlayer {

    private Integer playerId;
    private Integer curMoCard;
    private List<Integer> handCardList = new ArrayList<>(16);
    private List<Integer> pengCardList = new ArrayList<>(16);
    private List<Integer> minGangCardList = new ArrayList<>(16);
    private List<Integer> anGangCardList = new ArrayList<>(16);

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
