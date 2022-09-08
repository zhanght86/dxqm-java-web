package jnpf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 大屏基本配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Data
@TableName("blade_visual_config")
public class VisualConfigEntity {
    /** 主键 */
    @TableId("ID")
    private String id;

    /** 可视化表主键 */
    @TableField("VISUAL_ID")
    private String visualId;

    /** 配置json */
    @TableField("DETAIL")
    private String detail;

    /** 组件json */
    @TableField("COMPONENT")
    private String component;

}
