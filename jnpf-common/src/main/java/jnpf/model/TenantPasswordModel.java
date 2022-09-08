package jnpf.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 租户修改密码
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:57
 */
@Data
public class TenantPasswordModel {
    /**
     * 手机号
     */
    @JSONField(name = "Mobile")
    private String mobile;
    /**
     * 短信验证码
     */
    @JSONField(name = "SmsCode")
    private String smsCode;
    /**
     * 密码
     */
    @JSONField(name = "Password")
    private String password;
    /**
     * 公司名
     */
    @JSONField(name = "CompanyName")
    private String companyName;
    /**
     * 姓名
     */
    @JSONField(name = "Name")
    private String name;
}
