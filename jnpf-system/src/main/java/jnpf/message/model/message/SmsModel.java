package jnpf.message.model.message;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 发送短信配置模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/20 14:22
 */
@Data
public class SmsModel {
    private String smsCompany;
    private String smsKeyId;
    private String smsKeySecret;
    private String smsTemplateId;
    private String smsSignName;
    @ApiModelProperty(value = "腾讯云需要用的AppId")
    private String smsAppId;

}
