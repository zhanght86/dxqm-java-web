package jnpf.message.util;

import com.alibaba.fastjson.JSONObject;
import jnpf.base.UserInfo;
import jnpf.base.entity.SysConfigEntity;
import jnpf.base.service.SysconfigService;
import jnpf.message.entity.MessageEntity;
import jnpf.message.entity.MessageReceiveEntity;
import jnpf.message.entity.SynThirdInfoEntity;
import jnpf.message.enums.MessageTypeEnum;
import jnpf.message.model.message.DingTalkModel;
import jnpf.message.model.message.EmailModel;
import jnpf.message.model.message.SentMessageForm;
import jnpf.message.model.message.SmsModel;
import jnpf.message.service.MessageService;
import jnpf.message.service.MessagereceiveService;
import jnpf.message.service.SynThirdInfoService;
import jnpf.model.login.BaseSystemInfo;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息实体类
 *
 * @版本： V3.2.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/22 9:06
 */
@Component
public class SentMessageUtil {

    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private SysconfigService sysconfigService;
    @Autowired
    private MessageService messageService;
    @Autowired
    private MessagereceiveService messagereceiveService;
    @Autowired
    private SynThirdInfoService synThirdInfoService;

    private JSONObject SendQyWebChat(SentMessageForm sentMessageForm, UserInfo userInfo, String sendType) {
        String content = StringUtil.isNotEmpty(sentMessageForm.getTitle()) ? sentMessageForm.getTitle() : sentMessageForm.getContent();
        List<String> userIdList = sentMessageForm.getToUserIds();
        List<SysConfigEntity> configList = sysconfigService.getList("SysConfig");
        Map<String, String> objModel = new HashMap<>();
        for (SysConfigEntity entity : configList) {
            objModel.put(entity.getFkey(), entity.getValue());
        }
        BaseSystemInfo config = JsonUtil.getJsonToBean(objModel, BaseSystemInfo.class);
        String corpId = config.getQyhCorpId();
        String agentId = config.getQyhAgentId();
        // 获取的应用的Secret值
        String corpSecret = config.getQyhAgentSecret();
        String wxUserId = "";
        StringBuilder toWxUserId = new StringBuilder();
        String toUserIdAll = "";
        StringBuilder nullUserInfo = new StringBuilder();
        List<MessageReceiveEntity> messageReceiveList = new ArrayList<>();
        JSONObject retJson = new JSONObject();

        // 相关参数验证
        if (StringUtil.isEmpty(corpId)) {
            retJson.put("code", false);
            retJson.put("error", "企业ID为空");
            return retJson;
        }
        if (StringUtil.isEmpty(corpSecret)) {
            retJson.put("code", false);
            retJson.put("error", "Secret为空");
            return retJson;
        }
        if (StringUtil.isEmpty(agentId)) {
            retJson.put("code", false);
            retJson.put("error", "AgentId为空");
            return retJson;
        }
        if (StringUtil.isEmpty(content)) {
            retJson.put("code", false);
            retJson.put("error", "内容为空");
            return retJson;
        }
        if (userIdList == null && userIdList.size() < 1) {
            retJson.put("code", false);
            retJson.put("error", "接收人为空");
            return retJson;
        }

        // 创建消息实体
        MessageEntity messageEntity = JnpfMessageUtil.setMessageEntity(userInfo.getUserId(), content, null, Integer.parseInt(sendType));

        // 获取接收人员的企业微信号、创建消息用户实体
        for (String userId : userIdList) {
            wxUserId = "";
            // 从同步表获取对应的企业微信ID
            SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId("1", "2", userId);
            if (synThirdInfoEntity != null) {
                wxUserId = synThirdInfoEntity.getThirdObjId();
            }
            if (StringUtil.isEmpty(wxUserId)) {
                nullUserInfo = nullUserInfo.append(",").append(userId);
            } else {
                toWxUserId = toWxUserId.append("|").append(wxUserId);
            }
            messageReceiveList.add(JnpfMessageUtil.setMessageReceiveEntity(messageEntity.getId(), userId));
        }

        // 处理企业微信号信息串并验证
        toUserIdAll = toWxUserId.toString();
        if (StringUtil.isNotEmpty(toUserIdAll)) {
            toUserIdAll = toUserIdAll.substring(1);
        }
        if (StringUtil.isEmpty(toUserIdAll)) {
            retJson.put("code", false);
            retJson.put("error", "接收人对应的企业微信号全部为空");
            return retJson;
        }

        // 批量发送企业信息信息
        retJson = QyWebChatUtil.sendWxMessage(corpId, corpSecret, agentId, toUserIdAll, content);
        if (!retJson.getBoolean("code")) {
            return retJson;
        }

        // 企业微信号为空的信息写入备注
        if (StringUtil.isNotEmpty(nullUserInfo.toString())) {
            messageEntity.setDescription(nullUserInfo.toString().substring(1) + "对应的企业微信号为空");
        }
        // 写入系统的消息表、消息用户表
        messageService.save(messageEntity);
        messagereceiveService.saveBatch(messageReceiveList);

        retJson.put("code", true);
        retJson.put("error", "发送成功");
        return retJson;
    }

    /**
     * 向钉钉发送消息
     *
     * @param sentMessageForm
     * @param userInfo
     * @param objModel
     * @param sendType
     * @return
     * @throws Exception
     */
    private JSONObject SendDingTalk(SentMessageForm sentMessageForm, UserInfo userInfo, Map<String, String> objModel, String sendType) {
        String content = StringUtil.isNotEmpty(sentMessageForm.getTitle()) ? sentMessageForm.getTitle() : sentMessageForm.getContent();
        List<String> userIdList = sentMessageForm.getToUserIds();
        DingTalkModel dingTalkModel = JsonUtil.getJsonToBean(objModel, DingTalkModel.class);
        String appKey = dingTalkModel.getDingSynAppKey();
        String appSecret = dingTalkModel.getDingSynAppSecret();
        String agentId = dingTalkModel.getDingAgentId();
        String dingUserId = "";
        StringBuilder toDingUserId = new StringBuilder();
        String toUserIdAll = "";
        StringBuilder nullUserInfo = new StringBuilder();
        List<MessageReceiveEntity> messageReceiveList = new ArrayList<>();
        JSONObject retJson = new JSONObject();

        // 相关参数验证
        if (StringUtil.isEmpty(appKey)) {
            retJson.put("code", false);
            retJson.put("error", "AppKey为空");
            return retJson;
        }
        if (StringUtil.isEmpty(appSecret)) {
            retJson.put("code", false);
            retJson.put("error", "AppSecret为空");
            return retJson;
        }
        if (StringUtil.isEmpty(agentId)) {
            retJson.put("code", false);
            retJson.put("error", "AgentId为空");
            return retJson;
        }
        if (StringUtil.isEmpty(content)) {
            retJson.put("code", false);
            retJson.put("error", "内容为空");
            return retJson;
        }
        if (userIdList == null && userIdList.size() < 1) {
            retJson.put("code", false);
            retJson.put("error", "接收人为空");
            return retJson;
        }

        // 创建消息实体
        MessageEntity messageEntity = JnpfMessageUtil.setMessageEntity(userInfo.getUserId(), content, null, Integer.parseInt(sendType));

        // 获取接收人员的钉钉号、创建消息用户实体
        for (String userId : userIdList) {
            dingUserId = "";
            // 从同步表获取对应用户的钉钉ID
            SynThirdInfoEntity synThirdInfoEntity = synThirdInfoService.getInfoBySysObjId("2", "2", userId);
            if (synThirdInfoEntity != null) {
                dingUserId = synThirdInfoEntity.getThirdObjId();
            }
            if (StringUtil.isEmpty(dingUserId)) {
                nullUserInfo = nullUserInfo.append(",").append(userId);
            } else {
                toDingUserId = toDingUserId.append(",").append(dingUserId);
            }
            messageReceiveList.add(JnpfMessageUtil.setMessageReceiveEntity(messageEntity.getId(), userId));
        }

        // 处理接收人员的钉钉号信息串并验证
        toUserIdAll = toDingUserId.toString();
        if (StringUtil.isNotEmpty(toUserIdAll)) {
            toUserIdAll = toUserIdAll.substring(1);
        }
        if (StringUtil.isEmpty(toUserIdAll)) {
            retJson.put("code", false);
            retJson.put("error", "接收人对应的钉钉号全部为空");
            return retJson;
        }

        // 批量发送钉钉信息
        retJson = DingTalkUtil.sendDingMessage(appKey, appSecret, agentId, toUserIdAll, content);
        if (!retJson.getBoolean("code")) {
            return retJson;
        }

        // 钉钉号为空的信息写入备注
        if (StringUtil.isNotEmpty(nullUserInfo.toString())) {
            messageEntity.setDescription(nullUserInfo.toString().substring(1) + "对应的钉钉号为空");
        }
        // 写入系统的消息表、消息用户表
        messageService.save(messageEntity);
        messagereceiveService.saveBatch(messageReceiveList);

        retJson.put("code", true);
        retJson.put("error", "发送成功");
        return retJson;
    }

    /**
     * 发送短信(阿里云、腾讯云)
     *
     * @param sentMessageForm
     * @param userInfo
     * @param objModel
     * @param sendType
     * @return
     * @throws Exception
     */
    private JSONObject SendSms(@RequestBody @Valid SentMessageForm sentMessageForm, UserInfo userInfo, Map<String, String> objModel, String sendType)  {
        SmsModel smsModel = JsonUtil.getJsonToBean(objModel, SmsModel.class);
        Map<String, String> msg = sentMessageForm.getSmsContent();
        String content = sentMessageForm.getContent();
        String smsCompany = smsModel.getSmsCompany();
        String smsAccessKeyId = smsModel.getSmsKeyId();
        String smsAccessKeySecret = smsModel.getSmsKeySecret();
        String smsTemplateId = smsModel.getSmsTemplateId();
        String smsAppId = smsModel.getSmsAppId();
        String smsSignName = smsModel.getSmsSignName();
        List<String> userIdList = sentMessageForm.getToUserIds();
        StringBuilder nullUserInfo = new StringBuilder();
        List<MessageReceiveEntity> messageReceiveList = new ArrayList<>();
        String userPhoneAll = "";
        String userPhone = "";
        StringBuilder toUserPhone = new StringBuilder();
        JSONObject retJson = new JSONObject();
        Integer phoneCount = 0;
        Integer phoneMax = "2".equals(smsCompany) ? 200 : 1000;

        // 相关参数验证
        if (StringUtil.isEmpty(smsCompany)) {
            retJson.put("code",false);
            retJson.put("error","短信厂家为空");
            return retJson;
        }
        if (StringUtil.isEmpty(smsAccessKeyId)) {
            retJson.put("code",false);
            retJson.put("error","AccessKeyId为空");
            return retJson;
        }
        if (StringUtil.isEmpty(smsAccessKeySecret)) {
            retJson.put("code",false);
            retJson.put("error","AccessKeySecret为空");
            return retJson;
        }
        if (StringUtil.isEmpty(smsTemplateId)) {
            retJson.put("code",false);
            retJson.put("error","短信模板ID为空");
            return retJson;
        }
        if (StringUtil.isEmpty(smsAppId) && "2".equals(smsCompany)) {
            retJson.put("code",false);
            retJson.put("error","应用AppId为空");
            return retJson;
        }
        if(userIdList==null && userIdList.size()<1){
            retJson.put("code",false);
            retJson.put("error","接收人为空");
            return retJson;
        }

        // 创建消息实体
        MessageEntity messageEntity = JnpfMessageUtil.setMessageEntity(userInfo.getUserId(),content,null,Integer.parseInt(sendType));

        // 获取接收人员的手机号码、创建消息用户实体
        for(String userId : userIdList){
            UserEntity userEntity = userService.getInfo(userId);
            if(userEntity!=null){
                userPhone = StringUtil.isEmpty(userEntity.getMobilePhone())?"":userEntity.getMobilePhone();
            }
            if(userPhone!=null && !"".equals(userPhone)){
                // 腾讯云需要在手机号码前加+86的前缀，代表区域
                toUserPhone = "2".equals(smsCompany) ? toUserPhone.append(",+86").append(userPhone) : toUserPhone.append(",").append(userPhone);
                phoneCount ++;
            }else{
                nullUserInfo = nullUserInfo.append(",").append(userId);
            }
            messageReceiveList.add(JnpfMessageUtil.setMessageReceiveEntity(messageEntity.getId(),userId));
        }

        // 处理接收人员的手机号码信息串并验证
        userPhoneAll = toUserPhone.toString();
        if(StringUtil.isNotEmpty(userPhoneAll)){
            userPhoneAll = userPhoneAll.substring(1);
        }
        if(StringUtil.isEmpty(userPhoneAll)){
            retJson.put("code",false);
            retJson.put("error","接收人对应的手机号码全部为空");
            return retJson;
        }

        // 阿里云：手机号上限不超过1000个
        // 腾讯云：手机号上限不超过200个
        if(phoneCount>phoneMax){
            retJson.put("code",false);
            retJson.put("error","接收人对应的手机号码数超过短信商的上限" + phoneMax.toString() + "个!");
            return retJson;
        }

        // 根据标志往阿里云或腾讯云批量发送消息
        if("1".equals(smsCompany)){
            // 阿里云-批量发送 测试通过
            content = msg.get("1");
            retJson = SmsAliYunUtil.sendSms(smsAccessKeyId,smsAccessKeySecret,smsTemplateId,content,userPhoneAll,smsSignName);
        }
        if("2".equals(smsCompany)){
            //腾讯云-批量发送 测试通过
            content = msg.get("2");
            retJson = SmsTenCentCloudUtil.sendSms(smsAccessKeyId,smsAccessKeySecret,smsAppId,smsTemplateId,content,userPhoneAll.split(","),smsSignName);
        }
        if(!retJson.getBoolean("code")){
            return retJson;
        }

        // 手机号码为空的信息写入备注
        if(StringUtil.isNotEmpty(nullUserInfo.toString())){
            messageEntity.setDescription(nullUserInfo.toString().substring(1) + "对应的手机号码为空");
        }
        // 写入系统的消息表、消息用户表
        messageService.save(messageEntity);
        messagereceiveService.saveBatch(messageReceiveList);

        retJson.put("code",true);
        retJson.put("error","发送成功");
        return retJson;
    }

    /**
     * 发送邮件
     *
     * @param sentMessageForm
     * @param userInfo
     * @param objModel
     * @param sendType
     * @return
     * @throws Exception
     */
    private JSONObject SendMail(SentMessageForm sentMessageForm, UserInfo userInfo, Map<String, String> objModel, String sendType) {
        List<String> userIdList = sentMessageForm.getToUserIds();
        EmailModel emailModel = JsonUtil.getJsonToBean(objModel, EmailModel.class);
        StringBuilder nullUserInfo = new StringBuilder();
        List<MessageReceiveEntity> messageReceiveList = new ArrayList<>();
        StringBuilder toUserMail = new StringBuilder();
        String userEmailAll = "";
        String userEmail = "";
        String userName = "";
        JSONObject retJson = new JSONObject();

        // 相关参数验证
        if (StringUtil.isEmpty(emailModel.getEmailSmtpHost())) {
            retJson.put("code", false);
            retJson.put("error", "SMTP服务为空");
            return retJson;
        }
        if (StringUtil.isEmpty(emailModel.getEmailSmtpPort())) {
            retJson.put("code", false);
            retJson.put("error", "SMTP端口为空");
            return retJson;
        }
        if (StringUtil.isEmpty(emailModel.getEmailAccount())) {
            retJson.put("code", false);
            retJson.put("error", "发件人邮箱为空");
            return retJson;
        }
        if (StringUtil.isEmpty(emailModel.getEmailPassword())) {
            retJson.put("code", false);
            retJson.put("error", "发件人密码为空");
            return retJson;
        }
        if (StringUtil.isEmpty(sentMessageForm.getContent())) {
            retJson.put("code", false);
            retJson.put("error", "邮件内容为空");
            return retJson;
        }
        if (StringUtil.isEmpty(sentMessageForm.getTitle())) {
            retJson.put("code", false);
            retJson.put("error", "邮件标题为空");
            return retJson;
        }
        if (userIdList == null && userIdList.size() < 1) {
            retJson.put("code", false);
            retJson.put("error", "接收人为空");
            return retJson;
        }

        // 设置邮件内容
        emailModel.setEmailTitle(sentMessageForm.getTitle());
        // 设置邮件内容
        emailModel.setEmailContent(sentMessageForm.getContent());

        // 创建消息实体
        MessageEntity messageEntity = JnpfMessageUtil.setMessageEntity(userInfo.getUserId(), emailModel.getEmailTitle(), emailModel.getEmailContent(), Integer.parseInt(sendType));

        // 获取收件人的邮箱地址、创建消息用户实体
        for (String userId : userIdList) {
            UserEntity userEntity = userService.getInfo(userId);
            if (userEntity != null) {
                userEmail = StringUtil.isEmpty(userEntity.getEmail()) ? "" : userEntity.getEmail();
                userName = userEntity.getRealName();
            }
            if (userEmail != null && !"".equals(userEmail)) {
                toUserMail = toUserMail.append(",").append(userName).append("<").append(userEmail).append(">");
            } else {
                nullUserInfo = nullUserInfo.append(",").append(userId);
            }
            messageReceiveList.add(JnpfMessageUtil.setMessageReceiveEntity(messageEntity.getId(), userId));
        }

        // 处理接收人员的邮箱信息串并验证
        userEmailAll = toUserMail.toString();
        if (StringUtil.isNotEmpty(userEmailAll)) {
            userEmailAll = userEmailAll.substring(1);
        }
        if (StringUtil.isEmpty(userEmailAll)) {
            retJson.put("code", false);
            retJson.put("error", "接收人对应的邮箱全部为空");
            return retJson;
        }
        // 设置接收人员
        emailModel.setEmailToUsers(userEmailAll);
        // 发送邮件
        retJson = EmailUtil.sendMail(emailModel);
        if (!retJson.getBoolean("code")) {
            return retJson;
        }

        // 邮箱地址为空的信息写入备注
        if (StringUtil.isNotEmpty(nullUserInfo.toString())) {
            messageEntity.setDescription(nullUserInfo.toString().substring(1) + "对应的邮箱为空");
        }
        // 写入系统的消息表、消息用户表
        messageService.save(messageEntity);
        messagereceiveService.saveBatch(messageReceiveList);

        retJson.put("code", true);
        retJson.put("error", "发送成功");
        return retJson;
    }

    /**
     * 发送消息
     * @param sentMessageForm
     */
    public void sendMessage(SentMessageForm sentMessageForm) {
        List<String> sendTypeList = sentMessageForm.getSendType();
        List<String> toUserIdsList = sentMessageForm.getToUserIds();
        String title = sentMessageForm.getTitle();
        String content = sentMessageForm.getContent();
        UserInfo userInfo = userProvider.get();
        List<SysConfigEntity> configList = sysconfigService.getList("SysConfig");
        Map<String, String> objModel = new HashMap<>();
        for (SysConfigEntity entity : configList) {
            objModel.put(entity.getFkey(), entity.getValue());
        }
        boolean flag = true;
        String errorMsg = "";
        // 相关参数验证
        if (!(sendTypeList != null && sendTypeList.size() > 0)) {
            errorMsg = "消息类型组合为空";
            flag = false;
        }
        if (!(toUserIdsList != null && toUserIdsList.size() > 0)) {
            errorMsg = "接收人员为空";
            flag = false;
        }
        if (StringUtil.isEmpty(title)) {
            errorMsg = "标题为空";
            flag = false;
        }

        // 设备消息的返回对象
        JSONObject smsMsg = new JSONObject();
        smsMsg.put("code", true);
        smsMsg.put("error", "");

        JSONObject emailMsg = new JSONObject();
        emailMsg.put("code", true);
        emailMsg.put("error", "");

        JSONObject qyMsg = new JSONObject();
        qyMsg.put("code", true);
        qyMsg.put("error", "");

        JSONObject dingMsg = new JSONObject();
        dingMsg.put("code", true);
        dingMsg.put("error", "");


        if (flag) {
            for (String sendType : sendTypeList) {
                MessageTypeEnum typeEnum = MessageTypeEnum.getByCode(sendType);
                switch (typeEnum) {
                    case SysMessage:
                        // 站内消息
                        messageService.sentMessage(toUserIdsList, title, content);
                        break;
                    case SmsMessage:
                        // 短信
                        // 涉及到模板不能自定义，暂时不用 20210423
                         smsMsg = SendSms(sentMessageForm,userInfo,objModel,sendType);
                        break;
                    case MailMessage:
                        // 邮件
                        emailMsg = SendMail(sentMessageForm, userInfo, objModel, sendType);
                        break;
                    case QyMessage:
                        // 企业微信
                        qyMsg = SendQyWebChat(sentMessageForm, userInfo, sendType);
                        break;
                    case DingMessage:
                        // 钉钉
                        dingMsg = SendDingTalk(sentMessageForm, userInfo, objModel, sendType);
                        break;
                }
            }
            // 根据各类型的消息发送返回值来判断是否成功
            if (!smsMsg.getBoolean("code")) {
                errorMsg = errorMsg + ";短信：" + smsMsg.getString("error");
            }
            if (!emailMsg.getBoolean("code")) {
                errorMsg = errorMsg + ";邮件：" + emailMsg.getString("error");
            }
            if (!qyMsg.getBoolean("code")) {
                errorMsg = errorMsg + ";企业微信：" + qyMsg.getString("error");
            }
            if (!dingMsg.getBoolean("code")) {
                errorMsg = errorMsg + ";钉钉：" + dingMsg.getString("error");
            }
        }
    }

}
