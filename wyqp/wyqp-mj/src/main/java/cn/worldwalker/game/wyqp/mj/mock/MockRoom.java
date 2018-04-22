package cn.worldwalker.game.wyqp.mj.mock;

import java.util.ArrayList;
import java.util.List;

public class MockRoom {
    private List<Integer> remainCardList = new ArrayList<>(256);
    private Integer curPlayerId;
    private Integer curGame;
    private Integer roomBankerId;
    private Integer maiMaCount;
    private List<Integer> maCardList;
    //给几个不应该出现的牌就可以了，不一定要罗列全
    private List<Integer> discardList;
    private List<MockPlayer> mockPlayerList = new ArrayList<>(4);

    public List<Integer> getDiscardList() {
        return discardList;
    }

    public void setDiscardList(List<Integer> discardList) {
        this.discardList = discardList;
    }

    public List<Integer> getRemainCardList() {
        return remainCardList;
    }

    public void setRemainCardList(List<Integer> remainCardList) {
        this.remainCardList = remainCardList;
    }

    public Integer getCurPlayerId() {
        return curPlayerId;
    }

    public void setCurPlayerId(Integer curPlayerId) {
        this.curPlayerId = curPlayerId;
    }

    public Integer getRoomBankerId() {
        return roomBankerId;
    }

    public void setRoomBankerId(Integer roomBankerId) {
        this.roomBankerId = roomBankerId;
    }

    public Integer getMaiMaCount() {
        return maiMaCount;
    }

    public void setMaiMaCount(Integer maiMaCount) {
        this.maiMaCount = maiMaCount;
    }

    public List<Integer> getMaCardList() {
        return maCardList;
    }

    public void setMaCardList(List<Integer> maCardList) {
        this.maCardList = maCardList;
    }

    public List<MockPlayer> getMockPlayerList() {
        return mockPlayerList;
    }

    public void setMockPlayerList(List<MockPlayer> mockPlayerList) {
        this.mockPlayerList = mockPlayerList;
    }

    public Integer getCurGame() {
        return curGame;
    }

    public void setCurGame(Integer curGame) {
        this.curGame = curGame;
    }
}
