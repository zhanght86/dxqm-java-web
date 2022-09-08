package jnpf.message.websocket;

import com.alibaba.fastjson.JSONObject;
import jnpf.base.PageModel;
import jnpf.base.UserInfo;
import jnpf.config.ConfigValueUtil;
import jnpf.database.data.DataSourceContextHolder;
import jnpf.message.entity.ImContentEntity;
import jnpf.message.entity.MessageEntity;
import jnpf.message.service.ImContentService;
import jnpf.message.service.MessageService;
import jnpf.message.model.ImUnreadNumModel;
import jnpf.model.OnlineUserModel;
import jnpf.model.OnlineUserProvider;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.*;
import jnpf.util.JsonUtil;
import jnpf.util.context.SpringContext;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息聊天
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Slf4j
@Component
@ServerEndpoint(value = "/api/message/websocket")
@Scope("prototype")
public class WebSocket {

    private UserProvider userProvider;
    private ImContentService imContentService;
    private MessageService messageService;
    private ConfigValueUtil configValueUtil;
    private UserInfo userInfo;
    private RedisUtil redisUtil;
    private CacheKeyUtil cacheKeyUtil;
    private UserService userService;


    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        log.info("连接上来:" + session.getId());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        OnlineUserModel user = OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getConnectionId().equals(session.getId())).findFirst().isPresent() ? OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getConnectionId().equals(session.getId())).findFirst().get() : null;
        if (user != null) {
            String userId = user.getUserId();
            OnlineUserProvider.getOnlineUserList().remove(user);
            //通知所有在线，有用户离线
            for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList().stream().filter(t -> !userId.equals(t.getUserId()) && userInfo.getTenantId().equals(t.getTenantId())).collect(Collectors.toList())) {
                if (!item.getUserId().equals(userInfo.getUserId())) {
                    JSONObject map = new JSONObject();
                    map.put("method", "Offline");
                    map.put("userId", userInfo.getUserId());
                    synchronized (session) {
                        try {
                            item.getWebSocket().getBasicRemote().sendText(map.toJSONString());
                        } catch (Exception e) {
                            log.error("通知用户离线发生错误：" + e.getMessage());
                        }
                    }
                }
            }
            log.info("调用onclose,关闭的租户用户为:" + user.getTenantId() + "." + userId);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("消息内容:" + message);
        JSONObject receivedMessage = JSONObject.parseObject(message);
        String receivedMethod = receivedMessage.getString("method");
        String receivedToken = receivedMessage.getString("token");
        this.init();
        //测试版本可以关闭验证
//        if ("false".equals(configValueUtil.getTestVersion()) && StringUtil.isNotEmpty(receivedToken)) {
//            UserInfo userInfo = userProvider.get(receivedToken);
//            String tenant = StringUtil.isNotEmpty(userInfo.getTenantId()) ? userInfo.getTenantId() : "";
//            String online = String.valueOf(redisUtil.getString(tenant + CacheKeyUtil.LOGINONLINE + userInfo.getUserId()));
//            String apponline = String.valueOf(redisUtil.getString(tenant + CacheKeyUtil.MOBILELOGINONLINE + userInfo.getUserId()));
//            if (userInfo.getId() == null || (!online.equals(userInfo.getId()) && !apponline.equals(userInfo.getId()))) {
//                JSONObject object = new JSONObject();
//                object.put("method", "logout");
//                if (userInfo.getId() != null && redisUtil.exists(userInfo.getId())) {
//                    redisUtil.remove(userInfo.getId());
//                }
//                session.getAsyncRemote().sendText(object.toJSONString());
//            }
//        }
        switch (receivedMethod) {
            case "OnConnection":
                //建立连接
                System.out.println("开启新连接");
                //app-true, PC-false
                Boolean isMobileDevice = receivedMessage.getBoolean("mobileDevice");
                this.userInfo = userProvider.get(receivedMessage.getString("token"));
                if (this.userInfo == null) {
                    return;
                }
                if (Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
                    if (StringUtil.isNotEmpty(userInfo.getTenantDbConnectionString())) {
                        DataSourceContextHolder.setDatasource(userInfo.getTenantId(), userInfo.getTenantDbConnectionString());
                    } else {
                        break;
                    }
                }
                if (userInfo != null && userInfo.getUserId() != null) {
                    OnlineUserModel model = new OnlineUserModel();
                    model.setConnectionId(session.getId());
                    model.setUserId(userInfo.getUserId());
                    model.setTenantId(userInfo.getTenantId());
                    model.setIsMobileDevice(isMobileDevice);
                    model.setWebSocket(session);
                    Long userAll = OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getUserId().equals(userInfo.getUserId()) && t.getTenantId().equals(userInfo.getTenantId())).count();
                    Long userAllMobile = OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getUserId().equals(userInfo.getUserId()) && t.getTenantId().equals(userInfo.getTenantId()) && t.getIsMobileDevice().equals(true)).count();
                    Long userAllWeb = OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getUserId().equals(userInfo.getUserId()) && t.getTenantId().equals(userInfo.getTenantId()) && t.getIsMobileDevice().equals(false)).count();
                    //都不在线
                    if (userAll == 0) {
                        OnlineUserProvider.addModel(model);
                    }
                    //手机在线
                    else if (userAllMobile != 0 && userAllWeb == 0) {
                        if (model.getIsMobileDevice() == false) {
                            OnlineUserProvider.addModel(model);
                        }
                    }
                    //电脑在线
                    else {
                        if (model.getIsMobileDevice() == true) {
                            OnlineUserProvider.addModel(model);
                        }
                    }
                    List<OnlineUserModel> onlineUserList = OnlineUserProvider.getOnlineUserList().stream().filter(q -> !q.getUserId().equals(userInfo.getUserId()) && q.getTenantId().equals(userInfo.getTenantId())).collect(Collectors.toList());
                    //反馈信息给登录者
                    List<String> onlineUsers = onlineUserList.stream().map(t -> t.getUserId()).collect(Collectors.toList()).stream().distinct().collect(Collectors.toList());
                    List<ImUnreadNumModel> unreadNums = imContentService.getUnreadList(userInfo.getUserId());
                    int unreadNoticeCount = messageService.getUnreadNoticeCount(userInfo.getUserId());
                    int unreadMessageCount = messageService.getUnreadMessageCount(userInfo.getUserId());
                    MessageEntity noticeDefaultText = messageService.getInfoDefault(1);
                    MessageEntity messageDefaultText = messageService.getInfoDefault(2);
                    String noticeText = noticeDefaultText.getTitle() != null ? noticeDefaultText.getTitle() : "";
                    String messageText = messageDefaultText.getTitle() != null ? messageDefaultText.getTitle() : "";
                    Long noticeTime = noticeDefaultText.getCreatorTime() != null ? noticeDefaultText.getCreatorTime().getTime() : 0;
                    Long messageTime = messageDefaultText.getCreatorTime() != null ? messageDefaultText.getCreatorTime().getTime() : 0;
                    JSONObject object = new JSONObject();
                    object.put("method", "initMessage");
                    object.put("onlineUsers", onlineUsers);
                    object.put("unreadNums", JsonUtil.listToJsonField(unreadNums));
                    object.put("unreadNoticeCount", unreadNoticeCount);
                    object.put("noticeDefaultText", noticeText);
                    object.put("noticeDefaultTime", noticeTime);
                    object.put("unreadMessageCount", unreadMessageCount);
                    object.put("messageDefaultText", messageText);
                    object.put("messageDefaultTime", messageTime);
                    //收到用户显示消息
                    session.getAsyncRemote().sendText(object.toJSONString());
                    //通知所有在线用户，有用户在线
                    for (OnlineUserModel item : onlineUserList) {
                        if (!item.getUserId().equals(userInfo.getUserId())) {
                            JSONObject map = new JSONObject();
                            map.put("method", "Online");
                            map.put("userId", userInfo.getUserId());
                            item.getWebSocket().getAsyncRemote().sendText(map.toJSONString());
                        }
                    }
                }
                break;
            case "SendMessage":
                //发送消息
                String toUserId = receivedMessage.getString("toUserId");
                //text/voice/image
                String messageType = receivedMessage.getString("messageType");
                String messageContent = receivedMessage.getString("messageContent");
                String tenantId = userProvider.get(receivedMessage.getString("token")).getTenantId();
                if (this.userInfo == null) {
                    return;
                }
                if (Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
                    DataSourceContextHolder.setDatasource(userInfo.getTenantId(), userInfo.getTenantDbConnectionString());
                }
                String fileName = "";
                String directoryPath = configValueUtil.getImContentFilePath();
                if (!"text".equals(messageType)) {
                    String type = ("voice".equals(messageType) ? ".mp3" : ".png");
                    fileName = RandomUtil.uuId() + type;
                    String filePath = directoryPath + fileName;
                    JSONObject object = JSONObject.parseObject(messageContent);
                    if ("image".equals(messageType)) {
                        fileName = object.getString("name");
                    }else {
                        String fileBase64 = object.getString("base64").replaceAll("%", "").replaceAll(",", "").replaceAll(" ", "+");
                        Base64Util.base64ToFile(fileBase64, filePath);
                    }
                }
                List<OnlineUserModel> user = OnlineUserProvider.getOnlineUserList().stream().filter(q -> String.valueOf(q.getUserId()).equals(String.valueOf(userInfo.getUserId())) && String.valueOf(q.getTenantId()).equals(tenantId)).collect(Collectors.toList());
                OnlineUserModel onlineUser = user.size() > 0 ? user.get(0) : null;
                List<OnlineUserModel> toUser = OnlineUserProvider.getOnlineUserList().stream().filter(q -> String.valueOf(q.getTenantId()).equals(String.valueOf(onlineUser.getTenantId())) && String.valueOf(q.getUserId()).equals(String.valueOf(toUserId))).collect(Collectors.toList());
                if (user.size() != 0) {
                    //saveMessage
                    if ("text".equals(messageType)) {
                        imContentService.sendMessage(onlineUser.getUserId(), toUserId, messageContent, messageType);
                    } else if ("image".equals(messageType)) {
                        JSONObject image = new JSONObject();
                        image.put("path", UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                        image.put("width", JSONObject.parseObject(messageContent).getString("width"));
                        image.put("height", JSONObject.parseObject(messageContent).getString("height"));
                        imContentService.sendMessage(onlineUser.getUserId(), toUserId, image.toJSONString(), messageType);
                    } else if ("voice".equals(messageType)) {
                        JSONObject voice = new JSONObject();
                        voice.put("path", UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                        voice.put("length", JSONObject.parseObject(messageContent).getString("length"));
                        imContentService.sendMessage(onlineUser.getUserId(), toUserId, voice.toJSONString(), messageType);
                    }
                    for (int i = 0; i < user.size(); i++) {
                        OnlineUserModel model = user.get(i);
                        //sendMessage
                        JSONObject object = new JSONObject();
                        object.put("method", "sendMessage");
                        object.put("UserId", model.getUserId());
                        object.put("toUserId", toUserId);
                        object.put("dateTime", DateUtil.getNowDate().getTime());
                        //头像
                        object.put("headIcon",UploaderUtil.uploaderImg(userInfo.getUserIcon()));
                        //最新消息
                        object.put("latestDate",DateUtil.getNowDate().getTime());
                        //用户姓名
                        object.put("realName",userInfo.getUserName());
                        object.put("account",userInfo.getUserAccount());
                        //对方的名称账号头像
                        UserEntity entity = userService.getInfo(toUserId);
                        object.put("toAccount",entity.getAccount());
                        object.put("toRealName",entity.getRealName());
                        object.put("toHeadIcon",UploaderUtil.uploaderImg(entity.getHeadIcon()));
                        if ("text".equals(messageType)) {
                            object.put("messageType", messageType);
                            object.put("toMessage", messageContent);
                            model.getWebSocket().getAsyncRemote().sendText(object.toJSONString());
                        } else if ("image".equals(messageType)) {
                            JSONObject image = new JSONObject();
                            image.put("path", UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                            image.put("width", JSONObject.parseObject(messageContent).getString("width"));
                            image.put("height", JSONObject.parseObject(messageContent).getString("height"));
                            object.put("messageType", messageType);
                            object.put("toMessage", image);
                            model.getWebSocket().getAsyncRemote().sendText(object.toJSONString());
                        } else if ("voice".equals(messageType)) {
                            JSONObject voice = new JSONObject();
                            voice.put("path", UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                            voice.put("length", JSONObject.parseObject(messageContent).getString("length"));
                            object.put("messageType", messageType);
                            object.put("toMessage", voice);
                            model.getWebSocket().getAsyncRemote().sendText(object.toJSONString());
                        }
                    }
                }
                JSONObject receive = new JSONObject();
                receive.put("method", "receiveMessage");
                receive.put("formUserId", onlineUser.getUserId());
                if (toUser.size() != 0) {
                    for (int i = 0; i < toUser.size(); i++) {
                        OnlineUserModel onlineToUser = toUser.get(i);
                        if ("text".equals(messageType)) {
                            receive.put("messageType", messageType);
                            receive.put("formMessage", messageContent);
                            receive.put("dateTime", DateUtil.getNowDate().getTime());
                            //头像
                            receive.put("headIcon",UploaderUtil.uploaderImg(userInfo.getUserIcon()));
                            //最新消息
                            receive.put("latestDate",DateUtil.getNowDate().getTime());
                            //用户姓名
                            receive.put("realName",userInfo.getUserName());
                            receive.put("account",userInfo.getUserAccount());
                            synchronized (session) {
                                onlineToUser.getWebSocket().getAsyncRemote().sendText(receive.toJSONString());
                            }
                        } else if ("image".equals(messageType)) {
                            JSONObject image = new JSONObject();
                            image.put("path", UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                            image.put("width", JSONObject.parseObject(messageContent).getString("width"));
                            image.put("height", JSONObject.parseObject(messageContent).getString("height"));
                            receive.put("messageType", messageType);
                            receive.put("formMessage", image);
                            receive.put("dateTime", DateUtil.getNowDate().getTime());
                            //头像
                            receive.put("headIcon",UploaderUtil.uploaderImg(userInfo.getUserIcon()));
                            //最新消息
                            receive.put("latestDate",DateUtil.getNowDate().getTime());
                            //用户姓名
                            receive.put("realName",userInfo.getUserName());
                            receive.put("account",userInfo.getUserAccount());
                            synchronized (session) {
                                onlineToUser.getWebSocket().getAsyncRemote().sendText(receive.toJSONString());
                            }
                        } else if ("voice".equals(messageType)) {
                            JSONObject voice = new JSONObject();
                            voice.put("path", UploaderUtil.uploaderImg("/api/file/Image/IM/", fileName));
                            voice.put("length", JSONObject.parseObject(messageContent).getString("length"));
                            receive.put("messageType", messageType);
                            receive.put("formMessage", voice);
                            receive.put("dateTime", DateUtil.getNowDate().getTime());
                            //头像
                            receive.put("headIcon",UploaderUtil.uploaderImg(userInfo.getUserIcon()));
                            //最新消息
                            receive.put("latestDate",DateUtil.getNowDate().getTime());
                            //用户姓名
                            receive.put("realName",userInfo.getUserName());
                            receive.put("account",userInfo.getUserAccount());
                            synchronized (session) {
                                onlineToUser.getWebSocket().getAsyncRemote().sendText(receive.toJSONString());
                            }
                        }
                    }
                }
                break;
            case "UpdateReadMessage":
                //更新已读
                String formUserId = receivedMessage.getString("formUserId");
                tenantId = userProvider.get(receivedMessage.getString("token")).getTenantId();
                if (this.userInfo == null) {
                    return;
                }
                if (Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
                    DataSourceContextHolder.setDatasource(userInfo.getTenantId(), userInfo.getTenantDbConnectionString());
                }
                onlineUser = OnlineUserProvider.getOnlineUserList().stream().filter(q -> String.valueOf(q.getConnectionId()).equals(String.valueOf(session.getId()))).findFirst().orElse(new OnlineUserModel());
                if (onlineUser != null) {
                    imContentService.readMessage(formUserId, onlineUser.getUserId());
                }
                break;
            case "MessageList":
                //获取消息列表
                String sendUserId = receivedMessage.getString("toUserId");
                String receiveUserId = receivedMessage.getString("formUserId");
                tenantId = userProvider.get(receivedMessage.getString("token")).getTenantId();
                if (this.userInfo == null) {
                    return;
                }
                if (Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
                    DataSourceContextHolder.setDatasource(userInfo.getTenantId(), userInfo.getTenantDbConnectionString());
                }
                PageModel pageModel = new PageModel();
                pageModel.setPage(receivedMessage.getInteger("currentPage"));
                pageModel.setRows(receivedMessage.getInteger("pageSize"));
                pageModel.setSord(receivedMessage.getString("sord"));
                pageModel.setKeyword(receivedMessage.getString("keyword"));
                List<ImContentEntity> data = imContentService.getMessageList(sendUserId, receiveUserId, pageModel).stream().sorted(Comparator.comparing(ImContentEntity::getSendTime)).collect(Collectors.toList());
                JSONObject object = new JSONObject();
                object.put("method", "messageList");
                object.put("list", JsonUtil.getListToJsonArray(data));
                JSONObject pagination = new JSONObject();
                pagination.put("total", pageModel.getRecords());
                pagination.put("currentPage", pageModel.getPage());
                pagination.put("pageSize", receivedMessage.getInteger("pageSize"));
                object.put("pagination", pagination);
                session.getAsyncRemote().sendText(object.toJSONString());
                break;
            default:
                break;
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        OnlineUserModel user = OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getConnectionId().equals(session.getId())).findFirst().isPresent() ? OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getConnectionId().equals(session.getId())).findFirst().get() : null;
        if (user != null) {
            log.error("调用onError,租户：" + user.getTenantId() + ",用户：" + user.getUserId());
        }
        try {
            onClose(session);
        } catch (Exception e) {
            log.error("发生error,调用onclose失败，session为：" + session);
        }
        if (error.getMessage() != null) {
            error.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    private void init() {
        messageService = SpringContext.getBean(MessageService.class);
        imContentService = SpringContext.getBean(ImContentService.class);
        configValueUtil = SpringContext.getBean(ConfigValueUtil.class);
        userProvider = SpringContext.getBean(UserProvider.class);
        redisUtil = SpringContext.getBean(RedisUtil.class);
        cacheKeyUtil = SpringContext.getBean(CacheKeyUtil.class);
        userService = SpringContext.getBean(UserService.class);
    }

    /**
     * 缩略图
     *
     * @param imgPathOld
     * @param imgPathNew
     * @param width
     * @param height
     */
    private static void makeThumbnail(String imgPathOld, String imgPathNew, int width, int height) {
        try {
            if (FileUtil.fileIsFile(imgPathOld)) {
                Thumbnails.of(imgPathOld)
                        .size(width, height)
                        .toFile(imgPathNew);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
