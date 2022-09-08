package jnpf.engine.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.util.FormCloumnUtil;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.config.ConfigValueUtil;
import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.enums.FlowTaskOperatorEnum;
import jnpf.engine.enums.FlowTaskStatusEnum;
import jnpf.engine.model.flowengine.*;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.service.FlowEngineService;
import jnpf.engine.service.FlowTaskService;
import jnpf.database.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.model.FormAllModel;
import jnpf.model.FormEnum;
import jnpf.model.visiual.FormDataField;
import jnpf.model.visiual.FormDataModel;
import jnpf.model.visiual.JnpfKeyConsts;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.StringUtil;
import jnpf.util.file.fileinfo.DataFileExport;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程设计
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "流程引擎", value = "FlowEngine")
@RestController
@RequestMapping("/api/workflow/Engine/FlowEngine")
public class FlowEngineController {

    @Autowired
    private FlowEngineService flowEngineService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private DataFileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;

    /**
     * 获取流程设计列表
     *
     * @return
     */
    @ApiOperation("获取流程引擎列表")
    @GetMapping
    public ActionResult<ListVO<FlowEngineListVO>> list(PaginationFlowEngine pagination) {
        List<FlowEngineListVO> treeList = flowEngineService.getTreeList(pagination,true);
        ListVO vo = new ListVO();
        vo.setList(treeList);
        return ActionResult.success(vo);
    }

    /**
     * 获取流程设计列表
     *
     * @return
     */
    @ApiOperation("流程引擎下拉框")
    @GetMapping("/Selector")
    public ActionResult<ListVO<FlowEngineListVO>> listSelect(String type) {
        PaginationFlowEngine pagination = new PaginationFlowEngine();
        pagination.setFormType(type);
        pagination.setEnabledMark("1");
        List<FlowEngineListVO> treeList = flowEngineService.getTreeList(pagination,true);
        ListVO vo = new ListVO();
        vo.setList(treeList);
        return ActionResult.success(vo);
    }

    /**
     * 主表属性
     *
     * @return
     */
    @ApiOperation("表单主表属性")
    @GetMapping("/{id}/FormDataFields")
    public ActionResult<ListVO<FormDataField>> getFormDataField(@PathVariable("id") String id) throws WorkFlowException {
        FlowEngineEntity entity = flowEngineService.getInfo(id);
        List<FormDataField> formDataFieldList = new ArrayList<>();
        if (entity.getFormType() == 1) {
            List<FlowEngineModel> list = JsonUtil.getJsonToList(entity.getFormData(), FlowEngineModel.class);
            for (FlowEngineModel model : list) {
                FormDataField formDataField = new FormDataField();
                formDataField.setLabel(model.getFiledName());
                formDataField.setVModel(model.getFiledId());
                formDataFieldList.add(formDataField);
            }
        } else {
            //formTempJson
            FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
            List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
            List<FormAllModel> formAllModel = new ArrayList<>();
            FormCloumnUtil.recursionForm(list, formAllModel);
            //主表数据
            List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
            for (FormAllModel model : mast) {
                FieLdsModel fieLdsModel = model.getFormColumnModel().getFieLdsModel();
                String vmodel = fieLdsModel.getVModel();
                String jnpfKey = fieLdsModel.getConfig().getJnpfKey();
                if (StringUtil.isNotEmpty(vmodel) && !JnpfKeyConsts.RELATIONFORM.equals(jnpfKey) && !JnpfKeyConsts.RELATIONFLOW.equals(jnpfKey)) {
                    FormDataField formDataField = new FormDataField();
                    formDataField.setLabel(fieLdsModel.getConfig().getLabel());
                    formDataField.setVModel(fieLdsModel.getVModel());
                    formDataFieldList.add(formDataField);
                }
            }
        }
        ListVO<FormDataField> listVO = new ListVO();
        listVO.setList(formDataFieldList);
        return ActionResult.success(listVO);
    }

    /**
     * 列表
     *
     * @return
     */
    @ApiOperation("表单列表")
    @GetMapping("/{id}/FieldDataSelect")
    public ActionResult<ListVO<FlowEngineSelectVO>> getFormData(@PathVariable("id") String id) {
        List<FlowTaskEntity> flowTaskList = flowTaskService.getTaskList(id).stream().filter(t -> FlowTaskStatusEnum.Adopt.getCode().equals(t.getStatus())).collect(Collectors.toList());
        List<FlowEngineSelectVO> vo = new ArrayList<>();
        for (FlowTaskEntity taskEntity : flowTaskList) {
            FlowEngineSelectVO selectVO = JsonUtil.getJsonToBean(taskEntity, FlowEngineSelectVO.class);
            selectVO.setFullName(taskEntity.getFullName() + "/" + taskEntity.getEnCode());
            vo.add(selectVO);
        }
        ListVO listVO = new ListVO();
        listVO.setList(vo);
        return ActionResult.success(listVO);
    }

    /**
     * 列表
     *
     * @return
     */
    @ApiOperation("获取可见流程引擎列表")
    @GetMapping("/ListAll")
    public ActionResult<ListVO<FlowEngineListVO>> listAll() {
        PaginationFlowEngine pagination = new PaginationFlowEngine();
        List<FlowEngineListVO> treeList = flowEngineService.getTreeList(pagination,false);
        ListVO vo = new ListVO();
        vo.setList(treeList);
        return ActionResult.success(vo);
    }

    /**
     * 获取流程引擎信息
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("获取流程引擎信息")
    @GetMapping("/{id}")
    public ActionResult<FlowEngineInfoVO> info(@PathVariable("id") String id) throws DataException, WorkFlowException {
        FlowEngineEntity flowEntity = flowEngineService.getInfo(id);
        FlowEngineInfoVO vo = JsonUtilEx.getJsonToBeanEx(flowEntity, FlowEngineInfoVO.class);
        ChildNode childNode = JsonUtil.getJsonToBean(flowEntity.getFlowTemplateJson(), ChildNode.class);
        //判断下一节点是否是授权审批人
        int freeApprover = 0;
        if (childNode.getChildNode() != null) {
            String type = childNode.getChildNode().getProperties().getAssigneeType();
            if (String.valueOf(FlowTaskOperatorEnum.FreeApprover.getCode()).equals(type)) {
                freeApprover = 1;
            }
        }
        vo.setFreeApprover(freeApprover);
        return ActionResult.success(vo);
    }

    /**
     * 新建流程设计
     *
     * @return
     */
    @ApiOperation("新建流程引擎")
    @PostMapping
    public ActionResult create(@RequestBody @Valid FlowEngineCrForm flowEngineCrForm) {
        FlowEngineEntity flowEngineEntity = JsonUtil.getJsonToBean(flowEngineCrForm, FlowEngineEntity.class);
        if (flowEngineService.isExistByFullName(flowEngineEntity.getFullName(), flowEngineEntity.getId())) {
            return ActionResult.fail("流程名称不能重复");
        }
        if (flowEngineService.isExistByEnCode(flowEngineEntity.getEnCode(), flowEngineEntity.getId())) {
            return ActionResult.fail("流程编码不能重复");
        }
        flowEngineService.create(flowEngineEntity);
        return ActionResult.success("新建成功");
    }

    /**
     * 更新流程设计
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("更新流程引擎")
    @PutMapping("/{id}")
    public ActionResult update(@PathVariable("id") String id, @RequestBody @Valid FlowEngineUpForm flowEngineUpForm) {
        FlowEngineEntity flowEngineEntity = JsonUtil.getJsonToBean(flowEngineUpForm, FlowEngineEntity.class);
        if (flowEngineService.isExistByFullName(flowEngineUpForm.getFullName(), id)) {
            return ActionResult.fail("流程名称不能重复");
        }
        if (flowEngineService.isExistByEnCode(flowEngineUpForm.getEnCode(), id)) {
            return ActionResult.fail("流程编码不能重复");
        }
        boolean flag = flowEngineService.updateVisible(id, flowEngineEntity);
        if (flag == false) {
            return ActionResult.success("更新失败，数据不存在");
        }
        return ActionResult.success("更新成功");
    }

    /**
     * 删除流程设计
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("删除流程引擎")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) throws WorkFlowException {
        FlowEngineEntity entity = flowEngineService.getInfo(id);
        List<FlowTaskEntity> taskNodeList = flowTaskService.getTaskList(entity.getId());
        if (taskNodeList.size() > 0) {
            return ActionResult.fail("引擎在使用，不可删除");
        }
        if (entity != null) {
            flowEngineService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 复制流程表单
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("复制流程表单")
    @PostMapping("/{id}/Actions/Copy")
    public ActionResult copy(@PathVariable("id") String id) throws WorkFlowException {
        FlowEngineEntity flowEngineEntity = flowEngineService.getInfo(id);
        if (flowEngineEntity != null) {
            long time = System.currentTimeMillis();
            flowEngineEntity.setFullName(flowEngineEntity.getFullName() + "_" + time);
            flowEngineEntity.setEnCode(flowEngineEntity.getEnCode() + "_" + time);
            flowEngineEntity.setCreatorTime(new Date());
            flowEngineEntity.setId(null);
            flowEngineService.create(flowEngineEntity);
            return ActionResult.success("复制成功");
        }
        return ActionResult.fail("复制失败，数据不存在");
    }

    /**
     * 流程表单状态
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("更新流程表单状态")
    @PutMapping("/{id}/Actions/State")
    public ActionResult state(@PathVariable("id") String id) throws WorkFlowException {
        FlowEngineEntity entity = flowEngineService.getInfo(id);
        if (entity != null) {
            entity.setEnabledMark("1".equals(String.valueOf(entity.getEnabledMark())) ? 0 : 1);
            flowEngineService.update(id, entity);
            return ActionResult.success("更新表单成功");
        }
        return ActionResult.fail("更新失败，数据不存在");
    }

    /**
     * 发布流程引擎
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("发布流程设计")
    @PostMapping("/Release/{id}")
    public ActionResult release(@PathVariable("id") String id) throws WorkFlowException {
        FlowEngineEntity entity = flowEngineService.getInfo(id);
        if (entity != null) {
            entity.setEnabledMark(1);
            flowEngineService.update(id, entity);
            return ActionResult.success("发布成功");
        }
        return ActionResult.fail("发布失败，数据不存在");
    }

    /**
     * 停止流程引擎
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("停止流程设计")
    @PostMapping("/Stop/{id}")
    public ActionResult stop(@PathVariable("id") String id) throws WorkFlowException {
        FlowEngineEntity entity = flowEngineService.getInfo(id);
        if (entity != null) {
            entity.setEnabledMark(0);
            flowEngineService.update(id, entity);
            return ActionResult.success("停止成功");
        }
        return ActionResult.fail("停止失败，数据不存在");
    }

    /**
     * 工作流导出
     * @param id 主键值
     * @return
     * @throws WorkFlowException
     */
    @ApiOperation("工作流导出")
    @GetMapping("/{id}/Actions/ExportData")
    public ActionResult exportData(@PathVariable("id") String id) throws WorkFlowException {
        FlowExportModel model = flowEngineService.exportData(id);
        DownloadVO downloadVO = fileExport.exportFile(model, configValueUtil.getTemporaryFilePath());
        return ActionResult.success(downloadVO);
    }

    /**
     * 工作流导入
     * @param multipartFile 文件
     * @return
     * @throws WorkFlowException
     */
    @ApiOperation("工作流导入")
    @PostMapping(value = "/Actions/ImportData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult ImportData(@RequestPart("file") MultipartFile multipartFile) throws WorkFlowException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile)) {
            return ActionResult.fail("导入文件格式错误");
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile, configValueUtil.getTemporaryFilePath());
        FlowExportModel vo = JsonUtil.getJsonToBean(fileContent, FlowExportModel.class);
        return flowEngineService.ImportData(vo.getFlowEngine(),vo.getVisibleList());
    }

}
