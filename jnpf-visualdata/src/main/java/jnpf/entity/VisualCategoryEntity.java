package jnpf.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 大屏分类
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Data
@TableName("blade_visual_category")
public class VisualCategoryEntity {
    /** 主键 */
    @TableId("ID")
    private String id;

    /** 分类值 */
    @TableField("CATEGORY_KEY")
    private String categorykey;

    /** 分类名称 */
    @TableField("CATEGORY_VALUE")
    private String categoryvalue;

    /** 是否删除 */
    @TableField("IS_DELETED")
    private String isdeleted;

}
