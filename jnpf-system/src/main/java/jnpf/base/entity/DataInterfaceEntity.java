package jnpf.base.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
@TableName("base_datainterface")
public class DataInterfaceEntity {
    /**
     * 主键ID
     */
    @TableId("F_Id")
    private String id;

    /**
     * 分组ID
     */
    @TableField("F_CategoryId")
    private String categoryId;

    /**
     * 接口名称
     */
    @TableField("F_FullName")
    private String fullName;

    /**
     * 数据源id
     */
    @TableField("F_DbLinkId")
    private String dbLinkId;

    /**
     * 数据类型(1-动态数据SQL查询，2-静态数据,3-Api数据)
     */
    @TableField("F_DataType")
    private Integer dataType;

    /**
     * 接口路径
     */
    @TableField("F_Path")
    private String path;

    /**
     * 请求方式
     */
    @TableField("F_RequestMethod")
    private String requestMethod;

    /**
     * 返回类型
     */
    @TableField("F_ResponseType")
    private String responseType;

    /**
     * 查询语句
     */
    @TableField("F_Query")
    private String query;

    /**
     * 请求头参数
     */
    @TableField("F_RequestHeaders")
    private String requestHeaders;

    /**
     * 验证类型   0-不验证，1-授权验证，2-域名验证
     */
    @TableField("F_CheckType")
    private Integer checkType;

    /**
     * 请求参数JSON
     */
    @TableField("F_RequestParameters")
    private String requestParameters;

    /**
     * 返回参数JSON
     */
    @TableField("F_ResponseParameters")
    private String responseParameters;

    /**
     * 接口编码
     */
    @TableField("F_EnCode")
    private String enCode;

    /**
     * 排序码(默认0)
     */
    @TableField("F_SortCode")
    private Long sortCode;

    /**
     * 状态(0-默认，禁用，1-启用)
     */
    @TableField("F_EnabledMark")
    private Integer enabledMark;

    /**
     * 描述或说明
     */
    @TableField("F_Description")
    private String description;

    /**
     * 创建时间
     */
    @TableField(value = "F_CREATORTIME", fill = FieldFill.INSERT)
    private Date creatorTime;

    /**
     * 创建用户
     */
    @TableField(value = "F_CREATORUSERID", fill = FieldFill.INSERT)
    private String creatorUser;

    /**
     * 修改时间
     */
    @TableField(value = "F_LASTMODIFYTIME", fill = FieldFill.UPDATE)
    private Date lastModifyTime;

    /**
     * 修改用户
     */
    @TableField(value = "F_LASTMODIFYUSERID", fill = FieldFill.UPDATE)
    private String lastModifyUser;

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
