package jnpf.base.model.button;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:30
 */
@Data
public class ButtonModel {
    private String id;
    private String parentId;
    private String fullName;
    private String enCode;
    private String icon;
    private String urlAddress;
    private String moduleId;
}
