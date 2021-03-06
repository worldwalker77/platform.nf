package cn.worldwalker.game.wyqp.web.job;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.worldwalker.game.wyqp.common.channel.ChannelContainer;
import cn.worldwalker.game.wyqp.common.constant.Constant;
import cn.worldwalker.game.wyqp.common.domain.base.UserInfo;
import cn.worldwalker.game.wyqp.common.domain.jh.JhPlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.jh.JhRoomInfo;
import cn.worldwalker.game.wyqp.common.service.RedisOperationService;
import cn.worldwalker.game.wyqp.common.utils.GameUtil;
import cn.worldwalker.game.wyqp.jh.enums.JhPlayerStatusEnum;
import cn.worldwalker.game.wyqp.jh.service.JhGameService;

@Component(value="jhNoOperationOverTimeNoticeJob")
public class JhNoOperationOverTimeNoticeJob {
	
	private final static Log log = LogFactory.getLog(JhNoOperationOverTimeNoticeJob.class);
	
	@Autowired
	public RedisOperationService redisOperationService;
	@Autowired
	private ChannelContainer channelContainer;
	@Autowired
	private JhGameService jhGameService;
	
	private long noOperationTimeLimit = 2*60*1000;
	
	public void doTask(){
		String ip = Constant.localIp;
		if (StringUtils.isBlank(ip)) {
			return;
		}
		Map<String, String> map = redisOperationService.getAllJhNoOperationIpPlayerIdRoomIdTime();
		Set<Entry<String, String>> set = map.entrySet();
		for(Entry<String, String> entry : set){
			try {
				Integer playerId = Integer.valueOf(entry.getKey());
				String[] arr = entry.getValue().split("_");
				Integer roomId = Integer.valueOf(arr[0]);
				Integer curGame = Integer.valueOf(arr[1]);
				Integer stakeTimes = Integer.valueOf(arr[2]);
				Long time = Long.valueOf(arr[3]);
			
				JhRoomInfo jhRoomInfo = redisOperationService.getRoomInfoByRoomId(roomId, JhRoomInfo.class);
				if (jhRoomInfo == null) {
					redisOperationService.delJhNoOperationIpPlayerIdRoomIdTime(playerId);
					continue;
				}
				JhPlayerInfo playerInfo = (JhPlayerInfo)GameUtil.getPlayerByPlayerId(jhRoomInfo.getPlayerList(), playerId);
				
				/**如果，删除标记*/
				if (!(playerId.equals(jhRoomInfo.getCurPlayerId()) 
					&& curGame.equals(jhRoomInfo.getCurGame()) 
					&& stakeTimes.equals(playerInfo.getStakeTimes()) 
					&& (JhPlayerStatusEnum.notWatch.status.equals(playerInfo.getStatus()) || JhPlayerStatusEnum.watch.status.equals(playerInfo.getStatus())))) {
					redisOperationService.delJhNoOperationIpPlayerIdRoomIdTime(playerId);
					continue;
				}
				
				if (System.currentTimeMillis() - time < noOperationTimeLimit) {
					continue;
				}
				/**超过120秒没有操作，自动弃牌*/
				UserInfo userInfo = new UserInfo();
				userInfo.setRoomId(roomId);
				userInfo.setPlayerId(playerId);
				jhGameService.discardCards(null, null, userInfo);
			} catch (Exception e) {
				log.error("roomId:" + entry.getKey(), e);
			}
		}
		
	}
}
