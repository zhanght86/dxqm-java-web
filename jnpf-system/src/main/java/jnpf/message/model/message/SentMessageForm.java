package jnpf.message.model.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 消息模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/20 11:44
 */
@Data
public class SentMessageForm {
    @ApiModelProperty(value = "发送消息类型组合:0-站内消息，101-短信,102-邮件,103-企业微信,104-钉钉")
    private List<String> sendType;

    @ApiModelProperty(value = "接收人员用户ID组")
    private List<String> toUserIds;

    @ApiModelProperty(value = "标题")
    private String title;

    @ApiModelProperty(value = "内容")
    private String content;

    @ApiModelProperty(value = "短信内容")
    private Map<String,String> smsContent;


}
