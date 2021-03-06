package cn.worldwalker.game.wyqp.common.dao;

import cn.worldwalker.game.wyqp.common.backend.GameModel;
import cn.worldwalker.game.wyqp.common.backend.GameQuery;

import java.util.List;

public interface GameDao {
	
	public List<GameModel> getMyMembers(GameQuery gameQuery);
	public Long getMyMembersCount(GameQuery gameQuery);
	
	public List<GameModel> getBillingDetails(GameQuery gameQuery);
	public Long getBillingDetailsCount(GameQuery gameQuery);
	
	public List<GameModel> getWithDrawalRecords(GameQuery gameQuery);
	public Long getWithDrawalRecordsCount(GameQuery gameQuery);
	
	public GameModel getProxyInfo(GameQuery gameQuery);
	
	public GameModel getProxyByPhoneAndPassword(GameQuery gameQuery);
	
	public List<GameModel> getUserByCondition(GameQuery gameQuery);
	public Long getUserByConditionCount(GameQuery gameQuery);
	
	public Integer updateRoomCardNumByPlayerId(GameQuery gameQuery);
	
	public Integer updateProbabilityByPlayerId(GameQuery gameQuery);
	
	public List<GameModel> getProxys(GameQuery gameQuery);
	public Long getProxysCount(GameQuery gameQuery);
	
	public Integer insertProxy(GameQuery gameQuery);
	
	public Integer updateProxy(GameQuery gameQuery);
	
	public Integer insertProxyClub(GameQuery gameQuery);
	public List<GameModel> getProxyClubs(GameQuery gameQuery);
	public Long getProxyClubsCount(GameQuery gameQuery);
	public Integer updateProxyClub(GameQuery gameQuery);
	public Integer delProxyClub(GameQuery gameQuery);
	
	public Integer insertClubUser(GameQuery gameQuery);
	public List<GameModel> getClubUsers(GameQuery gameQuery);
	public Long getClubUsersCount(GameQuery gameQuery);
	public Integer updateClubUser(GameQuery gameQuery);
	public Integer delClubUser(GameQuery gameQuery);
	
	public List<GameModel> getJoinedClubs(GameQuery gameQuery);
	
	
	
}
