package jnpf.model.login;

import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:54
 */
@Data
public class PermissionModel {
    private String modelId;
    private String moduleName;
    private List<PermissionVO> button;
    private List<PermissionVO> column;
    private List<PermissionVO> resource;
}
