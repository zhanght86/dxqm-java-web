package jnpf.message.enums;

/**
 * 消息类型枚举
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/23 17:45
 */
public enum MessageTypeEnum {
    /**
     * 站内消息
     */
    SysMessage("1", "站内消息"),
    /**
     * 发送邮件
     */
    MailMessage("2", "发送邮件"),
    /**
     * 发送短信
     */
    SmsMessage("3", "发送短信"),
    /**
     * 钉钉消息
     */
    DingMessage("4", "发送钉钉消息"),
    /**
     * 企业微信
     */
    QyMessage("5", "发送企业微信消息");

    private String code;
    private String message;

    MessageTypeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 根据状态code获取枚举值
     *
     * @return
     */
    public static MessageTypeEnum getByCode(String code) {
        for (MessageTypeEnum status : MessageTypeEnum.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
