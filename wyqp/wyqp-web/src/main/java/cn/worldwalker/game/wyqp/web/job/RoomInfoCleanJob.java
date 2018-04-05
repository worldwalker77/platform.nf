package cn.worldwalker.game.wyqp.web.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.worldwalker.game.wyqp.common.channel.ChannelContainer;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.base.RedisRelaModel;
import cn.worldwalker.game.wyqp.common.domain.jh.JhRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.mj.MjRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.nn.NnRoomInfo;
import cn.worldwalker.game.wyqp.common.enums.GameTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.MsgTypeEnum;
import cn.worldwalker.game.wyqp.common.manager.CommonManager;
import cn.worldwalker.game.wyqp.common.result.Result;
import cn.worldwalker.game.wyqp.common.service.RedisOperationService;
import cn.worldwalker.game.wyqp.common.utils.GameUtil;


public class RoomInfoCleanJob /**extends SingleServerJobByRedis*/ {
	public final static Log log = LogFactory.getLog(RoomInfoCleanJob.class);
	@Autowired
	private RedisOperationService redisOperationService;
	@Autowired
	private ChannelContainer channelContainer;
	@Autowired
	public CommonManager commonManager;
//	@Override
	public void doTask() {
		/**如果微信开关打开,通过删除房间标志位进行房间删除*/
			List<RedisRelaModel> list = redisOperationService.getAllDissolveIpRoomIdTime();
			for(RedisRelaModel model : list){
				if (System.currentTimeMillis() - model.getUpdateTime() > 5*60*1000) {
					BaseRoomInfo roomInfo = null;
					if (GameTypeEnum.nn.gameType.equals(model.getGameType()) ) {
						roomInfo = redisOperationService.getRoomInfoByRoomId(model.getRoomId(), NnRoomInfo.class);
					}else if(GameTypeEnum.mj.gameType.equals(model.getGameType()) ){
						roomInfo = redisOperationService.getRoomInfoByRoomId(model.getRoomId(), MjRoomInfo.class);
					}else if(GameTypeEnum.jh.gameType.equals(model.getGameType()) ){
						roomInfo = redisOperationService.getRoomInfoByRoomId(model.getRoomId(), JhRoomInfo.class);
					}
					if (roomInfo == null) {
						redisOperationService.delDissolveIpRoomIdTime(model.getRoomId());
						return;
					}
					/**给玩家返回解散房间请求*/
					List playerList = roomInfo.getPlayerList();
					Result result = new Result();
					Map<String, Object> data = new HashMap<String, Object>();
					result.setData(data);
					/**解散后需要进行结算*/
					data.put("roomInfo", roomInfo);
					result.setMsgType(MsgTypeEnum.successDissolveRoom.msgType);
					channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
					
					log.info("定时任务销毁房间,roomId=" + model.getRoomId());
					redisOperationService.cleanPlayerAndRoomInfo(model.getRoomId(), GameUtil.getPlayerIdStrArr(playerList));
					if (roomInfo.getClubId() != null) {
						redisOperationService.delClubIdRoomId(roomInfo.getClubId(), roomInfo.getRoomId());
					}
					redisOperationService.delDissolveIpRoomIdTime(model.getRoomId());
					try {
						if (roomInfo.getCurGame() > 1) {
							commonManager.addUserRecord(roomInfo);
						}
					} catch (Exception e) {
						log.error("解散房间时添加记录失败", e);
					}
				}
			}
		
	}
	
}
