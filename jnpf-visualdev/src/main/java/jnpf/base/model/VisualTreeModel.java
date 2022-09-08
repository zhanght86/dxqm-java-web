package jnpf.base.model;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
public class VisualTreeModel extends SumTree {
    private String fullName;
    private Long num;
    private String enCode;
    private Integer state;
    private String type;
    private String tables;
    private Long creatorTime;
    private String creatorUser;
    private Long lastModifyTime;
    private String lastModifyUser;
}
