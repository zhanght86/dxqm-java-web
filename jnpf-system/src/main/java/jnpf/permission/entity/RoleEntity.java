package jnpf.permission.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 系统角色
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Data
@TableName("base_role")
public class RoleEntity {
    /**
     * 角色主键
     */
    @TableId("F_ID")
    private String id;

    /**
     * 角色名称
     */
    @TableField("F_FULLNAME")
    private String fullName;

    /**
     * 角色编码
     */
    @TableField("F_ENCODE")
    private String enCode;

    /**
     * 角色类型
     */
    @TableField("F_TYPE")
    private String type;

    /**
     * 扩展属性
     */
    @TableField("F_PROPERTYJSON")
    private String propertyJson;

    /**
     * 描述
     */
    @TableField("F_DESCRIPTION")
    private String description;

    /**
     * 排序码
     */
    @TableField("F_SORTCODE")
    private Long sortCode;

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
     * 修改时间
     */
    @TableField(value = "F_LASTMODIFYTIME",fill = FieldFill.UPDATE)
    private Date lastModifyTime;

    /**
     * 修改用户
     */
    @TableField(value = "F_LASTMODIFYUSERID",fill = FieldFill.UPDATE)
    private String lastModifyUserId;

    /**
     * 删除标志
     */
    @TableField("F_DELETEMARK")
    private Integer deleteMark;

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
}
