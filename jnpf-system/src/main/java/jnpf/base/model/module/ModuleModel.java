package jnpf.base.model.module;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:14
 */
@Data
public class ModuleModel {
    private String id;
    private String parentId;
    private String fullName;
    private String icon;
    /**
    * 1-类别、2-页面
    */
    private int type;
    private String urlAddress;
    private String linkTarget;
    private String category;
    private String description;
    private Long sortCode=999999L;
    private String enCode;
    private String propertyJson;
}
