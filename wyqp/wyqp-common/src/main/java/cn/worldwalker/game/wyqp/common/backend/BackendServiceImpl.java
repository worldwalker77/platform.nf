package cn.worldwalker.game.wyqp.common.backend;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.worldwalker.game.wyqp.common.constant.Constant;
import cn.worldwalker.game.wyqp.common.dao.GameDao;
import cn.worldwalker.game.wyqp.common.exception.BusinessException;
import cn.worldwalker.game.wyqp.common.exception.ExceptionEnum;
import cn.worldwalker.game.wyqp.common.result.Result;
import cn.worldwalker.game.wyqp.common.utils.JsonUtil;
import cn.worldwalker.game.wyqp.common.utils.MD5Util1;
import cn.worldwalker.game.wyqp.common.utils.RequestUtil;

@Service
public class BackendServiceImpl implements BackendService{
	private static final Logger log = Logger.getLogger(BackendServiceImpl.class);
	@Autowired
	private GameDao gameDao;
	@Autowired
	private BackendManager gameManager;
	
	private static Map<String, String> adminPhoneMap = new HashMap<String, String>();
	static{
		adminPhoneMap.put(Constant.adminMobile, Constant.adminMobile);
//		adminPhoneMap.put("13006339022", "13006339022");
	}
	@Override
	public boolean isAdmin(){
		String mobile = RequestUtil.getUserSession().getMobilePhone();
		return adminPhoneMap.containsKey(mobile);
	}
	@Override
	public Result doLogin(GameQuery gameQuery) {
		Result result = new Result();
		GameModel gameModel = gameDao.getProxyByPhoneAndPassword(gameQuery);
		if (gameModel == null) {
			result.setCode(1);
			result.setDesc("账号名或密码错误");
			return result;
		}
		UserSession userSession = new UserSession();
		userSession.setProxyId(gameModel.getProxyId());
		userSession.setPlayerId(gameModel.getPlayerId());
		userSession.setNickName(gameModel.getNickName());
		userSession.setRealName(gameModel.getRealName());
		userSession.setWechatNum(gameModel.getWechatNum());
		userSession.setMobilePhone(gameModel.getMobilePhone());
		RequestUtil.setUserSession(genToken(gameQuery.getMobilePhone()), userSession);
		return result;
	}
	public String genToken(String mobilePhone){
		String temp = mobilePhone + System.currentTimeMillis() + Thread.currentThread().getName();
		return MD5Util1.encryptByMD5(temp);
	}
	@Override
	public Result getProxyInfo(GameQuery gameQuery) {
		Result result = new Result();
		GameModel gameModel = null;
		try {
			gameModel = gameDao.getProxyInfo(gameQuery);
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setDesc("系统异常");
			return result;
		}
		if (gameModel == null) {
			result.setCode(1);
			result.setDesc("代理信息不存在");
			return result;
		}
		result.setData(gameModel);
		return result;
	}

	@Override
	public Result getBillingDetails(GameQuery gameQuery) {
		if (!isAdmin()) {
			gameQuery.setProxyId(RequestUtil.getUserSession().getProxyId());
		}
		gameQuery.setStartDate(gameQuery.getStartDate() + " 00:00:00");
		gameQuery.setEndDate(gameQuery.getEndDate()  + " 23:59:59");
		Result result = new Result();
		List<GameModel> list = null;
		Long total = 0L;
		try {
			list = gameDao.getBillingDetails(gameQuery);
			total = gameDao.getBillingDetailsCount(gameQuery);
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setDesc("系统异常");
			return result;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", total);
		map.put("rows", list);
		result.setData(map);
		return result;
	}

	@Override
	public Result getMyMembers(GameQuery gameQuery) {
		if (!isAdmin()) {
			gameQuery.setProxyId(RequestUtil.getUserSession().getProxyId());
		}
		gameQuery.setStartDate(gameQuery.getStartDate() + " 00:00:00");
		gameQuery.setEndDate(gameQuery.getEndDate()  + " 23:59:59");
		Result result = new Result();
		List<GameModel> list = null;
		Long total = 0L;
		try {
			list = gameDao.getMyMembers(gameQuery);
			total = gameDao.getMyMembersCount(gameQuery);
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setDesc("系统异常");
			return result;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", total);
		map.put("rows", list);
		result.setData(map);
		return result;
	}

	@Override
	public Result getWithDrawalRecords(GameQuery gameQuery) {
		if (!isAdmin()) {
			gameQuery.setProxyId(RequestUtil.getUserSession().getProxyId());
		}
		gameQuery.setStartDate(gameQuery.getStartDate() + " 00:00:00");
		gameQuery.setEndDate(gameQuery.getEndDate()  + " 23:59:59");
		Result result = new Result();
		List<GameModel> list = null;
		Long total = 0L;
		try {
			list = gameDao.getWithDrawalRecords(gameQuery);
			total = gameDao.getWithDrawalRecordsCount(gameQuery);
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setDesc("系统异常");
			return result;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", total);
		map.put("rows", list);
		result.setData(map);
		return result;
	}
	@Override
	public Result getUserByCondition(GameQuery gameQuery) {
		List<GameModel> modelList = gameDao.getUserByCondition(gameQuery);
		Result result = new Result();
		result.setData(modelList);
		return result;
	}
	@Override
	public Result doGiveAwayRoomCards(Integer toPlayerId, Integer roomCardNum) {
		Result result = new Result();
		try {
			gameManager.giveAwayRoomCards(toPlayerId, roomCardNum);
		} catch (BusinessException e) {
			result.setCode(e.getBussinessCode());
			result.setDesc(e.getMessage());
		} catch (Exception e) {
			result.setCode(ExceptionEnum.SYSTEM_ERROR.index);
			result.setDesc(ExceptionEnum.SYSTEM_ERROR.description);
		}
		return result;
	}
	@Override
	public Result getWinProbability(GameQuery gameQuery) {
		Result result = new Result();
		/**如果不是管理员账号，则不让查看*/
		if (!isAdmin()) {
			result.setCode(1);
			result.setDesc("无权限");
			return result;
		}
		
		List<GameModel> list = null;
		Long total = 0L;
		try {
			list = gameDao.getUserByCondition(gameQuery);
			total = gameDao.getUserByConditionCount(gameQuery);
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setDesc("系统异常");
			return result;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", total);
		map.put("rows", list);
		result.setData(map);
		return result;
	}
	@Override
	public Result modifyWinProbability(GameQuery gameQuery) {
		Result result = new Result();
		/**如果不是管理员账号，则不让查看*/
		if (!isAdmin()) {
			result.setCode(1);
			result.setDesc("无权限");
			return result;
		}
		if (gameQuery.getWinProbability() < 0 || gameQuery.getWinProbability() > 100) {
			result.setCode(1);
			result.setDesc("概率范围为0-100");
			return result;
		}
		try {
			gameDao.updateProbabilityByPlayerId(gameQuery);
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setDesc("系统异常");
			return result;
		}
		return result;
	}
	@Override
	public Result getProxys(GameQuery gameQuery) {
		if (!isAdmin()) {
			gameQuery.setProxyId(RequestUtil.getUserSession().getProxyId());
		}
		gameQuery.setStartDate(gameQuery.getStartDate() + " 00:00:00");
		gameQuery.setEndDate(gameQuery.getEndDate()  + " 23:59:59");
		Result result = new Result();
		List<GameModel> list = null;
		Long total = 0L;
		try {
			list = gameDao.getProxys(gameQuery);
			total = gameDao.getProxysCount(gameQuery);
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setDesc("系统异常");
			return result;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", total);
		map.put("rows", list);
		result.setData(map);
		return result;
	}
	@Override
	public Result modifyProxy(GameQuery gameQuery) {
		Result result = new Result();
		if (!isAdmin()) {
			result.setCode(1);
			result.setDesc("无权限");
			return result;
		}
		if (gameQuery.getProxyId() == null 
			|| StringUtils.isBlank(gameQuery.getMobilePhone())
			|| gameQuery.getPlayerId() == null
			|| StringUtils.isBlank(gameQuery.getWechatNum())) {
			result.setCode(1);
			result.setDesc("参数不能为空");
			return result;
		}
		try {
			GameQuery tempQuery = new GameQuery();
			tempQuery.setPlayerId(gameQuery.getPlayerId());
			List<GameModel> list = gameDao.getUserByCondition(tempQuery);
			if (CollectionUtils.isEmpty(list)) {
				result.setCode(1);
				result.setDesc("游戏id不存在");
				return result;
			}
			if (gameQuery.getProxyId() > 0) {
				gameDao.updateProxy(gameQuery);
			}else{
				gameQuery.setPassword(gameQuery.getMobilePhone());
				gameDao.insertProxy(gameQuery);
			}
		} catch (Exception e) {
			log.error("modifyProxy异常，gameQuery：" + JsonUtil.toJson(gameQuery), e);
			result.setCode(1);
			result.setDesc("数据库异常");
			return result;
		}
		return result;
	}
	@Override
	public Result doModifyPassword(GameQuery gameQuery) {
		
		Result result = new Result();
		if (StringUtils.isBlank(gameQuery.getMobilePhone())
			|| StringUtils.isBlank(gameQuery.getOldPassword())
			|| StringUtils.isBlank(gameQuery.getNewPassword())) {
			result.setCode(1);
			result.setDesc("参数不能为空");
			return result;
		}
		try {
			GameQuery tempQuery = new GameQuery();
			tempQuery.setMobilePhone(gameQuery.getMobilePhone());
			tempQuery.setPassword(gameQuery.getOldPassword());
			tempQuery.setProxyId(RequestUtil.getProxyId());
			GameModel tempGameModel = gameDao.getProxyByPhoneAndPassword(tempQuery);
			if (null == tempGameModel) {
				result.setCode(1);
				result.setDesc("手机号或者老密码错误");
				return result;
			}
			GameQuery tempQuery1 = new GameQuery();
			tempQuery1.setProxyId(RequestUtil.getProxyId());
			tempQuery1.setNewPassword(gameQuery.getNewPassword());
			gameDao.updateProxy(gameQuery);
		} catch (Exception e) {
			log.error("doModifyPassword异常，gameQuery：" + JsonUtil.toJson(gameQuery), e);
			result.setCode(1);
			result.setDesc("数据库异常");
			return result;
		}
		return result;
	}
	@Override
	public Result getProxyClubs(GameQuery gameQuery) {
		if (!isAdmin()) {
			gameQuery.setProxyId(RequestUtil.getUserSession().getProxyId());
		}
		Result result = new Result();
		List<GameModel> list = null;
		Long total = 0L;
		try {
			list = gameDao.getProxyClubs(gameQuery);
			total = gameDao.getProxyClubsCount(gameQuery);
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setDesc("系统异常");
			return result;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", total);
		map.put("rows", list);
		result.setData(map);
		return result;
	}
	/**
	 * 增加俱乐部/修改俱乐部
	 */
	@Override
	public Result modifyProxyClub(GameQuery gameQuery) {
		Result result = new Result();
		if (gameQuery.getClubId() == null ) {
			result.setCode(1);
			result.setDesc("参数不能为空");
			return result;
		}
		gameQuery.setProxyId(RequestUtil.getProxyId());
		gameQuery.setPlayerId(RequestUtil.getPlayerId());
		try {
			if (gameQuery.getClubId() > 0) {
				gameDao.updateProxyClub(gameQuery);
			}else{
				gameDao.insertProxyClub(gameQuery);
			}
		} catch (Exception e) {
			log.error("modifyProxyClub异常，gameQuery：" + JsonUtil.toJson(gameQuery), e);
			result.setCode(1);
			result.setDesc("数据库异常");
			return result;
		}
		return result;
	}
	@Override
	public Result insertClubUser(GameQuery gameQuery) {
		/**加入俱乐部，前台操作*/
		return null;
	}
	@Override
	public Result getClubUsers(GameQuery gameQuery) {
		if (!isAdmin()) {
			gameQuery.setProxyId(RequestUtil.getUserSession().getProxyId());
		}
		Result result = new Result();
		List<GameModel> list = null;
		Long total = 0L;
		try {
			list = gameDao.getClubUsers(gameQuery);
			total = gameDao.getClubUsersCount(gameQuery);
		} catch (Exception e) {
			e.printStackTrace();
			result.setCode(1);
			result.setDesc("系统异常");
			return result;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("total", total);
		map.put("rows", list);
		result.setData(map);
		return result;
	}
	/**
	 * 添加俱乐部玩家
	 * 
	 */
	@Override
	public Result modifyClubUser(GameQuery gameQuery) {
		Result result = new Result();
		if (gameQuery.getClubId() == null || gameQuery.getPlayerId() == null  || gameQuery.getStatus() == null ) {
			result.setCode(1);
			result.setDesc("参数不能为空");
			return result;
		}
		if (gameQuery.getStatus() != 0 && gameQuery.getStatus() != 1) {
			result.setCode(1);
			result.setDesc("审核状态必须为1或者0");
			return result;
		}
		Integer proxyId = RequestUtil.getProxyId();
		/**俱乐部是否存在*/
		GameQuery tempQuery  = new GameQuery();
		tempQuery.setClubId(gameQuery.getClubId());
		tempQuery.setProxyId(proxyId);
		List<GameModel> modelist = gameDao.getProxyClubs(tempQuery);
		if (CollectionUtils.isEmpty(modelist)) {
			result.setCode(1);
			result.setDesc("您没有此俱乐部");
			return result;
		}
		/**玩家是否存在**/
//		GameQuery tempQuery1  = new GameQuery();
//		tempQuery1.setPlayerId(gameQuery.getPlayerId());
//		modelist = gameDao.getUserByCondition(tempQuery1);
//		if (CollectionUtils.isEmpty(modelist)) {
//			result.setCode(1);
//			result.setDesc("玩家id不存在");
//			return result;
//		}
		
		gameQuery.setProxyId(proxyId);
//		gameQuery.setNickName(modelist.get(0).getNickName());
		try {
			if (gameQuery.getId() > 0) {
				gameDao.updateClubUser(gameQuery);
			}else{
				gameDao.insertClubUser(gameQuery);
			}
		} catch (Exception e) {
			log.error("modifyClubUser异常，gameQuery：" + JsonUtil.toJson(gameQuery), e);
			result.setCode(1);
			result.setDesc("数据库异常");
			return result;
		}
		return result;
	}
	@Override
	public Result auditClubUser(GameQuery gameQuery) {
		Result result = new Result();
		if (gameQuery.getClubId() == null || gameQuery.getPlayerId() == null) {
			result.setCode(1);
			result.setDesc("参数不能为空");
			return result;
		}
		Integer proxyId = RequestUtil.getProxyId();
		/**判断玩家是否在此代理的俱乐部中*/
		GameQuery tempQuery  = new GameQuery();
		tempQuery.setClubId(gameQuery.getClubId());
		tempQuery.setProxyId(proxyId);
		tempQuery.setPlayerId(gameQuery.getPlayerId());
		List<GameModel> modelist = gameDao.getClubUsers(gameQuery);
		if (CollectionUtils.isEmpty(modelist)) {
			result.setCode(1);
			result.setDesc("此玩家不在您的俱乐部中");
			return result;
		}
		gameQuery.setProxyId(proxyId);
		gameQuery.setStatus(1);
		gameDao.updateClubUser(gameQuery);
		return result;
	}
	@Override
	public Result delClubUser(GameQuery gameQuery) {
		Result result = new Result();
		if (gameQuery.getClubId() == null || gameQuery.getPlayerId() == null) {
			result.setCode(1);
			result.setDesc("参数不能为空");
			return result;
		}
		Integer proxyId = RequestUtil.getProxyId();
		/**判断玩家是否在此代理的俱乐部中*/
		GameQuery tempQuery  = new GameQuery();
		tempQuery.setClubId(gameQuery.getClubId());
		tempQuery.setProxyId(proxyId);
		tempQuery.setPlayerId(gameQuery.getPlayerId());
		List<GameModel> modelist = gameDao.getClubUsers(gameQuery);
		if (CollectionUtils.isEmpty(modelist)) {
			result.setCode(1);
			result.setDesc("此玩家不在您的俱乐部中");
			return result;
		}
		gameQuery.setProxyId(proxyId);
		gameDao.delClubUser(gameQuery);
		return result;
	}
	@Override
	public Result delProxyClub(GameQuery gameQuery) {
		Result result = new Result();
		if (gameQuery.getClubId() == null) {
			result.setCode(1);
			result.setDesc("参数不能为空");
			return result;
		}
		Integer proxyId = RequestUtil.getProxyId();
		/**判断俱乐部是否属于此代理*/
		GameQuery tempQuery  = new GameQuery();
		tempQuery.setClubId(gameQuery.getClubId());
		tempQuery.setProxyId(proxyId);
		List<GameModel> modelist = gameDao.getProxyClubs(tempQuery);
		if (CollectionUtils.isEmpty(modelist)) {
			result.setCode(1);
			result.setDesc("无权限删除此俱乐部");
			return result;
		}
		gameQuery.setProxyId(proxyId);
		gameDao.delProxyClub(gameQuery);
		return result;
	}

}
