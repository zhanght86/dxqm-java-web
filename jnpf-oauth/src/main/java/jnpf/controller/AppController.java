package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.model.module.MenuListVO;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.service.ModuleService;
import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.model.flowengine.FlowEngineListVO;
import jnpf.engine.service.FlowEngineService;
import jnpf.base.entity.ModuleEntity;
import jnpf.model.app.*;
import jnpf.model.login.UserMenuModel;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.model.authorize.AuthorizeVO;
import jnpf.permission.model.user.UserAllModel;
import jnpf.permission.service.AuthorizeService;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.UserProvider;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * App登陆数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/3/16
 */
@Api(tags = "App登陆数据", value = "AppData")
@RestController
@RequestMapping("/api/oauth/App")
public class AppController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ModuleService moduleService;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private FlowEngineService flowEngineService;

    /**
     * 主页数据
     *
     * @return
     */
    @ApiOperation("主页数据")
    @GetMapping("/CurrentUser")
    public ActionResult<AppUserVO> data() {
        UserInfo userInfo = userProvider.get();
        AppUserVO userVO = new AppUserVO();
        UserEntity userEntity = userService.getInfo(userInfo.getUserId());
        List<UserAllModel> userList = userService.getAll();
        UserAllModel model = userList.stream().filter(t -> t.getId().equals(userEntity.getId())).findFirst().get();
        AppInfoModel infoVO = JsonUtil.getJsonToBean(model, AppInfoModel.class);
        infoVO.setDepartmentName(model.getDepartment());
        infoVO.setOrganizeName(model.getOrganize());
        infoVO.setMobilePhone(userEntity.getMobilePhone());
        infoVO.setBirthday(userEntity.getBirthday() != null ? userEntity.getBirthday().getTime() : 0);
        userVO.setUserInfo(infoVO);
        //工作区
        List<FlowEngineEntity> flowFormList = flowEngineService.getFlowFormList();
        List<FlowEngineListVO> data = JsonUtil.getJsonToList(flowFormList, FlowEngineListVO.class);
        List<AppFlowFormModel> flowListVO = JsonUtil.getJsonToList(data, AppFlowFormModel.class);
        String json = userEntity.getPropertyJson() != null ? userEntity.getPropertyJson() : "[]";
        List<String> jsonList = JsonUtil.getJsonToList(json, String.class);
        List<AppDataModel> isData = new ArrayList<>();
        //app常用数据
        for (AppFlowFormModel formModel : flowListVO) {
            boolean num = jsonList.stream().filter(t -> t.contains(formModel.getId())).count() > 0;
            formModel.setIsData(false);
            if (num) {
                formModel.setIsData(true);
                AppDataModel dataModel = JsonUtil.getJsonToBean(formModel, AppDataModel.class);
                isData.add(dataModel);
            }
        }
        userVO.setFlowFormList(flowListVO);
        userVO.setAppDataList(isData);
        //应用菜单
        AuthorizeVO authorizeModel = authorizeService.getAuthorize(true);
        List<ModuleModel> buttonList = authorizeModel.getModuleList();
        List<ModuleEntity> menuList = moduleService.getList();
        List<ModuleEntity> childMenuList = menuList.stream().filter(t -> "App".equals(t.getCategory()) && t.getEnabledMark() == 1).collect(Collectors.toList());
        List<UserMenuModel> list = JsonUtil.getJsonToList(childMenuList, UserMenuModel.class);
        List<SumTree<UserMenuModel>> menuListAll = TreeDotUtils.convertListToTreeDot(list);
        List<MenuListVO> menuvo = JsonUtil.getJsonToList(menuListAll, MenuListVO.class);
        List<AppMenuModel> menu = new LinkedList<>();
        for (MenuListVO listVO : menuvo) {
            if (listVO.getType() == 1 && listVO.getChildren() != null) {
                List<AppMenuModel> jsonToList = JsonUtil.getJsonToList(listVO.getChildren(), AppMenuModel.class);
                for (AppMenuModel appModel : jsonToList) {
                    boolean count = buttonList.stream().filter(t -> t.getId().equals(appModel.getId())).count() > 0;
                    if (count) {
                        menu.add(appModel);
                    }
                }
            }
        }
        userVO.setMenuList(menu);
        return ActionResult.success(userVO);
    }

}
