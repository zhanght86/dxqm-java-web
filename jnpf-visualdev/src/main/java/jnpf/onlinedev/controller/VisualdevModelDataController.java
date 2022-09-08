package jnpf.onlinedev.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.service.DblinkService;
import jnpf.base.util.FlowDataUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.model.flowtask.FlowTableModel;
import jnpf.engine.service.FlowEngineService;
import jnpf.engine.service.FlowTaskService;
import jnpf.exception.WorkFlowException;
import jnpf.model.visiual.FormDataModel;
import jnpf.model.visiual.OnlineDevData;
import jnpf.onlinedev.model.*;
import jnpf.onlinedev.service.VisualDevListService;
import jnpf.onlinedev.util.AutoFeildsUtil;
import jnpf.util.JsonUtilEx;
import jnpf.base.ActionResult;
import jnpf.base.util.VisualUtils;
import jnpf.base.vo.PaginationVO;
import jnpf.base.UserInfo;
import jnpf.base.service.VisualdevService;
import jnpf.config.ConfigValueUtil;
import jnpf.base.entity.VisualdevEntity;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;
import jnpf.database.exception.DataException;
import jnpf.base.vo.DownloadVO;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.util.*;
import jnpf.util.JsonUtil;
import jnpf.util.enums.ExportModelTypeEnum;
import jnpf.util.file.FileExport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

/**
 * 0代码无表开发
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@Api(tags = "0代码无表开发", value = "OnlineDev")
@RestController
@RequestMapping("/api/visualdev/OnlineDev")
public class VisualdevModelDataController {


    @Autowired
    private VisualdevModelDataService visualdevModelDataService;
    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private FileExport fileExport;
    @Autowired
    private FlowEngineService flowEngineService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private VisualDevListService visualDevListService;
    @Autowired
    private DblinkService dblinkService;
    @Autowired
    private FlowDataUtil flowDataUtil;

    @ApiOperation("获取数据列表")
    @PostMapping("/{modelId}/List")
    public ActionResult list(@PathVariable("modelId") String modelId,@RequestBody PaginationModel paginationModel) throws ParseException, IOException, SQLException, DataException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);

        List<Map<String, Object>> realList = visualDevListService.getRealList(visualdevEntity, paginationModel);

        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationModel, PaginationVO.class);

        return ActionResult.page(realList, paginationVO);
    }

    @ApiOperation("获取列表表单配置JSON")
    @GetMapping("/{modelId}/Config")
    public ActionResult getData(@PathVariable("modelId") String modelId) {
        VisualdevEntity entity = visualdevService.getInfo(modelId);
        if (entity == null) {
            return ActionResult.fail("功能不存在");
        }
        DataInfoVO vo = JsonUtil.getJsonToBean(entity, DataInfoVO.class);
        if (StringUtil.isNotEmpty(entity.getWebType())){
            if (entity.getWebType()!=null&&entity.getWebType().equals("3")){
                try {
                    FlowEngineEntity engineEntity = flowEngineService.getInfo(entity.getFlowId());
                    vo.setFlowEnCode(engineEntity.getEnCode());
                    vo.setFlowId(entity.getFlowId());
                } catch (WorkFlowException e) {
                    e.printStackTrace();
                }
            }
        }

        return ActionResult.success(vo);
    }


    @ApiOperation("获取列表配置JSON")
    @GetMapping("/{modelId}/ColumnData")
    public ActionResult getColumnData(@PathVariable("modelId") String modelId) {
        VisualdevEntity entity = visualdevService.getInfo(modelId);
        FormDataInfoVO vo = JsonUtil.getJsonToBean(entity, FormDataInfoVO.class);
        return ActionResult.success(vo);
    }


    @ApiOperation("获取表单配置JSON")
    @GetMapping("/{modelId}/FormData")
    public ActionResult getFormData(@PathVariable("modelId") String modelId) {
        VisualdevEntity entity = visualdevService.getInfo(modelId);
        ColumnDataInfoVO vo = JsonUtil.getJsonToBean(entity, ColumnDataInfoVO.class);
        return ActionResult.success(vo);
    }

    @ApiOperation("获取工作流模板JSON")
    @GetMapping("/{modelId}/FlowTemplate")
    public ActionResult getFlowTemplate(@PathVariable("modelId") String modelId){
        VisualdevEntity entity = visualdevService.getInfo(modelId);
        FlowTemplateInfoVo vo = JsonUtil.getJsonToBean(entity,FlowTemplateInfoVo.class);
        return ActionResult.success(vo);
    }

    @ApiOperation("获取数据信息")
    @GetMapping("/{modelId}/{id}")
    public ActionResult info(@PathVariable("id") String id, @PathVariable("modelId") String modelId) throws DataException, ParseException, SQLException, IOException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        //有表
        if (!StringUtil.isEmpty(visualdevEntity.getTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            VisualdevModelDataInfoVO vo = visualdevModelDataService.tableInfo(id, visualdevEntity);
            return ActionResult.success(vo);
        }
        //无表
        VisualdevModelDataEntity entity = visualdevModelDataService.getInfo(id);
        Map<String, Object> formData = JsonUtil.stringToMap(visualdevEntity.getFormData());
        List<FieLdsModel> modelList = JsonUtil.getJsonToList(formData.get("fields").toString(), FieLdsModel.class);
        //去除模板多级控件
        modelList = VisualUtils.deleteMore(modelList);
        String data = AutoFeildsUtil.autoFeilds(modelList, entity.getData());
        entity.setData(data);
        VisualdevModelDataInfoVO vo = JsonUtilEx.getJsonToBeanEx(entity, VisualdevModelDataInfoVO.class);
        return ActionResult.success(vo);
    }

    @ApiOperation("获取数据信息(带转换数据)")
    @GetMapping("/{modelId}/{id}/DataChange")
    public ActionResult infoWithDataChange(@PathVariable("modelId") String modelId, @PathVariable("id") String id) throws DataException, ParseException, IOException, SQLException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        if (redisUtil.exists(CacheKeyUtil.VISIUALDATA + modelId)) {
            redisUtil.remove(CacheKeyUtil.VISIUALDATA + modelId);
        }
        //有表
        if (!StringUtil.isEmpty(visualdevEntity.getTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            VisualdevModelDataInfoVO vo = visualdevModelDataService.tableInfoDataChange(id, visualdevEntity);
            return ActionResult.success(vo);
        }
        //无表
        VisualdevModelDataInfoVO vo = visualdevModelDataService.infoDataChange(id, visualdevEntity);
        return ActionResult.success(vo);
    }


    @ApiOperation("添加数据")
    @PostMapping("/{modelId}")
    public ActionResult create(@PathVariable("modelId") String modelId, @RequestBody VisualdevModelDataCrForm visualdevModelDataCrForm) throws  WorkFlowException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        Map<String, Object> map = JsonUtil.stringToMap(visualdevModelDataCrForm.getData());
        FormDataModel formData = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<FlowTableModel> tableModels = JsonUtil.getJsonToList(visualdevEntity.getTables(),FlowTableModel.class);
        DbLinkEntity linkEntity = null;
        if(StringUtil.isNotEmpty(visualdevEntity.getDbLinkId())){
            linkEntity =dblinkService.getInfo(visualdevEntity.getDbLinkId());
        }
        String mainId = RandomUtil.uuId();
        Map<String, Object> map1 = flowDataUtil.create(map, list, tableModels,mainId ,new HashMap<>(16),linkEntity);

        return  visualdevModelDataService.visualCreate(visualdevEntity,map1,visualdevModelDataCrForm,mainId);
    }


    @ApiOperation("修改数据")
    @PutMapping("/{modelId}/{id}")
    public ActionResult update(@PathVariable("id") String id, @PathVariable("modelId") String modelId, @RequestBody VisualdevModelDataUpForm visualdevModelDataUpForm) throws DataException, SQLException, WorkFlowException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        Map<String, Object> map = JsonUtil.stringToMap(visualdevModelDataUpForm.getData());
        FormDataModel formData = JsonUtil.getJsonToBean(visualdevEntity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<FlowTableModel> tableModels = JsonUtil.getJsonToList(visualdevEntity.getTables(),FlowTableModel.class);
        DbLinkEntity linkEntity = null;
        if(StringUtil.isNotEmpty(visualdevEntity.getDbLinkId())){
            linkEntity =dblinkService.getInfo(visualdevEntity.getDbLinkId());
        }
        Map<String, Object> map1 = flowDataUtil.update(map, list, tableModels,id ,linkEntity);

        return visualdevModelDataService.visualUpdate(id,visualdevEntity,map1,visualdevModelDataUpForm);
    }


    @ApiOperation("删除数据")
    @DeleteMapping("/{modelId}/{id}")
    public ActionResult delete(@PathVariable("id") String id, @PathVariable("modelId") String modelId) throws DataException, SQLException,WorkFlowException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        //删除流程任务
        if (StringUtil.isNotEmpty(visualdevEntity.getWebType())) {
            if (visualdevEntity.getWebType()!=null&&visualdevEntity.getWebType().equals("3")) {
                if (StringUtil.isNotEmpty(visualdevEntity.getFlowId())) {
                    FlowTaskEntity taskEntity = flowTaskService.getInfoSubmit(id);
                    if (taskEntity != null) {
                        flowTaskService.delete(taskEntity);
                    }
                } else {
                    return ActionResult.fail("未关联流程引擎");
                }
            }
        }
        if (!StringUtil.isEmpty(visualdevEntity.getTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            boolean result = visualdevModelDataService.tableDelete(id, visualdevEntity);
            if (result) {
                return ActionResult.success("删除成功");
            } else {
                return ActionResult.fail("删除失败，数据不存在");
            }
        }
        VisualdevModelDataEntity entity = visualdevModelDataService.getInfo(id);
        if (entity != null) {
            visualdevModelDataService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    @ApiOperation("批量删除数据")
    @PostMapping("/batchDelete/{modelId}")
    public ActionResult beachDelete(@RequestBody BatchRemoveIdsVo idsVo, @PathVariable("modelId") String modelId) throws DataException, SQLException, WorkFlowException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        List<String> idsList= new ArrayList<>();
        List<String> idsVoList= Arrays.asList(idsVo.getIds());
        if (visualdevEntity.getWebType()!=null&&visualdevEntity.getWebType().equals("3")){
            for (String id:idsVoList){
                FlowTaskEntity taskEntity = flowTaskService.getInfoSubmit(id);
                if (taskEntity!=null){
                    if (taskEntity.getStatus().equals(0)||taskEntity.getStatus().equals(4)){
                        idsList.add(id);
                        flowTaskService.delete(taskEntity);
                    }
                }else {
                    idsList.add(id);
                }
            }
        }else {
            idsList=idsVoList;
        }
        if (idsList.size()==0){
            return ActionResult.fail("该流程已发起，无法删除");
        }
        if (!StringUtil.isEmpty(visualdevEntity.getTables()) && !OnlineDevData.TABLE_CONST.equals(visualdevEntity.getTables())) {
            ActionResult result = visualdevModelDataService.tableDeleteMore(idsList, visualdevEntity);
            return result;
        }
        if (visualdevModelDataService.removeByIds(idsList)) {
            return ActionResult.success("删除成功");
        }else if (visualdevEntity.getWebType()!=null&&visualdevEntity.getWebType().equals("3") &&idsList.size()>0){
            //分组页面
            return ActionResult.fail("该流程已发起，无法删除");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }


    @ApiOperation("导入")
    @PostMapping("/Model/{modelId}/Actions/Import")
    public ActionResult imports(@PathVariable("modelId") String modelId) {
        VisualdevModelDataEntity entity = visualdevModelDataService.getInfo(modelId);
        List<MultipartFile> list = UpUtil.getFileAll();
        MultipartFile file = list.get(0);
        if (file.getOriginalFilename().contains(".xlsx")) {
            String filePath = configValueUtil.getTemporaryFilePath();
            String fileName = RandomUtil.uuId() + "." + UpUtil.getFileType(file);
            //保存文件
            FileUtil.upFile(file, filePath, fileName);
            File temporary = new File(filePath + fileName);
            return ActionResult.success("导入成功");
        } else {
            return ActionResult.fail("选择文件不符合导入");
        }
    }

    @ApiOperation("导出")
    @PostMapping("/{modelId}/Actions/Export")
    public ActionResult export(@PathVariable("modelId") String modelId, @RequestBody PaginationModelExport paginationModelExport) throws ParseException, IOException, SQLException, DataException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        String[] keys = paginationModelExport.getSelectKey();
        //关键字过滤
        List<Map<String, Object>> realList = visualdevModelDataService.exportData(keys, paginationModelExport, visualdevEntity);
        UserInfo userInfo = userProvider.get();
        DownloadVO vo = VisualUtils.createModelExcel(visualdevEntity.getFormData(), configValueUtil.getTemporaryFilePath(), realList, keys, userInfo);
        return ActionResult.success(vo);
    }

    @ApiOperation("功能导出")
    @PostMapping("/{modelId}/Actions/ExportData")
    public ActionResult exportData(@PathVariable("modelId") String modelId){
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        BaseDevModelVO vo = JsonUtil.getJsonToBean(visualdevEntity,BaseDevModelVO.class);
        vo.setModelType(ExportModelTypeEnum.Design.getMessage());
        DownloadVO downloadVO=fileExport.exportFile(vo,configValueUtil.getTemporaryFilePath());
        return ActionResult.success(downloadVO);
    }

    @ApiOperation("功能导入")
    @PostMapping(value = "/Model/Actions/ImportData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult ImportData(@RequestPart("file") MultipartFile multipartFile) throws SQLException, DataException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile)){
            return ActionResult.fail("导入文件格式错误");
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile, configValueUtil.getTemporaryFilePath());
        BaseDevModelVO vo = JsonUtil.getJsonToBean(fileContent,BaseDevModelVO.class);
        if (vo.getModelType()==null||!vo.getModelType().equals(ExportModelTypeEnum.Design.getMessage())){
            return ActionResult.fail("请导入对应功能的json文件");
        }
        VisualdevEntity visualdevEntity = JsonUtil.getJsonToBean(vo,VisualdevEntity.class);
        String modelId =visualdevEntity.getId();
        if (StringUtil.isNotEmpty(modelId)){
            VisualdevEntity entity = visualdevService.getInfo(modelId);
            if (entity!=null){
                return ActionResult.fail("已存在相同功能");
            }
        }
        visualdevEntity.setCreatorTime(DateUtil.getNowDate());
        visualdevEntity.setLastModifyTime(null);
        visualdevService.create(visualdevEntity);
        return ActionResult.success("导入成功");
    }

    /**
     * 模板下载
     *
     * @return
     */
    @ApiOperation("模板下载")
    @GetMapping("/TemplateDownload")
    public ActionResult<DownloadVO> templateDownload() {
        UserInfo userInfo = userProvider.get();
//        String path = configValueUtil.getTemplateFilePath() + "employee_import_template.xlsx";
        DownloadVO vo = DownloadVO.builder().build();
        try {
            vo.setName("职员信息.xlsx");
            vo.setUrl(UploaderUtil.uploaderFile("/api/file/DownloadModel?encryption=", userInfo.getId() + "#" + "职员信息.xlsx" + "#" + "Temporary"));
        } catch (Exception e) {
            log.error("信息导出Excel错误:{}", e.getMessage());
        }
        return ActionResult.success(vo);
    }

    /**
     * 在线开发大写转小写
     *
     * @return
     */
    @ApiOperation("在线开发大写转小写")
    @GetMapping("/changeTypeToLowOne")
    public void changeTypeToLow(String modelId) {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        visualdevEntity = VisualUtils.changeType(visualdevEntity);
        visualdevService.update(visualdevEntity.getId(), visualdevEntity);
        List<VisualdevModelDataEntity> list = visualdevModelDataService.getList(modelId);
        if (list != null && list.size() > 0) {
            List<Map<String, Object>> dataList = VisualUtils.toLowerKeyList(JsonUtil.getJsonToListMap(JsonUtilEx.getObjectToString(list)));
            list = JsonUtil.getJsonToList(dataList, VisualdevModelDataEntity.class);
            visualdevModelDataService.saveBatch(list);
        }
    }

    /**
     * 在线开发大写转小写
     *
     * @return
     */
    @ApiOperation("全部在线开发大写转小写")
    @GetMapping("/changeTypeToLowBatch")
    public void changeTypeToLowBatch() {
        List<VisualdevEntity> list = visualdevService.getList();
        for (VisualdevEntity entity : list) {
            entity = VisualUtils.changeType(entity);
            visualdevService.update(entity.getId(), entity);
            List<VisualdevModelDataEntity> modellist = visualdevModelDataService.getList(entity.getId());
            if (list != null && list.size() > 0) {
                List<Map<String, Object>> dataList = VisualUtils.toLowerKeyList(JsonUtil.getJsonToListMap(JsonUtilEx.getObjectToString(modellist)));
                modellist = JsonUtil.getJsonToList(dataList, VisualdevModelDataEntity.class);
                visualdevModelDataService.updateBatchById(modellist);
            }
        }

    }

}

