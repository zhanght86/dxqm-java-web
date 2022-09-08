package jnpf.base.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.model.dictionarytype.DictionaryExportModel;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.service.DictionaryTypeService;
import jnpf.base.vo.DownloadVO;
import jnpf.util.file.FileExport;
import jnpf.base.vo.ListVO;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.entity.DictionaryTypeEntity;
import jnpf.config.ConfigValueUtil;
import jnpf.database.exception.DataException;
import jnpf.base.model.dictionarydata.*;
import jnpf.base.model.dictionarytype.DictionaryTypeListVO;
import jnpf.base.model.dictionarytype.DictionaryTypeSelectModel;
import jnpf.base.model.dictionarytype.DictionaryTypeSelectVO;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.StringUtil;
import jnpf.util.treeutil.ListToTreeUtil;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 字典数据
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "数据字典", value = "DictionaryData")
@RestController
@RequestMapping("/api/system/DictionaryData")
public class DictionaryDataController {

    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private DictionaryTypeService dictionaryTypeService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private FileExport fileExport;

    /**
     * 获取数据字典列表
     *
     * @return
     */
    @ApiOperation("获取数据字典列表")
    @GetMapping("/{dictionaryTypeId}")
    public ActionResult<ListVO<DictionaryDataListTreeVO>> bindDictionary(@PathVariable("dictionaryTypeId") String dictionaryTypeId, PageDictionaryData pageDictionaryData) {
        List<DictionaryDataEntity> data = dictionaryDataService.getList(dictionaryTypeId);
        List<DictionaryDataEntity> dataAll = data;
        if(StringUtil.isNotEmpty(pageDictionaryData.getKeyword())){
            data = data.stream().filter(t->t.getFullName().contains(pageDictionaryData.getKeyword()) || t.getEnCode().contains(pageDictionaryData.getKeyword())).collect(Collectors.toList());
        }
        if (pageDictionaryData.getIsTree() != null && "1".equals(pageDictionaryData.getIsTree())) {
            List<DictionaryDataEntity> treeData = JsonUtil.getJsonToList(ListToTreeUtil.treeWhere(data, dataAll), DictionaryDataEntity.class);
            List<DictionaryDataModel> voListVO = JsonUtil.getJsonToList(treeData, DictionaryDataModel.class);
            List<SumTree<DictionaryDataModel>> sumTrees = TreeDotUtils.convertListToTreeDot(voListVO);
            List<DictionaryDataListVO> list = JsonUtil.getJsonToList(sumTrees, DictionaryDataListVO.class);
            ListVO<DictionaryDataListVO> treeVo = new ListVO<>();
            treeVo.setList(list);
            return ActionResult.success(treeVo);
        }
        List<DictionaryDataModel> voListVO = JsonUtil.getJsonToList(data, DictionaryDataModel.class);
        ListVO<DictionaryDataModel> treeVo = new ListVO<>();
        treeVo.setList(voListVO);
        return ActionResult.success(treeVo);
    }


    /**
     * 获取数据字典列表
     *
     * @return
     */
    @ApiOperation("获取数据字典列表(分类+内容)")
    @GetMapping("/All")
    public ActionResult allBindDictionary() {
        List<DictionaryTypeEntity> dictionaryTypeList = dictionaryTypeService.getList();
        List<DictionaryDataEntity> dictionaryDataList = dictionaryDataService.getList().stream().filter(t -> "1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());
        List<Map<String, Object>> list = new ArrayList<>();
        for (DictionaryTypeEntity dictionaryTypeEntity : dictionaryTypeList) {
            List<DictionaryDataEntity> childNodeList = dictionaryDataList.stream().filter(t -> dictionaryTypeEntity.getId().equals(t.getDictionaryTypeId())).collect(Collectors.toList());
            if (dictionaryTypeEntity.getIsTree().compareTo(1) == 0) {
                List<Map<String, Object>> selectList = new ArrayList<>();
                for (DictionaryDataEntity item : childNodeList) {
                    Map<String, Object> ht = new HashMap<>(16);
                    ht.put("fullName", item.getFullName());
                    ht.put("id", item.getId());
                    ht.put("parentId", item.getParentId());
                    selectList.add(ht);
                }
                //==============转换树
                List<SumTree<DictionaryDataAllModel>> list1 = TreeDotUtils.convertListToTreeDot(JsonUtil.getJsonToList(selectList, DictionaryDataAllModel.class));
                List<DictionaryDataAllVO> list2 = JsonUtil.getJsonToList(list1, DictionaryDataAllVO.class);
                //==============
                Map<String, Object> htItem = new HashMap<>(16);
                htItem.put("id", dictionaryTypeEntity.getId());
                htItem.put("enCode", dictionaryTypeEntity.getEnCode());
                htItem.put("dictionaryList", list2);
                htItem.put("isTree", 1);
                list.add(htItem);
            } else {
                List<Map<String, Object>> selectList = new ArrayList<>();
                for (DictionaryDataEntity item : childNodeList) {
                    Map<String, Object> ht = new HashMap<>(16);
                    ht.put("enCode", item.getEnCode());
                    ht.put("id", item.getId());
                    ht.put("fullName", item.getFullName());
                    selectList.add(ht);
                }
                Map<String, Object> htItem = new HashMap<>(16);
                htItem.put("id", dictionaryTypeEntity.getId());
                htItem.put("enCode", dictionaryTypeEntity.getEnCode());
                htItem.put("dictionaryList", selectList);
                htItem.put("isTree", 0);
                list.add(htItem);
            }
        }
        ListVO<Map<String, Object>> vo = new ListVO<>();
        vo.setList(list);
        return ActionResult.success(vo);
    }


    /**
     * 获取数据字典下拉框数据
     *
     * @param dictionaryTypeId 类别主键
     * @return
     */
    @ApiOperation("获取数据字典分类下拉框数据")
    @GetMapping("{dictionaryTypeId}/Selector")
    public ActionResult<ListVO<DictionaryDataListTreeVO>> treeView(@PathVariable("dictionaryTypeId") String dictionaryTypeId, String isTree) {

        DictionaryTypeEntity typeEntity = dictionaryTypeService.getInfo(dictionaryTypeId);
        List<DictionaryDataModel> treeList = new ArrayList<>();
        DictionaryDataModel treeViewModel = new DictionaryDataModel();
        treeViewModel.setId("0");
        treeViewModel.setFullName(typeEntity.getFullName());
        treeViewModel.setParentId("-1");
        treeViewModel.setIcon("fa fa-tags");
        treeList.add(treeViewModel);
        if (isTree != null && "1".equals(isTree)) {
            List<DictionaryDataEntity> data = dictionaryDataService.getList(dictionaryTypeId).stream().filter(t -> "1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());;
            for (DictionaryDataEntity entity : data) {
                DictionaryDataModel treeModel = new DictionaryDataModel();
                treeModel.setId(entity.getId());
                treeModel.setFullName(entity.getFullName());
                treeModel.setParentId("-1".equals(entity.getParentId()) ? entity.getDictionaryTypeId() : entity.getParentId());
                treeList.add(treeModel);
            }
        }
        List<SumTree<DictionaryDataModel>> sumTrees = TreeDotUtils.convertListToTreeDot(treeList);
        List<DictionaryDataSelectVO> list = JsonUtil.getJsonToList(sumTrees, DictionaryDataSelectVO.class);
        ListVO<DictionaryDataSelectVO> treeVo = new ListVO<>();
        treeVo.setList(list);
        return ActionResult.success(treeVo);
    }

    /**
     * 获取字典分类
     *
     * @return
     */
    @ApiOperation("获取某个字典数据下拉框列表")
    @GetMapping("/{dictionaryTypeId}/Data/Selector")
    public ActionResult<List<DictionaryTypeListVO>> selectorOneTreeView(@PathVariable("dictionaryTypeId") String dictionaryTypeId) {
        List<DictionaryDataEntity> data = dictionaryDataService.getList().stream().filter(t -> dictionaryTypeId.equals(t.getDictionaryTypeId())&&"1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());
        List<DictionaryTypeSelectModel> voListVO = JsonUtil.getJsonToList(data, DictionaryTypeSelectModel.class);
        List<SumTree<DictionaryTypeSelectModel>> sumTrees = TreeDotUtils.convertListToTreeDot(voListVO);
        List<DictionaryTypeSelectVO> list = JsonUtil.getJsonToList(sumTrees, DictionaryTypeSelectVO.class);
        ListVO<DictionaryTypeSelectVO> vo = new ListVO<>();
        vo.setList(list);
        return ActionResult.success(vo);
    }


    /**
     * 获取数据字典信息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("获取数据字典信息")
    @GetMapping("/{id}/Info")
    public ActionResult<DictionaryDataInfoVO> info(@PathVariable("id") String id) throws DataException {
        DictionaryDataEntity entity = dictionaryDataService.getInfo(id);
        DictionaryDataInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, DictionaryDataInfoVO.class);
        return ActionResult.success(vo);
    }

    /**
     * 重复验证（名称）
     *
     * @param dictionaryTypeId 类别主键
     * @param fullName         名称
     * @param id               主键值
     * @return
     */
    @ApiOperation("（待定）重复验证（名称）")
    @GetMapping("/IsExistByFullName")
    public ActionResult isExistByFullName(String dictionaryTypeId, String fullName, String id) {
        boolean data = dictionaryDataService.isExistByFullName(dictionaryTypeId, fullName, id);
        return ActionResult.success(data);
    }

    /**
     * 重复验证（编码）
     *
     * @param dictionaryTypeId 类别主键
     * @param enCode           编码
     * @param id               主键值
     * @return
     */
    @ApiOperation("（待定）重复验证（编码）")
    @GetMapping("/IsExistByEnCode")
    public ActionResult isExistByEnCode(String dictionaryTypeId, String enCode, String id) {
        boolean data = dictionaryDataService.isExistByEnCode(dictionaryTypeId, enCode, id);
        return ActionResult.success(data);
    }


    /**
     * 添加数据字典
     *
     * @param dictionaryDataCrForm 实体对象
     * @return
     */
    @ApiOperation("添加数据字典")
    @PostMapping
    public ActionResult create(@RequestBody @Valid DictionaryDataCrForm dictionaryDataCrForm) {
        DictionaryDataEntity entity = JsonUtil.getJsonToBean(dictionaryDataCrForm, DictionaryDataEntity.class);
        if (dictionaryDataService.isExistByFullName(entity.getDictionaryTypeId(), entity.getFullName(), entity.getId())) {
            return ActionResult.fail("字典名称不能重复");
        }
        if (dictionaryDataService.isExistByEnCode(entity.getDictionaryTypeId(), entity.getEnCode(), entity.getId())) {
            return ActionResult.fail("字典编码不能重复");
        }
        dictionaryDataService.create(entity);
        return ActionResult.success("新建成功");
    }

    /**
     * 修改数据字典
     *
     * @param dictionaryDataUpForm 实体对象
     * @param id                   主键值
     * @return
     */
    @ApiOperation("修改数据字典")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid DictionaryDataUpForm dictionaryDataUpForm) {
        DictionaryDataEntity entity = JsonUtil.getJsonToBean(dictionaryDataUpForm, DictionaryDataEntity.class);
        if (dictionaryDataService.isExistByFullName(entity.getDictionaryTypeId(), entity.getFullName(), id)) {
            return ActionResult.fail("字典名称不能重复");
        }
        if (dictionaryDataService.isExistByEnCode(entity.getDictionaryTypeId(), entity.getEnCode(), id)) {
            return ActionResult.fail("字典编码不能重复");
        }
        boolean flag = dictionaryDataService.update(id, entity);
        if (flag == false) {
            return ActionResult.fail("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");

    }

    /**
     * 删除数据字典
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("删除数据字典")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) {
        DictionaryDataEntity entity = dictionaryDataService.getInfo(id);
        if (entity != null) {
            dictionaryDataService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 更新字典状态
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("更新字典状态")
    @PutMapping("/{id}/Actions/State")
    public ActionResult update(@PathVariable("id") String id) {
        DictionaryDataEntity entity = dictionaryDataService.getInfo(id);
        if (entity != null) {
            if ("1".equals(String.valueOf(entity.getEnabledMark()))) {
                entity.setEnabledMark(0);
            } else {
                entity.setEnabledMark(1);
            }
            boolean flag = dictionaryDataService.update(entity.getId(), entity);
            if (flag == false) {
                return ActionResult.success("更新失败，数据不存在");
            }
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 数据字典导出功能
     * @param id    接口id
     */
    @ApiOperation("导出数据字典数据")
    @GetMapping("/{id}/Action/Export")
    public ActionResult exportFile(@PathVariable("id") String id){
        DictionaryExportModel exportModel = dictionaryDataService.exportData(id);
        //导出文件
        DownloadVO downloadVO = fileExport.exportFile(exportModel, configValueUtil.getTemporaryFilePath());
        return ActionResult.success(downloadVO);
    }

    /**
     * 数据字典导入功能
     * @param multipartFile
     * @return
     * @throws DataException
     */
    @ApiOperation("数据字典导入功能")
    @PostMapping(value = "/Action/Import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult importFile(@RequestPart("file") MultipartFile multipartFile) throws DataException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile)){
            return ActionResult.fail("导入文件格式错误");
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile, configValueUtil.getTemporaryFilePath());
        try {
            DictionaryExportModel exportModel = JsonUtil.getJsonToBean(fileContent, DictionaryExportModel.class);
            List<DictionaryTypeEntity> list = exportModel.getList();
            //父级分类id不存在的话，直接抛出异常
            //如果分类只有一个
            if (list.size() == 1 && !"-1".equals(list.get(0).getParentId()) && dictionaryTypeService.getInfo(list.get(0).getParentId()) == null){
                return ActionResult.fail("导入失败，查询不到上级分类");
            }
            //如果有多个需要验证分类是否存在
            if (list.stream().filter(t->"-1".equals(t.getParentId())).collect(Collectors.toList()).size() < 1){
                for (DictionaryTypeEntity dictionaryTypeEntity: list) {
                    //判断父级是否存在
                    if (dictionaryTypeService.getInfo(dictionaryTypeEntity.getParentId()) == null){
                        return ActionResult.fail("导入失败，查询不到上级分类");
                    }
                }
            }
            //判断数据是否存在
            boolean isExists = dictionaryDataService.importData(exportModel);
            if (isExists){
                return ActionResult.fail("数据已存在");
            }
        }catch (Exception e){
            throw new DataException("导入失败，数据有误");
        }
        return ActionResult.success("导入成功");
    }

}
