package jnpf.base.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.model.dataInterface.*;
import jnpf.base.vo.DownloadVO;
import jnpf.util.file.FileExport;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.vo.ListVO;
import jnpf.base.entity.DataInterfaceEntity;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.config.ConfigValueUtil;
import jnpf.database.exception.DataException;
import jnpf.permission.model.user.UserAllModel;
import jnpf.permission.service.UserService;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Api(tags = "数据接口", value = "DataInterface")
@RestController
@RequestMapping(value = "/api/system/DataInterface")
public class DataInterfaceController {
    @Autowired
    private DataInterfaceService dataInterfaceService;
    @Autowired
    private UserService userService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private FileExport fileExport;

    /**
     * 获取接口列表(分页)
     *
     * @param pagination
     * @return
     */
    @ApiOperation("获取接口列表(分页)")
    @GetMapping
    public ActionResult<PageListVO<DataInterfaceListVO>> getList(PaginationDataInterface pagination) {
        List<DataInterfaceEntity> data = dataInterfaceService.getList(pagination);
        List<UserAllModel> userServiceAll = userService.getAll();
        for (DataInterfaceEntity entity : data) {
            UserAllModel userAllVO = userServiceAll.stream().filter(t -> t.getId().equals(entity.getCreatorUser())).findFirst().orElse(null);
            if (userAllVO != null) {
                entity.setCreatorUser(userAllVO.getRealName() + "/" + userAllVO.getAccount());
            }
        }
        List<DataInterfaceListVO> list = JsonUtil.getJsonToList(data, DataInterfaceListVO.class);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        return ActionResult.page(list, paginationVO);
    }

    /**
     * 获取接口列表下拉框
     *
     * @return
     */
    @ApiOperation("获取接口列表下拉框")
    @GetMapping("/Selector")
    public ActionResult<DataInterfaceTreeVO> getSelector() {
        List<DataInterfaceTreeModel> tree = new ArrayList<>();
        List<DataInterfaceEntity> data = dataInterfaceService.getList();
        for (DataInterfaceEntity entity : data) {
            DictionaryDataEntity dictionaryDataEntity = dictionaryDataService.getInfo(entity.getCategoryId());
            if (dictionaryDataEntity != null) {
                DataInterfaceTreeModel firstModel = JsonUtil.getJsonToBean(dictionaryDataEntity, DataInterfaceTreeModel.class);
                firstModel.setCategoryId("0");
                tree.add(firstModel);
                DataInterfaceTreeModel treeModel = JsonUtil.getJsonToBean(entity, DataInterfaceTreeModel.class);
                treeModel.setCategoryId("1");
                treeModel.setParentId(dictionaryDataEntity.getId());
                tree.add(treeModel);
            }
        }
        tree = tree.stream().distinct().collect(Collectors.toList());
        List<SumTree<DataInterfaceTreeModel>> sumTrees = TreeDotUtils.convertListToTreeDot(tree);
        List<DataInterfaceTreeVO> list = JsonUtil.getJsonToList(sumTrees, DataInterfaceTreeVO.class);
        ListVO vo = new ListVO();
        vo.setList(list);
        return ActionResult.success(list);
    }

    /**
     * 获取接口数据
     *
     * @param id
     * @return
     */
    @ApiOperation("获取接口数据")
    @GetMapping("/{id}")
    public ActionResult<DataInterfaceVo> getInfo(@PathVariable("id") String id) throws DataException {
        DataInterfaceEntity entity = dataInterfaceService.getInfo(id);
        DataInterfaceVo vo = JsonUtilEx.getJsonToBeanEx(entity, DataInterfaceVo.class);
        if (vo.getCheckType() == null) {
            vo.setCheckType(0);
        }
        return ActionResult.success(vo);
    }

    /**
     * 添加接口
     *
     * @param dataInterfaceCrForm
     * @return
     */
    @ApiOperation("添加接口")
    @PostMapping
    public ActionResult create(@RequestBody @Valid DataInterfaceCrForm dataInterfaceCrForm) {
        DataInterfaceEntity entity = JsonUtil.getJsonToBean(dataInterfaceCrForm, DataInterfaceEntity.class);
        if (dataInterfaceService.isExistByFullName(entity.getFullName(), entity.getId())) {
            return ActionResult.fail("名称不能重复");
        }
        if (dataInterfaceService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("编码不能重复");
        }
        dataInterfaceService.create(entity);
        return ActionResult.success("接口创建成功");
    }

    /**
     * 修改接口
     *
     * @param dataInterfaceUpForm
     * @param id
     * @return
     */
    @ApiOperation("修改接口")
    @PutMapping("/{id}")
    public ActionResult update(@RequestBody @Valid DataInterfaceUpForm dataInterfaceUpForm, @PathVariable("id") String id) throws DataException {
        DataInterfaceEntity entity = JsonUtilEx.getJsonToBeanEx(dataInterfaceUpForm, DataInterfaceEntity.class);
        if (dataInterfaceService.isExistByFullName(entity.getFullName(), id)) {
            return ActionResult.fail("名称不能重复");
        }
        if (dataInterfaceService.isExistByEnCode(entity.getEnCode(), id)) {
            return ActionResult.fail("编码不能重复");
        }
        boolean flag = dataInterfaceService.update(entity, id);
        if (flag == false) {
            return ActionResult.fail("接口修改失败，数据不存在");
        }
        return ActionResult.success("接口修改成功");
    }

    /**
     * 删除接口
     *
     * @param id
     * @return
     */
    @ApiOperation("删除接口")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable String id) {
        DataInterfaceEntity entity = dataInterfaceService.getInfo(id);
        if (entity != null) {
            dataInterfaceService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 更新接口状态
     *
     * @param id
     * @return
     */
    @ApiOperation("更新接口状态")
    @PutMapping("/{id}/Actions/State")
    public ActionResult update(@PathVariable("id") String id) throws DataException {
        DataInterfaceEntity entity = dataInterfaceService.getInfo(id);
        if (entity != null) {
            if ("0".equals(String.valueOf(entity.getEnabledMark()))) {
                entity.setEnabledMark(1);
            } else {
                entity.setEnabledMark(0);
            }
            dataInterfaceService.update(entity, id);
            return ActionResult.success("更新接口状态成功");
        }
        return ActionResult.fail("更新接口状态失败，数据不存在");
    }

    /**
     * 访问接口
     *
     * @param id
     * @return
     */
    @ApiOperation("访问接口")
    @GetMapping("/{id}/Actions/Response")
    public ActionResult get(@PathVariable("id") String id) {
        return dataInterfaceService.infoToId(id);
    }

    /**
     * 数据接口导出功能
     *
     * @param id 接口id
     */
    @ApiOperation("导出数据接口数据")
    @GetMapping("/{id}/Action/Export")
    public ActionResult exportFile(@PathVariable("id") String id) {
        DataInterfaceEntity entity = dataInterfaceService.getInfo(id);
        DataInterfaceExport dataInterfaceExport = JsonUtil.getJsonToBean(entity, DataInterfaceExport.class);
        //导出文件
        DownloadVO downloadVO = fileExport.exportFile(dataInterfaceExport, configValueUtil.getTemporaryFilePath());
        return ActionResult.success(downloadVO);
    }

    /**
     * 数据接口导入功能
     *
     * @param multipartFile
     * @return
     * @throws DataException
     */
    @ApiOperation("数据接口导入功能")
    @PostMapping(value = "/Action/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult importFile(@RequestPart("file") MultipartFile multipartFile) throws DataException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile)) {
            return ActionResult.fail("导入文件格式错误");
        }
        //读取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile, configValueUtil.getTemporaryFilePath());
        try {
            DataInterfaceEntity entity = JsonUtil.getJsonToBean(fileContent, DataInterfaceEntity.class);
            //id为空切名称不存在时
            if (dataInterfaceService.getInfo(entity.getId()) == null &&
                    !dataInterfaceService.isExistByFullName(entity.getFullName(), entity.getId()) &&
                    !dataInterfaceService.isExistByEnCode(entity.getEnCode(), entity.getId())) {
                dataInterfaceService.create(entity);
                return ActionResult.success("导入成功");
            }
        } catch (Exception e) {
            throw new DataException("导入失败，数据有误");
        }
        return ActionResult.fail("数据已存在");
    }

}
