package cn.worldwalker.game.wyqp.common.service;

import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import cn.worldwalker.game.wyqp.common.backend.GameModel;
import cn.worldwalker.game.wyqp.common.backend.GameQuery;
import cn.worldwalker.game.wyqp.common.channel.ChannelContainer;
import cn.worldwalker.game.wyqp.common.constant.Constant;
import cn.worldwalker.game.wyqp.common.dao.GameDao;
import cn.worldwalker.game.wyqp.common.domain.base.BaseMsg;
import cn.worldwalker.game.wyqp.common.domain.base.BasePlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRequest;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.base.OrderModel;
import cn.worldwalker.game.wyqp.common.domain.base.ProductModel;
import cn.worldwalker.game.wyqp.common.domain.base.RedisRelaModel;
import cn.worldwalker.game.wyqp.common.domain.base.UserFeedbackModel;
import cn.worldwalker.game.wyqp.common.domain.base.UserInfo;
import cn.worldwalker.game.wyqp.common.domain.base.UserModel;
import cn.worldwalker.game.wyqp.common.domain.base.UserRecordModel;
import cn.worldwalker.game.wyqp.common.domain.base.WeiXinUserInfo;
import cn.worldwalker.game.wyqp.common.enums.ChatTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.DissolveStatusEnum;
import cn.worldwalker.game.wyqp.common.enums.GameTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.MsgTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.OnlineStatusEnum;
import cn.worldwalker.game.wyqp.common.enums.PayStatusEnum;
import cn.worldwalker.game.wyqp.common.enums.PayTypeEnum;
import cn.worldwalker.game.wyqp.common.enums.PlayerStatusEnum;
import cn.worldwalker.game.wyqp.common.enums.RoomStatusEnum;
import cn.worldwalker.game.wyqp.common.exception.BusinessException;
import cn.worldwalker.game.wyqp.common.exception.ExceptionEnum;
import cn.worldwalker.game.wyqp.common.manager.CommonManager;
import cn.worldwalker.game.wyqp.common.result.Result;
import cn.worldwalker.game.wyqp.common.roomlocks.RoomLockContainer;
import cn.worldwalker.game.wyqp.common.rpc.WeiXinRpc;
import cn.worldwalker.game.wyqp.common.utils.DateUtil;
import cn.worldwalker.game.wyqp.common.utils.GameUtil;
import cn.worldwalker.game.wyqp.common.utils.IPUtil;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
import cn.worldwalker.game.wyqp.common.utils.SnowflakeIdGenerator;
import cn.worldwalker.game.wyqp.common.utils.UrlImgDownLoadUtil;
import cn.worldwalker.game.wyqp.common.utils.wxpay.DateUtils;
import cn.worldwalker.game.wyqp.common.utils.wxpay.HttpUtil;
import cn.worldwalker.game.wyqp.common.utils.wxpay.MapUtils;
import cn.worldwalker.game.wyqp.common.utils.wxpay.PayCommonUtil;
import cn.worldwalker.game.wyqp.common.utils.wxpay.WeixinConstant;
import cn.worldwalker.game.wyqp.common.utils.wxpay.XMLUtil;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

public abstract class BaseGameService {
	
	public final static Log log = LogFactory.getLog(BaseGameService.class);
	
	@Autowired
	public RedisOperationService redisOperationService;
	@Autowired
	public ChannelContainer channelContainer;
	@Autowired
	public CommonManager commonManager;
	@Autowired
	public GameDao gameDao;
	
	@Autowired
	public WeiXinRpc weiXinRpc;
	
	public Result login(String code, String deviceType, HttpServletRequest request) {
		Result result = new Result();
		if (StringUtils.isBlank(code)) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		WeiXinUserInfo weixinUserInfo = weiXinRpc.getWeiXinUserInfo(code);
		if (null == weixinUserInfo) {
			throw new BusinessException(ExceptionEnum.QUERY_WEIXIN_USER_INFO_FAIL);
		}
		weixinUserInfo.setName(GameUtil.emojiFilter(weixinUserInfo.getName()));
		UserModel userModel = commonManager.getUserByWxOpenId(weixinUserInfo.getOpneid());
		Integer extensionCode = GameUtil.genExtensionCode();
		if (null == userModel) {
			userModel = new UserModel();
			userModel.setNickName(weixinUserInfo.getName());
			userModel.setHeadImgUrl(weixinUserInfo.getHeadImgUrl());
			userModel.setWxOpenId(weixinUserInfo.getOpneid());
			userModel.setRoomCardNum(10);
			userModel.setExtensionCode(extensionCode);
			commonManager.insertUser(userModel);
		}
		/**从redis查看此用户是否有roomId*/
		Integer roomId = null;
		RedisRelaModel redisRelaModel = redisOperationService.getRoomIdGameTypeByPlayerId(userModel.getPlayerId());
		if (redisRelaModel != null) {
			roomId = redisRelaModel.getRoomId();
		}
		UserInfo userInfo = new UserInfo();
		userInfo.setPlayerId(userModel.getPlayerId());
		userInfo.setRoomId(roomId);
		userInfo.setNickName(weixinUserInfo.getName());
		userInfo.setSex(weixinUserInfo.getSex());
		userInfo.setLevel(userModel.getUserLevel() == null ? 1 : userModel.getUserLevel());
        //noinspection Duplicates
        if ("true".equals(Constant.useWss)){
            userInfo.setServerIp(Constant.wssDomain);
            userInfo.setPort(String.valueOf(Constant.wssPort));
        } else {
            userInfo.setServerIp(Constant.localIp);
            userInfo.setPort(String.valueOf(Constant.websocketPort));
        }
		userInfo.setRemoteIp(IPUtil.getRemoteIp(request));
		String loginToken = GameUtil.genToken(userModel.getPlayerId());
		userInfo.setHeadImgUrl(UrlImgDownLoadUtil.getLocalImgUrl(weixinUserInfo.getHeadImgUrl(), userModel.getPlayerId()));
		userInfo.setToken(loginToken);
		/**设置赢牌概率*/
		userInfo.setWinProbability(userModel.getWinProbability());
		userInfo.setRoomCardNum(userModel.getRoomCardNum());
		userInfo.setExtensionCode(userModel.getExtensionCode());
		redisOperationService.setUserInfo(loginToken, userInfo);
		/****-------*/
//		redisOperationService.hdelOfflinePlayerIdRoomIdGameTypeTime(userModel.getPlayerId());
		Integer clubId = redisOperationService.getClubIdByPlayerId(userModel.getPlayerId());
		userInfo.setClubId(clubId);
		result.setData(userInfo);
		return result;
	}
	
	public Result preLogin(String token, String deviceType, HttpServletRequest request) {
		Result result = new Result();
		if (StringUtils.isBlank(token)) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		UserInfo userInfo = redisOperationService.getUserInfo(token);
		if (userInfo != null) {
			if (redisOperationService.isLoginFuseOpen()) {
				UserModel userModel = commonManager.getUserById(userInfo.getPlayerId());
				if (userModel != null) {
					userInfo.setRoomCardNum(userModel.getRoomCardNum());
				}
			}
			result.setData(userInfo);
			redisOperationService.setUserInfo(token, userInfo);
		}
		return result;
	}
	
	public Result login1(String code, String deviceType,HttpServletRequest request) {
		Result result = new Result();
		Integer roomId = null;
		Integer playerId = GameUtil.genPlayerId();
		UserInfo userInfo = new UserInfo();
		userInfo.setPlayerId(playerId);
		userInfo.setRoomId(roomId);
		userInfo.setNickName(String.valueOf(playerId));
		userInfo.setSex(0);
		userInfo.setLevel(1);
        //noinspection Duplicates
        if ("true".equals(Constant.useWss)){
            userInfo.setServerIp(Constant.wssDomain);
            userInfo.setPort(String.valueOf(Constant.wssPort));
        } else {
            userInfo.setServerIp(Constant.localIp);
            userInfo.setPort(String.valueOf(Constant.websocketPort));
        }
		userInfo.setRemoteIp(IPUtil.getRemoteIp(request));
		String loginToken =GameUtil.genToken(playerId);
		userInfo.setToken(loginToken);
		userInfo.setHeadImgUrl("http://wx.qlogo.cn/mmopen/wibbRT31wkCR4W9XNicL2h2pgaLepmrmEsXbWKbV0v9ugtdibibDgR1ybONiaWFtVeVtYWGWhObRiaiaicMgw8zat8Y5p6YzQbjdstE2/0");
		redisOperationService.setUserInfo(loginToken, userInfo);
		userInfo.setHasClub(0);
		userInfo.setIsBindPhone(0);
		userInfo.setIsRealNameCert(0);
		userInfo.setRoomCardNum(10);
		result.setData(userInfo);
		return result;
	}
	
	public void entryHall(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = null;
		result = new Result();
		result.setMsgType(MsgTypeEnum.entryHall.msgType);
		Integer playerId = request.getMsg().getPlayerId();
		/**将channel与playerId进行映射*/
		channelContainer.addChannel(ctx, playerId);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
		notice(ctx, request, userInfo);
	}
	
	public void createRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		/**校验创建房间的开关是否打开，如果没打开，就提示用户；此时可能是需要升级，暂停服务*/
		if (!redisOperationService.isCreateRoomFuseOpen()) {
			throw new BusinessException(ExceptionEnum.SYSTEM_UPGRADE);
		}
		Result result = null;
		BaseMsg msg = request.getMsg();
		
		RedisRelaModel redisRelaModel = redisOperationService.getRoomIdGameTypeByPlayerId(msg.getPlayerId());
		if (redisRelaModel != null) {
			throw new BusinessException(ExceptionEnum.ALREADY_IN_ROOM.index, ExceptionEnum.ALREADY_IN_ROOM.description + ",房间号：" + redisRelaModel.getRoomId());
		}
		/**校验房卡数量是否足够*/
		Integer clubId = redisOperationService.getClubIdByPlayerId(msg.getPlayerId());
		if (redisOperationService.isLoginFuseOpen()) {
			Integer roomCardCheckPlayerId = userInfo.getPlayerId();
			/**如果玩家是从俱乐部牌桌创建房间的，则只需要扣老板房卡*/
			if (clubId != null) {
				GameQuery gameQuery = new GameQuery();
				gameQuery.setClubId(clubId);
				List<GameModel> gmList = gameDao.getProxyClubs(gameQuery);
				roomCardCheckPlayerId = gmList.get(0).getPlayerId();
			}
			commonManager.roomCardCheck(roomCardCheckPlayerId, request.getGameType(), msg.getPayType(), msg.getTotalGames());
		}
		
		Integer roomId = GameUtil.genRoomId();
		int i = 0;
		while(i < 3){
			/**如果不存在则跳出循环，此房间号可以使用*/
			if (!redisOperationService.isRoomIdExist(roomId)) {
				break;
			}
			/**如果此房间号存在则重新生成*/
			roomId = GameUtil.genRoomId();
			i++;
			if (i >= 3) {
				throw new BusinessException(ExceptionEnum.GEN_ROOM_ID_FAIL);
			}
		}
		/**将当前房间号设置到userInfo中*/
		userInfo.setRoomId(roomId);
		redisOperationService.setUserInfo(request.getToken(), userInfo);
		
		/**doCreateRoom抽象方法由具体实现类去实现*/
		BaseRoomInfo roomInfo = doCreateRoom(ctx, request, userInfo);
		
		/**组装房间对象*/
		roomInfo.setRoomId(roomId);
		/**设置此房间唯一标示*/
		roomInfo.setRoomUuid(SnowflakeIdGenerator.idWorker.nextId());
		roomInfo.setRoomOwnerId(msg.getPlayerId());
		roomInfo.setPayType(msg.getPayType());
		roomInfo.setTotalGames(msg.getTotalGames());
		roomInfo.setPlayerNumLimit(msg.getPlayerNumLimit()==null?12:msg.getPlayerNumLimit());
		roomInfo.setCurGame(0);
		roomInfo.setStatus(RoomStatusEnum.justBegin.status);
		roomInfo.setServerIp(Constant.localIp);
		Date date = new Date();
		roomInfo.setCreateTime(date);
		roomInfo.setUpdateTime(date);
		
		/**如果玩家是从俱乐部创建的房间，则设置俱乐部id与房间id列表对应关系*/
		if (clubId != null) {
			redisOperationService.setClubIdRoomId(clubId, roomId);
			roomInfo.setClubId(clubId);
			roomInfo.setTableNum(msg.getTableNum());
		}
		List playerList = roomInfo.getPlayerList();
		BasePlayerInfo playerInfo = (BasePlayerInfo)playerList.get(0);
		playerInfo.setPlayerId(msg.getPlayerId());
		playerInfo.setLevel(1);
		playerInfo.setOrder(1);
		playerInfo.setStatus(PlayerStatusEnum.notReady.status);
		playerInfo.setOnlineStatus(OnlineStatusEnum.online.status);
		playerInfo.setRoomCardNum(10);
		playerInfo.setWinTimes(0);
		playerInfo.setLoseTimes(0);
		/**设置地理位置信息*/
		playerInfo.setAddress(userInfo.getAddress());
		playerInfo.setX(userInfo.getX());
		playerInfo.setY(userInfo.getY());
		/**设置当前用户ip*/
		playerInfo.setIp(userInfo.getRemoteIp());
		playerInfo.setNickName(userInfo.getNickName());
		playerInfo.setHeadImgUrl(userInfo.getHeadImgUrl());
		/**概率控制*/
		playerInfo.setWinProbability(userInfo.getWinProbability());
		playerInfo.setSex(userInfo.getSex());
		redisOperationService.setRoomIdGameTypeUpdateTime(roomId, request.getGameType(), new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		redisOperationService.setPlayerIdRoomIdGameType(userInfo.getPlayerId(), roomId, request.getGameType());
		/**设置clubId+tableNum 与 roomId的映射关系*/
		if (clubId != null) {
			redisOperationService.setClubIdTableNumRoomId(clubId, msg.getTableNum(), roomId);
		}
		
		/**设置返回信息*/
		result = new Result();
		result.setMsgType(MsgTypeEnum.createRoom.msgType);
		result.setGameType(request.getGameType());
		result.setData(roomInfo);
		channelContainer.sendTextMsgByPlayerIds(result, userInfo.getPlayerId());
		/**设置房间锁，此房间的请求排队进入*/
		RoomLockContainer.setLockByRoomId(roomId, new ReentrantLock());
		/**如果是从俱乐部创建的房间,通知其他进入俱乐部的玩家刷新牌桌列表*/
		noticeAllClubPlayerTablePlayerNum(null, clubId);
		
	}
	
	private void noticeAllClubPlayerTablePlayerNum(Integer playerId, Integer clubId){
		if (clubId == null) {
			return;
		}
		Result result = new Result();
		result.setGameType(GameTypeEnum.common.gameType);
		result.setMsgType(MsgTypeEnum.getClubTables.msgType);
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		List<GameModel> tableList = gameDao.getClubTables(gameQuery);
		if (CollectionUtils.isEmpty(tableList)) {
			return;
		}
		for(GameModel model : tableList){
			Integer roomId = redisOperationService.getRoomIdByClubIdTableNum(clubId, model.getTableNum());
			log.info("clubId:" + clubId + ",tableNum:" + model.getTableNum() + ",roomId:" + roomId);
			if (roomId != null) {
				UserInfo userInfo = new UserInfo();
				userInfo.setRoomId(roomId);
				BaseRoomInfo roomInfo = getRoomInfo(null, null, userInfo);
				List playerList = roomInfo.getPlayerList();
				int size = playerList.size();
				int num = 0;
				for(int i = 0; i < size; i++){
					BasePlayerInfo playerInfo = (BasePlayerInfo)playerList.get(i);
					if (redisOperationService.getRoomIdGameTypeByPlayerId(playerInfo.getPlayerId()) != null) {
						num++;
					}
				}
				model.setPlayerNum(num);
			}else{
				model.setPlayerNum(0);
			}
		}
		result.setData(tableList);
		if (playerId != null) {
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		List<Integer> playerIds = redisOperationService.getPlayerIdsByClubId(clubId);
		log.info("桌牌人数更新的玩家列表：" + JsonUtil.toJson(playerIds));
		channelContainer.sendTextMsgByPlayerIds(result, playerIds);
		
	}
	
	public void replaceCreateRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		/**校验创建房间的开关是否打开，如果没打开，就提示用户；此时可能是需要升级，暂停服务*/
		if (!redisOperationService.isCreateRoomFuseOpen()) {
			throw new BusinessException(ExceptionEnum.SYSTEM_UPGRADE);
		}
		Result result = null;
		BaseMsg msg = request.getMsg();
		
		RedisRelaModel redisRelaModel = redisOperationService.getRoomIdGameTypeByPlayerId(msg.getPlayerId());
		if (redisRelaModel != null) {
			throw new BusinessException(ExceptionEnum.ALREADY_IN_ROOM.index, ExceptionEnum.ALREADY_IN_ROOM.description + ",房间号：" + redisRelaModel.getRoomId());
		}
		/**校验房卡数量是否足够*/
		//TODO
		if (redisOperationService.isLoginFuseOpen()) {
			commonManager.roomCardCheck(userInfo.getPlayerId(), request.getGameType(), msg.getPayType(), msg.getTotalGames());
		}
		
		Integer roomId = GameUtil.genRoomId();
		int i = 0;
		while(i < 3){
			/**如果不存在则跳出循环，此房间号可以使用*/
			if (!redisOperationService.isRoomIdExist(roomId)) {
				break;
			}
			/**如果此房间号存在则重新生成*/
			roomId = GameUtil.genRoomId();
			i++;
			if (i >= 3) {
				throw new BusinessException(ExceptionEnum.GEN_ROOM_ID_FAIL);
			}
		}
		/**将当前房间号设置到userInfo中*/
		userInfo.setRoomId(roomId);
		redisOperationService.setUserInfo(request.getToken(), userInfo);
		
		/**doCreateRoom抽象方法由具体实现类去实现*/
		BaseRoomInfo roomInfo = doCreateRoom(ctx, request, userInfo);
		
		/**组装房间对象*/
		roomInfo.setRoomId(roomId);
		/**设置此房间唯一标示*/
		roomInfo.setRoomUuid(SnowflakeIdGenerator.idWorker.nextId());
		roomInfo.setRoomOwnerId(msg.getPlayerId());
		roomInfo.setPayType(msg.getPayType());
		roomInfo.setTotalGames(msg.getTotalGames());
		roomInfo.setPlayerNumLimit(msg.getPlayerNumLimit()==null?12:msg.getPlayerNumLimit());
		roomInfo.setCurGame(0);
		roomInfo.setStatus(RoomStatusEnum.justBegin.status);
		roomInfo.setServerIp(Constant.localIp);
		Date date = new Date();
		roomInfo.setCreateTime(date);
		roomInfo.setUpdateTime(date);
		
		Integer clubId = redisOperationService.getClubIdByPlayerId(msg.getPlayerId());
		/**如果玩家是从俱乐部创建的房间，则设置俱乐部id与房间id对应关系*/
		if (clubId != null) {
			redisOperationService.setClubIdRoomId(clubId, roomId);
			roomInfo.setClubId(clubId);
		}
		
		redisOperationService.setRoomIdGameTypeUpdateTime(roomId, request.getGameType(), new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
//		redisOperationService.setPlayerIdRoomIdGameType(userInfo.getPlayerId(), roomId, request.getGameType());
		
		/**设置返回信息*/
		result = new Result();
		result.setMsgType(MsgTypeEnum.replaceCreateRoom.msgType);
		result.setGameType(request.getGameType());
		result.setData(roomInfo);
		channelContainer.sendTextMsgByPlayerIds(result, userInfo.getPlayerId());
		/**设置房间锁，此房间的请求排队进入*/
		RoomLockContainer.setLockByRoomId(roomId, new ReentrantLock());
		
	}
	
	public abstract BaseRoomInfo doCreateRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo);
	
	public void entryRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = null;
		BaseMsg msg = request.getMsg();
		Integer playerId = userInfo.getPlayerId();
		/**进入房间的时候，房间号参数是前台传过来的，所以不能从userInfo里面取得*/
		Integer roomId = msg.getRoomId();
		/**参数为空*/
		if (roomId == null) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		if (!redisOperationService.isRoomIdExist(roomId)) {
			throw new BusinessException(ExceptionEnum.ROOM_ID_NOT_EXIST);
		}
		/**先获取房间信息，判断此玩家是否在房间里面，如果在房间里面就走刷新接口*/
		userInfo.setRoomId(roomId);
		BaseRoomInfo roomInfo = getRoomInfo(ctx, request, userInfo);
		List playerList = roomInfo.getPlayerList();
		boolean isExist = false;
		for(int i = 0; i < playerList.size(); i++ ){
			BasePlayerInfo tempPlayerInfo = (BasePlayerInfo)playerList.get(i);
			/**如果加入的玩家id已经存在*/
			if (playerId.equals(tempPlayerInfo.getPlayerId())) {
				isExist = true;
				break;
			}
		}
		/**如果申请加入房间的玩家已经存在房间中，则只需要走刷新接口*/
		if (isExist) {
			
			refreshRoom(ctx, request, userInfo);
			return;
		}
		/**房间里面是否有clubId，如果有则需要校验此玩家是否属于此俱乐部*/
		if (roomInfo.getClubId() != null) {
			if (redisOperationService.getClubIdByPlayerId(playerId) == null) {
				throw new BusinessException(ExceptionEnum.NOT_IN_CLUB);
			}
		}
		
		/**如果不在房间里面就走加入房间*/
		roomInfo = doEntryRoom(ctx, request, userInfo);
		playerList = roomInfo.getPlayerList();
		int size = playerList.size();
		/**如果是麻将（金花和牛牛除外），那么游戏已经开始，则不允许再加入 add by liujinfengnew*/
		if (roomInfo.getGameType().equals(GameTypeEnum.mj.gameType)) {
			if (size >= 5) {
				throw new BusinessException(ExceptionEnum.EXCEED_MAX_PLAYER_NUM);
			}
			if (roomInfo.getStatus() > RoomStatusEnum.justBegin.status) {
				throw new BusinessException(ExceptionEnum.NOT_IN_READY_STATUS);
			}
		}
		if (redisOperationService.isLoginFuseOpen()) {
			/**如果是aa支付，则校验房卡数量是否足够*/
			if (PayTypeEnum.AAPay.type.equals(roomInfo.getPayType())) {
				commonManager.roomCardCheck(userInfo.getPlayerId(), request.getGameType(), roomInfo.getPayType(), roomInfo.getTotalGames());
			}
		}
		/**最多12个玩家*/
		if (size >= 12) {
			throw new BusinessException(ExceptionEnum.EXCEED_MAX_PLAYER_NUM);
		}
		userInfo.setRoomId(roomId);
		redisOperationService.setUserInfo(request.getToken(), userInfo);
		
		
		/**取list最后一个，即为本次加入的玩家，设置公共信息*/
		BasePlayerInfo playerInfo = (BasePlayerInfo)playerList.get(playerList.size() - 1);
		playerInfo.setPlayerId(userInfo.getPlayerId());
		playerInfo.setNickName(userInfo.getNickName());
		playerInfo.setHeadImgUrl(userInfo.getHeadImgUrl());
		playerInfo.setLevel(1);
		/***/
		BasePlayerInfo lastPlayerInfo = (BasePlayerInfo)playerList.get(playerList.size() - 2);
		playerInfo.setOrder(lastPlayerInfo.getOrder() + 1);
		/**加入房间的时候，需要判断当前房间的状态，确定新加入的玩家应该是什么状态*/
		if (roomInfo.getStatus().equals(RoomStatusEnum.justBegin.status) ) {//刚开始准备
			playerInfo.setStatus(PlayerStatusEnum.notReady.status);
		}else if(roomInfo.getStatus().equals(RoomStatusEnum.curGameOver.status) ){//小局结束
			playerInfo.setStatus(PlayerStatusEnum.notReady.status);
		}else if(roomInfo.getStatus().equals(RoomStatusEnum.totalGameOver.status) ){//一圈结束
			throw new BusinessException(ExceptionEnum.TOTAL_GAME_OVER);
		}else{//其他状态
			playerInfo.setStatus(PlayerStatusEnum.observer.status);
		}
		
		playerInfo.setOnlineStatus(OnlineStatusEnum.online.status);
		playerInfo.setRoomCardNum(10);
		playerInfo.setWinTimes(0);
		playerInfo.setLoseTimes(0);
		playerInfo.setIp(userInfo.getRemoteIp());
		/**设置地理位置信息*/
		playerInfo.setAddress(userInfo.getAddress());
		playerInfo.setX(userInfo.getX());
		playerInfo.setY(userInfo.getY());
		/**概率控制*/
		playerInfo.setWinProbability(userInfo.getWinProbability());
		playerInfo.setSex(userInfo.getSex());
		roomInfo.setUpdateTime(new Date());
		/**计算当前进入的玩家和其他玩家间的距离*/
		for(int i = size - 2; i >= 0; i-- ){
			BasePlayerInfo tempPlayerInfo = (BasePlayerInfo)playerList.get(i);
			String distance = GameUtil.getLatLngDistance(playerInfo, tempPlayerInfo);
			if (StringUtils.isNotBlank(distance)) {
				String flag = "0";
				Double realDistance = 0.0;
				if (!distance.contains("km")) {
					realDistance = Double.valueOf(distance.substring(0, distance.length() - 1));
					if (realDistance < 20) {
						flag = "1";
					}
				}
				String key = null;
				if (playerInfo.getPlayerId() < tempPlayerInfo.getPlayerId()) {
					key = playerInfo.getPlayerId() + "_" + tempPlayerInfo.getPlayerId();
					
				}else{
					key = tempPlayerInfo.getPlayerId() + "_" + playerInfo.getPlayerId();
				}
				roomInfo.getDistanceMap().put(key, distance + "_" + flag);
			}
			
//			String key = null;
//			if (playerInfo.getPlayerId() < tempPlayerInfo.getPlayerId()) {
//				key = playerInfo.getPlayerId() + "_" + tempPlayerInfo.getPlayerId();
//			}else{
//				key = tempPlayerInfo.getPlayerId() + "_" + playerInfo.getPlayerId();
//			}
//			roomInfo.getDistanceMap().put(key, "10m_1");
		}
		redisOperationService.setRoomIdGameTypeUpdateTime(roomId, request.getGameType(), new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		redisOperationService.setPlayerIdRoomIdGameType(userInfo.getPlayerId(), roomId, request.getGameType());
		
		result = new Result();
		/**如果是麻将，则走正常逻辑返回房间信息*/
		if (roomInfo.getGameType().equals(GameTypeEnum.mj.gameType)) {
			result.setGameType(request.getGameType());
			result.setMsgType(MsgTypeEnum.entryRoom.msgType);
			result.setData(roomInfo);
			/**给此房间中的所有玩家发送消息*/
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
		}else{/**如果是炸金花或者斗牛，由于需要玩家随时可以加入，所以需要通过刷新接口给每个玩家返回房间信息*/
			refreshRoomForAllPlayer(roomInfo);
		}
		
		noticeAllClubPlayerTablePlayerNum(null, roomInfo.getClubId());
		
	}
	
	public void refreshRoomForAllPlayer(BaseRoomInfo roomInfo){
		Result result = new Result();
		result.setGameType(roomInfo.getGameType());
		result.setMsgType(MsgTypeEnum.refreshRoom.msgType);
		List playerList = roomInfo.getPlayerList();
		int size = playerList.size();
		UserInfo userInfo = new UserInfo();
		BasePlayerInfo playerInfo = null;
		for(int i = 0; i < size; i++){
			playerInfo = (BasePlayerInfo)playerList.get(i);
			userInfo.setRoomId(roomInfo.getRoomId());
			userInfo.setPlayerId(playerInfo.getPlayerId());
			/**复用刷新接口*/
			List<BaseRoomInfo> roomInfoList = doRefreshRoom(null, null, userInfo);
			BaseRoomInfo returnRoomInfo = roomInfoList.get(1);
			result.setData(returnRoomInfo);
			/**返回给当前玩家刷新信息*/
			channelContainer.sendTextMsgByPlayerIds(result, playerInfo.getPlayerId());
		}
	}
	
	public abstract BaseRoomInfo doEntryRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo);
	
	public void dissolveRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		Integer playerId = userInfo.getPlayerId();
		Integer roomId = userInfo.getRoomId();
		BaseRoomInfo roomInfo = getRoomInfo(ctx, request, userInfo);
		
		List playerList = roomInfo.getPlayerList();
		if (!GameUtil.isExistPlayerInRoom(playerId, playerList)) {
			throw new BusinessException(ExceptionEnum.PLAYER_NOT_IN_ROOM);
		}
		GameUtil.setDissolveStatus(playerList, playerId, DissolveStatusEnum.agree);
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		redisOperationService.setRoomIdGameTypeUpdateTime(roomId, new Date());
		int size = playerList.size();
		if (size == 1) {
			/**解散房间*/
			redisOperationService.cleanPlayerAndRoomInfo(roomId, GameUtil.getPlayerIdStrArr(playerList));
			if (roomInfo.getClubId() != null) {
				redisOperationService.delRoomIdByClubIdTableNum(roomInfo.getClubId(), roomInfo.getTableNum());
				log.info("解散俱乐部房间后，删除映射关系clubId:" + roomInfo.getClubId() + ",tableNum:" + roomInfo.getTableNum());
				
			}
			/**将用户缓存信息里面的roomId设置为null*/
			userInfo.setRoomId(null);
			redisOperationService.setUserInfo(request.getToken(), userInfo);
			result.setMsgType(MsgTypeEnum.successDissolveRoom.msgType);
			data.put("roomId", roomId);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			noticeAllClubPlayerTablePlayerNum(null, roomInfo.getClubId());
			return;
		}
		
		Integer playerStatus = GameUtil.getPlayerStatus(playerList, playerId);
		/**如果玩家的状态是观察者,并且一局都没有玩过，则随时可以退出*/
		if (playerStatus.equals(PlayerStatusEnum.observer.status) && GameUtil.getPlayedCountByPlayerId(playerList, playerId) < 1) {
			/**删除玩家*/
			GameUtil.removePlayer(playerList, playerId);
			redisOperationService.cleanPlayerAndRoomInfoForSignout(roomId, String.valueOf(playerId));
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			/**将用户缓存信息里面的roomId设置为null*/
			userInfo.setRoomId(null);
			redisOperationService.setUserInfo(request.getToken(), userInfo);
			result.setMsgType(MsgTypeEnum.entryHall.msgType);
			/**当前玩家返回大厅*/
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			/**其他玩家通知刷新*/
			refreshRoomForAllPlayer(roomInfo);
			return;
		}
		/**房间状态是最开始准备阶段*/
		if (roomInfo.getStatus().equals(RoomStatusEnum.justBegin.status)) {
			
			/**如果在游戏最开始准备阶段退出的是庄家（即房主），则直接解散房间*/
			if (roomInfo.getRoomBankerId().equals(playerId)) {
//				Integer tempId = GameUtil.getNextPlayerId(playerList, playerId);
//				roomInfo.setRoomOwnerId(tempId);
//				if (request.getGameType().equals(GameTypeEnum.jh.gameType)) {
//					roomInfo.setRoomBankerId(tempId);
//				}
				/**解散房间*/
				redisOperationService.cleanPlayerAndRoomInfo(roomId, GameUtil.getPlayerIdStrArr(playerList));
				if (roomInfo.getClubId() != null) {
					redisOperationService.delRoomIdByClubIdTableNum(roomInfo.getClubId(), roomInfo.getTableNum());
				}
				/**将用户缓存信息里面的roomId设置为null*/
				userInfo.setRoomId(null);
				redisOperationService.setUserInfo(request.getToken(), userInfo);
				result.setMsgType(MsgTypeEnum.successDissolveRoom.msgType);
				data.put("roomId", roomId);
				channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
				noticeAllClubPlayerTablePlayerNum(null, roomInfo.getClubId());
				return;
			}
			
			/**如果是非庄家玩家退出，则只是此玩家退出*/
			GameUtil.removePlayer(playerList, playerId);
			redisOperationService.cleanPlayerAndRoomInfoForSignout(roomId, String.valueOf(playerId));
			redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
			/**将用户缓存信息里面的roomId设置为null*/
			userInfo.setRoomId(null);
			redisOperationService.setUserInfo(request.getToken(), userInfo);
			result.setMsgType(MsgTypeEnum.successDissolveRoom.msgType);
			/**当前玩家返回大厅*/
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			/**其他玩家通知刷新*/
			refreshRoomForAllPlayer(roomInfo);
			noticeAllClubPlayerTablePlayerNum(null, roomInfo.getClubId());
			return;
		}
		/**设置解散房间标志位*/
		redisOperationService.setDissolveIpRoomIdTime(playerId, roomId, request.getGameType());
		result.setMsgType(MsgTypeEnum.dissolveRoom.msgType);
		data.put("roomId", roomId);
		data.put("playerId", playerId);
		RedisRelaModel model = redisOperationService.getDissolveIpRoomIdTime(roomId);
		data.put("playerList", GameUtil.getPList(playerList,playerId,model.getUpdateTime()));
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
	}
	
	
	public void agreeDissolveRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		Integer roomId = msg.getRoomId();
		BaseRoomInfo roomInfo = getRoomInfo(ctx, request, userInfo);
		if (roomInfo == null) {
			log.warn("房间信息不存在");
			channelContainer.sendTextMsgByPlayerIds(new Result(0, MsgTypeEnum.entryHall.msgType), msg.getPlayerId());
			return;
		}
		List playerList = roomInfo.getPlayerList();
		if (!GameUtil.isExistPlayerInRoom(msg.getPlayerId(), playerList)) {
			throw new BusinessException(ExceptionEnum.PLAYER_NOT_IN_ROOM);
		}
		int size = playerList.size();
		int agreeDissolveCount = 0;
		for(int i = 0; i < size; i++){
			BasePlayerInfo player = (BasePlayerInfo)playerList.get(i);
			if (player.getPlayerId().equals(msg.getPlayerId())) {
				player.setDissolveStatus(DissolveStatusEnum.agree.status);
			}
			if (DissolveStatusEnum.agree.status.equals(player.getDissolveStatus())) {
				agreeDissolveCount++;
			}
		}
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdGameTypeUpdateTime(roomId, new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		/**如果大部分人同意，则推送解散消息并解散房间*/
		if (agreeDissolveCount >= (playerList.size()/2 + 1)) {
			/**解散房间*/
			redisOperationService.cleanPlayerAndRoomInfo(roomId, GameUtil.getPlayerIdStrArr(playerList));
			if (roomInfo.getClubId() != null) {
				redisOperationService.delRoomIdByClubIdTableNum(roomInfo.getClubId(), roomInfo.getTableNum());
				log.info("解散俱乐部房间后，删除映射关系clubId:" + roomInfo.getClubId() + ",tableNum:" + roomInfo.getTableNum());
			}
			/**删除解散标志位*/
			redisOperationService.delDissolveIpRoomIdTime(roomId);
			
			/**解散后需要进行结算*/
			data.put("roomInfo", roomInfo);
			result.setMsgType(MsgTypeEnum.successDissolveRoom.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
			
			noticeAllClubPlayerTablePlayerNum(null, roomInfo.getClubId());
			try {
				commonManager.addUserRecord(roomInfo);
			} catch (Exception e) {
				log.error("解散房间时添加记录失败", e);
			}
			return ;
		}
		result.setMsgType(MsgTypeEnum.agreeDissolveRoom.msgType);
		data.put("roomId", roomId);
		data.put("playerId", msg.getPlayerId());
		RedisRelaModel model = redisOperationService.getDissolveIpRoomIdTime(roomId);
		if (model != null) {
			data.put("playerList", GameUtil.getPList(playerList,model.getPlayerId(),model.getUpdateTime()));
		}
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
	}
	
	public void disagreeDissolveRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		Integer roomId = msg.getRoomId();
		BaseRoomInfo roomInfo = getRoomInfo(ctx, request, userInfo);
		if (null == roomInfo) {
			log.warn("房间信息不存在");
			channelContainer.sendTextMsgByPlayerIds(new Result(0, MsgTypeEnum.entryHall.msgType), msg.getPlayerId());
			return;
		}
		List playerList = roomInfo.getPlayerList();
		if (!GameUtil.isExistPlayerInRoom(msg.getPlayerId(), playerList)) {
			throw new BusinessException(ExceptionEnum.PLAYER_NOT_IN_ROOM);
		}
		int disagreeDissolveCount = 0;
		int size = playerList.size();
		for(int i = 0; i < size; i++){
			BasePlayerInfo player = (BasePlayerInfo)playerList.get(i);
			if (player.getPlayerId().equals(msg.getPlayerId())) {
				player.setDissolveStatus(DissolveStatusEnum.disagree.status);
			}
			if (DissolveStatusEnum.disagree.status.equals(player.getDissolveStatus())) {
				disagreeDissolveCount++;
			}
		}
		roomInfo.setUpdateTime(new Date());
		RedisRelaModel model = redisOperationService.getDissolveIpRoomIdTime(roomId);
		if (model != null) {
			List<Map<String, Object>> disPlayerList = GameUtil.getPList(playerList,model.getPlayerId(),model.getUpdateTime());
			data.put("playerList", disPlayerList);
		}
		
		/**如果一半人不同意，则不能解散房间*/
		if (disagreeDissolveCount >= (playerList.size()/2)) {
			/**删除解散房间标志位*/
			redisOperationService.delDissolveIpRoomIdTime(roomId);
			/**将玩家解散标志位都置为0*/
			GameUtil.resetPlayerDissolve(playerList);
		}
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		result.setMsgType(MsgTypeEnum.disagreeDissolveRoom.msgType);
		data.put("roomId", roomId);
		data.put("playerId", msg.getPlayerId());
		
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
		redisOperationService.setRoomIdGameTypeUpdateTime(roomId, new Date());
	}
	
	public void delRoomConfirmBeforeReturnHall(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		Integer roomId = msg.getRoomId();
		BaseRoomInfo roomInfo = getRoomInfo(ctx, request, userInfo);
		if (null == roomInfo) {
			throw new BusinessException(ExceptionEnum.ROOM_ID_NOT_EXIST);
		}
		List playerList = roomInfo.getPlayerList();
		if (!GameUtil.isExistPlayerInRoom(msg.getPlayerId(), playerList)) {
			throw new BusinessException(ExceptionEnum.PLAYER_NOT_IN_ROOM);
		}
		
		int agreeDissolveCount = 0;
		int size = playerList.size();
		for(int i = 0; i < size; i++){
			BasePlayerInfo player = (BasePlayerInfo)playerList.get(i);
			if (player.getPlayerId().equals(msg.getPlayerId())) {
				player.setDissolveStatus(DissolveStatusEnum.agree.status);
			}
			if (player.getDissolveStatus().equals(DissolveStatusEnum.agree.status)) {
				agreeDissolveCount++;
			}
		}
		roomInfo.setUpdateTime(new Date());
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		/**如果所有人都有确认消息，则解散房间*/
		if (agreeDissolveCount >= playerList.size()) {
			/**解散房间*/
			redisOperationService.cleanPlayerAndRoomInfo(roomId, GameUtil.getPlayerIdStrArr(playerList));
			if (roomInfo.getClubId() != null) {
				redisOperationService.delRoomIdByClubIdTableNum(roomInfo.getClubId(), roomInfo.getTableNum());
				log.info("解散俱乐部房间后，删除映射关系clubId:" + roomInfo.getClubId() + ",tableNum:" + roomInfo.getTableNum());
				
			}
			noticeAllClubPlayerTablePlayerNum(null, roomInfo.getClubId());
		}else{/**如果只有部分人确认，则只删除当前玩家的标记*/
			redisOperationService.hdelOfflinePlayerIdRoomIdGameTypeTime(msg.getPlayerId());
			redisOperationService.hdelPlayerIdRoomIdGameType(msg.getPlayerId());
			noticeAllClubPlayerTablePlayerNum(msg.getPlayerId(), roomInfo.getClubId());
		}
		/**通知玩家返回大厅*/
		result.setMsgType(MsgTypeEnum.delRoomConfirmBeforeReturnHall.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, msg.getPlayerId());
		/**将roomId从用户信息中去除*/
		userInfo.setRoomId(null);
		redisOperationService.setUserInfo(request.getToken(), userInfo);
	}
	
	public void chatMsg(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		Integer roomId = msg.getRoomId();
		BaseRoomInfo roomInfo = getRoomInfo(ctx, request, userInfo);
		if (null == roomInfo) {
			throw new BusinessException(ExceptionEnum.ROOM_ID_NOT_EXIST);
		}
		List playerList = roomInfo.getPlayerList();
		if (!GameUtil.isExistPlayerInRoom(msg.getPlayerId(), playerList)) {
			throw new BusinessException(ExceptionEnum.PLAYER_NOT_IN_ROOM);
		}
		result.setMsgType(MsgTypeEnum.chatMsg.msgType);
		if (ChatTypeEnum.specialEmotion.type == msg.getChatType()) {
			data.put("playerId", msg.getPlayerId());
			data.put("otherPlayerId", msg.getOtherPlayerId());
			data.put("chatMsg", msg.getChatMsg());
			data.put("chatType", msg.getChatType());
			List<Integer> playerIdList = new ArrayList<Integer>();
			Integer[] playerIdArr = new Integer[2];
			playerIdArr[0] = msg.getPlayerId();
			playerIdArr[1] = msg.getOtherPlayerId();
			channelContainer.sendTextMsgByPlayerIds(result, playerIdArr);
			return;
		}else if(ChatTypeEnum.voiceChat.type == msg.getChatType()){
			data.put("playerId", msg.getPlayerId());
			data.put("chatMsg", msg.getChatMsg());
			data.put("chatType", msg.getChatType());
			channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, msg.getPlayerId()));
			return;
		}
		
		data.put("playerId", msg.getPlayerId());
		data.put("chatMsg", msg.getChatMsg());
		data.put("chatType", msg.getChatType());
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArr(playerList));
		redisOperationService.setRoomIdGameTypeUpdateTime(roomId, new Date());
	}
	
	public void syncPlayerLocation(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.syncPlayerLocation.msgType);
		BaseMsg msg = request.getMsg();
		userInfo.setAddress(msg.getAddress());
		userInfo.setX(msg.getX());
		userInfo.setY(msg.getY());
		redisOperationService.setUserInfo(request.getToken(), userInfo);
	}
	
	public void queryPlayerInfo(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		Integer roomId = msg.getRoomId();
		BaseRoomInfo roomInfo = getRoomInfo(ctx, request, userInfo);
		if (null == roomInfo) {
			throw new BusinessException(ExceptionEnum.ROOM_ID_NOT_EXIST);
		}
		List playerList = roomInfo.getPlayerList();
		if (!GameUtil.isExistPlayerInRoom(msg.getPlayerId(), playerList)) {
			throw new BusinessException(ExceptionEnum.PLAYER_NOT_IN_ROOM);
		}
		Integer otherPlayerId = msg.getOtherPlayerId();
		Integer playerId = msg.getPlayerId();
		BasePlayerInfo otherPlayer = null;
		BasePlayerInfo curPlayer = null;
		int size = playerList.size();
		for(int i = 0; i < size; i++){
			BasePlayerInfo player = (BasePlayerInfo)playerList.get(i);
			if (player.getPlayerId().equals(otherPlayerId)) {
				otherPlayer = player;
			}else if(player.getPlayerId().equals(playerId)){
				curPlayer = player;
			}
		}
		if (otherPlayer != null) {
			data.put("playerId", otherPlayer.getPlayerId());
			data.put("nickName", otherPlayer.getNickName());
			data.put("headImgUrl", otherPlayer.getHeadImgUrl());
			data.put("address", otherPlayer.getAddress());
			String distance = GameUtil.getLatLngDistance(curPlayer, otherPlayer);
			data.put("distance", distance);
			data.put("ip", otherPlayer.getIp());
		}else{
			data.put("playerId", curPlayer.getPlayerId());
			data.put("nickName", curPlayer.getNickName());
			data.put("headImgUrl", curPlayer.getHeadImgUrl());
			data.put("address", curPlayer.getAddress());
			data.put("ip", curPlayer.getIp());
		}
		result.setMsgType(MsgTypeEnum.queryPlayerInfo.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, msg.getPlayerId());
		redisOperationService.setRoomIdGameTypeUpdateTime(roomId, request.getGameType(), new Date());
	}
	
	public void getAllPlayerDistance(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		result.setMsgType(MsgTypeEnum.getAllPlayerDistance.msgType);
		BaseRoomInfo roomInfo = getRoomInfo(ctx, request, userInfo);
		if (roomInfo != null) {
			result.setData(roomInfo.getDistanceMap());
		}
		channelContainer.sendTextMsgByPlayerIds(result, msg.getPlayerId());
		redisOperationService.setRoomIdGameTypeUpdateTime(msg.getRoomId(), request.getGameType(), new Date());
	}
	
	public void userRecord(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		Integer clubId = msg.getClubId();
		Integer timeFlag = msg.getTimeFlag();
		UserRecordModel qmodel = new UserRecordModel();
		qmodel.setGameType(request.getGameType());
		if (clubId != null) {
			qmodel.setClubId(clubId);
			Date date = new Date();
			qmodel.setStartTime(DateUtil.getNDaysBeforeStartTime(date, timeFlag));
			qmodel.setEndTime(DateUtil.getNDaysBeforeEndTime(date, timeFlag));
		}else{
			qmodel.setPlayerId(userInfo.getPlayerId());
		}
		log.info("=====战绩参数：" + JsonUtil.toJson(qmodel));
		List<UserRecordModel> list = commonManager.getUserRecord(qmodel);
		result.setMsgType(MsgTypeEnum.userRecord.msgType);
		result.setData(list);
		channelContainer.sendTextMsgByPlayerIds(result, msg.getPlayerId());
	}
	
	public void userRecordDetail(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		UserRecordModel qmodel = new UserRecordModel();
		qmodel.setRecordUuid(Long.valueOf(request.getMsg().getRecordUuid()));
		List<UserRecordModel> list = commonManager.getUserRecordDetail(qmodel);
		result.setMsgType(MsgTypeEnum.userRecordDetail.msgType);
		result.setData(list);
		channelContainer.sendTextMsgByPlayerIds(result, msg.getPlayerId());
	}
	
	public void playBack(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		if (StringUtils.isBlank(request.getMsg().getRecordDetailUuid())) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		Long recordDetailUuid = null;
		try {
			recordDetailUuid = Long.valueOf(request.getMsg().getRecordDetailUuid());
		} catch (NumberFormatException e) {
			throw new BusinessException(ExceptionEnum.PLAY_BACK_CODE_ERROR);
		}
		/**通过detailuuid查到uuid*/
		UserRecordModel model = new UserRecordModel();
		model.setRecordDetailUuid(recordDetailUuid);
		List<UserRecordModel> detailModelList = commonManager.getUserRecordDetail(model);
		if (CollectionUtils.isEmpty(detailModelList)) {
			throw new BusinessException(ExceptionEnum.PLAY_BACK_CODE_ERROR);
		}
		Long recordUuid = detailModelList.get(0).getRecordUuid();
		Integer curGame = detailModelList.get(0).getCurGame();
		/**再通过uuid查到房间remark*/
		model = commonManager.getRoomRemarkByUuid(recordUuid);
		if (model == null) {
			throw new BusinessException(ExceptionEnum.PLAY_BACK_CODE_ERROR);
		}
		String remark = model.getRemark();
		Map<String, Object> remarkMap = JsonUtil.toObject(remark, Map.class);
		if (remarkMap == null) {
			remarkMap = new HashMap<String, Object>();
		}
		remarkMap.put("roomId", model.getRoomId());
		remarkMap.put("gameType", model.getGameType());
		remarkMap.put("detailType", model.getDetailType());
		remarkMap.put("payType", model.getPayType());
		remarkMap.put("totalGames", model.getTotalGames());
		remarkMap.put("curGame", curGame);
		List<String> list = commonManager.getPlayBack(recordDetailUuid);
		result.setMsgType(MsgTypeEnum.playBack.msgType);
		data.put("remarkMap", remarkMap);
		data.put("playBackList", list);
		channelContainer.sendTextMsgByPlayerIds(result, msg.getPlayerId());
	}
	
	public static void main(String[] args) {
		String remark = "{\"code\":0,\"msgType\":208,\"gameType\":2,\"data\":{\"roomOwnerId\":861111,\"huangFanNum\":0,\"playerList\":[{\"headImgUrl\":\"http://wx.qlogo.cn/mmopen/wibbRT31wkCR4W9XNicL2h2pgaLepmrmEsXbWKbV0v9ugtdibibDgR1ybONiaWFtVeVtYWGWhObRiaiaicMgw8zat8Y5p6YzQbjdstE2/0\",\"nickName\":\"861111\",\"playerId\":861111,\"order\":1},{\"headImgUrl\":\"http://wx.qlogo.cn/mmopen/wibbRT31wkCR4W9XNicL2h2pgaLepmrmEsXbWKbV0v9ugtdibibDgR1ybONiaWFtVeVtYWGWhObRiaiaicMgw8zat8Y5p6YzQbjdstE2/0\",\"nickName\":\"840549\",\"playerId\":840549,\"order\":2},{\"headImgUrl\":\"http://wx.qlogo.cn/mmopen/wibbRT31wkCR4W9XNicL2h2pgaLepmrmEsXbWKbV0v9ugtdibibDgR1ybONiaWFtVeVtYWGWhObRiaiaicMgw8zat8Y5p6YzQbjdstE2/0\",\"nickName\":\"160950\",\"playerId\":160950,\"order\":3},{\"headImgUrl\":\"http://wx.qlogo.cn/mmopen/wibbRT31wkCR4W9XNicL2h2pgaLepmrmEsXbWKbV0v9ugtdibibDgR1ybONiaWFtVeVtYWGWhObRiaiaicMgw8zat8Y5p6YzQbjdstE2/0\",\"nickName\":\"209599\",\"playerId\":209599,\"order\":4}],\"roomBankerId\":861111,\"isCurGameKaiBao\":0,\"dices\":[5,5]}}";
		Map<String, Object> remarkMap = JsonUtil.toObject(remark, Map.class);
		System.out.println(JsonUtil.toJson(remarkMap));
	}

	public void userFeedback(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) {
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		UserFeedbackModel model = new UserFeedbackModel();
		model.setPlayerId(msg.getPlayerId());
		model.setMobilePhone(msg.getMobilePhone());
		model.setFeedBack(msg.getFeedBack());
		model.setType(msg.getFeedBackType());
		commonManager.insertFeedback(model);
		result.setMsgType(MsgTypeEnum.userFeedback.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, msg.getPlayerId());
	}
	
	public abstract BaseRoomInfo getRoomInfo(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo);
	
	public void ready(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){}
	
	public void refreshRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		
		BaseMsg msg = request.getMsg();
		Integer roomId = msg.getRoomId();
		Integer playerId = msg.getPlayerId();
		List<BaseRoomInfo> roomInfoList = doRefreshRoom(ctx, request, userInfo);
		BaseRoomInfo roomInfo = roomInfoList.get(0);
		BaseRoomInfo returnRoomInfo = roomInfoList.get(1);
		if (null == roomInfo) {
			log.warn("房间信息不存在");
			/**房间不存在，则需要删除离线用户与房间的关系标记，防止循环刷新，但是房间不存在*/
			redisOperationService.hdelOfflinePlayerIdRoomIdGameTypeTime(playerId);
			channelContainer.sendTextMsgByPlayerIds(new Result(0, MsgTypeEnum.entryHall.msgType), playerId);
			return;
		}
		List playerList = roomInfo.getPlayerList();
		if (!GameUtil.isExistPlayerInRoom(playerId, playerList)) {
			channelContainer.sendTextMsgByPlayerIds(new Result(0, MsgTypeEnum.entryHall.msgType), playerId);
			return;
		}
		result.setGameType(roomInfo.getGameType());
		result.setMsgType(MsgTypeEnum.refreshRoom.msgType);
		/**设置解散玩家列表，如果有*/
		RedisRelaModel model = redisOperationService.getDissolveIpRoomIdTime(roomId);
		if (model != null) {
			/**一圈结束的时候不返回解散列表*/
			if ((roomInfo.getStatus() != 4)&&(roomInfo.getStatus() != 6)) {
				returnRoomInfo.setDisList(GameUtil.getPList(playerList,model.getPlayerId(),model.getUpdateTime()));
			}
		}
		result.setData(returnRoomInfo);
		/**返回给当前玩家刷新信息*/
		redisOperationService.setPlayerIdRoomIdGameType(playerId, roomId, request.getGameType());
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
		
		
		/**设置当前玩家缓存中为在线状态*/
		GameUtil.setOnlineStatus(playerList, playerId, OnlineStatusEnum.online);
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		/**给其他的玩家发送当前玩家上线通知*/
		Result result1 = new Result();
		Map<String, Object> data1 = new HashMap<String, Object>();
		result1.setData(data1);
		data1.put("playerId", msg.getPlayerId());
		result1.setGameType(roomInfo.getGameType());
		result1.setMsgType(MsgTypeEnum.onlineNotice.msgType);
		channelContainer.sendTextMsgByPlayerIds(result1, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
		/**删除此玩家的离线标记*/
		redisOperationService.hdelOfflinePlayerIdRoomIdGameTypeTime(playerId);
		redisOperationService.setRoomIdGameTypeUpdateTime(roomId, new Date());
	}
	
	public abstract List<BaseRoomInfo> doRefreshRoom(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo);
	
	public void productList(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(GameTypeEnum.common.gameType);
		result.setMsgType(MsgTypeEnum.productList.msgType);
		commonManager.getProductList();
		result.setData(commonManager.getProductList());
		/**返回给当前玩家刷新信息*/
		channelContainer.sendTextMsgByPlayerIds(result, userInfo.getPlayerId());
	}
	
	public void bindProxy(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Integer proxyId = request.getMsg().getProxyId();
		if (proxyId == null) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		Integer proxyCount = commonManager.getProxyCountByProxyId(proxyId);
		if (proxyCount < 1) {
			throw new BusinessException(ExceptionEnum.PROXY_NOT_EXIST);
		}
		Integer proxyUserCount = commonManager.getProxyUserCountByPlayerId(userInfo.getPlayerId());
		if (proxyUserCount > 0) {
			throw new BusinessException(ExceptionEnum.HAS_BIND_PROXY);
		}
		commonManager.insertProxyUser(proxyId, userInfo.getPlayerId(), userInfo.getNickName());
		/**绑定代理后送15张房卡*/
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("addNum", 15);
		param.put("playerId", userInfo.getPlayerId());
		commonManager.addRoomCard(param);
		/**更新后再查询*/
		UserModel userModel = commonManager.getUserById(userInfo.getPlayerId());
		/**推送房卡更新消息*/
		roomCardNumUpdate(userModel.getRoomCardNum(), userInfo.getPlayerId());
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(GameTypeEnum.common.gameType);
		result.setMsgType(MsgTypeEnum.bindProxy.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, userInfo.getPlayerId());
	}
	
	public void checkBindProxy(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		if (!"sjsj".equals(Constant.curCompany)) {
			Integer proxyId = commonManager.getProxyIdByPlayerId(userInfo.getPlayerId());
			if (proxyId == null) {
				throw new BusinessException(ExceptionEnum.NEED_BIND_PROXY);
			}
		}
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(GameTypeEnum.common.gameType);
		result.setMsgType(MsgTypeEnum.checkBindProxy.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, userInfo.getPlayerId());
	}
	
	public void notice(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
        Result result = new Result();
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("noticeContent", Constant.noticeMsg);
        result.setData(data);
        result.setGameType(GameTypeEnum.common.gameType);
        result.setMsgType(MsgTypeEnum.notice.msgType);
        channelContainer.sendTextMsgByPlayerIds(result, userInfo.getPlayerId());
    }
	
	/**
	 *  微信预支付 统一下单入口(websocket协议)
	 * @param productId
	 * @param playerId
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public void unifiedOrder(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo) throws Exception{
		BaseMsg msg = request.getMsg();
		Integer productId = msg.getProductId();
		Integer playerId = userInfo.getPlayerId();
		String ip = userInfo.getRemoteIp();
		
		ProductModel productModel = commonManager.getProductById(productId);
		if (productModel == null) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		Long orderId = commonManager.insertOrder(playerId, productId, productModel.getRoomCardNum(), productModel.getPrice());
		SortedMap<String, Object> parameters = prepareOrder(ip, String.valueOf(orderId), productModel.getPrice(), productModel.getRemark());
		/**生成签名*/
		parameters.put("sign", PayCommonUtil.createSign(Charsets.UTF_8.toString(), parameters));
		/**生成xml格式字符串*/
		String requestXML = PayCommonUtil.getRequestXml(parameters);
		String responseStr = HttpUtil.httpsRequest(Constant.UNIFIED_ORDER_URL, "POST", requestXML);
		/**检验API返回的数据里面的签名是否合法，避免数据在传输的过程中被第三方篡改*/
		if (!PayCommonUtil.checkIsSignValidFromResponseString(responseStr)) {
			log.error("微信统一下单失败,签名可能被篡改 "+responseStr);
			throw new BusinessException(ExceptionEnum.UNIFIED_ORDER_FAIL);
		}
		/**解析结果 resultStr*/
		SortedMap<String, Object> resutlMap = XMLUtil.doXMLParse(responseStr);
		if (resutlMap != null && WeixinConstant.FAIL.equals(resutlMap.get("return_code"))) {
			log.error("微信统一下单失败,订单编号: " + orderId + " 失败原因:"+ resutlMap.get("return_msg"));
			throw new BusinessException(ExceptionEnum.UNIFIED_ORDER_FAIL);
		}
		/**获取到 prepayid*/
		/**商户系统先调用该接口在微信支付服务后台生成预支付交易单，返回正确的预支付交易回话标识后再在APP里面调起支付。*/
		SortedMap<String, Object> map = buildClientJson(resutlMap);
		map.put("outTradeNo", orderId);
		log.info("统一下定单成功 "+map.toString());
		
		Result result = new Result();
		result.setGameType(GameTypeEnum.common.gameType);
		result.setMsgType(MsgTypeEnum.unifiedOrder.msgType);
		result.setData(map);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 *  微信预支付 统一下单入口(http协议)
	 * @param productId
	 * @param playerId
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public Result unifiedOrder(Integer productId, Integer playerId, String ip) throws Exception{
		Result result = new Result();
		ProductModel productModel = commonManager.getProductById(productId);
		if (productModel == null) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		Long orderId = commonManager.insertOrder(playerId, productId, productModel.getRoomCardNum(), productModel.getPrice());
		SortedMap<String, Object> parameters = prepareOrder(ip, String.valueOf(orderId), productModel.getPrice(), productModel.getRemark());
		/**生成签名*/
		parameters.put("sign", PayCommonUtil.createSign(Charsets.UTF_8.toString(), parameters));
		/**生成xml格式字符串*/
		String requestXML = PayCommonUtil.getRequestXml(parameters);
		String responseStr = HttpUtil.httpsRequest(Constant.UNIFIED_ORDER_URL, "POST", requestXML);
		/**检验API返回的数据里面的签名是否合法，避免数据在传输的过程中被第三方篡改*/
		if (!PayCommonUtil.checkIsSignValidFromResponseString(responseStr)) {
			log.error("微信统一下单失败,签名可能被篡改 "+responseStr);
			throw new BusinessException(ExceptionEnum.UNIFIED_ORDER_FAIL);
		}
		/**解析结果 resultStr*/
		SortedMap<String, Object> resutlMap = XMLUtil.doXMLParse(responseStr);
		if (resutlMap != null && WeixinConstant.FAIL.equals(resutlMap.get("return_code"))) {
			log.error("微信统一下单失败,订单编号: " + orderId + " 失败原因:"+ resutlMap.get("return_msg"));
			throw new BusinessException(ExceptionEnum.UNIFIED_ORDER_FAIL);
		}
		/**获取到 prepayid*/
		/**商户系统先调用该接口在微信支付服务后台生成预支付交易单，返回正确的预支付交易回话标识后再在APP里面调起支付。*/
		SortedMap<String, Object> map = buildClientJson(resutlMap);
		map.put("outTradeNo", orderId);
		log.info("统一下定单成功 "+map.toString());
		result.setData(map);
		return result;
	}
	
	
	/**
	 * 微信回调告诉微信支付结果 注意：同样的通知可能会多次发送给此接口，注意处理重复的通知。
	 * 对于支付结果通知的内容做签名验证，防止数据泄漏导致出现“假通知”，造成资金损失。
	 * 
	 * @param params
	 * @return
	 */
	public String callback(String responseStr) {
		try {
			Map<String, Object> map = XMLUtil.doXMLParse(responseStr);
			/**校验签名 防止数据泄漏导致出现“假通知”，造成资金损失*/
			if (!PayCommonUtil.checkIsSignValidFromResponseString(responseStr)) {
				log.error("微信回调失败,签名可能被篡改 " + responseStr);
				return PayCommonUtil.setXML(WeixinConstant.FAIL, "invalid sign");
			}
			if (WeixinConstant.FAIL.equalsIgnoreCase(map.get("result_code").toString())) {
				log.error("微信回调失败的原因："+responseStr);
				return PayCommonUtil.setXML(WeixinConstant.FAIL, "weixin pay fail");
			}
			if (WeixinConstant.SUCCESS.equalsIgnoreCase(map.get("result_code")
					.toString())) {
				/**对数据库的操作,更新订单状态为已付款*/
				String outTradeNo = (String) map.get("out_trade_no");
				String transactionId = (String) map.get("transaction_id");
				String totlaFee = (String) map.get("total_fee");
				Integer totalPrice = Integer.valueOf(totlaFee);
				/**根据订单号查询订单信息*/
				OrderModel order = commonManager.getOderByOrderId(Long.valueOf(outTradeNo));
				Integer payStatus = order.getPayStatus();
				
				/**如果支付状态为已经支付，则说明此次回调为重复回调，直接返回成功*/
				if (PayStatusEnum.pay.type.equals(payStatus)) {
					return PayCommonUtil.setXML(WeixinConstant.SUCCESS, "OK");
				}
				Integer playerId = order.getPlayerId();
				Integer roomCardNum = commonManager.updateOrderAndUser(playerId, order.getRoomCardNum(), Long.valueOf(outTradeNo), transactionId, totalPrice);
				
				/**推送房卡更新消息*/
				roomCardNumUpdate(roomCardNum, playerId);
				
				/**告诉微信服务器，我收到信息了，不要在调用回调action了*/
				log.info("回调成功："+responseStr);
				return PayCommonUtil.setXML(WeixinConstant.SUCCESS, "OK");
			}
		} catch (Exception e) {
			log.error("回调异常" + e.getMessage());
			return PayCommonUtil.setXML(WeixinConstant.FAIL,"weixin pay server exception");
		}
		return PayCommonUtil.setXML(WeixinConstant.FAIL, "weixin pay fail");
	}
	
	public void roomCardNumUpdate( Integer roomCardNum, Integer playerId){
		/**推送房卡更新消息*/
		Result result = new Result();
		result.setMsgType(MsgTypeEnum.roomCardNumUpdate.msgType);
		result.setGameType(0);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("playerId", playerId);
		data.put("roomCardNum", roomCardNum);
		result.setData(data);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 * 生成订单信息
	 * 
	 * @param ip
	 * @param orderId
	 * @return
	 */
	private SortedMap<String, Object> prepareOrder(String ip, String orderId, int price, String productBody) {
		Map<String, Object> oparams = ImmutableMap.<String, Object> builder()
				.put("appid", Constant.APPID)// 服务号的应用号
				.put("body", productBody)// 商品描述
				.put("mch_id", Constant.MCH_ID)// 商户号 ？
				.put("nonce_str", PayCommonUtil.CreateNoncestr())// 16随机字符串(大小写字母加数字)
				.put("out_trade_no", orderId)// 商户订单号
				.put("total_fee", price)// 支付金额 单位分 注意:前端负责传入分
				.put("spbill_create_ip", ip)// IP地址
				.put("notify_url", Constant.WEIXIN_PAY_CALL_BACK_URL) // 微信回调地址
				.put("trade_type", Constant.TRADE_TYPE)// 支付类型 app
				.build();
		return MapUtils.sortMap(oparams);
	}
	
	/**
	 * 生成预付快订单完成，返回给android,ios唤起微信所需要的参数。
	 * 
	 * @param resutlMap
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private SortedMap<String, Object> buildClientJson(
			Map<String, Object> resutlMap) throws UnsupportedEncodingException {
		// 获取微信返回的签名
		Map<String, Object> params = ImmutableMap.<String, Object> builder()
				.put("appid", Constant.APPID)
				.put("noncestr", PayCommonUtil.CreateNoncestr())
				.put("package", "Sign=WXPay")
				.put("partnerid", Constant.MCH_ID)
				.put("prepayid", resutlMap.get("prepay_id"))
				.put("timestamp", DateUtils.getTimeStamp()) // 10 位时间戳
				.build();
		// key ASCII排序 // 这里用treemap也是可以的 可以用treemap // TODO
		SortedMap<String, Object> sortMap = MapUtils.sortMap(params);
		sortMap.put("package", "Sign=WXPay");
		// paySign的生成规则和Sign的生成规则同理
		String paySign = PayCommonUtil.createSign(Charsets.UTF_8.toString(), sortMap);
		sortMap.put("sign", paySign);
		return sortMap;
	}
	/******************************************************以下为俱乐部相关********************************************************/
	
	/**
	 * 创建俱乐部
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void createClub(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.createClub.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		if (msg.getStatus() == null) {
			msg.setStatus(1);
		}
		GameQuery gameQuery = new GameQuery();
		gameQuery.setPlayerId(playerId);
		Long clubCount = gameDao.getProxyClubsCount(gameQuery);
		if (clubCount >= 5) {
			throw new BusinessException(ExceptionEnum.CREATE_CLUB_LIMIT_ERROR);
		}
		Integer clubId = commonManager.createClub(msg.getClubName(), msg.getClubOwnerWord(), msg.getStatus(), 
				playerId, userInfo.getNickName(), userInfo.getHeadImgUrl());
		
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		data.put("roomCardNum", 0);
		data.put("clubId", clubId);
		data.put("clubName", msg.getClubName());
		data.put("clubOwnerWord", msg.getClubOwnerWord());
		data.put("nickName", userInfo.getNickName());
		data.put("wechatNum", userInfo.getNickName());
		result.setMsgType(MsgTypeEnum.entryClub.msgType);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
		/**设置玩家id与俱乐部id的关系，记忆下次直接进入俱乐部*/
		redisOperationService.setPlayerIdClubId(playerId, clubId);
		/**设置当前这个俱乐部玩家列表*/
		redisOperationService.setClubIdPlayerId(clubId, playerId);
	}
	
	/**
	 * 从俱乐部列表进入俱乐部
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void entryClub(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.entryClub.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = msg.getClubId();
		/**俱乐部是否存在*/
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		List<GameModel> list = gameDao.getProxyClubs(gameQuery);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException(ExceptionEnum.CLUB_ID_NOT_EXIST);
		}
		GameModel gameModel = list.get(0);
		/**如果不是俱乐部创始人进入，则需要校验玩家是否在俱乐部中*/
		if (!playerId.equals(gameModel.getPlayerId())) {
			gameQuery.setPlayerId(playerId);
			list = gameDao.getClubUsers(gameQuery);
			/**玩家是否在俱乐部中*/
			if (CollectionUtils.isEmpty(list)) {
				throw new BusinessException(ExceptionEnum.USER_NOT_IN_CLUB);
			}
		}
		
		result.setMsgType(MsgTypeEnum.entryClub.msgType);
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		data.put("roomCardNum", gameModel.getRoomCardNum());
		data.put("clubId", clubId);
		data.put("clubName", gameModel.getClubName());
		data.put("clubOwnerWord", gameModel.getClubOwnerWord());
		data.put("nickName", gameModel.getNickName());
		data.put("wechatNum", gameModel.getNickName());
		data.put("playerId", gameModel.getPlayerId());
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
		/**设置玩家id与俱乐部id的关系，记忆下次直接进入俱乐部*/
		redisOperationService.setPlayerIdClubId(playerId, clubId);
		/**设置当前这个俱乐部玩家列表*/
		redisOperationService.setClubIdPlayerId(clubId, playerId);
		/**设置俱乐部锁，只要俱乐部不解散，这个锁一直存在*/
		RoomLockContainer.setLockByClubId(clubId, new ReentrantLock());
	}
	/**
	 * 从大厅输入俱乐部号码加入俱乐部
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void joinClub(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = msg.getClubId();
		/**每个玩家只能加入不超过10个俱乐部*/
		GameQuery tempQuery = new GameQuery();
		tempQuery.setPlayerId(playerId);
		tempQuery.setStatus(1);
		Long joinCount = gameDao.getClubUsersCount(tempQuery);
		if (joinCount >= 10) {
			throw new BusinessException(ExceptionEnum.JOIN_CLUB_LIMIT_ERROR);
		}	
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		List<GameModel> list = gameDao.getProxyClubs(gameQuery);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException(ExceptionEnum.CLUB_ID_NOT_EXIST);
		}
		
		GameModel gameModel = list.get(0);
		data.put("roomCardNum", gameModel.getRoomCardNum());
		data.put("nickName", gameModel.getNickName());
		data.put("wechatNum", gameModel.getNickName());
		/**如果进入俱乐部的是主人本人，则直接进入*/
		if (playerId.equals(gameModel.getPlayerId())) {
			result.setMsgType(MsgTypeEnum.entryClub.msgType);
			data.put("clubId", clubId);
			data.put("clubName", gameModel.getClubName());
			data.put("clubOwnerWord", gameModel.getClubOwnerWord());
			data.put("playerId", gameModel.getPlayerId());
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			redisOperationService.setPlayerIdClubId(playerId, clubId);
			return;
		}
		
		gameQuery.setPlayerId(playerId);
//		gameQuery.setStatus(1);
		list = gameDao.getClubUsers(gameQuery);
		/**如果当前玩家已经在俱乐部中*/
		if (!CollectionUtils.isEmpty(list)) {
			/**如果状态是已经审核通过*/
			if (list.get(0).getStatus() > 0) {
				result.setMsgType(MsgTypeEnum.entryClub.msgType);
				data.put("clubId", clubId);
				data.put("clubName", gameModel.getClubName());
				data.put("clubOwnerWord", gameModel.getClubOwnerWord());
				data.put("playerId", gameModel.getPlayerId());
				channelContainer.sendTextMsgByPlayerIds(result, playerId);
				redisOperationService.setPlayerIdClubId(playerId, clubId);
				return;
			}else{/**如果状态是未审核，则需要提醒玩家，已经提交申请，等待审核 */
				throw new BusinessException(ExceptionEnum.HAS_APPLY);
			}
			
		}
		gameQuery.setProxyId(gameModel.getProxyId());
		gameQuery.setNickName(userInfo.getNickName());
		if (gameModel.getStatus() > 0) {
			gameQuery.setStatus(0);
			gameDao.insertClubUser(gameQuery);
			result.setMsgType(MsgTypeEnum.joinClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
		}else{/**如果不需要审核*/
			gameQuery.setStatus(1);
			gameDao.insertClubUser(gameQuery);
			result.setMsgType(MsgTypeEnum.entryClub.msgType);
			data.put("clubId", clubId);
			data.put("clubName", gameModel.getClubName());
			data.put("clubOwnerWord", gameModel.getClubOwnerWord());
			data.put("playerId", gameModel.getPlayerId());
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			redisOperationService.setPlayerIdClubId(playerId, clubId);
			/**设置当前这个俱乐部玩家列表*/
			redisOperationService.setClubIdPlayerId(clubId, playerId);
		}
	}
	
	/**
	 * 退出俱乐部到大厅
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void exitClub(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.exitClub.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		/**退出俱乐部需要删除playerId与俱乐部id的对应关系，去掉记忆*/
		redisOperationService.hdelPlayerIdClubId(playerId);
		/**设置当前这个俱乐部玩家列表*/
		redisOperationService.delClubIdPlayerId(clubId, playerId);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 * 创始人移除俱乐部玩家
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void delClubUser(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.delClubUser.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer otherPlayerId = msg.getOtherPlayerId();
		
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		if (clubId == null) {
			log.warn("玩家没有进入俱乐部");
			result.setMsgType(MsgTypeEnum.exitClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		/**校验是否是俱乐部创始人*/
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		gameQuery.setPlayerId(playerId);
		List<GameModel> list = gameDao.getProxyClubs(gameQuery);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException(ExceptionEnum.NO_PERMISSION);
		}
		gameQuery.setPlayerId(otherPlayerId);
		/**删除俱乐部玩家*/
		gameDao.delClubUser(gameQuery);
		/**删除playerId与俱乐部id的对应关系，去掉记忆*/
		redisOperationService.hdelPlayerIdClubId(otherPlayerId);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 * 玩家自己退出俱乐部并删除表数据
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void exitAndDelClubUser(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		Map<String, Object> data = new HashMap<String, Object>();
		result.setData(data);
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.exitAndDelClubUser.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		if (clubId == null) {
			log.warn("玩家没有进入俱乐部");
			result.setMsgType(MsgTypeEnum.exitClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		/**校验玩家是否属于这个俱乐部*/
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		gameQuery.setPlayerId(playerId);
		gameDao.delClubUser(gameQuery);
		/**删除playerId与俱乐部id的对应关系，去掉记忆*/
		redisOperationService.hdelPlayerIdClubId(playerId);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 * 创始人审核俱乐部玩家
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void auditClubMember(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.auditClubMember.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer otherPlayerId = msg.getOtherPlayerId();
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		if (clubId == null) {
			log.warn("玩家没有进入俱乐部");
			result.setMsgType(MsgTypeEnum.exitClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		
		/**校验是否是俱乐部创始人*/
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		gameQuery.setPlayerId(playerId);
		List<GameModel> list = gameDao.getProxyClubs(gameQuery);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException(ExceptionEnum.NO_PERMISSION);
		}
		/**如果是拒绝则物理删除记录*/
		if (msg.getStatus() == 2) {
			gameQuery.setPlayerId(otherPlayerId);
			gameDao.delClubUser(gameQuery);
		}else{
			/**审核俱乐部玩家*/
			gameQuery.setStatus(1);
			gameQuery.setPlayerId(otherPlayerId);
			gameDao.updateClubUser(gameQuery);
		}
		
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 * 创始人删除俱乐部
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void delClub(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.delClub.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = msg.getClubId();
		
		/**校验是否是俱乐部创始人*/
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		gameQuery.setPlayerId(playerId);
		List<GameModel> list = gameDao.getProxyClubs(gameQuery);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException(ExceptionEnum.NO_PERMISSION);
		}
		/**查询俱乐部玩家列表*/
		gameQuery.setPlayerId(null);
		list = gameDao.getClubUsers(gameQuery);
		/**删除俱乐部及俱乐部玩家*/
		commonManager.delClub(clubId);
		
		for(GameModel model : list){
			/**删除playerId与俱乐部id的对应关系，去掉记忆*/
			redisOperationService.hdelPlayerIdClubId(model.getPlayerId());
		}
		/**删除俱乐部*/
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 * 查询俱乐部玩家列表
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void getClubMembers(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.getClubMembers.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		if (clubId == null) {
			log.warn("玩家没有进入俱乐部");
			result.setMsgType(MsgTypeEnum.exitClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		gameQuery.setStatus(1);
		/**默认返回100条记录*/
		gameQuery.setLimit(100);
		List<GameModel> onlineList = new ArrayList<GameModel>();
		List<GameModel> offlineList = new ArrayList<GameModel>();
		List<GameModel> list = gameDao.getClubUsers(gameQuery);
		GameModel tempModel = null;
		int onlineNum = 0;
		for(GameModel modle : list){
			tempModel = new GameModel();
			tempModel.setPlayerId(modle.getPlayerId());
			tempModel.setNickName(modle.getNickName());
			tempModel.setHeadImgUrl(modle.getHeadImgUrl());
			tempModel.setCreateTime(modle.getCreateTime());
			if (channelContainer.isPlayIdActive(modle.getPlayerId())) {
				tempModel.setOnlineStatus(1);
				onlineList.add(tempModel);
				onlineNum++;
			}else{
				tempModel.setOnlineStatus(0);
				offlineList.add(tempModel);
			}
		}
		onlineList.addAll(offlineList);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("playerList", onlineList);
		data.put("onlineNum", onlineNum);
		data.put("totalNum", onlineList.size());
		result.setData(data);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 * 查询待审核的俱乐部玩家列表
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void getUnAuditClubMembers(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.getUnAuditClubMembers.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		if (clubId == null) {
			log.warn("玩家没有进入俱乐部");
			result.setMsgType(MsgTypeEnum.exitClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		gameQuery.setPlayerId(playerId);
		List<GameModel> list = gameDao.getProxyClubs(gameQuery);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException(ExceptionEnum.NO_PERMISSION);
		}
		
		GameQuery gameQuery1 = new GameQuery();
		gameQuery1.setClubId(clubId);
		gameQuery1.setStatus(0);
		/**默认返回100条记录*/
		gameQuery.setLimit(100);
		list = gameDao.getClubUsers(gameQuery1);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("playerList", list);
		result.setData(data);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 * 创建的俱乐部列表
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void getCreatedClubs(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.getCreatedClubs.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		GameQuery gameQuery = new GameQuery();
		gameQuery.setPlayerId(playerId);
		List<GameModel> list = gameDao.getCreatedClubs(gameQuery);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("clubList", list);
		result.setData(data);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 * 加入的俱乐部列表
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void getJoinedClubs(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.getJoinedClubs.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		GameQuery gameQuery = new GameQuery();
		gameQuery.setPlayerId(playerId);
		gameQuery.setStatus(1);
		List<GameModel> list = gameDao.getJoinedClubs(gameQuery);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("clubList", list);
		result.setData(data);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	/**
	 * 获取创建的和加入的俱乐部列表
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void getClubs(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.getClubs.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		GameQuery gameQuery = new GameQuery();
		gameQuery.setPlayerId(playerId);
		List<GameModel> createdList = gameDao.getCreatedClubs(gameQuery);
		gameQuery.setStatus(1);
		List<GameModel> joinedList = gameDao.getJoinedClubs(gameQuery);
		List<GameModel> clubList = new ArrayList<GameModel>();
		clubList.addAll(joinedList);
		clubList.addAll(createdList);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("clubList", clubList);
		data.put("createdNum", createdList.size());
		data.put("joinedNum", joinedList.size());
		result.setData(data);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	public void createClubTable(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.createClubTable.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		if (clubId == null) {
			log.warn("玩家没有进入俱乐部");
			result.setMsgType(MsgTypeEnum.exitClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		
		GameQuery gameQuery1 = new GameQuery();
		gameQuery1.setClubId(clubId);
		gameQuery1.setPlayerId(playerId);
		List<GameModel> list = gameDao.getProxyClubs(gameQuery1);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException(ExceptionEnum.NO_PERMISSION);
		}
		
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		
		Integer tableNum = gameDao.getMaxClubTableNum(gameQuery);
		if (tableNum == null) {
			tableNum = 0;
		}else{
			tableNum++;
		}
		gameQuery = doCreateGameQuery(ctx, request, userInfo);
		gameQuery.setClubId(clubId);
		gameQuery.setTableNum(tableNum);
		gameQuery.setGameType(request.getGameType());
		gameQuery.setDetailType(request.getDetailType());
		gameQuery.setPayType(msg.getPayType());
		gameQuery.setTotalGames(msg.getTotalGames());
		gameDao.insertClubTable(gameQuery);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
		noticeAllClubPlayerTablePlayerNum(null, clubId);
	}
	
	public abstract GameQuery doCreateGameQuery(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo);
	/**
	 * 查询俱乐部房间列表
	 * @param ctx
	 * @param request
	 * @param userInfo
	 */
	public void getClubTables(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.getClubTables.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		if (clubId == null) {
			log.warn("玩家没有进入俱乐部");
			result.setMsgType(MsgTypeEnum.exitClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		List<GameModel> tableList = gameDao.getClubTables(gameQuery);
		for(GameModel model : tableList){
			Integer roomId = redisOperationService.getRoomIdByClubIdTableNum(clubId, model.getTableNum());
			log.info("clubId:" + clubId + ",tableNum:" + model.getTableNum() + ",roomId:" + roomId);
			if (roomId != null) {
				userInfo.setRoomId(roomId);
				BaseRoomInfo roomInfo = getRoomInfo(null, null, userInfo);
				List playerList = roomInfo.getPlayerList();
				int size = playerList.size();
				int num = 0;
				for(int i = 0; i < size; i++){
					BasePlayerInfo playerInfo = (BasePlayerInfo)playerList.get(i);
					if (redisOperationService.getRoomIdGameTypeByPlayerId(playerInfo.getPlayerId()) != null) {
						num++;
					}
				}
				model.setPlayerNum(num);
			}else{
				model.setPlayerNum(0);
			}
		}
		result.setData(tableList);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	public void delClubTable(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.delClubTable.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		if (clubId == null) {
			log.warn("玩家没有进入俱乐部");
			result.setMsgType(MsgTypeEnum.exitClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		/**校验clubId是否属于这个玩家，否则无权限*/
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		gameQuery.setPlayerId(playerId);
		List<GameModel> list = gameDao.getProxyClubs(gameQuery);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException(ExceptionEnum.NO_AUTHORITY);
		}
		gameQuery.setTableNum(msg.getTableNum());
		gameDao.delClubTable(gameQuery);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	public void updateClubTable(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.updateClubTable.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		if (clubId == null) {
			log.warn("玩家没有进入俱乐部");
			result.setMsgType(MsgTypeEnum.exitClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		/**校验clubId是否属于这个玩家，否则无权限*/
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		gameQuery.setPlayerId(playerId);
		List<GameModel> list = gameDao.getProxyClubs(gameQuery);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException(ExceptionEnum.NO_AUTHORITY);
		}
		gameQuery = doCreateGameQuery(ctx, request, userInfo);
		gameQuery.setClubId(clubId);
		gameQuery.setTableNum(msg.getTableNum());
		gameQuery.setGameType(request.getGameType());
		gameQuery.setDetailType(request.getDetailType());
		gameQuery.setPayType(msg.getPayType());
		gameQuery.setTotalGames(msg.getTotalGames());
		gameDao.updateClubTable(gameQuery);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	public void updateClubNotice(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		Result result = new Result();
		result.setGameType(request.getGameType());
		result.setMsgType(MsgTypeEnum.updateClubNotice.msgType);
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer clubId = redisOperationService.getClubIdByPlayerId(playerId);
		if (clubId == null) {
			log.warn("玩家没有进入俱乐部");
			result.setMsgType(MsgTypeEnum.exitClub.msgType);
			channelContainer.sendTextMsgByPlayerIds(result, playerId);
			return;
		}
		/**校验clubId是否属于这个玩家，否则无权限*/
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		gameQuery.setPlayerId(playerId);
		List<GameModel> list = gameDao.getProxyClubs(gameQuery);
		if (CollectionUtils.isEmpty(list)) {
			throw new BusinessException(ExceptionEnum.NO_AUTHORITY);
		}
		gameQuery.setStatus(msg.getStatus());
		gameQuery.setClubName(msg.getClubName());
		gameQuery.setClubOwnerWord(msg.getClubOwnerWord());
		gameQuery.setUpdateTime(new Date());
		gameDao.updateProxyClub(gameQuery);
		channelContainer.sendTextMsgByPlayerIds(result, playerId);
	}
	
	private List<Map<String, Object>> getClubRoomList(Integer clubId, Integer status){
		List<Map<String, Object>> newRoomList = new ArrayList<Map<String,Object>>();
		List<Integer> roomIdList = redisOperationService.getRoomIdsByClubId(clubId);
		BaseRoomInfo roomInfo = null;
		UserInfo userInfo = new UserInfo();
		for(Integer roomId : roomIdList){
			userInfo.setRoomId(roomId);
			roomInfo = getRoomInfo(null, null, userInfo);
			if (roomInfo == null) {
				redisOperationService.delClubIdRoomId(clubId, roomId);
				continue;
			}
			/**status位1，说明请求的是等待中的房间*/
			if (status == 1) {
				/**过滤掉游戏已经开始的房间*/
				if (roomInfo.getStatus() > 1) {
					continue;
				}
			}
			List playerList = roomInfo.getPlayerList();
			int size = playerList.size();
			BasePlayerInfo playerInfo = null;
			Map<String, Object> newRoomInfo = new HashMap<String, Object>();
			List<Map<String, Object>> newPlayerList = new ArrayList<Map<String,Object>>();
			newRoomInfo.put("playerList", newPlayerList);
			newRoomInfo.put("roomId", roomId);
			newRoomInfo.put("gameType", roomInfo.getGameType());
			newRoomInfo.put("detailType", roomInfo.getDetailType());
			newRoomInfo.put("status", roomInfo.getStatus());
			for(int i = 0; i < size; i++){
				Map<String, Object> newPlayer = new HashMap<String, Object>();
				playerInfo = (BasePlayerInfo)playerList.get(i);
				newPlayer.put("playerId", playerInfo.getPlayerId());
				newPlayer.put("nickName", playerInfo.getNickName());
				newPlayer.put("headIgUrl", playerInfo.getHeadImgUrl());
				newPlayer.put("onlineStatus", playerInfo.getOnlineStatus());
				newPlayerList.add(newPlayer);
			}
			newRoomList.add(newRoomInfo);
		}
		return newRoomList;
	}
	public void offlineNotice(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer roomId = msg.getRoomId();
		if (roomId == null) {
			return;
		}
		/**设置离线playerId与roomId的映射关系*/
		redisOperationService.setOfflinePlayerIdRoomIdGameTypeTime(playerId, roomId, request.getGameType(), new Date());
		/**设置当前玩家为离线状态并通知其他玩家此玩家离线*/
		BaseRoomInfo roomInfo = getRoomInfo(ctx, request, userInfo);
		if (roomInfo == null) {
			redisOperationService.cleanPlayerAndRoomInfoForSignout(roomId, String.valueOf(playerId));
			userInfo.setRoomId(null);
			redisOperationService.setUserInfo(request.getToken(), userInfo);
			return;
		}
		List playerList = roomInfo.getPlayerList();
		List<Integer> playerIdList = new ArrayList<Integer>();
		for(Object object : playerList){
			BasePlayerInfo basePlayerInfo = (BasePlayerInfo)object;
			if (playerId.equals(basePlayerInfo.getPlayerId())) {
				basePlayerInfo.setOnlineStatus(OnlineStatusEnum.offline.status);
			}
		}
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		
		Result result = new Result();
		result.setMsgType(MsgTypeEnum.offlineNotice.msgType);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("playerId", playerId);
		result.setData(data);
		result.setGameType(request.getGameType());
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
	}
	
	public void onlineNotice(ChannelHandlerContext ctx, BaseRequest request, UserInfo userInfo){
		BaseMsg msg = request.getMsg();
		Integer playerId = msg.getPlayerId();
		Integer roomId = msg.getRoomId();
		if (roomId == null) {
			return;
		}
		/**删除离线playerId与roomId的映射关系*/
		redisOperationService.hdelOfflinePlayerIdRoomIdGameTypeTime(playerId);
		/**设置当前玩家为离线状态并通知其他玩家此玩家离线*/
		BaseRoomInfo roomInfo = getRoomInfo(ctx, request, userInfo);
		if (roomInfo == null) {
			redisOperationService.cleanPlayerAndRoomInfoForSignout(roomId, String.valueOf(playerId));
			userInfo.setRoomId(null);
			redisOperationService.setUserInfo(request.getToken(), userInfo);
			return;
		}
		List playerList = roomInfo.getPlayerList();
		List<Integer> playerIdList = new ArrayList<Integer>();
		for(Object object : playerList){
			BasePlayerInfo basePlayerInfo = (BasePlayerInfo)object;
			if (playerId.equals(basePlayerInfo.getPlayerId())) {
				basePlayerInfo.setOnlineStatus(OnlineStatusEnum.online.status);
			}
		}
		redisOperationService.setRoomIdRoomInfo(roomId, roomInfo);
		
		Result result = new Result();
		result.setMsgType(MsgTypeEnum.onlineNotice.msgType);
		Map<String, Object> data = new HashMap<String, Object>();
		data.put("playerId", playerId);
		result.setData(data);
		result.setGameType(request.getGameType());
		channelContainer.sendTextMsgByPlayerIds(result, GameUtil.getPlayerIdArrWithOutSelf(playerList, playerId));
	}
	
}
