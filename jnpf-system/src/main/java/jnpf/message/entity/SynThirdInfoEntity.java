package jnpf.message.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 第三方工具的公司-部门-用户同步表模型
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021/4/23 17:06
 */
@Data
@TableName("base_synthirdinfo")
public class SynThirdInfoEntity implements Serializable {

    /**
     * 主键
     */
    @TableId("F_ID")
    private String id;

    /**
     * 第三方类型(1:企业微信;2:钉钉)
     */
    @TableField("F_THIRDTYPE")
    private Integer thirdtype;

    /**
     * 数据类型(1:组织(公司与部门);2:用户)
     */
    @TableField("F_DATATYPE")
    private Integer datatype;

    /**
     * 本地对象ID(公司ID、部门ID、用户ID)
     */
    @TableField("F_SYSOBJID")
    private String sysObjId;

    /**
     * 第三方对象ID(公司ID、部门ID、用户ID)
     */
    @TableField("F_THIRDOBJID")
    private String thirdObjId;

    /**
     * 同步状态(0:未同步;1:同步成功;2:同步失败)
     */
    @TableField("F_SYNSTATE")
    private Integer synstate;

    /**
     * 描述
     */
    @TableField("F_DESCRIPTION")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "F_CREATORTIME",fill = FieldFill.INSERT)
    private Date creatorTime;

    /**
     * 创建用户
     */
    @TableField(value = "F_CREATORUSERID",fill = FieldFill.INSERT)
    private String creatorUserId;

    /**
     * 修改用户
     */
    @TableField(value = "F_LASTMODIFYUSERID",fill = FieldFill.UPDATE)
    private String lastModifyUserId;

    /**
     * 修改时间
     */
    @TableField(value = "F_LASTMODIFYTIME",fill = FieldFill.UPDATE)
    @JSONField(name = "F_LastModifyTime")
    private Date lastModifyTime;

}
