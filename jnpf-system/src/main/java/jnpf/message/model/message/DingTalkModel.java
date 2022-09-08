package jnpf.message.model.message;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 钉钉发送信息配置模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/22 14:12
 */
@Data
public class DingTalkModel {
    @NotBlank(message = "应用凭证必填")
    private String dingSynAppKey;
    @NotBlank(message = "凭证密钥必填")
    private String dingSynAppSecret;
    private String dingAgentId;
}
