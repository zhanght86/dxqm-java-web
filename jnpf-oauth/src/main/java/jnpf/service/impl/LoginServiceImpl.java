package jnpf.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import jnpf.base.UserInfo;
import jnpf.base.model.button.ButtonModel;
import jnpf.base.model.column.ColumnModel;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.model.resource.ResourceModel;
import jnpf.base.service.SysconfigService;
import jnpf.base.util.Props;
import jnpf.config.ConfigValueUtil;
import jnpf.database.data.DataSourceContextHolder;
import jnpf.exception.LoginException;
import jnpf.model.login.*;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.model.authorize.AuthorizeVO;
import jnpf.permission.model.user.UserAllModel;
import jnpf.permission.service.*;
import jnpf.portal.model.PortalSelectModel;
import jnpf.portal.service.PortalService;
import jnpf.service.LoginService;
import jnpf.util.*;
import jnpf.util.treeutil.ListToTreeUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import jnpf.util.wxutil.HttpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/3/16
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private Props props;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private SysconfigService sysconfigService;
    @Autowired
    private PortalService portalService;

    @Override
    public UserInfo checkTenant(LoginForm loginForm) throws LoginException {
        UserInfo userInfo = new UserInfo();
        String tenantId = "";
        if (Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
            String[] tenantAccount = loginForm.getAccount().split("\\@");
            tenantId = tenantAccount.length == 1 ? loginForm.getAccount() : tenantAccount[0];
            loginForm.setAccount(tenantAccount.length == 1 ? "admin" : tenantAccount[1]);
            if (tenantAccount.length > 2 || tenantId.length() > 11) {
                throw new LoginException("账号有误，请重新输入！");
            }
            Map<String, Object> resulList;
            //切换成租户库
            JSONObject object;
            if (props.getPortUrl().contains("https")) {
                object = HttpUtil.httpRequest(props.getPortUrl() + tenantId, "GET", null);
            } else {
                object = HttpUtil.httpRequest(props.getPortUrl() + tenantId, "GET", null);
            }
            if (object == null || "500".equals(object.get("code").toString())) {
                throw new LoginException("登陆繁忙，请稍后再试");
            }
            if ("400".equals(object.getString("code"))) {
                throw new LoginException(object.getString("msg"));
            }
            resulList = JsonUtil.stringToMap(object.getString("data"));
            String name = resulList.get("java") != null ? String.valueOf(resulList.get("java")) : String.valueOf(resulList.get("dbName"));
            userInfo.setTenantId(tenantId);
            userInfo.setTenantDbConnectionString(name);
            DataSourceContextHolder.setDatasource(tenantId, name);
        }
        return userInfo;
    }

    @Override
    public UserInfo userInfo(UserInfo userInfo, UserEntity userEntity) throws LoginException {
        userInfo.setIsAdministrator(BooleanUtil.toBoolean(String.valueOf(userEntity.getIsAdministrator())));
        userInfo.setUserId(userEntity.getId());
        userInfo.setUserAccount(userEntity.getAccount());
        userInfo.setUserName(userEntity.getRealName());
        userInfo.setUserIcon(userEntity.getHeadIcon());
        userInfo.setDepartmentId(userEntity.getOrganizeId());
        //公司Id
        OrganizeEntity organizeEntity = organizeService.getById(userEntity.getOrganizeId());
        if (organizeEntity == null) {
            List<OrganizeEntity> list = organizeService.getList();
            if (list.size() > 0) {
                organizeEntity = list.get(0);
            }
        }
        userInfo.setOrganizeId(organizeEntity != null ? organizeEntity.getParentId() : "");
        userInfo.setManagerId(userEntity.getManagerId());
        boolean b = BooleanUtil.toBoolean(String.valueOf((userEntity.getIsAdministrator())));
        userInfo.setSubOrganizeIds(this.getSubOrganizeIds(userEntity.getOrganizeId(), b));

        HashSet<String> subordinateIds = new HashSet<>();
        this.getSubordinateId(userEntity.getId(), b, subordinateIds);

        userInfo.setSubordinateIds(subordinateIds.toArray(new String[subordinateIds.size()]));
        userInfo.setPositionIds(this.getPositionId(userEntity.getId(), b));
        userInfo.setRoleIds(this.getRoleId(userEntity.getId(), b));
        userInfo.setLoginIpAddress(IpUtil.getIpAddr());
        userInfo.setLoginTime(DateUtil.getmmNow());
        userInfo.setLoginPlatForm(ServletUtil.getUserAgent());
        userInfo.setPrevLoginTime(userEntity.getPrevLogTime());
        userInfo.setPrevLoginIpAddress(userEntity.getPrevLogIp());
        userInfo.setPrevLoginIpAddressName(IpUtil.getIpCity(userEntity.getPrevLogIp()));
        BaseSystemInfo sysConfigInfo = sysconfigService.getSysInfo();
        String time = sysConfigInfo.getTokenTimeout();
        //安全验证
        if (!"admin".equals(userEntity.getId()) && "1".equals(sysConfigInfo.getWhitelistSwitch())) {
            List<String> ipList = Arrays.asList(sysConfigInfo.getWhitelistIp().split(","));
            if (!ipList.contains(IpUtil.getIpAddr())) {
                throw new LoginException("此IP未在白名单中，请联系管理员");
            }
        }
        Integer minu = Integer.valueOf(time);
        userInfo.setOverdueTime(DateUtil.dateAddMinutes(null, minu));
        userInfo.setTokenTimeout(minu);
        return userInfo;
    }

    @Override
    public boolean isExistUser(String account) throws LoginException {
        UserEntity userEntity = userService.getUserEntity(account);
        if (userEntity == null) {
            throw new LoginException("无效的账号");
        }
        //判断是否组织、岗位、角色、部门主管是否为空，为空则抛出异常
        //判断是否为管理员，是否为Admin(Admin为最高账号，不受限制)
        if (!"admin".equals(userEntity.getId()) || userEntity.getIsAdministrator() != 1) {
            //组织id为空则直接抛出异常
            if (StringUtil.isEmpty(userEntity.getOrganizeId())) {
                throw new LoginException("账号异常，请联系管理员修改所属组织信息");
            }
            // 岗位id为空则直接抛出异常
//            if (StringUtil.isEmpty(userEntity.getPositionId())) {
//                throw new LoginException("账号异常，请联系管理员修改所属岗位信息");
//            }
//            //角色id为空则直接抛出异常
//            if (StringUtil.isEmpty(userEntity.getRoleId())) {
//                throw new LoginException("账号异常，请联系管理员修改角色信息");
//            }
//            //主管id为空则直接抛出异常
//            if (StringUtil.isEmpty(userEntity.getManagerId())) {
//                throw new LoginException("账号异常，请联系管理员修改主管信息");
//            }
        }

        //判断用户所属的角色是否被禁用
        RoleEntity roleEntity;
        if (!StringUtil.isEmpty(userEntity.getRoleId())) {
            String[] roleIds = userEntity.getRoleId().split(",");
            int i = 0;
            if (userEntity.getIsAdministrator() == 0) {
                for (String roleId : roleIds) {
                    roleEntity = roleService.getInfo(roleId);
                    if (roleEntity != null && roleEntity.getEnabledMark() != null && roleEntity.getEnabledMark() != 0) {
                        i = 1;
                    }
                }
                if (i != 1) {
                    throw new LoginException("权限不足");
                }
            }

        }
        if (userEntity.getIsAdministrator() == 0) {
            if (userEntity.getEnabledMark() == null) {
                throw new LoginException("账号未被激活");
            }
            if (userEntity.getEnabledMark() == 0) {
                throw new LoginException("账号被禁用");
            }
        }
        if (userEntity.getDeleteMark() != null && userEntity.getDeleteMark() == 1) {
            throw new LoginException("账号已被删除");
        }
        return true;
    }

    /**
     * 获取用户登陆信息
     *
     * @return
     */
    @Override
    public PcUserVO getCurrentUser() {
        BaseSystemInfo baseSystemInfo = sysconfigService.getSysInfo();
        UserInfo userInfo = userProvider.get();
        AuthorizeVO authorizeModel = authorizeService.getAuthorize(true);

        //获取用户的信息
        UserCommonInfoVO infoVO = JsonUtil.getJsonToBean(userInfo(userInfo, baseSystemInfo), UserCommonInfoVO.class);
        //转成tree的方法
        List<ModuleModel> menuList = authorizeModel.getModuleList().stream().filter(t -> "Web".equals(t.getCategory())).collect(Collectors.toList());
        List<UserMenuModel> menu = JsonUtil.getJsonToList(menuList, UserMenuModel.class);
        //外层菜单排序
        menu = menu.stream().sorted(Comparator.comparing(UserMenuModel::getSortCode)).collect(Collectors.toList());
        List<SumTree<UserMenuModel>> menus = TreeDotUtils.convertListToTreeDot(menu);
        //返回前台tree的list
        List<MenuTreeVO> list = JsonUtil.getJsonToList(menus, MenuTreeVO.class);
        //保证内层排序
        list = list.stream().sorted(Comparator.comparing(MenuTreeVO::getSortCode)).collect(Collectors.toList());
        //获取全部用户
        List<UserAllModel> userList = userService.getAll();
        UserAllModel userAllVO = userList.stream().filter(t -> t.getId().equals(infoVO.getUserId())).findFirst().orElse(new UserAllModel());
        //赋值门户id
        infoVO.setPortalId(userAllVO.getPortalId());
        //赋值部门
        infoVO.setDepartmentId(userAllVO.getOrganizeId());
        infoVO.setDepartmentName(userAllVO.getDepartment());
        //赋值公司
        infoVO.setOrganizeName(userAllVO.getOrganize());
        //赋值岗位
        String[] positionId = userAllVO.getPositionId() != null ? userAllVO.getPositionId().split(",") : new String[]{};
        String[] positionName = userAllVO.getPositionName() != null ? userAllVO.getPositionName().split(",") : new String[]{};
        List<UserPositionVO> positionVO = new ArrayList<>();
        //判断数组长度是否越界
        if (positionId.length != positionName.length) {
            return null;
        }
        for (int i = 0; i < positionId.length; i++) {
            UserPositionVO userPositionVO = new UserPositionVO();
            userPositionVO.setId(positionId[i]);
            userPositionVO.setName(positionName[i]);
            positionVO.add(userPositionVO);
        }

        List<PermissionModel> models = new ArrayList<>();
        for (ModuleModel moduleModel : menuList) {
            PermissionModel model = new PermissionModel();
            model.setModelId(moduleModel.getId());
            model.setModuleName(moduleModel.getFullName());
            List<ButtonModel> buttonModels = authorizeModel.getButtonList().stream().filter(t -> moduleModel.getId().equals(t.getModuleId())).collect(Collectors.toList());
            List<ColumnModel> columnModels = authorizeModel.getColumnList().stream().filter(t -> moduleModel.getId().equals(t.getModuleId())).collect(Collectors.toList());
            List<ResourceModel> resourceModels = authorizeModel.getResourceList().stream().filter(t -> moduleModel.getId().equals(t.getModuleId())).collect(Collectors.toList());
            model.setButton(JsonUtil.getJsonToList(buttonModels, PermissionVO.class));
            model.setColumn(JsonUtil.getJsonToList(columnModels, PermissionVO.class));
            model.setResource(JsonUtil.getJsonToList(resourceModels, PermissionVO.class));
            if (moduleModel.getType() != 1) {
                models.add(model);
            }
        }
        Iterator<MenuTreeVO> iterator = list.iterator();

        while (iterator.hasNext()) {
            MenuTreeVO menuTreeVO = iterator.next();
            if (!"-1".equals(menuTreeVO.getParentId())) {
                iterator.remove();
            }
        }
        //门户
        if (StringUtil.isEmpty(infoVO.getPortalId())) {
            List<PortalSelectModel> portalList = portalService.getList("1").stream().filter(t -> !"0".equals(t.getParentId())).collect(Collectors.toList());
            if (portalList.size() > 0) {
                infoVO.setPortalId(portalList.get(0).getId());
            }
        }
        infoVO.setPositionIds(positionVO);
        PcUserVO userVO = new PcUserVO(list, models, infoVO);
        userVO.setPermissionList(models);
        userVO.getUserInfo().setHeadIcon(UploaderUtil.uploaderImg(userInfo.getUserIcon()));
        return userVO;
    }

    /**
     * 获取角色
     *
     * @param userId
     * @return
     */
    private String[] getRoleId(String userId, boolean isAdmin) {
        List<UserRelationEntity> data = userRelationService.getListByUserId(userId);
        if (!isAdmin) {
            data = data.stream().filter(m -> "Role".equals(m.getObjectType())).collect(Collectors.toList());
        }
        List<String> list = data.stream().map(t -> t.getObjectId()).collect(Collectors.toList());
        return list.toArray(new String[list.size()]);
    }

    /**
     * 获取下属
     *
     * @param userId
     * @return
     */
    private void getSubordinateId(String userId, boolean isAdmin, HashSet<String> userList) {
        // 获取所有用户数据
        List<UserAllModel> data = userService.getAll();
        if (isAdmin) {
            List<UserAllModel> collect = data.stream().filter(t -> userId.equals(t.getId())).collect(Collectors.toList());
            data.remove(collect.size() > 0 ? collect.get(0) : "");
            List<String> userListAll = data.stream().map(t -> t.getId()).collect(Collectors.toList());
            userList.addAll(userListAll);
        } else {
            //获取到自己的信息
            List<UserAllModel> userIdAll = data.stream().filter(t -> t.getId().equals(userId)).collect(Collectors.toList());
            //获取主管是自己的信息
            List<UserAllModel> collect = userIdAll.stream().filter(t -> userId.equals(t.getManagerId())).collect(Collectors.toList());
            for (UserAllModel userAllModel : collect) {
                userList.add(userAllModel.getId());
            }
            //遍历下属是主管的人的下属
            for (String id : userList) {
                List<UserAllModel> collect1 = collect.stream().filter(t -> id.equals(t.getId())).collect(Collectors.toList());
                if (collect1.size() > 0) {
                    boolean isAdministrator = BooleanUtil.toBoolean(String.valueOf(collect1.get(0).getIsAdministrator()));
                    getSubordinateId(id, isAdministrator, userList);
                }
            }
        }
    }


    /**
     * 获取下属机构
     *
     * @param organizeId
     * @param isAdmin
     * @return
     */
    private String[] getSubOrganizeIds(String organizeId, boolean isAdmin) {
        List<OrganizeEntity> data = organizeService.getList();
        if (!isAdmin) {
            data = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(organizeId, data), OrganizeEntity.class);
        }
        List<String> list = data.stream().map(t -> t.getId()).collect(Collectors.toList());
        return list.toArray(new String[list.size()]);
    }

    /**
     * 获取岗位
     *
     * @param userId
     * @return
     */
    private String[] getPositionId(String userId, boolean isAdmin) {
        //获取list
        List<UserRelationEntity> data = userRelationService.getListByUserId(userId);
        //获取一个字段的值
        if (!isAdmin) {
            data = data.stream().filter(m -> "Position".equals(m.getObjectType())).collect(Collectors.toList());
        }
        List<String> list = data.stream().map(t -> t.getObjectId()).collect(Collectors.toList());
        return list.toArray(new String[list.size()]);
    }


    /**
     * 登录信息
     *
     * @param userInfo   回话信息
     * @param systemInfo 系统信息
     * @return
     */
    private Map<String, Object> userInfo(UserInfo userInfo, BaseSystemInfo systemInfo) {
        Map<String, Object> dictionary = new HashMap<>(16);
        dictionary.put("userId", userInfo.getUserId());
        dictionary.put("userAccount", userInfo.getUserAccount());
        dictionary.put("userName", userInfo.getUserName());
        dictionary.put("icon", userInfo.getUserIcon());
        dictionary.put("gender", userInfo.getUserGender());
        dictionary.put("organizeId", userInfo.getOrganizeId());
        dictionary.put("prevLogin", systemInfo.getLastLoginTimeSwitch() == 1 ? 1 : 0);
        dictionary.put("prevLoginTime", userInfo.getPrevLoginTime());
        dictionary.put("prevLoginIPAddress", userInfo.getPrevLoginIpAddress());
        dictionary.put("prevLoginIPAddressName", userInfo.getPrevLoginIpAddressName());
        dictionary.put("serviceDirectory", configValueUtil.getServiceDirectoryPath());
        dictionary.put("webDirectory", configValueUtil.getCodeAreasName());
        return dictionary;
    }

}
