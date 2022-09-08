package jnpf.permission.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.Page;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.vo.ListVO;
import jnpf.database.exception.DataException;
import jnpf.permission.entity.RoleEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.model.role.*;
import jnpf.permission.service.RoleService;
import jnpf.permission.service.UserRelationService;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Api(tags = "角色管理", value = "Role")
@RestController
@RequestMapping("/api/permission/Role")
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private DictionaryDataService dataService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRelationService userRelationService;

    /**
     * 获取角色列表
     *
     * @param page
     * @return
     */
    @ApiOperation("获取角色列表")
    @GetMapping
    public ActionResult<RoleListVO> list(Page page) {
        List<RoleEntity> data = roleService.getList(page);
        List<String> type = data.stream().map(t -> t.getType()).distinct().collect(Collectors.toList());
        List<DictionaryDataEntity> typeList = dataService.getDictionName(type);
        List<RoleModel> modelAll = new LinkedList<>();
        for (DictionaryDataEntity entity : typeList) {
            RoleModel model = new RoleModel();
            model.setFullName(entity.getFullName());
            model.setId(entity.getId());
            Long num = data.stream().filter(t->t.getType().equals(entity.getEnCode())).count();
            model.setNum(num);
            modelAll.add(model);
        }
        for (RoleEntity entity : data) {
            RoleModel model = JsonUtil.getJsonToBean(entity, RoleModel.class);
            DictionaryDataEntity dataEntity = typeList.stream().filter(t -> t.getEnCode().equals(entity.getType())).findFirst().orElse(null);
            if (dataEntity != null) {
                model.setParentId(dataEntity.getId());
                modelAll.add(model);
            }
        }
        List<SumTree<RoleModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<RoleListVO> list = JsonUtil.getJsonToList(trees, RoleListVO.class);
        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    /**
     * 角色下拉框列表
     *
     * @return
     */
    @ApiOperation("角色下拉框列表")
    @GetMapping("/Selector")
    public ActionResult<ListVO<RoleSelectorVO>> listAll() {
        List<RoleEntity> data = roleService.getList().stream().filter(t -> "1".equals(String.valueOf(t.getEnabledMark()))
        ).collect(Collectors.toList());
        List<String> type = data.stream().map(t -> t.getType()).distinct().collect(Collectors.toList());
        List<DictionaryDataEntity> typeList = dataService.getDictionName(type);
        List<RoleModel> modelAll = new LinkedList<>();
        for (DictionaryDataEntity entity : typeList) {
            RoleModel model = new RoleModel();
            model.setFullName(entity.getFullName());
            model.setId(entity.getId());
            Long num = data.stream().filter(t->t.getType().equals(entity.getEnCode())).count();
            model.setNum(num);
            modelAll.add(model);
        }
        for (RoleEntity entity : data) {
            RoleModel model = JsonUtil.getJsonToBean(entity, RoleModel.class);
            DictionaryDataEntity dataEntity = typeList.stream().filter(t -> t.getEnCode().equals(entity.getType())).findFirst().orElse(null);
            if (dataEntity != null) {
                model.setParentId(dataEntity.getId());
                model.setType("role");
                modelAll.add(model);
            }
        }
        List<SumTree<RoleModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<RoleSelectorVO> list = JsonUtil.getJsonToList(trees, RoleSelectorVO.class);
        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(vo);
    }

    /**
     * 获取角色信息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("获取角色信息")
    @GetMapping("/{id}")
    public ActionResult<RoleInfoVO> getInfo(@PathVariable("id") String id) throws DataException {
        RoleEntity entity = roleService.getInfo(id);
        RoleInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, RoleInfoVO.class);
        return ActionResult.success(vo);
    }


    /**
     * 新建角色
     *
     * @param roleCrForm
     * @return
     */
    @ApiOperation("新建角色")
    @PostMapping
    public ActionResult create(@RequestBody @Valid RoleCrForm roleCrForm) {
        RoleEntity entity = JsonUtil.getJsonToBean(roleCrForm, RoleEntity.class);
        if (roleService.isExistByFullName(roleCrForm.getFullName(), entity.getId())) {
            return ActionResult.fail("角色名称不能重复");
        }
        if (roleService.isExistByEnCode(roleCrForm.getEnCode(), entity.getId())) {
            return ActionResult.fail("角色编码不能重复");
        }
        roleService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 更新角色
     *
     * @param id         主键值
     * @param roleUpForm
     * @return
     */
    @ApiOperation("更新角色")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid RoleUpForm roleUpForm) {
        RoleEntity entity = JsonUtil.getJsonToBean(roleUpForm, RoleEntity.class);
        if (roleService.isExistByFullName(roleUpForm.getFullName(), id)) {
            return ActionResult.fail("角色名称不能重复");
        }
        if (roleService.isExistByEnCode(roleUpForm.getEnCode(), id)) {
            return ActionResult.fail("角色编码不能重复");
        }
        boolean flag = roleService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除角色
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("删除角色")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        RoleEntity entity = roleService.getInfo(id);
        if (entity != null) {
            List<UserRelationEntity> userRelList = userRelationService.getListByObjectId(id);
            for (UserRelationEntity entity1 : userRelList) {
                UserEntity entity2 = userService.getById(entity1.getUserId());
                if (entity2 != null) {
                    String newRoleId = entity2.getRoleId().replace(id, "");
                    if (entity2.getRoleId().contains(id)) {
                        if (newRoleId.length() != 0 && newRoleId.substring(0, 1) == ",") {
                            entity2.setRoleId(newRoleId.substring(1));
                        } else if (newRoleId.length() != 0) {
                            entity2.setRoleId(newRoleId.replace(",,", ","));
                        }
                    }
                }
            }
            userRelationService.deleteListByObjectId(id);
            roleService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
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
        RoleEntity entity = roleService.getInfo(id);
        if (entity != null) {
            if ("1".equals(String.valueOf(entity.getEnabledMark()))) {
                entity.setEnabledMark(0);
            } else {
                entity.setEnabledMark(1);
            }
            roleService.update(id, entity);
            return ActionResult.success("操作成功");
        }
        return ActionResult.fail("操作失败，数据不存在");
    }

}
