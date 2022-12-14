package jnpf.permission.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Data
@TableName(value = "base_user")
public class UserEntity {
    /**
     * 用户主键
     */
    @TableId("F_ID")
    private String id;
    /**
     * 账户
     */
    @TableField("F_ACCOUNT")
    private String account;

    /**
     * 姓名
     */
    @TableField("F_REALNAME")
    private String realName;

    /**
     * 快速查询
     */
    @TableField("F_QUICKQUERY")
    private String quickQuery;

    /**
     * 呢称
     */
    @TableField("F_NICKNAME")
    private String nickName;

    /**
     * 头像
     */
    @TableField("F_HEADICON")
    private String headIcon;

    /**
     * 性别
     */
    @TableField("F_GENDER")
    private Integer gender;

    /**
     * 生日
     */
    @TableField(value = "F_BIRTHDAY",fill= FieldFill.UPDATE)
    private Date birthday;

    /**
     * 手机
     */
    @TableField("F_MOBILEPHONE")
    private String mobilePhone;

    /**
     * 电话
     */
    @TableField("F_TELEPHONE")
    private String telePhone;

    /**
     * F_Landline
     */
    @TableField("F_LANDLINE")
    private String landline;

    /**
     * 邮箱
     */
    @TableField("F_EMAIL")
    private String email;

    /**
     * 民族
     */
    @TableField("F_NATION")
    private String nation;

    /**
     * 籍贯
     */
    @TableField("F_NATIVEPLACE")
    private String nativePlace;

    /**
     * 入职日期
     */
    @TableField(value = "F_ENTRYDATE",fill= FieldFill.UPDATE)
    private Date entryDate;

    /**
     * 证件类型
     */
    @TableField("F_CERTIFICATESTYPE")
    private String certificatesType;

    /**
     * 证件号码
     */
    @TableField("F_CERTIFICATESNUMBER")
    private String certificatesNumber;

    /**
     * 文化程度
     */
    @TableField("F_EDUCATION")
    private String education;

    /**
     * F_UrgentContacts
     */
    @TableField("F_URGENTCONTACTS")
    private String urgentContacts;

    /**
     * 紧急电话
     */
    @TableField("F_URGENTTELEPHONE")
    private String urgentTelePhone;

    /**
     * 通讯地址
     */
    @TableField("F_POSTALADDRESS")
    private String postalAddress;

    /**
     * 自我介绍
     */
    @TableField("F_SIGNATURE")
    private String signature;

    /**
     * 密码
     */
    @TableField("F_PASSWORD")
    private String password;

    /**
     * 秘钥
     */
    @TableField("F_SECRETKEY")
    private String secretkey;

    /**
     * 首次登录时间
     */
    @TableField("F_FIRSTLOGTIME")
    private Date irstLogTime;

    /**
     * 首次登录IP
     */
    @TableField("F_FIRSTLOGIP")
    private String firstLogIp;

    /**
     * 前次登录时间
     */
    @TableField("F_PREVLOGTIME")
    private Date prevLogTime;

    /**
     * 前次登录IP
     */
    @TableField("F_PREVLOGIP")
    private String prevLogIp;

    /**
     * 最后登录时间
     */
    @TableField("F_LASTLOGTIME")
    private Date lastLogTime;

    /**
     * 最后登录IP
     */
    @TableField("F_LASTLOGIP")
    private String lastLogIp;

    /**
     * 登录成功次数
     */
    @TableField("F_LOGSUCCESSCOUNT")
    private Integer logSuccessCount;

    /**
     * 登录错误次数
     */
    @TableField("F_LOGERRORCOUNT")
    private Integer logErrorCount;

    /**
     * 最后修改密码时间
     */
    @TableField("F_CHANGEPASSWORDDATE")
    private Date changePasswordDate;

    /**
     * 系统语言
     */
    @TableField("F_LANGUAGE")
    private String language;

    /**
     * 系统样式
     */
    @TableField("F_THEME")
    private String theme;

    /**
     * 常用菜单
     */
    @TableField("F_COMMONMENU")
    private String commonMenu;

    /**
     * 是否管理员
     */
    @TableField("F_ISADMINISTRATOR")
    private Integer isAdministrator;

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

    /**
     * 主管主键
     */
    @TableField("F_MANAGERID")
    private String managerId;

    /**
     * 组织主键
     */
    @TableField("F_ORGANIZEID")
    private String organizeId;

    /**
     * 岗位主键
     */
    @TableField("F_POSITIONID")
    private String positionId;

    /**
     * 角色主键
     */
    @TableField("F_ROLEID")
    private String roleId;

    /**
     * 角色主键
     */
    @TableField("F_PORTALID")
    private String portalId;


}
