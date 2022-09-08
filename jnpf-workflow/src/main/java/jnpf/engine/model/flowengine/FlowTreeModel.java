package jnpf.engine.model.flowengine;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
public class FlowTreeModel extends SumTree {
    private String enCode;
    private Long num;
    private String fullName;
    private String formType;
    private String type;
    private String icon;
    private String category;
    private String iconBackground;
    private String visibleType;
    private String creatorUser;
    private Long creatorTime;
    private Long sortCode;
    private Integer enabledMark;
}
