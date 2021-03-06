package cn.worldwalker.game.wyqp.common.constant;

import cn.worldwalker.game.wyqp.common.utils.CustomizedPropertyConfigurer;

public class Constant {
	
	public final static String curCompany = CustomizedPropertyConfigurer.getContextProperty("cur.company");
	/**roomId与roomInfo的映射*/
	public final static String roomIdRoomInfoMap = curCompany + "_room_id_room_info_map";
	/**roomId与gameType,updateTime的映射*/
	public final static String roomIdGameTypeUpdateTimeMap = curCompany + "_room_id_game_type_update_time_map";
	/**playerId与roomId,gameType的映射*/
	public final static String playerIdRoomIdGameTypeMap = curCompany + "_player_id_room_id_game_type_map";
	/**offline playerId与roomId,gameType,time的映射关系*/
	public final static String offlinePlayerIdRoomIdGameTypeTimeMap = curCompany + "_offline_player_id_romm_id_game_type_time_map";
	
	
	/**ip与此ip上连接数的映射关系*/
	public final static String ipConnectCountMap = curCompany + "_ip_connect_count_map";
	/**房卡操作失败数据list*/
	public final static String roomCardOperationFailList = curCompany + "_room_card_operation_fail_list";
	
	/**请求和返回信息日志打印开关*/
	public final static String logInfoFuse = curCompany + "_log_info_fuse";
	/**登录切换开关*/
	public final static String loginFuse = curCompany + "_login_fuse";
	
	/**创建房间切换开关，主要是为了升级的时候，先停止玩家创建房间，再升级*/
	public final static String createRoomFuse = curCompany + "_create_room_fuse";
	
	/**本机ip地址*/
	public final static String localIp = CustomizedPropertyConfigurer.getContextProperty("local.ip");
	
	public final static Integer websocketPort = Integer.valueOf(CustomizedPropertyConfigurer.getContextProperty("websocket.port"));
	
	/**牛牛中，庄类型为抢庄的时候，机器ip与房间id，time的list 映射*/
	public final static String nnRobIpRoomIdTimeMap = curCompany + "_nn_rob_ip_room_id_time_map_" + localIp;
	/**牛牛中，5秒定时器，自动翻牌*/
	public final static String nnShowCardIpRoomIdTimeMap = curCompany + "_nn_show_card_ip_room_id_time_map_" + localIp;
	/**炸金花两个玩家开始准备后，倒计时10秒，没准备的玩家直接设置为观察者，其余的玩家继续游戏*/
	public final static String notReadyIpRoomIdTimeMap = curCompany + "_not_ready_ip_room_id_time_map_" + localIp;
	/**解散房间标志位*/
	public final static String dissolveIpRoomIdTimeMap = curCompany + "_dissolve_room_ip_room_id_time_map_" + localIp;
	/**炸金花玩家超过30s没操作，则自动弃牌*/
	public final static String jhNoOperationIpPlayerIdRoomIdTimeMap = curCompany + "_jh_no_operation_ip_player_id_room_id_time_map_" + localIp;
	
	/**sms短信验证码超时map*/
	public final static String smsValidCodeMap = curCompany + "_sms_valide_code_map";

	public static String noticeMsg = "游戏忠告:文明游戏，禁止赌博及其他违法行为  游戏代理及相关咨询加微信：" + CustomizedPropertyConfigurer.getContextProperty("proxy.cus.weixin");

	
	/******************金花相关********************/
	/**底注*/
	public final static Integer stakeButtom = 1;
	/**押注的上限*/
	public final static Integer stakeLimit = 10;
	/**跟注次数上限*/
	public final static Integer stakeTimesLimit = 30;
	
	
	/***微信appid,appsecrect**/
	public final static String APPID = CustomizedPropertyConfigurer.getContextProperty("APPID");// 应用号
	public final static String APP_SECRECT = CustomizedPropertyConfigurer.getContextProperty("APP_SECRECT");// 应用密码
	
	
	/**微信登录相关*/
	public static String getWXUserInfoUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=" + "ACCESS_TOKEN&openid=OPENID";
	public static String getOpenidAndAccessCode = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + APPID + "&secret=" + APP_SECRECT + "&grant_type=authorization_code&code=CODE";
	
	/**微信支付相关*/
	public final static String MCH_ID = CustomizedPropertyConfigurer.getContextProperty("MCH_ID");// 商户号 xxxx 公众号商户id
	public final static String API_KEY = CustomizedPropertyConfigurer.getContextProperty("API_KEY");// API密钥
	public final static String SIGN_TYPE = "MD5";// 签名加密方式
	public final static String TRADE_TYPE = "APP";// 支付类型
	/**微信支付统一订单接口*/
	public final static String UNIFIED_ORDER_URL = "https://api.mch.weixin.qq.com/pay/unifiedorder";
	/**微信支付回调地址*/
	public final static String WEIXIN_PAY_CALL_BACK_URL = CustomizedPropertyConfigurer.getContextProperty("weixin.pay.call.back.url");
	public final static String getAccessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + APPID + "&secret=" + APP_SECRECT;
	public final static String getTicketUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=ACCESS_TOKEN&type=jsapi";
	
	public final static String domain = CustomizedPropertyConfigurer.getContextProperty("domain");
	public final static String staticNginxRoot = CustomizedPropertyConfigurer.getContextProperty("static.nginx.root");
	public final static String weixinHeadimgFolder = CustomizedPropertyConfigurer.getContextProperty("weixin.headimg.folder");
	public final static String h5GameStaticFolder = CustomizedPropertyConfigurer.getContextProperty("h5.game.static.folder");
	
	public final static String localWxHeadImgPath = staticNginxRoot + weixinHeadimgFolder;
	
	public final static String downloadWxHeadImgUrl = "http://" + domain + weixinHeadimgFolder;
	
	public final static int userInfoOverTimeLimit = Integer.valueOf(CustomizedPropertyConfigurer.getContextProperty("user.info.over.time.limit"));
	
	public final static int gameInfoStorageType = Integer.valueOf(CustomizedPropertyConfigurer.getContextProperty("game.info.storage.type"));
	
	public final static String clientFileUploadFolder = CustomizedPropertyConfigurer.getContextProperty("client.file.upload.folder");
	
	public final static String clientFileUnrarFolder = CustomizedPropertyConfigurer.getContextProperty("client.file.unrar.folder");
	
	public final static String clientFileUploadPath = staticNginxRoot + clientFileUploadFolder;
	
	public final static String clientFileUnrarPath = staticNginxRoot + clientFileUnrarFolder;
	
	public final static String voiceFileUploadPath = staticNginxRoot + CustomizedPropertyConfigurer.getContextProperty("voice.folder");
	
	public static final String UPDATE_RUL = "http://" + domain + clientFileUnrarFolder  + "VERSION";//"D:/test/";
	
	public static final String CODE_URL = "http://" + domain + clientFileUnrarFolder + "VERSION/game_code_VERSION.zip";//"D:/test/";
	
	public static final int isTest = Integer.valueOf(CustomizedPropertyConfigurer.getContextProperty("is.test"));

	public static final String mjTablePath =CustomizedPropertyConfigurer.getContextProperty("mj.table.path");
//    public static final String mjTablePath = "C:/Users/jinfeng.liu/Desktop/game/tables/";

	public static final String sendSmsUrl = CustomizedPropertyConfigurer.getContextProperty("send.sms.url");
	
	public static final String smsAppId = CustomizedPropertyConfigurer.getContextProperty("sms.app.id");
	
	public static final String smsApiKey = CustomizedPropertyConfigurer.getContextProperty("sms.api.key");
	
	public static final String smsContent = "您的验证码是：CODE。请不要把验证码泄露给其他人。";
	
	/**playerId与clubId的映射*/
	public final static String playerIdClubIdMap = curCompany + "_player_id_club_id_map";
	/**clubId与roomId关系映射*/
	public final static String clubIdRoomIdSet = curCompany + "_club_id_room_id_set_";
	
	public final static String playerIdTimeMap = curCompany + "_player_id_time_map";
	
	public static final String logFuseValue = CustomizedPropertyConfigurer.getContextProperty("log.fuse.value");
	
	public static final String loginFuseValue = CustomizedPropertyConfigurer.getContextProperty("login.fuse.value");
	
	public static final String createRoomFuseValue = CustomizedPropertyConfigurer.getContextProperty("create.room.fuse.value");
	
	public static final String apiSecret = CustomizedPropertyConfigurer.getContextProperty("api.secret");
	
	public static final String useWss = CustomizedPropertyConfigurer.getContextProperty("wss.use");
    public static final String wssDomain = CustomizedPropertyConfigurer.getContextProperty("wss.domain");
    public static final String wssPort = CustomizedPropertyConfigurer.getContextProperty("wss.port");

}


