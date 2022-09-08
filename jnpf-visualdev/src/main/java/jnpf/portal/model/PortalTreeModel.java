package jnpf.portal.model;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 */
@Data
public class PortalTreeModel extends SumTree {
    private String fullName;
    private Long num;
    private String enCode;
    private Long creatorTime;
    private Integer enabledMark;
    private String creatorUser;
    private Long lastModifyTime;
    private String lastModifyUser;
}
