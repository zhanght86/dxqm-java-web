package jnpf.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * app常用数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021-07-08
 */
@Data
@TableName("base_appdata")
public class AppDataEntity {
    /**
     * 单据主键
     */
    @TableId("F_ID")
    private String id;

    /**
     * 对象类型
     */
    @TableField("F_OBJECTTYPE")
    private String objectType;

    /**
     * 对象主键
     */
    @TableField("F_OBJECTID")
    private String objectId;

    /**
     * 数据
     */
    @TableField("F_OBJECTDATA")
    private String objectData;

    /**
     * 描述
     */
    @TableField("F_DESCRIPTION")
    private String description;

    /**
     * 有效标志
     */
    @TableField("F_ENABLEDMARK")
    private Integer enabledMark;

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
     * 删除时间
     */
    @TableField("F_DELETETIME")
    private Date deleteTime;

    /**
     * 删除用户
     */
    @TableField("F_DELETEUSERID")
    private String deleteUserId;

    /**
     * 删除标志
     */
    @TableField("F_DELETEMARK")
    private Integer deleteMark;
}
