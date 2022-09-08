package jnpf.base;


import lombok.Data;
import lombok.experimental.Accessors;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/06/18 By:YanYu
 */
@Data
@Accessors(chain = true)
public class DbTableModel {

    /**
    * 标识
    */
    private String id;

    /**
    * 表名
    */
    private String table;

    /**
     * 新建表名
     */
    private String newTable;

    /**
    * 表说明
     * （PS:属性名歧义,但涉及多平台，故内部做处理）
    */
    private String tableName;

    public String getTableComment(){
        return tableName;
    }

    /**
     * 说明
     * (PS:这个字段用来返回，字段名+注释)
     */
    private String description;

    /**
    * 大小
    */
    private String size;

    /**
    * 总数
    */
    private Integer sum;

    /**
    * 主键
    */
    private String primaryKey;

    /**
    * 数据源主键
    */
    private String dataSourceId;


}
