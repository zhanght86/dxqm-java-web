package jnpf.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 产品商品
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 15:57:50
 */
@Data
@TableName("ext_productgoods")
public class ProductgoodsEntity {
    /**
     * 主键
     */
    @TableId("F_ID")
    private String id;

    /**
     * 分类主键
     */
    @TableField("F_CLASSIFYID")
    private String classifyId;

    /**
     * 产品编号
     */
    @TableField("F_CODE")
    private String code;

    /**
     * 产品名称
     */
    @TableField("F_FULLNAME")
    private String fullName;

    /**
     * 订货类型
     */
    @TableField("F_TYPE")
    private String type;

    /** 产品规格 */
    @TableField("F_PRODUCTSPECIFICATION")
    private String productSpecification;

    /** 单价 */
    @TableField("F_MONEY")
    private String money;

    /**
     * 库存数
     */
    @TableField("F_QTY")
    private Integer qty;

    /** 金额 */
    @TableField("F_AMOUNT")
    private String amount;

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
