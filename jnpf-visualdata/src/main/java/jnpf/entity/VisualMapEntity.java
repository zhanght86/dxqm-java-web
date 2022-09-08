package jnpf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 大屏地图配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Data
@TableName("blade_visual_map")
public class VisualMapEntity {
    /** 主键 */
    @TableId("ID")
    private String id;

    /** 地图名称 */
    @TableField("NAME")
    private String name;

    /** 地图数据 */
    @TableField("DATA")
    private String data;

}
