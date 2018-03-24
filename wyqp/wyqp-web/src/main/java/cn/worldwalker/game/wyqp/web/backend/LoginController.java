package cn.worldwalker.game.wyqp.web.backend;

import cn.worldwalker.game.wyqp.common.backend.BackendService;
import cn.worldwalker.game.wyqp.common.backend.GameQuery;
import cn.worldwalker.game.wyqp.common.domain.mj.MjPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.result.Result;
import cn.worldwalker.game.wyqp.common.service.RedisOperationService;
import cn.worldwalker.game.wyqp.mj.robot.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class LoginController {

    private static final int waitTime = 100;

	@Autowired
	private BackendService gameService;
	
	@RequestMapping("login/index")
	public ModelAndView index(String redirectUrl,HttpServletResponse response, HttpServletRequest request){
		ModelAndView mv = new ModelAndView();
		mv.addObject("redirectUrl", redirectUrl);
		mv.setViewName("backend/proxy/login");
		return mv;
	}
	
	@RequestMapping("login/doLogin")
	@ResponseBody
	public Result doLogin(String mobilePhone, String password){
		GameQuery gameQuery = new GameQuery();
		gameQuery.setMobilePhone(mobilePhone);
		gameQuery.setPassword(password);
		return gameService.doLogin(gameQuery);
		
	}


    @RequestMapping("addRobot")
    @ResponseBody
    public String addRobot(Integer roomId, Integer cnt) throws Exception {

        Client client1 = new Client(2);
        client1.init();
        Thread.sleep(waitTime);
        client1.entryHall();
        Thread.sleep(waitTime);
        client1.entryRoom(roomId);
        Thread.sleep(waitTime);
        client1.playerReady();

        Client client2 = new Client(3);
        client2.init();
        Thread.sleep(waitTime);
        client2.entryHall();
        Thread.sleep(waitTime);
        client2.entryRoom(roomId);
        Thread.sleep(waitTime);
        client2.playerReady();


        Client client3 = new Client(4);
        client3.init();
        Thread.sleep(waitTime);
        client3.entryHall();
        Thread.sleep(waitTime);
        client3.entryRoom(roomId);
        Thread.sleep(waitTime);
        client3.playerReady();
        Thread.sleep(waitTime);

        return "OK";
    }


    @RequestMapping("refreshRoom")
    @ResponseBody
    public String refreshRoom(Integer roomId, Integer sceneId){
        RedisOperationService redisOperationService = new RedisOperationService();
        MjRoomInfo mjRoomInfo = redisOperationService.getRoomInfoByRoomId(roomId, MjRoomInfo.class);
        List<MjPlayerInfo> playerList = mjRoomInfo.getPlayerList();

        MjRoomInfo mjRoomInfoNew = new MjRoomInfo();
        List<MjPlayerInfo> newPlayerList = mjRoomInfoNew.getPlayerList();

        //开始替换
        mjRoomInfoNew.setRoomId(mjRoomInfo.getRoomId());
        for (int i=0; i<4; i++){
            newPlayerList.get(i).setPlayerId(playerList.get(i).getPlayerId());
        }

        redisOperationService.setRoomIdRoomInfo(roomId,mjRoomInfoNew);

	    return "OK";
    }


}
