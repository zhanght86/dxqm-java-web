package jnpf.base.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 数据接口调用日志
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-03
 */
@Data
@TableName("base_datainterfacelog")
public class DataInterfaceLogEntity implements Serializable {

    /**
     * 主键id
     */
    @TableId("F_Id")
    private String id;

    /**
     * 调用接口id
     */
    @TableField("F_InvokId")
    private String invokId;

    /**
     * 调用时间
     */
    @TableField(value = "F_InvokTime", fill = FieldFill.INSERT)
    private Date invokTime;

    /**
     * 调用者id
     */
    @TableField("F_UserId")
    private String userId;

    /**
     * 请求ip
     */
    @TableField("F_InvokIp")
    private String invokIp;

    /**
     * 请求设备
     */
    @TableField("F_InvokDevice")
    private String invokDevice;

    /**
     * 请求类型
     */
    @TableField("F_InvokType")
    private String invokType;

    /**
     * 请求耗时
     */
    @TableField("F_InvokWasteTime")
    private Integer invokWasteTime;

}
