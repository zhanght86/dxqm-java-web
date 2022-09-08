package jnpf.permission.model.user;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class UserAllModel extends SumTree {
    private String id;
    private String account;
    private String gender;
    private String realName;
    private String headIcon;
    private String department;
    private String departmentId;
    private String organizeId;
    private String organize;
    private String roleId;
    private String roleName;
    private String positionId;
    private String positionName;
    private String managerId;
    private String managerName;
    private String quickQuery;
    private String portalId;
    private Integer isAdministrator;
}
