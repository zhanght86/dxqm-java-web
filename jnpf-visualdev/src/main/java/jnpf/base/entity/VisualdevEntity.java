package jnpf.base.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Data
@TableName("base_visualdev")
public class VisualdevEntity {


    @TableId("F_ID")
    private String id;


    @TableField("F_DESCRIPTION")
    private String description;


    @TableField("F_SORTCODE")
    private Long sortCode;


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
    private String creatorUser;

    /**
     * 修改时间
     */
    @TableField(value = "F_LASTMODIFYTIME",fill = FieldFill.UPDATE)
    private Date lastModifyTime;

    /**
     * 修改用户
     */
    @TableField(value = "F_LASTMODIFYUSERID",fill = FieldFill.UPDATE)
    private String lastModifyUser;


    @TableField("F_DELETEMARK")
    private Integer deleteMark;


    @TableField("F_DELETETIME")
    private Date deleteTime;


    @TableField("F_DELETEUSERID")
    private String deleteUserId;


    /**
     * 名称
     */
    @TableField("F_FULLNAME")
    private String fullName;


    /**
     * 编码
     */
    @TableField("F_ENCODE")
    private String enCode;


    /**
     * 状态(0-暂存（默认），1-发布)
     */
    @TableField("F_STATE")
    private Integer state;

    /**
     * 类型(1-应用开发,2-移动开发,3-流程表单,4-Web表单,5-App表单)
     */
    @TableField("F_TYPE")
    private Integer type;

    /**
     * 关联的表
     */
    @TableField("F_TABLE")
    private String tables;

    /**
     * 分类(数据字典维护)
     */
    @TableField("F_CATEGORY")
    private String category;

    /**
     * 表单配置JSON
     */
    @TableField("F_FORMDATA")
    private String formData;

    /**
     * 列表配置JSON
     */
    @TableField("F_COLUMNDATA")
    private String columnData;
    /**
     * 关联数据连接id
     */
    @TableField("F_DbLinkId")
    private String dbLinkId;
    /**
     * 页面类型（1、纯表单，2、表单加列表，3、表单列表工作流）
     */
    @TableField("F_WebType")
    private String webType;

    /**
     * 工作流模板JSON
     */
    @TableField("F_FlowTemplateJson")
    private String flowTemplateJson;

    /**
     * 关联工作流连接id
     */
    @TableField("F_FlowId")
    private String flowId;
}

