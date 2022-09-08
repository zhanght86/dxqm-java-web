package jnpf.message.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class UserOnlineVO {
    @JSONField(name="UserId")
    private String userId;
    @JSONField(name = "UserAccount")
    private String userAccount;
    @JSONField(name = "UserName")
    private String userName;
    @JSONField(name = "LoginTime")
    private String loginTime;
    @JSONField(name = "LoginIPAddress")
    private String loginIPAddress;
    @JSONField(name = "LoginPlatForm")
    private String loginPlatForm;
}
