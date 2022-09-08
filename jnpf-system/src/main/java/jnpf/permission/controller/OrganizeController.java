package jnpf.permission.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.Pagination;
import jnpf.base.vo.ListVO;
import jnpf.message.service.SynThirdDingTalkService;
import jnpf.message.service.SynThirdQyService;
import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.database.exception.DataException;
import jnpf.permission.model.organize.*;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.StringUtil;
import jnpf.util.treeutil.ListToTreeUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 组织机构
 * 组织架构：公司》部门》岗位》用户
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Api(tags = "组织管理", value = "Organize")
@RestController
@RequestMapping("/api/permission/Organize")
public class OrganizeController {

    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private UserService userService;
    @Autowired
    private SynThirdQyService synThirdQyService;
    @Autowired
    private SynThirdDingTalkService synThirdDingTalkService;

    //---------------------------组织管理--------------------------------------------

    /**
     * 获取组织列表
     *
     * @param pagination
     * @return
     */
    @ApiOperation("获取组织列表")
    @GetMapping
    public ActionResult<ListVO<OrganizeListVO>> getList(Pagination pagination) {
        List<OrganizeEntity> data = organizeService.getList();
        List<OrganizeEntity> dataAll = data;
        List<OrganizeEntity> list = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(data, dataAll), OrganizeEntity.class);
        list = list.stream().filter(t -> "company".equals(t.getCategory())).collect(Collectors.toList());
        List<OrganizeModel> oraganizeList = JsonUtil.getJsonToList(list, OrganizeModel.class);
        List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertListToTreeDot(oraganizeList);
        List<OrganizeListVO> listVO = JsonUtil.getJsonToList(trees, OrganizeListVO.class);

        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            listVO = listVO.stream().filter(t -> t.getFullName().toLowerCase().contains(pagination.getKeyword()) || t.getEnCode().toLowerCase().contains(pagination.getKeyword())).collect(Collectors.toList());
        }
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 获取组织下拉框列表
     *
     * @return
     */
    @ApiOperation("获取组织下拉框列表")
    @GetMapping("/Selector")
    public ActionResult<ListVO<OrganizeSelectorVO>> getSelector(Pagination pagination) {
        List<OrganizeEntity> allList = organizeService.getList();
        List<OrganizeEntity> data = allList.stream().filter(t -> "1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());
        List<OrganizeEntity> dataAll = data;
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            data = data.stream().filter(
                    t -> t.getFullName().contains(pagination.getKeyword())|| t.getEnCode().contains(pagination.getKeyword())
            ).collect(Collectors.toList());
        }
        List<OrganizeEntity> list = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(data, dataAll), OrganizeEntity.class);
        list = list.stream().filter(t -> "company".equals(t.getCategory())).collect(Collectors.toList());
        List<OrganizeModel> models = JsonUtil.getJsonToList(list, OrganizeModel.class);
        for (OrganizeModel model : models) {
            model.setIcon("icon-ym icon-ym-tree-organization3");
        }
        List<OrganizeModel> modelAll = new ArrayList<>();
        modelAll.addAll(models);
        List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<OrganizeSelectorVO> listVO = JsonUtil.getJsonToList(trees, OrganizeSelectorVO.class);

        //将子节点全部删除
        Iterator<OrganizeSelectorVO> iterator = listVO.iterator();
        while (iterator.hasNext()) {
            OrganizeSelectorVO oraganizeSelectorVO = iterator.next();
            if (!"-1".equals(oraganizeSelectorVO.getParentId())&&!"0".equals(oraganizeSelectorVO.getParentId())) {
                iterator.remove();
            }
        }
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 组织树形
     *
     * @return
     */
    @ApiOperation("获取组织/公司树形")
    @GetMapping("/Tree")
    public ActionResult<ListVO<OrganizeTreeVO>> tree() {
        List<OrganizeEntity> allList = organizeService.getList();
        List<OrganizeEntity> list = allList.stream().filter(t -> "1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());
        list = list.stream().filter(t -> "company".equals(t.getCategory())).collect(Collectors.toList());
        List<OrganizeModel> models = JsonUtil.getJsonToList(list, OrganizeModel.class);
        for (OrganizeModel model : models) {
            model.setIcon("icon-ym icon-ym-tree-organization3");
        }
        List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertListToTreeDot(models);
        List<OrganizeTreeVO> listVO = JsonUtil.getJsonToList(trees, OrganizeTreeVO.class);
        //将子节点全部删除
        Iterator<OrganizeTreeVO> iterator = listVO.iterator();
        while (iterator.hasNext()) {
            OrganizeTreeVO orananizeTreeVO = iterator.next();
            if (!"-1".equals(orananizeTreeVO.getParentId())) {
                iterator.remove();
            }
        }
        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }

    /**
     * 获取组织信息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("获取组织信息")
    @GetMapping("/{id}")
    public ActionResult<OrganizeInfoVO> info(@PathVariable("id") String id) throws DataException {
        OrganizeEntity entity = organizeService.getInfo(id);
        OrganizeInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, OrganizeInfoVO.class);
        return ActionResult.success(vo);
    }


    /**
     * 新建组织
     *
     * @param oraganizeCrForm
     * @return
     */
    @ApiOperation("新建组织")
    @PostMapping
    public ActionResult create(@RequestBody @Valid OrganizeCrForm oraganizeCrForm) {
        OrganizeEntity entity = JsonUtil.getJsonToBean(oraganizeCrForm, OrganizeEntity.class);
        entity.setCategory("company");
        if (organizeService.isExistByFullName(entity, false, false)) {
            return ActionResult.fail("组织名称不能重复");
        }
        if (organizeService.isExistByEnCode(entity, false, false)) {
            return ActionResult.fail("组织编码不能重复");
        }
        organizeService.create(entity);
        try{
            //创建组织后判断是否需要同步到企业微信
            synThirdQyService.createDepartmentSysToQy(false,entity,"");
            //创建组织后判断是否需要同步到钉钉
            synThirdDingTalkService.createDepartmentSysToDing(false,entity,"");
        }catch (Exception e){

        }
        return ActionResult.success("新建成功");
    }

    /**
     * 更新组织
     *
     * @param id              主键值
     * @param oraganizeUpForm 实体对象
     * @return
     */
    @ApiOperation("更新组织")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid OrganizeUpForm oraganizeUpForm) {
        OrganizeEntity entity = JsonUtil.getJsonToBean(oraganizeUpForm, OrganizeEntity.class);
        if (id.equals(entity.getParentId())) {
            return ActionResult.fail("上级公司和公司不能是同一个");
        }
        entity.setId(id);
        if (organizeService.isExistByFullName(entity, false, true)) {
            return ActionResult.fail("组织名称不能重复");
        }
        if (organizeService.isExistByEnCode(entity, false, true)) {
            return ActionResult.fail("组织编码不能重复");
        }
        boolean flag = organizeService.update(id, entity);
        try{
            //修改组织后判断是否需要同步到企业微信
            synThirdQyService.updateDepartmentSysToQy(false,organizeService.getInfo(id),"");
            //修改组织后判断是否需要同步到钉钉
            synThirdDingTalkService.updateDepartmentSysToDing(false,organizeService.getInfo(id),"");
        }catch (Exception e){

        }
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除组织
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("删除组织")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        if (organizeService.allowdelete(id)) {
            OrganizeEntity organizeEntity = organizeService.getInfo(id);
            if (organizeEntity != null) {
                organizeService.delete(organizeEntity);
                try {
                    //删除组织后判断是否需要同步到企业微信
                    synThirdQyService.deleteDepartmentSysToQy(false,id,"");
                    //删除组织后判断是否需要同步到钉钉
                    synThirdDingTalkService.deleteDepartmentSysToDing(false,id,"");
                }catch (Exception e){

                }
                return ActionResult.success("删除成功");
            }
            return ActionResult.fail("删除失败，数据不存在");
        } else {
            return ActionResult.fail("此记录被关联引用,不允许被删除");
        }
    }

    /**
     * 更新组织状态
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("更新组织状态")
    @PutMapping("/{id}/Actions/State")
    public ActionResult update(@PathVariable("id") String id) {
        OrganizeEntity organizeEntity = organizeService.getInfo(id);
        if (organizeEntity != null) {
            if ("1".equals(String.valueOf(organizeEntity.getEnabledMark()))) {
                organizeEntity.setEnabledMark(0);
            } else {
                organizeEntity.setEnabledMark(1);
            }
            organizeService.update(organizeEntity.getId(), organizeEntity);
            return ActionResult.success("更新成功");
        }
        return ActionResult.success("更新失败，数据不存在");
    }


    //---------------------------部门管理--------------------------------------------

    /**
     * 获取部门列表
     *
     * @param companyId
     * @return
     */
    @ApiOperation("获取部门列表")
    @GetMapping("/{companyId}/Department")
    public ActionResult<ListVO<OrganizeDepartListVO>> getListDepartment(@PathVariable("companyId") String companyId, Pagination pagination) {
        List<OrganizeEntity> dataAll = organizeService.getList();
        String parentId = dataAll.stream().filter(t -> t.getId().equals(companyId)).findFirst().orElse(new OrganizeEntity()).getId();
        List<OrganizeEntity> data = new ArrayList<>();
        //将部门之下的子集搜索出来
        List<OrganizeEntity> dataCopy = dataAll.stream().filter(t -> "department".equals(t.getCategory())).collect(Collectors.toList());
        for (OrganizeEntity entity : dataCopy) {
            List<OrganizeEntity> data1 = new ArrayList<>();
            data1.add(entity);
            String id = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(data1, dataAll), OrganizeEntity.class).stream().filter(t -> companyId.equals(t.getId())).findFirst().orElse(new OrganizeEntity()).getId();
            if (parentId.equals(id)) {
                data.add(entity);
            }
        }
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            data = data.stream().filter(
                    t -> t.getFullName().contains(pagination.getKeyword()) || t.getEnCode().contains(pagination.getKeyword())
            ).collect(Collectors.toList());
        }
        List<OrganizeModel> models = JsonUtil.getJsonToList(data, OrganizeModel.class);

//        给部门经理赋值
        for(OrganizeModel model:models){
            if(!StringUtil.isEmpty(model.getManager())){
                UserEntity entity=userService.getById(model.getManager());
                if(entity!=null){
                    model.setManager(entity.getRealName()+"/"+entity.getAccount());
                }
            }
        }
        List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertListToTreeDot(models);
        //去掉子公司的部门
        trees= trees.stream().filter(t->parentId.equals(t.getParentId())).collect(Collectors.toList());
        List<OrganizeDepartListVO> listvo = JsonUtil.getJsonToList(trees, OrganizeDepartListVO.class);
        ListVO vo = new ListVO();
        vo.setList(listvo);
        return ActionResult.success(vo);
    }

    /**
     * 获取部门下拉框列表
     *
     * @return
     */
    @ApiOperation("获取部门下拉框列表")
    @GetMapping("/Department/Selector")
    public ActionResult<ListVO<OrganizeDepartSelectorListVO>> getListDepartment() {
        List<OrganizeEntity> allList = organizeService.getList();
        List<OrganizeEntity> data = allList.stream().filter(t -> "1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());
        List<OrganizeModel> models = JsonUtil.getJsonToList(data, OrganizeModel.class);
        for (OrganizeModel model : models) {
            if("department".equals(model.getType())){
                model.setIcon("icon-ym icon-ym-tree-department1");
            }else if("company".equals(model.getType())){
                model.setIcon("icon-ym icon-ym-tree-organization3");
            }
        }
        List<SumTree<OrganizeModel>> trees = TreeDotUtils.convertListToTreeDot(models);
        List<OrganizeDepartSelectorListVO> listVO = JsonUtil.getJsonToList(trees, OrganizeDepartSelectorListVO.class);

        //将子节点全部删除
        Iterator<OrganizeDepartSelectorListVO> iterator = listVO.iterator();
         while (iterator.hasNext()) {
            OrganizeDepartSelectorListVO organizeDepartSelectorListVO = iterator.next();
           if(!"-1".equals(organizeDepartSelectorListVO.getParentId())){
               iterator.remove();
           }
        }

        ListVO vo = new ListVO();
        vo.setList(listVO);
        return ActionResult.success(vo);
    }


    /**
     * 新建部门
     *
     * @param oraganizeDepartCrForm
     * @return
     */
    @ApiOperation("新建部门")
    @PostMapping("/Department")
    public ActionResult createDepartment(@RequestBody @Valid OrganizeDepartCrForm oraganizeDepartCrForm) {
        OrganizeEntity entity = JsonUtil.getJsonToBean(oraganizeDepartCrForm, OrganizeEntity.class);
        entity.setCategory("department");
        //判断同一个父级下是否含有同一个名称
        if (organizeService.isExistByFullName(entity, false, false)){
            return ActionResult.fail("部门名称不能重复");
        }
        //判断同一个父级下是否含有同一个名称
        if (organizeService.isExistByEnCode(entity, false, false)){
            return ActionResult.fail("部门编码不能重复");
        }
        organizeService.create(entity);
        try{
            //创建部门后判断是否需要同步到企业微信
            synThirdQyService.createDepartmentSysToQy(false,entity,"");
            //创建部门后判断是否需要同步到钉钉
            synThirdDingTalkService.createDepartmentSysToDing(false,entity,"");
        }catch (Exception e){

        }
        return ActionResult.success("新建成功");
    }

    /**
     * 更新部门
     *
     * @param id                    主键值
     * @param oraganizeDepartUpForm
     * @return
     */
    @ApiOperation("更新部门")
    @PutMapping("/Department/{id}")
    public ActionResult updateDepartment(@PathVariable("id") String id, @RequestBody @Valid OrganizeDepartUpForm oraganizeDepartUpForm) {
        OrganizeEntity entity = JsonUtil.getJsonToBean(oraganizeDepartUpForm, OrganizeEntity.class);
        if (id.equals(entity.getParentId())) {
            return ActionResult.fail("上级部门和部门不能是同一个");
        }
        entity.setId(id);
        //判断同一个父级下是否含有同一个名称
        if (organizeService.isExistByFullName(entity, false, true)){
            return ActionResult.fail("部门名称不能重复");
        }
        //判断同一个父级下是否含有同一个名称
        if (organizeService.isExistByEnCode(entity, false, true)){
            return ActionResult.fail("部门编码不能重复");
        }
        boolean flag = organizeService.update(id, entity);
        try{
            //修改部门后判断是否需要同步到企业微信
            synThirdQyService.updateDepartmentSysToQy(false,organizeService.getInfo(id),"");
            //修改部门后判断是否需要同步到钉钉
            synThirdDingTalkService.updateDepartmentSysToDing(false,organizeService.getInfo(id),"");
        }catch (Exception e){

        }
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除部门
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("删除部门")
    @DeleteMapping("/Department/{id}")
    public ActionResult deleteDepartment(@PathVariable("id") String id) {
        if (organizeService.allowdelete(id)) {
            OrganizeEntity organizeEntity = organizeService.getInfo(id);
            if (organizeEntity != null) {
                organizeService.delete(organizeEntity);
                try {
                    //删除部门后判断是否需要同步到企业微信
                    synThirdQyService.deleteDepartmentSysToQy(false,id,"");
                    //删除部门后判断是否需要同步到钉钉
                    synThirdDingTalkService.deleteDepartmentSysToDing(false,id,"");
                }catch (Exception e){

                }
                return ActionResult.success("删除成功");
            }
            return ActionResult.fail("删除失败，数据不存在");
        } else {
            return ActionResult.fail("此记录被关联引用,不允许被删除");
        }
    }

    /**
     * 更新部门状态
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("更新部门状态")
    @PutMapping("/Department/{id}/Actions/State")
    public ActionResult updateDepartment(@PathVariable("id") String id) {
        OrganizeEntity organizeEntity = organizeService.getInfo(id);
        if (organizeEntity != null) {
            if ("1".equals(String.valueOf(organizeEntity.getEnabledMark()))) {
                organizeEntity.setEnabledMark(0);
            } else {
                organizeEntity.setEnabledMark(1);
            }
            organizeService.update(organizeEntity.getId(), organizeEntity);
            return ActionResult.success("更新成功");
        }
        return ActionResult.fail("更新失败，数据不存在");
    }

    /**
     * 获取部门信息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("获取部门信息")
    @GetMapping("/Department/{id}")
    public ActionResult<OrganizeDepartInfoVO> infoDepartment(@PathVariable("id") String id) throws DataException {
        OrganizeEntity entity = organizeService.getInfo(id);
        OrganizeDepartInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, OrganizeDepartInfoVO.class);
        return ActionResult.success(vo);
    }

}
