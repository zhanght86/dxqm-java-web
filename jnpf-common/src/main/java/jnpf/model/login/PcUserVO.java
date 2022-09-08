package jnpf.model.login;

import lombok.Builder;
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
@Builder
public class PcUserVO {
    private List<MenuTreeVO> menuList;
    private List<PermissionModel> permissionList;
    private UserCommonInfoVO userInfo;

    public PcUserVO() {
    }

    public PcUserVO(List<MenuTreeVO> menuList, List<PermissionModel> permissionList, UserCommonInfoVO userInfo) {
        this.menuList = menuList;
        this.permissionList = permissionList;
        this.userInfo = userInfo;
    }
}
