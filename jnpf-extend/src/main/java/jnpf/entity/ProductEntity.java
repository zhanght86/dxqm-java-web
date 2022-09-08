package jnpf.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 销售订单
 *
 * @版本： V3.1.0
 * @版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @作者： JNPF开发平台组
 * @日期： 2021-07-10 10:40:59
 */
@Data
@TableName("ext_product")
public class ProductEntity {
    /**
     * 名称
     */
    @TableId("F_ID")
    private String id;

    /**
     * 订单编号
     */
    @TableField("F_CODE")
    private String code;

    /**
     * 客户类别
     */
    @TableField("F_TYPE")
    private String type;

    /**
     * 客户id
     */
    @TableField("F_CUSTOMERID")
    private String customerId;

    /**
     * 客户名称
     */
    @TableField("F_CustomerName")
    private String customerName;

    /**
     * 制单人
     */
    @TableField("F_SALESMANID")
    private String salesmanId;

    /**
     * 制单人
     */
    @TableField("F_SALESMANNAME")
    private String salesmanName;

    /**
     * 制单日期
     */
    @TableField("F_SALESMANDATE")
    private Date salesmanDate;

    /**
     * 审核人
     */
    @TableField("F_AUDITNAME")
    private String auditName;

    /**
     * 审核日期
     */
    @TableField("F_AUDITDATE")
    private Date auditDate;

    /**
     * 审核状态
     */
    @TableField("F_AUDITSTATE")
    private Integer auditState;

    /**
     * 发货仓库
     */
    @TableField("F_GOODSWAREHOUSE")
    private String goodsWarehouse;

    /**
     * 发货日期
     */
    @TableField("F_GOODSDATE")
    private Date goodsDate;

    /**
     * 发货通知人
     */
    @TableField("F_CONSIGNOR")
    private String consignor;

    /**
     * 发货状态
     */
    @TableField("F_GOODSSTATE")
    private Integer goodsState;

    /**
     * 关闭状态
     */
    @TableField("F_CLOSESTATE")
    private Integer closeState;

    /**
     * 关闭日期
     */
    @TableField("F_CLOSEDATE")
    private Date closeDate;

    /**
     * 收款方式
     */
    @TableField("F_GATHERINGTYPE")
    private String gatheringType;

    /**
     * 业务员
     */
    @TableField("F_BUSINESS")
    private String business;

    /**
     * 送货地址
     */
    @TableField("F_ADDRESS")
    private String address;

    /**
     * 联系方式
     */
    @TableField("F_CONTACTTEL")
    private String contactTel;

    /**
     * 联系人
     */
    @TableField("F_CONTACTNAME")
    private String contactName;

    /**
     * 收货消息
     */
    @TableField("F_HARVESTMSG")
    private Integer harvestMsg;

    /**
     * 收货仓库
     */
    @TableField("F_HARVESTWAREHOUSE")
    private String harvestWarehouse;

    /**
     * 代发客户
     */
    @TableField("F_ISSUINGNAME")
    private String issuingName;

    /**
     * 让利金额
     */
    @TableField("F_PARTPRICE")
    private BigDecimal partPrice;

    /**
     * 优惠金额
     */
    @TableField("F_REDUCEDPRICE")
    private BigDecimal reducedPrice;

    /**
     * 折后金额
     */
    @TableField("F_DISCOUNTPRICE")
    private BigDecimal discountPrice;

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
