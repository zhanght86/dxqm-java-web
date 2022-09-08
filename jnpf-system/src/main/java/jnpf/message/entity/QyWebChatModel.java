package jnpf.message.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 企业微信的模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/5/25 14:18
 */
@Data
public class QyWebChatModel {
    @NotBlank(message = "必填")
    private String qyhCorpId;
    private String qyhAgentId;
    private String qyhAgentSecret;
    private String qyhCorpSecret;
}
