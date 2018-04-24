package cn.worldwalker.game.wyqp.common.domain.base;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.annotate.JsonSerialize;
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class BaseRoomInfo {
	/**房间id*/
	private Integer roomId;
	/**房主id*/
	private Integer roomOwnerId;
	/**当前局赢家*/
	private Integer curWinnerId;
	/**总赢家*/
	private Integer totalWinnerId;
	/**当前可操作人id*/
	private Integer curPlayerId;
	/**上一个操作人id*/
	private Integer lastPlayerId;
	/**庄家id*/
	private Integer roomBankerId;
	/**总局数*/
	private Integer totalGames;
	/**当前局数*/
	private Integer curGame = 0;
	/**支付方式 1：房主付费 2：AA付费*/
	private Integer payType;
	/**此房间所在服务器ip*/
	private String serverIp;
	/**房间的创建时间*/
	private Date createTime;
	/**房间的更新时间*/
	private Date updateTime;
	/**当前房间状态*/
	private Integer status;
	
	private List playerList;
	
	private Integer gameType;
	
	private Integer detailType = 1;
	
	private Integer playerNumLimit;
	/**当前房间随机uuid*/
	private Long roomUuid;
	/**当前局随机uuid*/
	private Long curGameUuid;
	
	private Integer clubId;
	
	private Integer tableNum;
	/**玩家相互之间的距离*/
	private Map<String, String> distanceMap = new HashMap<String, String>();
	/**解散玩家列表*/
	private List<Map<String, Object>> disList = null;
	/**房间信息备注*/
	private Map<String, Object> remark = new HashMap<String, Object>();
	
	public Integer getTableNum() {
		return tableNum;
	}

	public void setTableNum(Integer tableNum) {
		this.tableNum = tableNum;
	}

	public Map<String, Object> getRemark() {
		return remark;
	}

	public void setRemark(Map<String, Object> remark) {
		this.remark = remark;
	}

	public List<Map<String, Object>> getDisList() {
		return disList;
	}

	public void setDisList(List<Map<String, Object>> disList) {
		this.disList = disList;
	}

	public Map<String, String> getDistanceMap() {
		return distanceMap;
	}

	public void setDistanceMap(Map<String, String> distanceMap) {
		this.distanceMap = distanceMap;
	}

	public Integer getClubId() {
		return clubId;
	}

	public void setClubId(Integer clubId) {
		this.clubId = clubId;
	}

	public Integer getDetailType() {
		return detailType;
	}

	public void setDetailType(Integer detailType) {
		this.detailType = detailType;
	}

	public Long getRoomUuid() {
		return roomUuid;
	}

	public void setRoomUuid(Long roomUuid) {
		this.roomUuid = roomUuid;
	}

	public Long getCurGameUuid() {
		return curGameUuid;
	}

	public void setCurGameUuid(Long curGameUuid) {
		this.curGameUuid = curGameUuid;
	}

	public Integer getPlayerNumLimit() {
		return playerNumLimit;
	}

	public void setPlayerNumLimit(Integer playerNumLimit) {
		this.playerNumLimit = playerNumLimit;
	}

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Integer getRoomOwnerId() {
		return roomOwnerId;
	}

	public void setRoomOwnerId(Integer roomOwnerId) {
		this.roomOwnerId = roomOwnerId;
	}

	public Integer getCurWinnerId() {
		return curWinnerId;
	}

	public void setCurWinnerId(Integer curWinnerId) {
		this.curWinnerId = curWinnerId;
	}

	public Integer getTotalWinnerId() {
		return totalWinnerId;
	}

	public void setTotalWinnerId(Integer totalWinnerId) {
		this.totalWinnerId = totalWinnerId;
	}

	public Integer getCurPlayerId() {
		return curPlayerId;
	}

	public void setCurPlayerId(Integer curPlayerId) {
		this.curPlayerId = curPlayerId;
	}

	public Integer getRoomBankerId() {
		return roomBankerId;
	}

	public void setRoomBankerId(Integer roomBankerId) {
		this.roomBankerId = roomBankerId;
	}

	public Integer getTotalGames() {
		return totalGames;
	}

	public void setTotalGames(Integer totalGames) {
		this.totalGames = totalGames;
	}

	public Integer getCurGame() {
		return curGame;
	}

	public void setCurGame(Integer curGame) {
		this.curGame = curGame;
	}

	public Integer getPayType() {
		return payType;
	}

	public void setPayType(Integer payType) {
		this.payType = payType;
	}

	public String getServerIp() {
		return serverIp;
	}

	public void setServerIp(String serverIp) {
		this.serverIp = serverIp;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public List getPlayerList() {
		return playerList;
	}

	public Integer getGameType() {
		return gameType;
	}

	public void setGameType(Integer gameType) {
		this.gameType = gameType;
	}

	public Integer getLastPlayerId() {
		return lastPlayerId;
	}

	public void setLastPlayerId(Integer lastPlayerId) {
		this.lastPlayerId = lastPlayerId;
	}

}
