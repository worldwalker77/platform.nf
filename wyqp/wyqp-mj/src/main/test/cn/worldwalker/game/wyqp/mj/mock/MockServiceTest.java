package cn.worldwalker.game.wyqp.mj.mock;

import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.utils.HttpClientUtils;
import com.alibaba.fastjson.JSON;
import org.testng.annotations.Test;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockServiceTest {

    private MockService mockService = MockService.getInstance();
    private static final String HTTP_URL = "http://game.nf.worldwalker.cn";


    @Test
    public void testSetNextCard() throws Exception {

        List<Integer> cardList = new ArrayList<>(16);
        cardList.add(1);
        cardList.add(2);
        cardList.add(3);
        cardList.add(3);
        cardList.add(4);
        cardList.add(5);
        cardList.add(6);
        mockService.setNextCard(cardList, 5);
        System.out.println(cardList);
    }


    @Test
    public void testConvertToRoom() throws Exception {
        MockRoom mockRoom = new MockRoom();
        mockRoom.setCurPlayerId(1001);
        mockRoom.setDiscardList(Arrays.asList(1));
        mockRoom.setMaCardList(Arrays.asList(1));
        mockRoom.setRoomBankerId(1001);
        List<MockPlayer> mockPlayerList = new ArrayList<>(4);
        for (int i=0; i<4; i++){
            MockPlayer mockPlayer = new MockPlayer();
            mockPlayer.setPlayerId(1000+i);
            mockPlayerList.add(mockPlayer);
        }
        mockRoom.setMockPlayerList(mockPlayerList);
        mockPlayerList.get(0).getHandCardList().addAll(Arrays.asList(1,2,3,3));
        mockPlayerList.get(0).setCurMoCard(0);
        mockPlayerList.get(1).getHandCardList().addAll(Arrays.asList(0,0,0));
        mockPlayerList.get(1).setCurMoCard(0);
        mockPlayerList.get(2).getHandCardList().addAll(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13));
        mockPlayerList.get(2).setCurMoCard(0);
        mockPlayerList.get(3).getHandCardList().addAll(Arrays.asList(1,2,3,4,5,6,7,8,9,10,11,12,13));
        mockPlayerList.get(3).setCurMoCard(0);


        MjRoomInfo mjRoomInfo = mockService.convertToRoom(mockRoom);
        System.out.println(JSON.toJSONString(mockRoom));
        System.out.println(JSON.toJSONString(mjRoomInfo));
//        System.out.println(JSON.toJSONString(mockRoom));
        Integer roomId = 898044;
        String data = URLEncoder.encode(JSON.toJSONString(mjRoomInfo));
        String ret = HttpClientUtils.get(HTTP_URL + "/refreshRoom?roomId=" + roomId + "&data=" + data);
        System.out.println(ret);
    }

    @Test
    public void testConvertToRoom2() throws Exception {
        String value = "{\"curPlayerId\":1001,\"discardList\":[1],\"maCardList\":[1],\"mockPlayerList\":[{\"anGangCardList\":[],\"handCardList\":[2,3,4,5,6,7],\"minGangCardList\":[],\"pengCardList\":[]},{\"anGangCardList\":[],\"handCardList\":[2,3,4,5,6,7],\"minGangCardList\":[],\"pengCardList\":[]},{\"anGangCardList\":[],\"handCardList\":[2,3,4,5,6,7],\"minGangCardList\":[],\"pengCardList\":[]},{\"anGangCardList\":[],\"handCardList\":[2,3,4,5,6,7],\"minGangCardList\":[],\"pengCardList\":[]}],\"remainCardList\":[],\"roomBankerId\":1001}";
        MockRoom mockRoom = JSON.parseObject(value,MockRoom.class);
        System.out.println(value.length());
        System.out.println(JSON.toJSONString(mockRoom));
    }




}

