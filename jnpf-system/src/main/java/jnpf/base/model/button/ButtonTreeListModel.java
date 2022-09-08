package jnpf.base.model.button;


import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:30
 */
@Data
public class ButtonTreeListModel extends SumTree {
    private String  id;
    private String parentId;
    private String fullName;
    private String enCode;
    private String icon;
    private Integer enabledMark;
    private String description;
    private Long sortCode;
}
