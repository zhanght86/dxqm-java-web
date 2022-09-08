package jnpf.permission.model.authorize;


import jnpf.base.model.button.ButtonModel;
import jnpf.base.model.column.ColumnModel;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.model.resource.ResourceModel;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:29
 */
@Data
public class AuthorizeVO {
    // 菜单
//    private List<MenuModel> menuList;

    /**
     * 功能
     */
    private List<ModuleModel> moduleList;

    /**
     * 按钮
     */
    private List<ButtonModel> buttonList;

    /**
     * 视图
     */
    private List<ColumnModel> columnList;

    /**
     * 资源
     */
    private List<ResourceModel> resourceList;

    public AuthorizeVO(List<ModuleModel> moduleList, List<ButtonModel> buttonList, List<ColumnModel> columnList, List<ResourceModel> resourceList){
//        this.menuList = menuList;
        this.moduleList = moduleList;
        this.buttonList = buttonList;
        this.columnList = columnList;
        this.resourceList = resourceList;
    }
}
