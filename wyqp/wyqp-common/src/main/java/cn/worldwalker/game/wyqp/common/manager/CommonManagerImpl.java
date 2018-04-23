package cn.worldwalker.game.wyqp.common.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import cn.worldwalker.game.wyqp.common.backend.GameQuery;
import cn.worldwalker.game.wyqp.common.dao.ExtensionCodeBindDao;
import cn.worldwalker.game.wyqp.common.dao.GameDao;
import cn.worldwalker.game.wyqp.common.dao.OrderDao;
import cn.worldwalker.game.wyqp.common.dao.ProductDao;
import cn.worldwalker.game.wyqp.common.dao.ProxyDao;
import cn.worldwalker.game.wyqp.common.dao.RecordPlayBackDao;
import cn.worldwalker.game.wyqp.common.dao.RoomCardLogDao;
import cn.worldwalker.game.wyqp.common.dao.UserDao;
import cn.worldwalker.game.wyqp.common.dao.UserFeedbackDao;
import cn.worldwalker.game.wyqp.common.dao.UserRecordDao;
import cn.worldwalker.game.wyqp.common.dao.UserRecordDetailDao;
import cn.worldwalker.game.wyqp.common.dao.VersionDao;
import cn.worldwalker.game.wyqp.common.domain.base.BasePlayerInfo;
import cn.worldwalker.game.wyqp.common.domain.base.BaseRoomInfo;
import cn.worldwalker.game.wyqp.common.domain.base.ExtensionCodeBindModel;
import cn.worldwalker.game.wyqp.common.domain.base.OrderModel;
import cn.worldwalker.game.wyqp.common.domain.base.PlayBackModel;
import cn.worldwalker.game.wyqp.common.domain.base.ProductModel;
import cn.worldwalker.game.wyqp.common.domain.base.ProxyModel;
import cn.worldwalker.game.wyqp.common.domain.base.RecordModel;
import cn.worldwalker.game.wyqp.common.domain.base.RoomCardLogModel;
import cn.worldwalker.game.wyqp.common.domain.base.UserFeedbackModel;
import cn.worldwalker.game.wyqp.common.domain.base.UserModel;
import cn.worldwalker.game.wyqp.common.domain.base.UserRecordModel;
import cn.worldwalker.game.wyqp.common.enums.PlayerStatusEnum;
import cn.worldwalker.game.wyqp.common.enums.RoomCardConsumeEnum;
import cn.worldwalker.game.wyqp.common.enums.RoomCardOperationEnum;
import cn.worldwalker.game.wyqp.common.exception.BusinessException;
import cn.worldwalker.game.wyqp.common.exception.ExceptionEnum;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
@Component
public class CommonManagerImpl implements CommonManager{
	
	private static final Log log = LogFactory.getLog(CommonManagerImpl.class);
	
	@Autowired
	private UserDao userDao;
	@Autowired
	private RoomCardLogDao roomCardLogDao;
	@Autowired
	private UserFeedbackDao userFeedbackDao;
	@Autowired
	private UserRecordDao userRecordDao;
	@Autowired
	private UserRecordDetailDao userRecordDetailDao;
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private ProductDao productDao;
	@Autowired
	private ProxyDao proxyDao;
	@Autowired
	private VersionDao versionDao;
	@Autowired
	private RecordPlayBackDao recordPlayBackDao;
	@Autowired
	private ExtensionCodeBindDao extensionCodeBindDao;
	@Autowired
	private GameDao gameDao;
	
	@Override
	public UserModel getUserByWxOpenId(String openId){
		return userDao.getUserByWxOpenId(openId);
	}
	@Override
	public void insertUser(UserModel userModel){
		userDao.insertUser(userModel);
	}
	
	@Transactional
	@Override
	public List<Integer> deductRoomCard(BaseRoomInfo roomInfo, RoomCardOperationEnum operationEnum){
		List<Integer> playerIList = new ArrayList<Integer>();
		if (roomInfo.getPayType() == 1) {/**房主付费*/
			playerIList.add(roomInfo.getRoomOwnerId());
		}else{/**AA付费*/
			List playerList = roomInfo.getPlayerList();
			int size = playerList.size();
			for(int i = 0; i < size; i++){
				BasePlayerInfo player = (BasePlayerInfo)playerList.get(i);
				playerIList.add(player.getPlayerId());
			}
		}
		for(Integer playerId : playerIList){
			doDeductRoomCard(roomInfo.getGameType(), roomInfo.getPayType(), roomInfo.getTotalGames(), operationEnum, playerId);
		}
		return playerIList;
	}
	
	@Override
	public List<Integer> deductRoomCardForObserver(BaseRoomInfo roomInfo, RoomCardOperationEnum operationEnum) {
		List<Integer> playerIList = new ArrayList<Integer>();
		if (roomInfo.getPayType() == 1) {/**房主付费,则新进来的观察者不需要扣房卡*/
			return playerIList;
		}else{/**AA付费*/
			List playerList = roomInfo.getPlayerList();
			int size = playerList.size();
			for(int i = 0; i < size; i++){
				BasePlayerInfo player = (BasePlayerInfo)playerList.get(i);
				if (!PlayerStatusEnum.observer.status.equals(player.getStatus())) {
					continue;
				}
				playerIList.add(player.getPlayerId());
			}
		}
		for(Integer playerId : playerIList){
			doDeductRoomCard(roomInfo.getGameType(), roomInfo.getPayType(), roomInfo.getTotalGames(), operationEnum, playerId);
		}
		return playerIList;
	}
	
	@Override
	public Integer doDeductRoomCard(Integer gameType, Integer payType, Integer totalGames, RoomCardOperationEnum operationEnum, Integer playerId){
		RoomCardConsumeEnum consumeEnum = RoomCardConsumeEnum.getRoomCardConsumeEnum(gameType, payType, totalGames);
		Map<String, Object> map = new HashMap<String, Object>();
		int re = 0;
		int reTryCount = 1;
		UserModel userModel = null;
		do {
			userModel = userDao.getUserById(playerId);
			map.put("playerId", playerId);
			map.put("deductNum", consumeEnum.needRoomCardNum);
			map.put("roomCardNum", userModel.getRoomCardNum());
			map.put("updateTime", userModel.getUpdateTime());
			log.info("doDeductRoomCard, map:" + JsonUtil.toJson(map));
			re = userDao.deductRoomCard(map);
			if (re == 1) {
				break;
			}
			reTryCount++;
			log.info("扣除房卡重试第" + reTryCount + "次");
		} while (reTryCount < 4);/**扣除房卡重试三次*/
		if (reTryCount == 4) {
			throw new BusinessException(ExceptionEnum.ROOM_CARD_DEDUCT_THREE_TIMES_FAIL);
		}
		RoomCardLogModel roomCardLogModel = new RoomCardLogModel();
		roomCardLogModel.setGameType(gameType);
		roomCardLogModel.setPlayerId(playerId);
		roomCardLogModel.setDiffRoomCardNum(consumeEnum.needRoomCardNum);
		roomCardLogModel.setPreRoomCardNum(userModel.getRoomCardNum());
		Integer curRoomCardNum = userModel.getRoomCardNum() - consumeEnum.needRoomCardNum;
		roomCardLogModel.setCurRoomCardNum(curRoomCardNum);
		roomCardLogModel.setOperatorId(playerId);
		roomCardLogModel.setOperatorType(operationEnum.type);
		roomCardLogModel.setCreateTime(new Date());
		roomCardLogDao.insertRoomCardLog(roomCardLogModel);
		return curRoomCardNum;
	}
	@Override
	public void insertFeedback(UserFeedbackModel model) {
		userFeedbackDao.insertFeedback(model);
	}
	@Override
	public List<UserRecordModel> getUserRecord(UserRecordModel model) {
		List<UserRecordModel> list = userRecordDao.getUserRecord(model);
		for(UserRecordModel userRecordModel : list){
			userRecordModel.setRecordList(JsonUtil.json2list(userRecordModel.getRecordInfo(), RecordModel.class));
			userRecordModel.setRecordInfo(null);
		}
		return list;
	}
	@Override
	public List<UserRecordModel> getUserRecordDetail(UserRecordModel model) {
		List<UserRecordModel> list = userRecordDetailDao.getUserRecordDetail(model);
		for(UserRecordModel userRecordModel : list){
			userRecordModel.setRecordList(JsonUtil.json2list(userRecordModel.getRecordInfo(), RecordModel.class));
			userRecordModel.setRecordInfo(null);
		}
		return list;
	}
	
	@Override
	public void addUserRecord(BaseRoomInfo roomInfo) {
		if (roomInfo.getCurGame() < 2) {
			return;
		}
		List playerList = roomInfo.getPlayerList();
		if (CollectionUtils.isEmpty(playerList)) {
			return;
		}
		int size = playerList.size();
		List<RecordModel> recordModelList = new ArrayList<RecordModel>();
		for(int i = 0; i < size; i++){
			BasePlayerInfo player = (BasePlayerInfo)playerList.get(i);
			RecordModel recordModel = new RecordModel();
//			recordModel.setHeadImgUrl(player.getHeadImgUrl());
			recordModel.setNickName(player.getNickName());
			recordModel.setPlayerId(player.getPlayerId());
			recordModel.setScore(player.getTotalScore());
			recordModelList.add(recordModel);
		}
		String recordInfo = JsonUtil.toJson(recordModelList);
		List<UserRecordModel> modelList = new ArrayList<UserRecordModel>();
		Date createTime = new Date();
		for(int i = 0; i < size; i++){
			BasePlayerInfo player = (BasePlayerInfo)playerList.get(i);
			UserRecordModel model = new UserRecordModel();
			model.setRecordUuid(roomInfo.getRoomUuid());
			model.setGameType(roomInfo.getGameType());
			model.setDetailType(roomInfo.getDetailType());
			model.setRoomId(roomInfo.getRoomId());
			if (roomInfo.getClubId() == null) {
				model.setClubId(0);
			}else{
				model.setClubId(roomInfo.getClubId());
			}
			model.setPayType(roomInfo.getPayType());
			model.setTotalGames(roomInfo.getTotalGames());
			model.setPlayerId(player.getPlayerId());
			model.setScore(player.getTotalScore());
			model.setRecordInfo(recordInfo);
			try {
				model.setRemark(JsonUtil.toJson(roomInfo.getRemark()));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			model.setCreateTime(createTime);
			modelList.add(model);
		}
		userRecordDao.batchInsertRecord(modelList);
	}
	
	@Override
	public void addUserRecordDetail(BaseRoomInfo roomInfo) {
		List playerList = roomInfo.getPlayerList();
		if (CollectionUtils.isEmpty(playerList)) {
			return;
		}
		int size = playerList.size();
		List<RecordModel> recordModelList = new ArrayList<RecordModel>();
		for(int i = 0; i < size; i++){
			BasePlayerInfo player = (BasePlayerInfo)playerList.get(i);
			RecordModel recordModel = new RecordModel();
//			recordModel.setHeadImgUrl(player.getHeadImgUrl());
			recordModel.setNickName(player.getNickName());
			recordModel.setPlayerId(player.getPlayerId());
			recordModel.setScore(player.getCurScore());
			recordModelList.add(recordModel);
		}
		String recordInfo = JsonUtil.toJson(recordModelList);
		
		UserRecordModel model = new UserRecordModel();
		model.setRecordUuid(roomInfo.getRoomUuid());
		model.setRoomId(roomInfo.getRoomId());
		if (roomInfo.getClubId() == null) {
			model.setClubId(0);
		}else{
			model.setClubId(roomInfo.getClubId());
		}
		
		model.setRecordDetailUuid(roomInfo.getCurGameUuid());
		model.setCurGame(roomInfo.getCurGame());
		model.setRecordInfo(recordInfo);
		userRecordDetailDao.insertRecordDetail(model);
		
	}
	@Override
	public void roomCardCheck(Integer playerId, Integer gameType, Integer payType, Integer totalGames) {
		RoomCardConsumeEnum consumeEnum = RoomCardConsumeEnum.getRoomCardConsumeEnum(gameType,payType, totalGames);
		if (consumeEnum == null) {
			throw new BusinessException(ExceptionEnum.PARAMS_ERROR);
		}
		UserModel userModel = userDao.getUserById(playerId);
		Integer roomCardNum = userModel.getRoomCardNum();
		if (roomCardNum < consumeEnum.needRoomCardNum) {
			throw new BusinessException(ExceptionEnum.ROOM_CARD_NOT_ENOUGH);
		}
	}
	@Override
	public Long insertOrder(Integer playerId, Integer productId,
			Integer roomCardNum, Integer price) {
		OrderModel orderModel = new OrderModel();
		orderModel.setPlayerId(playerId);
		orderModel.setProductId(productId);
		orderModel.setRoomCardNum(roomCardNum);
		orderModel.setPrice(price);
		Integer res = orderDao.insertOrder(orderModel);
		if (res <= 0) {
			throw new BusinessException(ExceptionEnum.INSERT_ORDER_FAIL);
		}
		return orderModel.getOrderId();
	}
	@Transactional
	@Override
	public Integer updateOrderAndUser(Integer playerId, Integer addRoomCardNum, Long orderId, String transactionId, Integer wxPayPrice) {
		
		OrderModel orderModel = new OrderModel();
		orderModel.setOrderId(orderId);
		orderModel.setTransactionId(transactionId);
		orderModel.setWxPayPrice(wxPayPrice);
		/**更新订单的最终支付状态*/
		Integer res = orderDao.updateOrder(orderModel);
		if (res <= 0) {
			throw new BusinessException(ExceptionEnum.UPDATE_ORDER_FAIL);
		}
		/**更新用户的房卡数*/
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("playerId", playerId);
		map.put("addNum", addRoomCardNum);
		res = userDao.addRoomCard(map);
		if (res <= 0) {
			throw new BusinessException(ExceptionEnum.UPDATE_USER_ROOM_CARD_FAIL);
		}
		
		/**根据playerId去t_proxy_user表里面查proxy_id*/
		Integer proxyId = proxyDao.getProxyIdByPlayerId(playerId);
		
		/*************如果没有绑定代理则不走代理提成流程****************************************/
		if (proxyId == null) {
			/**查询用户当前房卡数并返回*/
			UserModel user = userDao.getUserById(playerId);
			return user.getRoomCardNum();
		}
		/**根据proxy_id查询当前代理的总收益及当前账户余额，主要是为更新做准备，防止更新覆盖*/
		ProxyModel resModel = proxyDao.getProxyInfo(proxyId);
		/**更新代理总收益,用户充值的一半分给代理*/
		ProxyModel proxyModel = new ProxyModel();
		Integer temp = wxPayPrice/2;
		/**总收益，防止覆盖更新*/
		proxyModel.setTotalIncome(resModel.getTotalIncome());
		proxyModel.setCurIncome(temp);
		/**更新代理账户余额,用户充值的一半分给代理*/
		/**账户余额，防止覆盖更新*/
		proxyModel.setRemainderAmount(resModel.getRemainderAmount());
		proxyModel.setCurRemainder(temp);
		/**提现金额，防止覆盖更新*/
		proxyModel.setExtractAmount(resModel.getExtractAmount());
		proxyModel.setProxyId(proxyId);
		/**以总收益、账户余额、已提现金额当做版本号更新代理总收益及账户余额*/
		res = proxyDao.updateProxyInfo(proxyModel);
		if (res <= 0) {
			throw new BusinessException(ExceptionEnum.UPDATE_PROXY_INCOME_FAIL);
		}
		/**查询用户当前房卡数并返回*/
		UserModel user = userDao.getUserById(playerId);
		return user.getRoomCardNum();
	}
	@Override
	public ProductModel getProductById(Integer productId) {
		return productDao.getProductById(productId);
	}
	@Override
	public List<ProductModel> getProductList() {
		return productDao.getProductList();
	}
	@Override
	public OrderModel getOderByOrderId(Long orderId) {
		return orderDao.getOderByOrderId(orderId);
	}
	@Override
	public UserModel getUserById(Integer playerId) {
		return userDao.getUserById(playerId);
	}
	
	@Override
	public void insertProxyUser(Integer proxyId, Integer playerId, String nickName) {
		ProxyModel model = new ProxyModel();
		model.setProxyId(proxyId);
		model.setPlayerId(playerId);
		model.setNickName(nickName);
		Integer res = proxyDao.insertProxyUser(model);
		if (res <= 0) {
			throw new BusinessException(ExceptionEnum.BIND_PROXY_FAIL);
		}
	}
	@Override
	public Integer getProxyCountByProxyId(Integer proxyId) {
		return proxyDao.getProxyCountByProxyId(proxyId);
	}
	@Override
	public Integer getProxyUserCountByPlayerId(Integer playerId) {
		return proxyDao.getProxyUserCountByPlayerId(playerId);
	}
	@Override
	public Integer getProxyIdByPlayerId(Integer playerId) {
		return proxyDao.getProxyIdByPlayerId(playerId);
	}
	@Override
	public Integer addRoomCard(Map<String, Object> map) {
		return userDao.addRoomCard(map);
	}
	@Override
	public Integer updateUserByPlayerId(UserModel userModel) {
		return userDao.updateUserByPlayerId(userModel);
	}
	@Override
	public long batchInsertPlayBack(List<PlayBackModel> modelList) {
		return recordPlayBackDao.batchInsertPlayBack(modelList);
	}
	@Override
	public List<String> getPlayBack(Long recordDetailUuid) {
		PlayBackModel model = new PlayBackModel();
		model.setRecordDetailUuid(recordDetailUuid);
		return recordPlayBackDao.getPlayBack(model);
	}
	@Override
	public UserModel getUserByExtensionCode(Integer extensionCode){
		UserModel model = new UserModel();
		model.setExtensionCode(extensionCode);
		List<UserModel> list = userDao.getUserByCondition(model);
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}
	@Override
	public ExtensionCodeBindModel getExtensionCodeBindLogByPlayerId(
			Integer playerId) {
		ExtensionCodeBindModel model = new ExtensionCodeBindModel();
		model.setPlayerId(playerId);
		List<ExtensionCodeBindModel> list = extensionCodeBindDao.getExtensionCodeBindLogByCondition(model);
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		return list.get(0);
	}
	@Override
	public void insertExtensionCodeBindLog(Integer extensionCode,
			Integer playerId, String nickName) {
		ExtensionCodeBindModel model = new ExtensionCodeBindModel();
		model.setExtensionCode(extensionCode);
		model.setPlayerId(playerId);
		model.setNickName(nickName);
		extensionCodeBindDao.insertExtensionCodeBindLog(model);
	}
	@Override
	public UserRecordModel getRoomRemarkByUuid(Long uuid) {
		UserRecordModel model = new UserRecordModel();
		model.setRecordUuid(uuid);
		return userRecordDao.getRoomRemarkByUuid(model);
	}
	
	@Override
	public Integer createClub(String clubName, String clubOwnerWord,
			Integer status, Integer playerId, String nickName, String headImgUrl) {
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubName(clubName);
		gameQuery.setClubOwnerWord(clubOwnerWord);
		/**俱乐部是否需要审核，1：需要 0：不需要*/
		gameQuery.setStatus(status);
		gameQuery.setPlayerId(playerId);
		gameQuery.setNickName(nickName);
		gameQuery.setWechatNum(nickName);
		gameQuery.setHeadImgUrl(headImgUrl);
		/**俱乐部剩余房卡数*/
		gameQuery.setRoomCardNum(0);
		/**创建俱乐部*/
		gameDao.insertProxyClub(gameQuery);
//		GameQuery gameQuery1 = new GameQuery();
//		gameQuery1.setClubId(gameQuery.getClubId());
//		gameQuery1.setPlayerId(playerId);
//		gameQuery1.setNickName(nickName);
//		gameQuery1.setHeadImgUrl(headImgUrl);
//		gameQuery1.setStatus(1);
//		/**默认创始人也加入俱乐部，并且不需要审核*/
//		gameDao.insertClubUser(gameQuery1);
		return gameQuery.getClubId();
	}
	@Override
	public void delClub(Integer clubId) {
		/**删除俱乐部玩家*/
		GameQuery gameQuery = new GameQuery();
		gameQuery.setClubId(clubId);
		gameQuery.setPlayerId(null);
		gameDao.delClubUser(gameQuery);
		gameDao.delProxyClub(gameQuery);
	}
}
