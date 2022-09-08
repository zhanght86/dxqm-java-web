package jnpf.base.controller;

import jnpf.base.entity.*;
import jnpf.base.service.*;
import jnpf.base.vo.DownloadVO;
import jnpf.config.ConfigValueUtil;
import jnpf.util.*;
import jnpf.database.exception.DataException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.model.module.*;
import jnpf.model.login.UserMenuModel;
import jnpf.util.file.FileExport;
import jnpf.util.treeutil.ListToTreeUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.TreeViewModel;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import jnpf.base.vo.ListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统功能
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "系统菜单", value = "menu")
@RestController
@RequestMapping("/api/system/Menu")
public class ModuleController {

    @Autowired
    private ModuleService moduleService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private FileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;


    /**
     * 获取菜单列表
     *
     * @param paginationMenu
     * @return
     */
    @ApiOperation("获取菜单列表")
    @GetMapping
    public ActionResult<ListVO<MenuListVO>> list(PaginationMenu paginationMenu) {
        List<ModuleEntity> data = moduleService.getList();
        if (!StringUtil.isEmpty(paginationMenu.getCategory())) {
            data = data.stream().filter(t -> paginationMenu.getCategory().equals(t.getCategory())).collect(Collectors.toList());
        }
        if (!StringUtil.isEmpty(paginationMenu.getKeyword())) {
            data = data.stream().filter(t -> t.getFullName().contains(paginationMenu.getKeyword())).collect(Collectors.toList());
        }
        List<UserMenuModel> list = JsonUtil.getJsonToList(data, UserMenuModel.class);
        List<SumTree<UserMenuModel>> menuList = TreeDotUtils.convertListToTreeDot(list);
        List<MenuListVO> menuvo = JsonUtil.getJsonToList(menuList, MenuListVO.class);
        ListVO vo = new ListVO();
        vo.setList(menuvo);
        return ActionResult.success(vo);
    }

    /**
     * 获取菜单列表(下拉框)
     *
     * @return
     */
    @ApiOperation("获取菜单列表(下拉框)")
    @GetMapping("/Selector")
    public ActionResult<ListVO<MenuSelectVO>> treeView(String category) {
        List<ModuleEntity> data = moduleService.getList().stream().filter(t -> !StringUtil.isEmpty(category) ? category.equals(String.valueOf(t.getCategory())) && "1".equals(String.valueOf(t.getType())) : "1".equals(String.valueOf(t.getType()))).collect(Collectors.toList());
        List<UserMenuModel> list = JsonUtil.getJsonToList(data, UserMenuModel.class);
        List<SumTree<UserMenuModel>> menuList = TreeDotUtils.convertListToTreeDot(list);
        List<MenuSelectVO> menuvo = JsonUtil.getJsonToList(menuList, MenuSelectVO.class);
        ListVO vo = new ListVO();
        vo.setList(menuvo);
        return ActionResult.success(vo);
    }
    /**
     * 获取菜单列表(下拉框)
     *
     * @return
     */
    @ApiOperation("获取菜单列表下拉框")
    @GetMapping("/Selector/All")
    public ActionResult<ListVO<MenuSelectVO>> menuSelect(String category) {
        List<ModuleEntity> data = moduleService.getList().stream().filter(t ->"1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());
        if(!StringUtil.isEmpty(category)){
            data.stream().filter(t ->category.equals(String.valueOf(t.getCategory()))).collect(Collectors.toList());
        }
        List<UserMenuModel> list = JsonUtil.getJsonToList(data, UserMenuModel.class);
        List<SumTree<UserMenuModel>> menuList = TreeDotUtils.convertListToTreeDot(list);
        List<MenuSelectAllVO> menuvo = JsonUtil.getJsonToList(menuList, MenuSelectAllVO.class);
        ListVO vo = new ListVO();
        vo.setList(menuvo);
        return ActionResult.success(vo);
    }


    /**
     * 系统功能类别树形
     *
     * @return
     */
    @ApiOperation("系统功能类别树形")
    @GetMapping("/TreeView")
    public ActionResult treeView() {
        List<ModuleEntity> moduleList = moduleService.getList().stream().filter(m -> "0".equals(m.getParentId())).collect(Collectors.toList());
        List<TreeViewModel> treeList = new ArrayList<>();
        TreeViewModel treeViewModel = new TreeViewModel();
        treeViewModel.setId("apply");
        treeViewModel.setText("软件开发平台");
        treeViewModel.setParentId("0");
        treeViewModel.setImg("fa fa-windows apply");
        treeList.add(treeViewModel);
        for (ModuleEntity entity : moduleList) {
            TreeViewModel treeModel = new TreeViewModel();
            treeModel.setId(entity.getId());
            treeModel.setText(entity.getFullName());
            treeModel.setParentId("apply");
            treeModel.setImg("fa fa-tags");
            treeList.add(treeModel);
        }
        return ActionResult.success(ListToTreeUtil.toTreeView(treeList));
    }


    /**
     * 获取菜单信息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("获取菜单信息")
    @GetMapping("/{id}")
    public ActionResult info(@PathVariable("id") String id) throws DataException {
        ModuleEntity entity = moduleService.getInfo(id);
        ModuleInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, ModuleInfoVO.class);
        return ActionResult.success(vo);
    }


    /**
     * 新建系统功能
     *
     * @param moduleCrForm 实体对象
     * @return
     */
    @ApiOperation("新建系统功能")
    @PostMapping
    public ActionResult create(@RequestBody @Valid ModuleCrForm moduleCrForm) {
        ModuleEntity entity = JsonUtil.getJsonToBean(moduleCrForm, ModuleEntity.class);
        if (entity.getUrlAddress() != null) {
            entity.setUrlAddress(entity.getUrlAddress().trim());
        }
        if (moduleService.isExistByFullName(entity.getFullName(), entity.getId(),moduleCrForm.getCategory())) {
            return ActionResult.fail("名称不能重复");
        }
        if (moduleService.isExistByEnCode(entity.getEnCode(), entity.getId(),moduleCrForm.getCategory())) {
            return ActionResult.fail("编码不能重复");
        }
        moduleService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 更新系统功能
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("更新系统功能")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid ModuleUpForm moduleUpForm) {
        ModuleEntity entity = JsonUtil.getJsonToBean(moduleUpForm, ModuleEntity.class);
        if (entity.getUrlAddress() != null) {
            entity.setUrlAddress(entity.getUrlAddress().trim());
        }
        if (moduleService.isExistByFullName(entity.getFullName(), id,moduleUpForm.getCategory())) {
            return ActionResult.fail("名称不能重复");
        }
        if (moduleService.isExistByEnCode(entity.getEnCode(), id,moduleUpForm.getCategory())) {
            return ActionResult.fail("编码不能重复");
        }
        boolean flag = moduleService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除系统功能
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("删除系统功能")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        ModuleEntity entity = moduleService.getInfo(id);
        if (entity != null) {
            List<ModuleEntity> list=moduleService.getList().stream().filter(t->t.getParentId().equals(entity.getId())).collect(Collectors.toList());
            if(list.size()>0){
                return ActionResult.fail("删除失败，请先删除子菜单。");
            }
            moduleService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 更新菜单状态
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("更新菜单状态")
    @PutMapping("/{id}/Actions/State")
    public ActionResult upState(@PathVariable("id") String id) {
        ModuleEntity entity = moduleService.getInfo(id);
        if (entity != null) {
            if (entity.getEnabledMark() == null || "1".equals(String.valueOf(entity.getEnabledMark()))) {
                entity.setEnabledMark(0);
            } else {
                entity.setEnabledMark(1);
            }
            moduleService.update(id, entity);
            //清除redis权限
            String cacheKey = cacheKeyUtil.getUserAuthorize() + userProvider.get().getUserId();
            if (redisUtil.exists(cacheKey)) {
                redisUtil.remove(cacheKey);
            }
            return ActionResult.success("更新成功");
        }
        return ActionResult.fail("更新失败，数据不存在");
    }

    /**
     * 系统菜单导出功能
     * @param id    接口id
     */
    @ApiOperation("导出系统菜单数据")
    @GetMapping("/{id}/Action/Export")
    public ActionResult exportFile(@PathVariable("id") String id){
        ModuleExportModel exportModel = moduleService.exportData(id);
        //导出文件
        DownloadVO downloadVO = fileExport.exportFile(exportModel, configValueUtil.getTemporaryFilePath());
        return ActionResult.success(downloadVO);
    }

    /**
     * 系统菜单导入功能
     * @param multipartFile
     * @return
     */
    @ApiOperation("系统菜单导入功能")
    @PostMapping(value = "/Action/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult importFile(@RequestPart("file") MultipartFile multipartFile) throws DataException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile)){
            return ActionResult.fail("导入文件格式错误");
        }
        //读取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile, configValueUtil.getTemporaryFilePath());
        try{
            //转model后导入
            ModuleExportModel exportModel = JsonUtil.getJsonToBean(fileContent, ModuleExportModel.class);
            //判断父级节点是否存在
            if (!"-1".equals(exportModel.getParentId()) && moduleService.getInfo(exportModel.getParentId()) == null){
                return ActionResult.fail("导入失败,查询不到上级分类");
            }
            boolean isExists = moduleService.importData(exportModel);
            if (isExists){
                return ActionResult.fail("数据已存在");
            }
        }catch (Exception e) {
            throw new DataException("导入失败，数据有误");
        }
        return ActionResult.success("导入成功");
    }

}
