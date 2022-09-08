package jnpf.database.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
@Accessors(chain = true)
public class DbTableFieldModel {

    /**
     * 字段名
     */
    private String field;

    private String newField;

    private String oldField;

    /**
     * 默认值
     */
    private String defaults;

    /**
     * 自增
     */
    private String identity;


    /**==================修改添加相关信息=======================**/

    /**
     * 字段说明
     * （PS:属性名歧义,但涉及多平台，故内部做处理）
     */
    private String fieldName;

    /**
     * 修正fielName作为字段注释
     * @return String
     */
    private String fieldComment;

    /**
     * 说明
     * (PS:这个字段用来返回，字段名+注释)
     */
    private String description;

    /**
     * 数据类型
     */
    private String dataType;

    /**
     * 数据长度
     */
    private String dataLength;

    /**
     * 主键
     */
    private Integer primaryKey;

    /**
     * 允许null值
     */
    private Integer allowNull;




}
