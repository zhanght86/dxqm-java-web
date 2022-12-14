package jnpf.base.model.systemconfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class SysConfigModel {
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "系统名称")
    private String sysName;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "系统描述")
    private String sysDescription;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "系统版本")
    private String sysVersion;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "版权信息")
    private String copyright;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "公司名称")
    private String companyName;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "公司简称")
    private String companyCode;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "公司地址")
    private String companyAddress;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "公司法人")
    private String companyContacts;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "公司电话")
    private String companyTelePhone;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "公司邮箱")
    private String companyEmail;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "单一登录方式")
    private String singleLogin;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "超出登出")
    private String tokenTimeout;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "是否开启上次登录提醒")
    private Integer lastLoginTimeSwitch;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "是否开启白名单验证")
    private Integer whitelistSwitch;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "白名单")
    private String whiteListIp;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "POP3服务主机地址")
    private String emailPop3Host;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "POP3服务端口")
    private String emailPop3Port;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "SMTP服务主机地址")
    private String emailSmtpHost;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "邮件显示名称")
    private String emailSmtpPort;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "系统名称")
    private String emailSenderName;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "邮箱账户")
    private String emailAccount;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "邮箱密码")
    private String emailPassword;
    @NotBlank(message = "必填")
    @ApiModelProperty(value = "是否开启SSL服务登录")
    private Integer emailSsl;


    @NotBlank(message = "必填")
    @ApiModelProperty(value = "授权密钥")
    private String registerKey;
    private String lastLoginTime;
    private String pageSize;
    private String sysTheme;
    private String isLog;

    /**
    * SMS配置字段
    */
    @ApiModelProperty(value = "厂商")
    private String smsCompany ;
    private String smsKeyId;
    private String smsKeySecret;
    private String smsTemplateId;
    @ApiModelProperty(value = "腾讯云需要用的AppId")
    private String smsAppId;
    private String smsSignName;

    /**
     * 企业微信配置
     */
    /** 发消息 */
    private String qyhCorpId;
    private String qyhAgentId;
    private String qyhAgentSecret;
    /** 同步消息 */
    private String qyhCorpSecret;
    private Integer qyhIsSynOrg;
    private Integer qyhIsSynUser;

    /**
     * 钉钉 发消息
     */
    private String dingSynAppKey;
    private String dingSynAppSecret;
    private String dingAgentId;
    private Integer dingSynIsSynOrg;
    private Integer dingSynIsSynUser;

//    @ApiModelProperty(value = "SmsKey")
//    private String accessKeyId ;
//    @ApiModelProperty(value = "Sms密钥")
//    private String accessKeySecret ;



}
