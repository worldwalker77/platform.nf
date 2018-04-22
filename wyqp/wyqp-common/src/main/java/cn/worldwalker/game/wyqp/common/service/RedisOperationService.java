package cn.worldwalker.game.wyqp.common.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import cn.worldwalker.game.wyqp.common.constant.Constant;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.base.RedisRelaModel;
import cn.worldwalker.game.wyqp.common.domain.base.RoomCardOperationFailInfo;
import cn.worldwalker.game.wyqp.common.domain.base.UserInfo;
import cn.worldwalker.game.wyqp.common.enums.RoomCardOperationEnum;
import cn.worldwalker.game.wyqp.common.roomlocks.RoomLockContainer;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
import cn.worldwalker.game.wyqp.common.utils.redis.JedisTemplate;

@Component
public class RedisOperationService {
	
	
	/**游戏信息存储方式，0：redis  1：内存   2：ehcache*/
	private int gameInfoStorageType = Constant.gameInfoStorageType;
	
//	@Autowired
	private JedisTemplate jedisTemplate;
	
	/**房间是否存在*/
	public boolean isRoomIdExist(Integer roomId){
		if (gameInfoStorageType == 0 ) {
			return jedisTemplate.hexists(Constant.roomIdGameTypeUpdateTimeMap, String.valueOf(roomId));
		}else{
			return GameInfoMemoryContainer.roomIdGameTypeUpdateTimeMap.containsKey(String.valueOf(roomId));
		}
		
	}
	
	/**roomId->roomInfo 映射*/
	public void setRoomIdRoomInfo(Integer roomId, BaseRoomInfo roomInfo){
		Date date = new Date();
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.roomIdRoomInfoMap, String.valueOf(roomId), JsonUtil.toJson(roomInfo));
			setRoomIdGameTypeUpdateTime(roomId, roomInfo.getGameType(), date);
		}else{
			System.out.println();
			GameInfoMemoryContainer.roomIdRoomInfoMap.put(String.valueOf(roomId), JsonUtil.toJson(roomInfo));
			GameInfoMemoryContainer.roomIdGameTypeUpdateTimeMap.put(String.valueOf(roomId), roomInfo.getGameType() + "_" + date.getTime());
		}
	}
	
	public <T> T getRoomInfoByRoomId(Integer roomId, Class<T> clazz){
		String roomInfoStr = null;
		if (gameInfoStorageType == 0 ) {
			roomInfoStr = jedisTemplate.hget(Constant.roomIdRoomInfoMap, String.valueOf(roomId));
		}else{
			roomInfoStr = GameInfoMemoryContainer.roomIdRoomInfoMap.get(String.valueOf(roomId));
		}
		if (StringUtils.isBlank(roomInfoStr)) {
			return null;
		}
		return JsonUtil.toObject(roomInfoStr, clazz);
	}
	
	/**roomId->gameType,updateTime 映射*/
	public void setRoomIdGameTypeUpdateTime(Integer roomId, Integer gameType, Date updateTime){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.roomIdGameTypeUpdateTimeMap, String.valueOf(roomId), gameType + "_" + updateTime.getTime());
		}else{
			GameInfoMemoryContainer.roomIdGameTypeUpdateTimeMap.put(String.valueOf(roomId), gameType + "_" + updateTime.getTime());
		}
		
	}
	
	public void setRoomIdGameTypeUpdateTime(Integer roomId, Date updateTime){
		RedisRelaModel redisRelaModel = getGameTypeUpdateTimeByRoomId(roomId);
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.roomIdGameTypeUpdateTimeMap, String.valueOf(roomId), redisRelaModel.getGameType() + "_" + updateTime.getTime());
		}else{
			GameInfoMemoryContainer.roomIdGameTypeUpdateTimeMap.put(String.valueOf(roomId), redisRelaModel.getGameType() + "_" + updateTime.getTime());
		}
		
	}
	
	public RedisRelaModel getGameTypeUpdateTimeByRoomId(Integer roomId){
		String str = null;
		if (gameInfoStorageType == 0 ) {
			str = jedisTemplate.hget(Constant.roomIdGameTypeUpdateTimeMap, String.valueOf(roomId));
		}else{
			str = GameInfoMemoryContainer.roomIdGameTypeUpdateTimeMap.get(String.valueOf(roomId));
		}
		if (StringUtils.isBlank(str)) {
			return null;
		}
		String[] arr = str.split("_");
		return new RedisRelaModel(null, roomId, Integer.valueOf(arr[0]), Long.valueOf(arr[1]));
	}
	
	public void delGameTypeUpdateTimeByRoomId(Integer roomId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.roomIdGameTypeUpdateTimeMap, String.valueOf(roomId));
		}else{
			GameInfoMemoryContainer.roomIdGameTypeUpdateTimeMap.remove(String.valueOf(roomId));
		}
		
	}
	
	
	public List<RedisRelaModel> getAllRoomIdGameTypeUpdateTime(){
		Map<String, String> map = new HashMap<String, String>();
		if (gameInfoStorageType == 0 ) {
			map = jedisTemplate.hgetAll(Constant.roomIdGameTypeUpdateTimeMap);
		}else{
			map.putAll(GameInfoMemoryContainer.roomIdGameTypeUpdateTimeMap);
		}
		if (map == null) {
			return null;
		}
		List<RedisRelaModel> list = new ArrayList<RedisRelaModel>();
		Set<Entry<String, String>> set = map.entrySet();
		for(Entry<String, String> entry : set){
			String key = entry.getKey();
			String value = entry.getValue();
			String[] arr = value.split("_");
			list.add(new RedisRelaModel(null, Integer.valueOf(key), Integer.valueOf(arr[0]), Long.valueOf(arr[1])));
		}
		return list;
	}
	
	/**解散房间ip->roomId->time 映射*/
	public void setDissolveIpRoomIdTime(Integer playerId, Integer roomId, Integer gameType){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.dissolveIpRoomIdTimeMap, String.valueOf(roomId), playerId + "_" + gameType + "_" + String.valueOf(System.currentTimeMillis()));
		}else{
			GameInfoMemoryContainer.dissolveIpRoomIdTimeMap.put(String.valueOf(roomId), playerId + "_" + gameType + "_" + String.valueOf(System.currentTimeMillis()));
		}
		
	}
	
	public void delDissolveIpRoomIdTime(Integer roomId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.dissolveIpRoomIdTimeMap, String.valueOf(roomId));
		}else{
			GameInfoMemoryContainer.dissolveIpRoomIdTimeMap.remove(String.valueOf(roomId));
		}
	}
	
	public RedisRelaModel getDissolveIpRoomIdTime(Integer roomId){
		String str = null;
		if (gameInfoStorageType == 0 ) {
			str = jedisTemplate.hget(Constant.dissolveIpRoomIdTimeMap, String.valueOf(roomId));
		}else{
			str = GameInfoMemoryContainer.dissolveIpRoomIdTimeMap.get(String.valueOf(roomId));
		}
		if (StringUtils.isBlank(str)) {
			return null;
		}
		String[] arr = str.split("_");
		return new RedisRelaModel(Integer.valueOf(arr[0]), Integer.valueOf(roomId), Integer.valueOf(arr[1]), Long.valueOf(arr[2]));
	}
	
	public List<RedisRelaModel> getAllDissolveIpRoomIdTime(){
		Map<String, String> map = new HashMap<String, String>();
		if (gameInfoStorageType == 0 ) {
			map = jedisTemplate.hgetAll(Constant.dissolveIpRoomIdTimeMap);
		}else{
			map.putAll(GameInfoMemoryContainer.dissolveIpRoomIdTimeMap);
			
		}
		if (map == null) {
			return null;
		}
		List<RedisRelaModel> list = new ArrayList<RedisRelaModel>();
		Set<Entry<String, String>> set = map.entrySet();
		for(Entry<String, String> entry : set){
			String key = entry.getKey();
			String value = entry.getValue();
			String[] arr = value.split("_");
			list.add(new RedisRelaModel(Integer.valueOf(arr[0]), Integer.valueOf(key), Integer.valueOf(arr[1]), Long.valueOf(arr[2])));
		}
		return list;
	}
	
	/**playerId->roomId,gameType 映射*/
	public void setPlayerIdRoomIdGameType(Integer playerId, Integer roomId, Integer gameType){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.playerIdRoomIdGameTypeMap, String.valueOf(playerId), roomId + "_" + gameType);
		}else{
			GameInfoMemoryContainer.playerIdRoomIdGameTypeMap.put(String.valueOf(playerId), roomId + "_" + gameType);
		}
		
	}
	
	public RedisRelaModel getRoomIdGameTypeByPlayerId(Integer playerId){
		String str = null;
		if (gameInfoStorageType == 0 ) {
			str = jedisTemplate.hget(Constant.playerIdRoomIdGameTypeMap, String.valueOf(playerId));
		}else{
			str = GameInfoMemoryContainer.playerIdRoomIdGameTypeMap.get(String.valueOf(playerId));
		}
		if (StringUtils.isBlank(str)) {
			return null;
		}
		String[] arr = str.split("_");
		return new RedisRelaModel(playerId, Integer.valueOf(arr[0]), Integer.valueOf(arr[1]), null);
	}
	
	public void hdelPlayerIdRoomIdGameType(Integer playerId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.playerIdRoomIdGameTypeMap, String.valueOf(playerId));
		}else{
			GameInfoMemoryContainer.playerIdRoomIdGameTypeMap.remove(String.valueOf(playerId));
		}
		
	}
	
	/**offline playerId->roomId,gameType 映射*/
	public void setOfflinePlayerIdRoomIdGameTypeTime(Integer playerId, Integer roomId, Integer gameType, Date time){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.offlinePlayerIdRoomIdGameTypeTimeMap, String.valueOf(playerId), roomId + "_" + gameType + "_" + time.getTime());
		}else{
			GameInfoMemoryContainer.offlinePlayerIdRoomIdGameTypeTimeMap.put(String.valueOf(playerId), roomId + "_" + gameType + "_" + time.getTime());
		}
		
	}
	
	public void hdelOfflinePlayerIdRoomIdGameTypeTime(Integer playerId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.offlinePlayerIdRoomIdGameTypeTimeMap, String.valueOf(playerId));
		}else{
			GameInfoMemoryContainer.offlinePlayerIdRoomIdGameTypeTimeMap.remove(String.valueOf(playerId));
		}
		
	}
	
	public RedisRelaModel getRoomIdGameTypeTimeByOfflinePlayerId(Integer playerId){
		String str = null;
		if (gameInfoStorageType == 0 ) {
			str = jedisTemplate.hget(Constant.offlinePlayerIdRoomIdGameTypeTimeMap, String.valueOf(playerId));
		}else{
			str = GameInfoMemoryContainer.offlinePlayerIdRoomIdGameTypeTimeMap.get(String.valueOf(playerId));
		}
		
		if (StringUtils.isBlank(str)) {
			return null;
		}
		String[] arr = str.split("_");
		return new RedisRelaModel(playerId, Integer.valueOf(arr[0]), Integer.valueOf(arr[1]), Long.valueOf(arr[2]));
	}
	
	public List<RedisRelaModel> getAllOfflinePlayerIdRoomIdGameTypeTime(){
		Map<String, String> map = new HashMap<String, String>();
		if (gameInfoStorageType == 0 ) {
			map = jedisTemplate.hgetAll(Constant.offlinePlayerIdRoomIdGameTypeTimeMap);
		}else{
			map.putAll(GameInfoMemoryContainer.offlinePlayerIdRoomIdGameTypeTimeMap);
		}
		if (map == null) {
			return null;
		}
		List<RedisRelaModel> list = new ArrayList<RedisRelaModel>();
		Set<Entry<String, String>> set = map.entrySet();
		for(Entry<String, String> entry : set){
			String key = entry.getKey();
			String value = entry.getValue();
			String[] arr = value.split("_");
			list.add(new RedisRelaModel(Integer.valueOf(key), Integer.valueOf(arr[0]), Integer.valueOf(arr[1]), Long.valueOf(arr[2])));
		}
		return list;
	}
	
	/**牛牛抢庄ip->roomId->time 映射*/
	public void setNnRobIpRoomIdTime(Integer roomId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.nnRobIpRoomIdTimeMap, String.valueOf(roomId), String.valueOf(System.currentTimeMillis()));
		}else{
			GameInfoMemoryContainer.nnRobIpRoomIdTimeMap.put(String.valueOf(roomId), String.valueOf(System.currentTimeMillis()));
		}
		
	}
	
	public void delNnRobIpRoomIdTime(Integer roomId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.nnRobIpRoomIdTimeMap, String.valueOf(roomId));
		}else{
			GameInfoMemoryContainer.nnRobIpRoomIdTimeMap.remove(String.valueOf(roomId));
		}
		
	}
	
	public Map<String, String> getAllNnRobIpRoomIdTime(){
		Map<String, String> map = new HashMap<String, String>();
		if (gameInfoStorageType == 0 ) {
			map = jedisTemplate.hgetAll(Constant.nnRobIpRoomIdTimeMap);
		}else{
			map.putAll(GameInfoMemoryContainer.nnRobIpRoomIdTimeMap);
		}
		return map;
	}
	/**牛牛翻牌ip->roomId->time 映射*/
	public void setNnShowCardIpRoomIdTime(Integer roomId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.nnShowCardIpRoomIdTimeMap, String.valueOf(roomId), String.valueOf(System.currentTimeMillis()));
		}else{
			GameInfoMemoryContainer.nnShowCardIpRoomIdTimeMap.put(String.valueOf(roomId), String.valueOf(System.currentTimeMillis()));
		}
		
	}
	
	public void delNnShowCardIpRoomIdTime(Integer roomId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.nnShowCardIpRoomIdTimeMap, String.valueOf(roomId));
		}else{
			GameInfoMemoryContainer.nnShowCardIpRoomIdTimeMap.remove(String.valueOf(roomId));
		}
		
	}
	
	public Map<String, String> getAllNnShowCardIpRoomIdTime(){
		Map<String, String> map = new HashMap<String, String>();
		if (gameInfoStorageType == 0 ) {
			map = jedisTemplate.hgetAll(Constant.nnShowCardIpRoomIdTimeMap);
		}else{
			map.putAll(GameInfoMemoryContainer.nnShowCardIpRoomIdTimeMap);
			
		}
		return map;
	}
	/**金花/牛牛未准备倒计时ip->roomId->time 映射*/
	public void setNotReadyIpRoomIdTime(Integer roomId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.notReadyIpRoomIdTimeMap, String.valueOf(roomId), String.valueOf(System.currentTimeMillis()));
		}else{
			GameInfoMemoryContainer.notReadyIpRoomIdTimeMap.put(String.valueOf(roomId), String.valueOf(System.currentTimeMillis()));
		}
		
	}
	
	public void delNotReadyIpRoomIdTime(Integer roomId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.notReadyIpRoomIdTimeMap, String.valueOf(roomId));
		}else{
			GameInfoMemoryContainer.notReadyIpRoomIdTimeMap.remove(String.valueOf(roomId));
		}
	}
	
	public Map<String, String> getAllNotReadyIpRoomIdTime(){
		Map<String, String> map = new HashMap<String, String>();
		if (gameInfoStorageType == 0 ) {
			map = jedisTemplate.hgetAll(Constant.notReadyIpRoomIdTimeMap);
		}else{
			map.putAll(GameInfoMemoryContainer.notReadyIpRoomIdTimeMap);
			
		}
		return map;
	}
	
	
	
	/**短信验证码超时map*/
	public void setSmsMobileValideCodeTime(String mobile, String validCode){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.smsValidCodeMap, mobile, validCode + "_" + String.valueOf(System.currentTimeMillis()));
		}else{
			GameInfoMemoryContainer.smsValidCodeMap.put(mobile, validCode + "_" + String.valueOf(System.currentTimeMillis()));
		}
		
	}
	
	public String getSmsMobileValideCodeTime(String mobile){
		String res = null;
		if (gameInfoStorageType == 0 ) {
			res = jedisTemplate.hget(Constant.smsValidCodeMap, mobile);
		}else{
			res = GameInfoMemoryContainer.smsValidCodeMap.get(mobile);
		}
		return res;
	}
	
	public void delSmsMobileValideCodeTime(String mobile){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.smsValidCodeMap, mobile);
		}else{
			GameInfoMemoryContainer.smsValidCodeMap.remove(mobile);
		}
	}
	
	public Map<String, String> getAllSmsMobileValideCodeTime(){
		Map<String, String> map = new HashMap<String, String>();
		if (gameInfoStorageType == 0 ) {
			map = jedisTemplate.hgetAll(Constant.smsValidCodeMap);
		}else{
			map.putAll(GameInfoMemoryContainer.smsValidCodeMap);
			
		}
		return map;
	}
	
	/**炸金花玩家超过120s没操作，则自动弃牌ip->playerId->roomId_curGame_stakeTimes_time*/
	public void setJhNoOperationIpPlayerIdRoomIdTime(Integer playerId, Integer roomId, Integer curGame, Integer stakeTimes){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.jhNoOperationIpPlayerIdRoomIdTimeMap, String.valueOf(playerId), roomId + "_" + curGame + "_" + stakeTimes + "_" +String.valueOf(System.currentTimeMillis()));
		}else{
			GameInfoMemoryContainer.jhNoOperationIpPlayerIdRoomIdTimeMap.put(String.valueOf(playerId), roomId + "_" + curGame + "_" + stakeTimes + "_" +String.valueOf(System.currentTimeMillis()));
		}
		
	}
	
	public void delJhNoOperationIpPlayerIdRoomIdTime(Integer playerId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.jhNoOperationIpPlayerIdRoomIdTimeMap, String.valueOf(playerId));
		}else{
			GameInfoMemoryContainer.jhNoOperationIpPlayerIdRoomIdTimeMap.remove(String.valueOf(playerId));
		}
		
	}
	
	public Map<String, String> getAllJhNoOperationIpPlayerIdRoomIdTime(){
		Map<String, String> map = new HashMap<String, String>();
		if (gameInfoStorageType == 0 ) {
			map = jedisTemplate.hgetAll(Constant.jhNoOperationIpPlayerIdRoomIdTimeMap);
		}else{
			map.putAll(GameInfoMemoryContainer.jhNoOperationIpPlayerIdRoomIdTimeMap);
		}
		return map;
	}
	
	
	/**用户userInfo设置*/
	public void setUserInfo(String token, UserInfo userInfo){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.setex(token, JsonUtil.toJson(userInfo), Constant.userInfoOverTimeLimit);
		}else{
			GameInfoMemoryContainer.tokenUserInfoMap.put(token, JsonUtil.toJson(userInfo));
			GameInfoMemoryContainer.tokenTimeMap.put(token, System.currentTimeMillis());
		}
		
	}
	
	public void expireUserInfo(String token){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.expire(token, Constant.userInfoOverTimeLimit);
		}else{
			GameInfoMemoryContainer.tokenTimeMap.put(token, System.currentTimeMillis());
		}
		
	}
	
	public UserInfo getUserInfo(String token){
		String temp = null;
		if (gameInfoStorageType == 0 ) {
			temp = jedisTemplate.get(token);
		}else{
			temp = GameInfoMemoryContainer.tokenUserInfoMap.get(token);
		}
		if (StringUtils.isNotBlank(temp)) {
			return JsonUtil.toObject(temp, UserInfo.class);
		}
		return null;
	}
	/**此方法使用内存的时候才会用**/
	public void delUserInfo(String token){
		GameInfoMemoryContainer.tokenUserInfoMap.remove(token);
		GameInfoMemoryContainer.tokenTimeMap.remove(token);
	}
	/**此方法使用内存的时候才会用**/
	public Map<String, Long> getAllTokenTimeMap(){
		Map<String, Long> map = new HashMap<String, Long>();
		map.putAll(GameInfoMemoryContainer.tokenTimeMap);
		return map;
	}
	
	
	/****/
	public void incrIpConnectCount(int incrBy){
		
		String ip = Constant.localIp;
  	    if (StringUtils.isNotBlank(ip)) {
  			if (gameInfoStorageType == 0 ) {
  				jedisTemplate.hincrBy(Constant.ipConnectCountMap, Constant.localIp, incrBy);
  			}else{
  				Integer curCount = GameInfoMemoryContainer.ipConnectCountMap.get(Constant.localIp) == null ? 0 : GameInfoMemoryContainer.ipConnectCountMap.get(Constant.localIp);
  				GameInfoMemoryContainer.ipConnectCountMap.put(Constant.localIp, curCount + incrBy);
  			}
  	    	
  	    }
	}
	
	/**清理用户及房间信息*/
	public void cleanPlayerAndRoomInfo(Integer roomId, String... playerIds){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.roomIdRoomInfoMap, String.valueOf(roomId));
			jedisTemplate.hdel(Constant.roomIdGameTypeUpdateTimeMap, String.valueOf(roomId));
			jedisTemplate.hdel(Constant.playerIdRoomIdGameTypeMap, playerIds);
			jedisTemplate.hdel(Constant.offlinePlayerIdRoomIdGameTypeTimeMap, playerIds);
			jedisTemplate.hdel(Constant.dissolveIpRoomIdTimeMap, String.valueOf(roomId));
		}else{
			GameInfoMemoryContainer.roomIdRoomInfoMap.remove(String.valueOf(roomId));
			GameInfoMemoryContainer.roomIdGameTypeUpdateTimeMap.remove(String.valueOf(roomId));
			GameInfoMemoryContainer.dissolveIpRoomIdTimeMap.remove(String.valueOf(roomId));
			for(String playerId : playerIds){
				GameInfoMemoryContainer.playerIdRoomIdGameTypeMap.remove(playerId);
				GameInfoMemoryContainer.offlinePlayerIdRoomIdGameTypeTimeMap.remove(playerId);
			}
		}
		
		RoomLockContainer.delLockByRoomId(roomId);
	}
	
	/**某个玩家退出时，清理相关信息*/
	public void cleanPlayerAndRoomInfoForSignout(Integer roomId, String... playerIds){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.playerIdRoomIdGameTypeMap, playerIds);
			jedisTemplate.hdel(Constant.offlinePlayerIdRoomIdGameTypeTimeMap, playerIds);
		}else{
			for(String playerId : playerIds){
				GameInfoMemoryContainer.playerIdRoomIdGameTypeMap.remove(playerId);
				GameInfoMemoryContainer.offlinePlayerIdRoomIdGameTypeTimeMap.remove(playerId);
			}
		}
		
	}
	
	/**房卡操作失败补偿*/
	public void lpushRoomCardOperationFailInfo(Integer playerId, Integer gameType, Integer payType, 
													Integer totalGames, RoomCardOperationEnum roomCardOperationEnum){
		
		RoomCardOperationFailInfo failInfo = new RoomCardOperationFailInfo(playerId,gameType,payType,totalGames,roomCardOperationEnum.type);
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.lpush(Constant.roomCardOperationFailList, JsonUtil.toJson(failInfo));
		}else{
			GameInfoMemoryContainer.roomCardOperationFailList.push(JsonUtil.toJson(failInfo));
		}
		
		
	}
	
	public RoomCardOperationFailInfo rpopRoomCardOperationFailInfo(){
		String failInfoStr = null;
		if (gameInfoStorageType == 0 ) {
			failInfoStr = jedisTemplate.rpop(Constant.roomCardOperationFailList);
		}else{
			if (CollectionUtils.isNotEmpty(GameInfoMemoryContainer.roomCardOperationFailList)) {
				failInfoStr = GameInfoMemoryContainer.roomCardOperationFailList.pop();
			}
		}
		if (StringUtils.isBlank(failInfoStr)) {
			return null;
		}
		return JsonUtil.toObject(failInfoStr, RoomCardOperationFailInfo.class);
	}
	/**playerId->clubId 映射*/
	public void setPlayerIdClubId(Integer playerId, Integer clubId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hset(Constant.playerIdClubIdMap, String.valueOf(playerId), String.valueOf(clubId));
		}else{
			GameInfoMemoryContainer.playerIdClubIdMap.put(String.valueOf(playerId), String.valueOf(clubId));
		}
		
	}
	
	public Integer getClubIdByPlayerId(Integer playerId){
		String str = null;
		if (gameInfoStorageType == 0 ) {
			str = jedisTemplate.hget(Constant.playerIdClubIdMap, String.valueOf(playerId));
		}else{
			str = GameInfoMemoryContainer.playerIdClubIdMap.get(String.valueOf(playerId));
		}
		if (StringUtils.isBlank(str)) {
			return null;
		}
		return Integer.valueOf(str);
	}
	
	public void hdelPlayerIdClubId(Integer playerId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.hdel(Constant.playerIdClubIdMap, String.valueOf(playerId));
		}else{
			GameInfoMemoryContainer.playerIdClubIdMap.remove(String.valueOf(playerId));
		}
	}
	
	public static Lock lock = new ReentrantLock();
	/**俱乐部与房间号列表关系*/
	public void setClubIdRoomId(Integer clubId, Integer roomId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.sadd(Constant.clubIdRoomIdSet + clubId, String.valueOf(roomId));
		}else{
			Vector<Integer> ve = GameInfoMemoryContainer.clubIdRoomIdVectorMap.get(clubId);
			if (ve == null) {
				lock.lock();
				try {
					ve = new Vector<Integer>();
					GameInfoMemoryContainer.clubIdRoomIdVectorMap.put(clubId, ve);
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					lock.unlock();
				}
			}
			ve.add(roomId);
		}
	}
	
	public List<Integer> getRoomIdsByClubId(Integer clubId){
		List<Integer> list = new ArrayList<Integer>();
		if (gameInfoStorageType == 0 ) {
			Set<String> set = jedisTemplate.smembers(Constant.clubIdRoomIdSet + clubId);
			if (!CollectionUtils.isEmpty(set)) {
				for(String temp : set){
					list.add(Integer.valueOf(temp));
				}
			}
		}else{
			Vector<Integer> vec = GameInfoMemoryContainer.clubIdRoomIdVectorMap.get(clubId);
			if (!CollectionUtils.isEmpty(vec)) {
				list.addAll(vec);
			}
		}
		return list;
	}
	
	public void delClubIdRoomId(Integer clubId, Integer roomId){
		if (gameInfoStorageType == 0 ) {
			jedisTemplate.srem(Constant.clubIdRoomIdSet + clubId, String.valueOf(roomId));
		}else{
			Vector<Integer> vec = GameInfoMemoryContainer.clubIdRoomIdVectorMap.get(clubId);
			if (!CollectionUtils.isEmpty(vec)) {
				vec.remove(roomId);
			}
		}
	}
	
	/**playerId->time 映射*/
//	public void setPlayerIdTime(Integer playerId){
//		if (gameInfoStorageType == 0 ) {
//			jedisTemplate.hset(Constant.playerIdTimeMap, String.valueOf(playerId), String.valueOf(System.currentTimeMillis()));
//		}else{
//			GameInfoMemoryContainer.playerIdTimeMap.put(String.valueOf(playerId), String.valueOf(System.currentTimeMillis()));
//		}
//	}
//	
//	public Map<String, String> getAllPlayerIdTime(){
//		Map<String, String> map = new HashMap<String, String>();
//		if (gameInfoStorageType == 0 ) {
//			map.putAll(jedisTemplate.hgetAll(Constant.playerIdTimeMap));
//		}else{
//			map.putAll(GameInfoMemoryContainer.playerIdTimeMap);
//		}
//		return map;
//	}
//	
//	public void hdelPlayerIdTime(Integer playerId){
//		if (gameInfoStorageType == 0 ) {
//			jedisTemplate.hdel(Constant.playerIdTimeMap, String.valueOf(playerId));
//		}else{
//			GameInfoMemoryContainer.playerIdTimeMap.remove(String.valueOf(playerId));
//		}
//		
//	}
	
	public boolean isLogFuseOpen(){
		String logInfoFuseValue = "0";
		if (gameInfoStorageType == 0 ) {
			logInfoFuseValue = jedisTemplate.get(Constant.logInfoFuse);
		}else{
			logInfoFuseValue = GameInfoMemoryContainer.logFuse;
		}
		if ("1".equals(logInfoFuseValue)) {
			return true;
		}
		return false;
	}
	
	public boolean isLoginFuseOpen(){
		String loginFuseValue = "0";
		if (gameInfoStorageType == 0 ) {
			loginFuseValue = jedisTemplate.get(Constant.loginFuse);
		}else{
			loginFuseValue = GameInfoMemoryContainer.loginFuse;
		}
		if ("1".equals(loginFuseValue)) {
			return true;
		}
		return false;
	}
	
	public boolean isCreateRoomFuseOpen(){
		String createRoomFuseValue = "0";
		if (gameInfoStorageType == 0 ) {
			createRoomFuseValue = jedisTemplate.get(Constant.createRoomFuse);
		}else{
			createRoomFuseValue = GameInfoMemoryContainer.createRoomFuse;
		}
		if ("1".equals(createRoomFuseValue)) {
			return true;
		}
		return false;
	}
	
}
