package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.entity.ModuleEntity;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.service.ModuleService;
import jnpf.base.vo.ListVO;
import jnpf.model.AppMenuListVO;
import jnpf.model.app.AppMenuModel;
import jnpf.model.login.UserMenuModel;
import jnpf.permission.model.authorize.AuthorizeVO;
import jnpf.permission.service.AuthorizeService;
import jnpf.util.JsonUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * app应用
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-07-08
 */
@Api(tags = "app应用", value = "Menu")
@RestController
@RequestMapping("/api/app/Menu")
public class AppMenuController {

    @Autowired
    private ModuleService moduleService;
    @Autowired
    private AuthorizeService authorizeService;

    /**
     * app应用
     *
     * @return
     */
    @ApiOperation("获取菜单列表")
    @GetMapping
    public ActionResult<ListVO<AppMenuModel>> list() {
        AuthorizeVO authorizeModel = authorizeService.getAuthorize(true);
        List<ModuleModel> buttonList = authorizeModel.getModuleList();
        List<ModuleEntity> menuList = moduleService.getList().stream().filter(t -> "App".equals(t.getCategory()) && t.getEnabledMark() == 1).collect(Collectors.toList());
        List<UserMenuModel> list = new LinkedList<>();
        for (ModuleEntity module : menuList) {
            boolean count = buttonList.stream().filter(t -> t.getId().equals(module.getId())).count() > 0;
            UserMenuModel userMenuModel = JsonUtil.getJsonToBean(module, UserMenuModel.class);
            if (count) {
                list.add(userMenuModel);
            }
        }
        List<SumTree<UserMenuModel>> menuAll = TreeDotUtils.convertListToTreeDot(list);
        List<AppMenuListVO> menuListAll = JsonUtil.getJsonToList(menuAll, AppMenuListVO.class);
        List<AppMenuListVO> data = new LinkedList<>();
        for(AppMenuListVO appMenu : menuListAll){
            if("-1".equals(appMenu.getParentId())){
                data.add(appMenu);
            }
        }
        ListVO listVO = new ListVO();
        listVO.setList(data);
        return ActionResult.success(listVO);
    }


}
