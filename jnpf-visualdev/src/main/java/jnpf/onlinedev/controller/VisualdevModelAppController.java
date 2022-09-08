package jnpf.onlinedev.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.PaginationVO;
import jnpf.base.service.VisualdevService;
import jnpf.base.entity.VisualdevEntity;
import jnpf.config.ConfigValueUtil;
import jnpf.database.exception.DataException;
import jnpf.generater.model.AppDataInfoVO;
import jnpf.onlinedev.model.*;
import jnpf.onlinedev.service.VisualdevModelAppService;
import jnpf.onlinedev.service.VisualdevModelDataService;
import jnpf.util.DateUtil;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import jnpf.util.enums.ExportModelTypeEnum;
import jnpf.util.file.FileExport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 * 0代码app无表开发
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "0代码app无表开发", value = "ModelAppData")
@RestController
@RequestMapping("/api/visualdev/OnlineDev/App")
public class VisualdevModelAppController {

    @Autowired
    private VisualdevModelAppService modelAppService;
    @Autowired
    private VisualdevService visualdevService;
    @Autowired
    private VisualdevModelDataService visualdevModelDataService;
    @Autowired
    private FileExport fileExport;
    @Autowired
    private ConfigValueUtil configValueUtil;

    @ApiOperation("获取数据列表")
    @GetMapping("/{modelId}/List")
    public ActionResult list(@PathVariable("modelId") String modelId, PaginationModel paginationModel) throws DataException, ParseException, SQLException, IOException {
        List<Map<String, Object>> realList = modelAppService.resultList(modelId, paginationModel);
        PaginationVO paginationVO = JsonUtil.getJsonToBean(paginationModel, PaginationVO.class);
        return ActionResult.page(realList, paginationVO);
    }

    @ApiOperation("获取列表表单配置JSON")
    @GetMapping("/{modelId}/Config")
    public ActionResult getData(@PathVariable("modelId") String modelId) {
        VisualdevEntity entity = visualdevService.getInfo(modelId);
        AppDataInfoVO vo = JsonUtil.getJsonToBean(entity, AppDataInfoVO.class);
        if (vo == null) {
            return ActionResult.fail("功能不存在");
        }
        return ActionResult.success(vo);
    }

    @ApiOperation("添加数据")
    @PostMapping("/{modelId}")
    public ActionResult create(@PathVariable("modelId") String modelId, @RequestBody VisualdevModelDataCrForm visualdevModelDataCrForm) throws DataException, SQLException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        modelAppService.create(visualdevEntity, visualdevModelDataCrForm.getData());
        return ActionResult.success("新建成功");
    }

    @ApiOperation("修改数据")
    @PutMapping("/{modelId}/{id}")
    public ActionResult update(@PathVariable("id") String id, @PathVariable("modelId") String modelId, @RequestBody VisualdevModelDataUpForm visualdevModelDataUpForm) throws DataException, SQLException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        return modelAppService.update(id, visualdevEntity, visualdevModelDataUpForm.getData());
    }

    @ApiOperation("删除数据")
    @DeleteMapping("/{modelId}/{id}")
    public ActionResult delete(@PathVariable("id") String id, @PathVariable("modelId") String modelId) throws DataException, SQLException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        boolean result = modelAppService.delete(id, visualdevEntity);
        if (result) {
            return ActionResult.success("删除成功");
        } else {
            return ActionResult.fail("删除失败，数据不存在");
        }
    }

    @ApiOperation("获取数据信息")
    @GetMapping("/{modelId}/{id}")
    public ActionResult info(@PathVariable("modelId") String modelId, @PathVariable("id") String id) throws DataException, ParseException, SQLException, IOException {
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        Map<String, Object> info = modelAppService.info(id, visualdevEntity);
        VisualdevModelDataInfoVO vo = JsonUtil.getJsonToBean(info, VisualdevModelDataInfoVO.class);
        return ActionResult.success(vo);
    }

    @ApiOperation("功能导出")
    @PostMapping("/{modelId}/Actions/ExportData")
    public ActionResult exportData(@PathVariable("modelId") String modelId){
        VisualdevEntity visualdevEntity = visualdevService.getInfo(modelId);
        BaseDevModelVO vo = JsonUtil.getJsonToBean(visualdevEntity,BaseDevModelVO.class);
        vo.setModelType(ExportModelTypeEnum.App.getMessage());
        DownloadVO downloadVO=fileExport.exportFile(vo,configValueUtil.getTemporaryFilePath());
        return ActionResult.success(downloadVO);
    }

    @ApiOperation("功能导入")
    @PostMapping(value = "/Model/Actions/ImportData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult ImportData(@RequestPart("file") MultipartFile multipartFile) throws SQLException, DataException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile)) {
            return ActionResult.fail("导入文件格式错误");
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile, configValueUtil.getTemporaryFilePath());
        BaseDevModelVO vo = JsonUtil.getJsonToBean(fileContent, BaseDevModelVO.class);
        if (vo.getModelType() == null || !vo.getModelType().equals(ExportModelTypeEnum.App.getMessage())) {
            return ActionResult.fail("请导入对应功能的json文件");
        }
        VisualdevEntity visualdevEntity = JsonUtil.getJsonToBean(vo, VisualdevEntity.class);
        String modelId = visualdevEntity.getId();
        if (StringUtil.isNotEmpty(modelId)) {
            VisualdevEntity entity = visualdevService.getInfo(modelId);
            if (entity != null) {
                return ActionResult.fail("已存在相同功能");
            }
        }
        visualdevEntity.setCreatorTime(DateUtil.getNowDate());
        visualdevEntity.setLastModifyTime(null);
        visualdevService.create(visualdevEntity);
        return ActionResult.success("导入成功");
    }
}
