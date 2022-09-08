package jnpf.model.tableexample;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

import java.util.Map;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:21
 */
@Data
public class TableExampleTreeModel extends SumTree {
    private Boolean loaded;
    private Boolean expanded;
    private Map<String, Object> ht;
    private String text;
}
