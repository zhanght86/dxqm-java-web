package jnpf.onlinedev.entity;

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
@TableName("base_visualdev_modeldata")
public class VisualdevModelDataEntity {


    @TableId("F_ID")
    private String id;


    @TableField("F_VISUALDEVID")
    private String visualDevId;


    @TableField("F_SORTCODE")
    private Long sortcode;


    @TableField("F_ENABLEDMARK")
    private Integer enabledmark;


    @TableField("F_CREATORTIME")
    private Date creatortime;


    @TableField("F_CREATORUSERID")
    private String creatoruserid;


    @TableField("F_LASTMODIFYTIME")
    private Date lastModifyTime;


    @TableField("F_LASTMODIFYUSERID")
    private String lastmodifyuserid;


    @TableField("F_DELETEMARK")
    private Integer deletemark;


    @TableField("F_DELETETIME")
    private Date deletetime;


    @TableField("F_DELETEUSERID")
    private String deleteuserid;
    @TableField("F_DATA")
    private String data;


}

