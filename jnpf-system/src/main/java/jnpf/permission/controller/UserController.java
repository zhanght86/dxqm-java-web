package jnpf.permission.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.Page;
import jnpf.message.service.SynThirdDingTalkService;
import jnpf.message.service.SynThirdQyService;
import jnpf.util.*;
import jnpf.base.ActionResult;
import jnpf.base.vo.PageListVO;
import jnpf.base.Pagination;
import jnpf.base.vo.PaginationVO;
import jnpf.base.vo.ListVO;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.database.exception.DataException;
import jnpf.permission.model.user.*;
import jnpf.permission.service.*;
import jnpf.base.util.*;
import jnpf.util.treeutil.ListToTreeUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Api(tags = "用户管理", value = "Users")
@Slf4j
@RestController
@RequestMapping("/api/permission/Users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private SynThirdQyService synThirdQyService;
    @Autowired
    private SynThirdDingTalkService synThirdDingTalkService;

    /**
     * 获取用户列表
     *
     * @param pagination
     * @return
     */
    @ApiOperation("获取用户列表")
    @GetMapping
    public ActionResult<PageListVO<UserListVO>> getList(PaginationUser pagination) {
        List<OrganizeEntity> organizeList = organizeService.getList().stream().filter(t -> "1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());
        List<OrganizeEntity> dataAll = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(pagination.getOrganizeId(), organizeList), OrganizeEntity.class);
        List<String> organizeIds = dataAll.stream().map(t -> t.getId()).collect(Collectors.toList());
        organizeIds.add(pagination.getOrganizeId());
        pagination.setOrganizeId(String.join(",", organizeIds));
        List<UserEntity> data = userService.getList(pagination, pagination.getOrganizeId());
        List<UserListVO> list = new ArrayList<>();
        for (UserEntity entity : data) {
            UserListVO user = JsonUtil.getJsonToBean(entity, UserListVO.class);
            //部门名称
            OrganizeEntity organize = organizeService.getInfo(entity.getOrganizeId());
            if (organize != null && StringUtil.isNotEmpty(organize.getFullName())) {
                user.setDepartment(organize.getFullName());
            }
            list.add(user);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 获取用户列表
     *
     * @return
     */
    @ApiOperation("获取所有用户列表")
    @GetMapping("/All")
    public ActionResult<ListVO<UserAllVO>> getAllUsers(Pagination pagination) {
        List<UserAllModel> uservo = userService.getAll().stream().filter(
                t -> !StringUtil.isEmpty(pagination.getKeyword()) ? t.getRealName().toLowerCase().contains(pagination.getKeyword())
                        || t.getAccount().toLowerCase().contains(pagination.getKeyword()) : t.getRealName() != null
        ).collect(Collectors.toList());
        List<UserAllVO> user = JsonUtil.getJsonToList(uservo, UserAllVO.class);
        ListVO vo = new ListVO();
        vo.setList(user);
        return ActionResult.success(vo);
    }

    /**
     * IM通讯获取用户接口
     *
     * @param pagination
     * @return
     */
    @ApiOperation("IM通讯获取用户")
    @GetMapping("/ImUser")
    public ActionResult<PageListVO<ImUserListVo>> getAllImUserUsers(Pagination pagination) {
        List<UserEntity> data = userService.getList(pagination, "");
        List<UserAllModel> userList = userService.getAll();
        List<ImUserListVo> list = new ArrayList<>();
        for (UserEntity entity : data) {
            ImUserListVo user = JsonUtil.getJsonToBean(entity, ImUserListVo.class);
            user.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
            UserAllModel ImUserListVo = userList.stream().filter(
                    t -> t.getId().equals(entity.getId())
            ).findFirst().orElse(new UserAllModel());
            user.setDepartment(ImUserListVo.getDepartment());
            list.add(user);
        }
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 获取用户下拉框列表
     *
     * @return
     */
    @ApiOperation("获取用户下拉框列表")
    @GetMapping("/Selector")
    public ActionResult<ListVO<UserSelectorVO>> selector() {
        List<OrganizeEntity> allOrganizeData = organizeService.getList();
        List<OrganizeEntity> organizeData = organizeService.getList().stream().filter(
                t -> "1".equals(String.valueOf(t.getEnabledMark()))
        ).collect(Collectors.toList());
        List<UserEntity> userData = userService.getList().stream().filter(
                t -> "1".equals(String.valueOf(t.getEnabledMark()))
        ).collect(Collectors.toList());
        List<UserSelectorModel> treeList = JsonUtil.getJsonToList(organizeData, UserSelectorModel.class);
        for (UserSelectorModel entity1 : treeList) {
            if ("department".equals(entity1.getType())) {
                entity1.setIcon("icon-ym icon-ym-tree-department1");
            } else if ("company".equals(entity1.getType())) {
                entity1.setIcon("icon-ym icon-ym-tree-organization3");
            }
        }
        for (UserEntity entity : userData) {
            UserSelectorModel treeModel = new UserSelectorModel();
            treeModel.setId(entity.getId());
            treeModel.setParentId(entity.getOrganizeId());
            treeModel.setFullName(entity.getRealName() + "/" + entity.getAccount());
            treeModel.setType("user");
            treeModel.setIcon("icon-ym icon-ym-tree-user2");
            treeList.add(treeModel);
        }
        List<SumTree<UserSelectorModel>> trees = TreeDotUtils.convertListToTreeDot(treeList);
        List<UserSelectorVO> listvo = JsonUtil.getJsonToList(trees, UserSelectorVO.class);
        List<OrganizeEntity> entities = allOrganizeData.stream().filter(
                t -> "1".equals(String.valueOf(t.getEnabledMark())) && "-1".equals(t.getParentId())
        ).collect(Collectors.toList());
        Iterator<UserSelectorVO> iterator = listvo.iterator();
        while (iterator.hasNext()) {
            UserSelectorVO userSelectorVO = iterator.next();
            for (OrganizeEntity entity : entities) {
                if (entity.getId().equals(userSelectorVO.getParentId())) {
                    iterator.remove();//使用迭代器的删除方法删除
                }
            }
        }
        ListVO vo = new ListVO();
        vo.setList(listvo);
        return ActionResult.success(vo);
    }

    /**
     * 获取用户下拉框列表
     *
     * @return
     */
    @ApiOperation("获取用户下拉框列表")
    @PostMapping("/ImUser/Selector/{organizeId}")
    public ActionResult imUserSelector(@PathVariable("organizeId") String organizeId, @RequestBody Page page) {
        List<UserSelectorVO> jsonToList = new ArrayList<>();
        //判断是否搜索关键字
        if (StringUtil.isNotEmpty(page.getKeyword())) {
            //通过关键字查询
            List<UserEntity> list = userService.getList(page.getKeyword());
            //遍历用户给要返回的值插入值
            for (UserEntity entity : list) {
                UserSelectorVO vo = JsonUtil.getJsonToBean(entity, UserSelectorVO.class);
                vo.setId(entity.getId());
                vo.setParentId(entity.getOrganizeId());
                vo.setFullName(entity.getRealName() + "/" + entity.getAccount());
                vo.setType("user");
                vo.setIcon("icon-ym icon-ym-tree-user2");
                vo.setHasChildren(false);
                vo.setIsLeaf(true);
                vo.setEnabledMark(1);
                jsonToList.add(vo);
            }
            ListVO vo = new ListVO();
            vo.setList(jsonToList);
            return ActionResult.success(vo);
        }
        //获取所有组织
        List<OrganizeEntity> collect = organizeService.getList().stream().filter(t -> t.getEnabledMark() == 1).collect(Collectors.toList());
        //所有用户
        List<UserAllModel> userServiceAll = userService.getAll();
        List list1 = new ArrayList<>();
        //判断时候传入组织id
        //如果传入组织id，则取出对应的子集
        if (!"0".equals(organizeId)) {
            //通过组织查询部门及人员
            //单个组织
            List<OrganizeEntity> list = collect.stream().filter(t -> organizeId.equals(t.getId())).collect(Collectors.toList());
            if (list.size() > 0) {
                //获取组织信息
                OrganizeEntity organizeEntity = list.get(0);
                //取出组织下的部门
                List<OrganizeEntity> collect1 = collect.stream().filter(t -> t.getParentId().equals(organizeEntity.getId())).collect(Collectors.toList());
                for (OrganizeEntity entitys : collect1) {
                    getDepartmentList(collect, entitys, userServiceAll, list1);
                    if (list1.size() > 0) {
                        UserSelectorVO vo = JsonUtil.getJsonToBean(entitys, UserSelectorVO.class);
                        if ("department".equals(entitys.getCategory())) {
                            vo.setIcon("icon-ym icon-ym-tree-department1");
                        } else if ("company".equals(entitys.getCategory())) {
                            vo.setIcon("icon-ym icon-ym-tree-organization3");
                        }
                        vo.setHasChildren(true);
                        vo.setIsLeaf(false);
                        jsonToList.add(vo);
                    }
                }
                //取出组织下的人员
                List<UserAllModel> collect2 = userServiceAll.stream().filter(t -> organizeId.equals(t.getDepartmentId())).collect(Collectors.toList());
                for (UserAllModel model : collect2) {
                    UserSelectorVO vo = JsonUtil.getJsonToBean(model, UserSelectorVO.class);
                    vo.setId(model.getId());
                    vo.setParentId(organizeId);
                    vo.setFullName(model.getRealName() + "/" + model.getAccount());
                    vo.setType("user");
                    vo.setIcon("icon-ym icon-ym-tree-user2");
                    vo.setHasChildren(false);
                    vo.setIsLeaf(true);
                    vo.setEnabledMark(1);
                    jsonToList.add(vo);
                }
            }
            ListVO vo = new ListVO();
            vo.setList(jsonToList);
            return ActionResult.success(vo);
        }

        //如果没有组织id，则取出所有组织，只取有效组织
        List<OrganizeEntity> organizeEntityList = collect.stream().filter(t -> "-1".equals(t.getParentId())).collect(Collectors.toList());
        //递归排除掉组织下没有人员的组织
        Set<OrganizeEntity> organizeSet = getOrganize(collect, organizeEntityList, userServiceAll);
        List<OrganizeEntity> organizeList = new ArrayList<>(organizeSet);
        jsonToList = JsonUtil.getJsonToList(organizeList, UserSelectorVO.class);
        //添加图标
        for (UserSelectorVO userSelectorVO : jsonToList) {
            userSelectorVO.setIcon("icon-ym icon-ym-tree-organization3");
            //是否含有子集
            for (OrganizeEntity organizeEntity : collect) {
                boolean equals = userSelectorVO.getId().equals(organizeEntity.getParentId());
                if (equals) {
                    userSelectorVO.setHasChildren(true);
                    userSelectorVO.setIsLeaf(false);
                    break;
                } else {
                    userSelectorVO.setHasChildren(false);
                    userSelectorVO.setIsLeaf(true);
                }
            }
        }
        ListVO vo = new ListVO();
        vo.setList(jsonToList);
        return ActionResult.success(vo);
    }

    /**
     * 递归排除所有没有人员的组织
     *
     * @param organizeEntityList
     * @return
     */
    private Set<OrganizeEntity> getOrganize(List<OrganizeEntity> collect, List<OrganizeEntity> organizeEntityList, List<UserAllModel> userServiceAll) {
        Set<OrganizeEntity> organizeEntitySet = new HashSet<>();
        //遍历组织
        for (OrganizeEntity entity : organizeEntityList) {
            //判断组织下是否有人员
            for (int i = 0; i < userServiceAll.size(); i++) {
                boolean flag = entity.getId().equals(userServiceAll.get(i).getOrganizeId());
                if (flag) {
                    organizeEntitySet.add(entity);
                    break;
                }
            }
            //组织下有人，则直接跳过当前组织循环
            if (false) {
                break;
            }
            //部门下是否有人员
            List<OrganizeEntity> collect1 = collect.stream().filter(t -> t.getParentId().equals(entity.getId())).collect(Collectors.toList());
            List list1 = new ArrayList();
            for (OrganizeEntity entity1 : collect1) {
                getDepartmentList(collect, entity1, userServiceAll, list1);
            }
            if (list1.size() > 0) {
                organizeEntitySet.add(entity);
            }
        }
        return organizeEntitySet;
    }

    /**
     * 递归遍历子部门
     *
     * @param organizeEntityList
     * @param entity
     * @param userServiceAll
     */
    private void getDepartmentList(List<OrganizeEntity> organizeEntityList, OrganizeEntity entity, List<UserAllModel> userServiceAll, List list) {
        //判读当前部门下是否有子部门
        List<OrganizeEntity> organizeEntities = organizeEntityList.stream().filter(t -> t.getParentId().equals(entity.getId())).collect(Collectors.toList());
        //如果有子部门则继续遍历子部门
        if (organizeEntities.size() > 0) {
            for (OrganizeEntity organizeEntity : organizeEntities) {
                getDepartmentList(organizeEntityList, organizeEntity, userServiceAll, list);
            }
        }
        //否则查询部门下是否有人
        List<UserAllModel> userAllModels = userServiceAll.stream().filter(t -> entity.getId().equals(t.getDepartmentId())).collect(Collectors.toList());
        //部门下有人则添加到子节点中
        if (userAllModels.size() > 0) {
            list.add(userAllModels.get(0));
        }
    }

    /**
     * 获取用户信息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("获取用户信息")
    @GetMapping("/{id}")
    public ActionResult<UserInfoVO> getInfo(@PathVariable("id") String id) throws DataException {
        UserEntity entity = userService.getInfo(id);
        if (entity != null && !StringUtil.isEmpty(entity.getRoleId())) {
            String[] roledIds = entity.getRoleId().split(",");
            StringBuilder newRoleId = new StringBuilder();
            for (String roleId : roledIds) {
                RoleEntity roleEntity = roleService.getInfo(roleId);
                if (roleEntity != null) {
                    newRoleId.append(roleId + ",");
                }
            }
            if (newRoleId.length() == 0) {
                entity.setRoleId("");
            }
            if (newRoleId.length() > 0) {
                entity.setRoleId(newRoleId.substring(0, newRoleId.length() - 1));
            }
        }
        if (entity != null && !StringUtil.isEmpty(entity.getPositionId())) {
            String[] positionIds = entity.getPositionId().split(",");
            StringBuilder newPositionIds = new StringBuilder();
            for (String positionId : positionIds) {
                PositionEntity positionEntity = positionService.getInfo(positionId);
                if (positionEntity != null && positionEntity.getEnabledMark() == 1) {
                    newPositionIds.append(positionId + ",");
                }
            }
            if (newPositionIds.length() == 0) {
                entity.setPositionId("");
            }
            if (newPositionIds.length() > 0) {
                entity.setPositionId(newPositionIds.substring(0, newPositionIds.length() - 1));
            }
        }
        entity.setHeadIcon(UploaderUtil.uploaderImg(entity.getHeadIcon()));
        //去除字段为空字符串
        if (StringUtil.isEmpty(entity.getRoleId())) {
            entity.setRoleId(null);
        }
        if (StringUtil.isEmpty(entity.getPositionId())) {
            entity.setPositionId(null);
        }
        UserInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, UserInfoVO.class);
        return ActionResult.success(vo);
    }


    /**
     * 新建用户
     *
     * @param userCrForm
     * @return
     */
    @ApiOperation("新建用户")
    @PostMapping
    public ActionResult create(@RequestBody @Valid UserCrForm userCrForm) {
        UserEntity entity = JsonUtil.getJsonToBean(userCrForm, UserEntity.class);
        entity.setPassword("4a7d1ed414474e4033ac29ccb8653d9b");
        if (StringUtil.isEmpty(entity.getHeadIcon())) {
            entity.setHeadIcon("001.png");
        }
        if (userService.isExistByAccount(userCrForm.getAccount())) {
            return ActionResult.fail("账户名称不能重复");
        }
        userService.create(entity);
        try {
            //添加用户之后判断是否需要同步到企业微信
            synThirdQyService.createUserSysToQy(false, entity, "");
            //添加用户之后判断是否需要同步到钉钉
            synThirdDingTalkService.createUserSysToDing(false, entity, "");
        } catch (Exception e) {

        }
        String catchKey = cacheKeyUtil.getAllUser();
        if (redisUtil.exists(catchKey)) {
            redisUtil.remove(catchKey);
        }
        return ActionResult.success("新建成功");
    }

    /**
     * 修改用户
     *
     * @param userUpForm
     * @param id         主键值
     * @return
     */
    @ApiOperation("修改用户")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid UserUpForm userUpForm) {
        UserEntity entity = JsonUtil.getJsonToBean(userUpForm, UserEntity.class);
        //将禁用的id加进数据
        UserEntity entity1 = userService.getInfo(id);
        if ("1".equals(String.valueOf(entity1.getIsAdministrator()))) {
            return ActionResult.fail("无法修改管理员账户");
        }
        //直属主管不能是自己
        if (userService.getUserEntity(userUpForm.getAccount()).getId().equals(userUpForm.getManagerId())){
            return ActionResult.fail("直属主管不能是自己");
        }
        if (userService.isExistByAccount(id)) {
            return ActionResult.fail("账户名称不能重复");
        }

        if (entity1.getRoleId() != null) {
            String roleIds = (userUpForm.getRoleId() + "," + entity1.getRoleId());
            if (",".equals(roleIds.substring(0, 1))) {
                roleIds = roleIds.substring(1);
            }
            userUpForm.setRoleId(roleIds);

        }
        if (entity1.getPositionId() != null) {
            String positionIds = (userUpForm.getPositionId() + "," + entity1.getPositionId());
            if (",".equals(positionIds.substring(0, 1))) {
                positionIds = positionIds.substring(1);
            }
            userUpForm.setPositionId(positionIds);
        }
        boolean flag = userService.update(id, entity);
        try {
            //修改用户之后判断是否需要同步到企业微信
            synThirdQyService.updateUserSysToQy(false, entity, "");
            //修改用户之后判断是否需要同步到钉钉
            synThirdDingTalkService.updateUserSysToDing(false, entity, "");
        } catch (Exception e) {

        }
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除用户
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("删除用户")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        UserEntity entity = userService.getInfo(id);
        if (entity != null) {
            if ("1".equals(String.valueOf(entity.getIsAdministrator()))) {
                return ActionResult.fail("无法删除管理员账户");
            }
            //判断是否是部门主管
            if (organizeService.getList().stream().filter(t -> id.equals(t.getManager())).collect(Collectors.toList()).size() > 0) {
                return ActionResult.fail("此用户为某部门主管，无法删除");
            }
            String tenantId = StringUtil.isEmpty(userProvider.get().getTenantId()) ? "" : userProvider.get().getTenantId();
            String catchKey = tenantId + "allUser";
            if (redisUtil.exists(catchKey)) {
                redisUtil.remove(catchKey);
            }
            userService.delete(entity);
            try {
                //删除用户之后判断是否需要同步到企业微信
                synThirdQyService.deleteUserSysToQy(false, id, "");
                //删除用户之后判断是否需要同步到钉钉
                synThirdDingTalkService.deleteUserSysToDing(false, id, "");
            } catch (Exception e) {

            }
            userProvider.removeOnLine(entity.getId());
            RemoveUtil.removeOnline(entity.getId());
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }


    /**
     * 修改用户密码
     *
     * @return
     */
    @ApiOperation("修改用户密码")
    @PostMapping("/{id}/Actions/ResetPassword")
    public ActionResult modifyPassword(@PathVariable("id") String id, @RequestBody @Valid UserResetPasswordForm userResetPasswordForm) {
        UserEntity entity = userService.getInfo(id);
        if (entity != null) {
            entity.setPassword(userResetPasswordForm.getUserPassword());
            userService.updatePassword(entity);
            userProvider.removeOnLine(entity.getId());
            RemoveUtil.removeOnline(entity.getId());
            return ActionResult.success("操作成功");
        }
        return ActionResult.success("操作失败,用户不存在");
    }

    /**
     * 更新用户状态
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("更新用户状态")
    @PutMapping("/{id}/Actions/State")
    public ActionResult disable(@PathVariable("id") String id) {
        UserEntity entity = userService.getInfo(id);
        if (entity != null) {
            if ("1".equals(String.valueOf(entity.getIsAdministrator()))) {
                return ActionResult.fail("无法修改管理员账户状态");
            }
            if (entity.getEnabledMark() != null) {
                if ("1".equals(String.valueOf(entity.getEnabledMark()))) {
                    entity.setEnabledMark(0);
                    userProvider.removeOnLine(entity.getId());
                    RemoveUtil.removeOnline(entity.getId());
                    userService.update(id, entity);
                } else {
                    entity.setEnabledMark(1);
                    userService.update(id, entity);
                }
            } else {
                entity.setEnabledMark(1);
                userService.update(id, entity);
            }
            return ActionResult.success("操作成功");
        }
        return ActionResult.success("操作失败,用户不存在");
    }

    /**
     * 获取用户基本信息
     *
     * @param userIdModel
     * @return
     */
    @ApiOperation("获取用户基本信息")
    @PostMapping("/getUserList")
    public ActionResult getUserList(@RequestBody UserIdModel userIdModel) {
        List<UserIdListVo> list = new ArrayList<>();
        for (String userId : userIdModel.getUserId()) {
            UserEntity entity = userService.getInfo(userId);
            if (entity == null) {
                break;
            }
            UserIdListVo vo = JsonUtil.getJsonToBean(entity, UserIdListVo.class);
            vo.setFullName(entity.getRealName() + "/" + entity.getAccount());
            list.add(vo);
        }
        ListVO listVO = new ListVO();
        listVO.setList(list);
        return ActionResult.success(listVO);
    }


}
