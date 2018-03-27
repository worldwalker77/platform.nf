package cn.worldwalker.game.wyqp.mj.mock;

import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.utils.HttpClientUtils;
import com.alibaba.fastjson.JSON;
import org.testng.annotations.Test;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
            mockPlayer.setHandCardList(Arrays.asList(0,1,2,3,4,5,6,7,8,9,10,11,12));
            mockPlayer.setPengCardList(Collections.EMPTY_LIST);
            mockPlayer.setMinGangCardList(Collections.EMPTY_LIST);
            mockPlayer.setAnGangCardList(Collections.EMPTY_LIST);
            mockPlayerList.add(mockPlayer);
        }
        mockRoom.setMockPlayerList(mockPlayerList);

        String xx = "{\"curPlayerId\":1001,\"discardList\":[1],\"maCardList\":[],\"mockPlayerList\":[{\"anGangCardList\":[],\"handCardList\":[0,1,2,3,4,5,6,7,8,9],\"minGangCardList\":[],\"pengCardList\":[33,33,33],\"playerId\":1000},{\"anGangCardList\":[],\"handCardList\":[0,1,2,3,4,5,6,7,8,9,10,11,12],\"minGangCardList\":[],\"pengCardList\":[],\"playerId\":1001},{\"anGangCardList\":[],\"handCardList\":[0,1,2,3,4,5,6,7,8,9,10,11,12],\"minGangCardList\":[],\"pengCardList\":[],\"playerId\":1002},{\"anGangCardList\":[],\"handCardList\":[0,1,2,3,4,5,6,7,8,9,10,11,12],\"minGangCardList\":[],\"pengCardList\":[],\"playerId\":1003}],\"remainCardList\":[],\"roomBankerId\":1001}";

        mockRoom = JSON.parseObject(xx, MockRoom.class);


        MjRoomInfo mjRoomInfo = mockService.convertToRoom(mockRoom);
        System.out.println(JSON.toJSONString(mockRoom));
        System.out.println(JSON.toJSONString(mjRoomInfo));
//        System.out.println(JSON.toJSONString(mockRoom));
        Integer roomId = 916711;
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

