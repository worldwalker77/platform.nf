package cn.worldwalker.game.wyqp.common.backend;

import cn.worldwalker.game.wyqp.common.result.Result;

public interface BackendService {
	
	public Result doLogin(GameQuery gameQuery);
	
	public Result getProxyInfo(GameQuery gameQuery);
	
	public Result getBillingDetails(GameQuery gameQuery);
	
	public Result getMyMembers(GameQuery gameQuery);
	
	public Result getWithDrawalRecords(GameQuery gameQuery);
	
	public Result getUserByCondition(GameQuery gameQuery);
	
	public Result doGiveAwayRoomCards(Integer toPlayerId, Integer roomCardNum);
	
	public Result getWinProbability(GameQuery gameQuery);
	
	public Result modifyWinProbability(GameQuery gameQuery);
	
	public boolean isAdmin();
	
	public Result getProxys(GameQuery gameQuery);
	
	public Result modifyProxy(GameQuery gameQuery);
	
	public Result doModifyPassword(GameQuery gameQuery);
	
	public Result getProxyClubs(GameQuery gameQuery);
	public Result modifyProxyClub(GameQuery gameQuery);
	public Result delProxyClub(GameQuery gameQuery);
	
	public Result insertClubUser(GameQuery gameQuery);
	public Result getClubUsers(GameQuery gameQuery);
	public Result modifyClubUser(GameQuery gameQuery);
	public Result auditClubUser(GameQuery gameQuery);
	public Result delClubUser(GameQuery gameQuery);
	
}
