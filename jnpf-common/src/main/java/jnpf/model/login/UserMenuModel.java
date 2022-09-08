package jnpf.model.login;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:59
 */
@Data
public class UserMenuModel extends SumTree{
    private String id;
    private String fullName;
    private Integer isButtonAuthorize;
    private Integer isColumnAuthorize;
    private Integer isDataAuthorize;
    private String enCode;
    private String parentId;
    private String icon;
    private String urlAddress;
    private String linkTarget;
    private Integer type;
    private Boolean isData;
    private Integer enabledMark;
    private Long sortCode;
    private String category;
    private String description;
    private String propertyJson;
}
