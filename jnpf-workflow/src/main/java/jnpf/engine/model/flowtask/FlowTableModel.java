package jnpf.engine.model.flowtask;

import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:17
 */
@Data
public class FlowTableModel {

    private String relationField;
    private String relationTable;
    private String table;
    private String tableName;
    private String tableField;
    private String typeId;
    private List<FlowFieldsModel> fields;

}
