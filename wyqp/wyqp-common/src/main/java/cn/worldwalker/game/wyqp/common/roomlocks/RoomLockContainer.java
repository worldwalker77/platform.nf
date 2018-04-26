package cn.worldwalker.game.wyqp.common.roomlocks;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

/**
 * 每个房间都会分配一把锁，控制此房间请求排队
 * @author jinfeng.liu
 *
 */
public class RoomLockContainer {
	private static final Map<Integer, Lock> roomLockMap = new ConcurrentHashMap<Integer, Lock>();
	
	private static final Map<Integer, Lock> clubLockMap = new ConcurrentHashMap<Integer, Lock>();
	
	public static Lock getLockByRoomId(Integer roomId){
		return roomLockMap.get(roomId);
	}
	
	public static void setLockByRoomId(Integer roomId, Lock lock){
		roomLockMap.put(roomId, lock);
	}
	
	public static void delLockByRoomId(Integer roomId){
		roomLockMap.remove(roomId);
	}

	public static Lock getLockByClubId(Integer clubId){
		return clubLockMap.get(clubId);
	}
	
	public static void setLockByClubId(Integer clubId, Lock lock){
		if (!clubLockMap.containsKey(clubId)) {
			clubLockMap.put(clubId, lock);
		}
	}
	
	public static void delLockByClubId(Integer clubId){
		clubLockMap.remove(clubId);
	}
	
	
}
