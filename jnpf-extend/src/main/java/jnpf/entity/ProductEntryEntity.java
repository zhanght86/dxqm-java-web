package jnpf.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 产品明细
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 10:40:59
 */
@Data
@TableName("ext_productentry")
public class ProductEntryEntity {

    /**
     * 主键
     */
    @TableId("F_ID")
    private String id;

    /**
     * 订单主键
     */
    @TableField("F_PRODUCTID")
    private String productId;

    /**
     * 产品编码
     */
    @TableField("F_PRODUCTCODE")
    private String productCode;

    /**
     * 产品名称
     */
    @TableField("F_PRODUCTNAME")
    private String productName;

    /**
     * 产品规格
     */
    @TableField("F_PRODUCTSPECIFICATION")
    private String productSpecification;

    /**
     * 数量
     */
    @TableField("F_QTY")
    private Integer qty;

    /**
     * 订货类型
     */
    @TableField("F_TYPE")
    private String type;

    /**
     * 单价
     */
    @TableField("F_MONEY")
    private BigDecimal money;

    /**
     * 折后单价
     */
    @TableField("F_PRICE")
    private BigDecimal price;

    /**
     * 单位
     */
    @TableField("F_UTIL")
    private String util;

    /**
     * 控制方式
     */
    @TableField("F_COMMANDTYPE")
    private String commandType;

    /**
     * 金额
     */
    @TableField("F_AMOUNT")
    private BigDecimal amount;

    /**
     * 活动
     */
    @TableField("F_ACTIVITY")
    private String activity;

    /**
     * 备注
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
