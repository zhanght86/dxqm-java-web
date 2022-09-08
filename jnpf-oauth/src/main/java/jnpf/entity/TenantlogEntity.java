package jnpf.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 租户日志
 *
 * @author JNPF开发平台组
 * @version V1.2.191207
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Data
@TableName("base_tenantlog")
public class TenantlogEntity {

    /**
     * 自然主键
     */
    @TableId("F_ID")
    @JSONField(name = "F_Id")
    private String fId;

    /**
     * 租户主键
     */
    @TableField("F_TENANTID")
    @JSONField(name = "F_TenantId")
    private String fTenantid;

    /**
     * 登录账户
     */
    @TableField("F_LOGINACCOUNT")
    @JSONField(name = "F_LoginAccount")
    private String fLoginaccount;

    /**
     * IP地址
     */
    @TableField("F_LOGINIPADDRESS")
    @JSONField(name = "F_LoginIPAddress")
    private String fLoginipaddress;

    /**
     * IP所在城市
     */
    @TableField("F_LOGINIPADDRESSNAME")
    @JSONField(name = "F_LoginIPAddressName")
    private String fLoginipaddressname;

    /**
     * 来源网站
     */
    @TableField("F_LOGINSOURCEWEBSITE")
    @JSONField(name = "F_LoginSourceWebsite")
    private String fLoginsourcewebsite;
    /**
     * 登录时间
     */
    @TableField("F_LOGINTIME")
    @JSONField(name = "F_LoginTime")
    private String fLogintime;

    /**
     * 描述
     */
    @TableField("F_DESCRIPTION")
    @JSONField(name = "F_Description")
    private String fDescription;

}
