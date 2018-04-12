package cn.worldwalker.game.wyqp.web.game;

import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.service.RedisOperationService;
import cn.worldwalker.game.wyqp.mj.mock.MockRoom;
import cn.worldwalker.game.wyqp.mj.mock.MockService;
import cn.worldwalker.game.wyqp.mj.robot.Client;
import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("test")
public class TestController {

    private static final int waitTime = 100;

    private MockService mockService = MockService.getInstance();

    @Autowired
    private RedisOperationService redisOperationService;

    @RequestMapping("addRobot")
    @ResponseBody
    public String addRobot(Integer roomId, Integer cnt) throws Exception {

	    int clientCnt = cnt == null ? 3 : cnt;
	    for (int i=0; i<clientCnt; i++){
            Client client = new Client(i);
            client.init();
            Thread.sleep(waitTime);
            client.entryHall();
            Thread.sleep(waitTime);
            client.entryRoom(roomId);
            Thread.sleep(waitTime);
            client.playerReady();
            Thread.sleep(waitTime);
        }
        return "addRobot OK";
    }


    @RequestMapping("refreshRoom")
    @ResponseBody
    public String refreshRoom(String data, Integer roomId){
        MjRoomInfo mjRoomInfoOld = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
        if (mjRoomInfoOld == null){
            return "此房间不存在";
        }
        MjRoomInfo mjRoomInfoNew = mockService.refreshRoom(mjRoomInfoOld, JSON.parseObject(data, MjRoomInfo.class) );
        //开始替换
        redisOperationService.setRoomIdRoomInfo(roomId,mjRoomInfoNew);
	    return JSON.toJSONString(mjRoomInfoNew);
    }

    @RequestMapping("getRoomInfo")
    @ResponseBody
    public MjRoomInfo getRoomInfo(Integer roomId){
        return redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
    }


    @RequestMapping("replaceRoom")
    @ResponseBody
    public String replaceRoom(Integer roomId, String data){
        MockRoom mockRoom = JSON.parseObject(data, MockRoom.class);
        MjRoomInfo mjRoomInfo = mockService.convertToRoom(mockRoom);

        MjRoomInfo mjRoomInfoOld = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
        if (mjRoomInfoOld == null){
           return "此房间不存在";
        }
        MjRoomInfo mjRoomInfoNew = mockService.refreshRoom(mjRoomInfoOld, mjRoomInfo);
        //开始替换
        redisOperationService.setRoomIdRoomInfo(roomId,mjRoomInfoNew);

        return JSON.toJSONString(mjRoomInfo);
    }


    @RequestMapping("setNextCard")
    @ResponseBody
    public String setNextCard(Integer roomId, Integer card){
        MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
        if (roomInfo == null){
            return "room not exist";
        }

        List<Integer> cardList = roomInfo.getTableRemainderCardList();

        boolean isOK = mockService.setNextCard(cardList,card);

        redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);

        return isOK ? "OK" : "No Card";

    }


    @RequestMapping("setNextCard2")
    @ResponseBody
    public String setNextCard2(Integer roomId, Integer card){
        MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
        if (roomInfo == null){
            return "room not exist";
        }
        List<Integer> cardList = roomInfo.getTableRemainderCardList();
        cardList.add(0,card);
        redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
        return "OK";
    }



    @RequestMapping("setCard")
    @ResponseBody
    public String setCard(Integer roomId, Integer playerId, String cards){

        String[] cardArray = cards.split(",");
        MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
        if (roomInfo == null){
            return "room not exist";
        }
        for (MjPlayerInfo mjPlayerInfo : roomInfo.getPlayerList()){
            if (mjPlayerInfo.getPlayerId().equals(playerId)){
                mjPlayerInfo.getHandCardList().clear();
                for (String s : cardArray){
                    mjPlayerInfo.getHandCardList().add(Integer.valueOf(s));
                }
            }
        }
        redisOperationService.setRoomIdRoomInfo(roomId,roomInfo);
        return "ok";

    }


    @RequestMapping("control")
    @ResponseBody
    public String setControl(Integer roomId, Integer playerId){
        MjRoomInfo roomInfo = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
        if (roomInfo == null){
            return "room not exist";
        }
        roomInfo.getControlGame().addAll(Arrays.asList(1,2,3,4));
        roomInfo.getControlPlayer().add(playerId);
        redisOperationService.setRoomIdRoomInfo(roomId,roomInfo);
        return "OK";
    }

}
