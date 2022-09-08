package jnpf.base.model.module;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class ModuleCrForm {
    private String parentId;
    private String fullName;
    private Integer isButtonAuthorize;
    private Integer isColumnAuthorize;
    private Integer isDataAuthorize;
    private String enCode;
    private String icon;
    private Integer type;
    private String urlAddress;
    private String linkTarget;
    /**
     * 菜单分类 Web、App
     */
    private String category;
    private String description;
    private Integer enabledMark;
    private long sortCode;
    private Object propertyJson;
}
