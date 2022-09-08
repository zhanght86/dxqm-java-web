package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.NoDataSourceBind;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.config.ConfigValueUtil;
import jnpf.entity.VisualCategoryEntity;
import jnpf.entity.VisualConfigEntity;
import jnpf.entity.VisualEntity;
import jnpf.enums.VisualImgEnum;
import jnpf.database.exception.DataException;
import jnpf.model.VisualPageVO;
import jnpf.model.visual.*;
import jnpf.model.visualcategory.VisualCategoryListVO;
import jnpf.model.visualconfig.VisualConfigInfoModel;
import jnpf.model.ImageVO;
import jnpf.service.VisualCategoryService;
import jnpf.service.VisualConfigService;
import jnpf.service.VisualService;
import jnpf.util.*;
import jnpf.util.enums.FileTypeEnum;
import jnpf.util.file.FileExport;
import jnpf.util.file.StorageType;
import jnpf.util.file.UploadUtil;
import lombok.extern.slf4j.Slf4j;
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
 * 大屏基本信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Slf4j
@RestController
@Api(tags = "大屏基本信息", value = "visual")
@RequestMapping("/api/blade-visual/visual")
public class VisualController {

    @Autowired
    private FileExport fileExport;
    @Autowired
    private VisualService visualService;
    @Autowired
    private VisualConfigService configService;
    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private VisualCategoryService categoryService;

    /**
     * 分页
     *
     * @param pagination
     * @return
     */
    @ApiOperation("分页")
    @GetMapping("/list")
    public ActionResult<PageListVO<VisualListVO>> list(VisualPaginationModel pagination) {
        List<VisualEntity> data = visualService.getList(pagination);
        List<VisualListVO> list = JsonUtil.getJsonToList(data, VisualListVO.class);
        VisualPageVO paginationVO = JsonUtil.getJsonToBean(pagination, VisualPageVO.class);
        paginationVO.setRecords(list);
        return ActionResult.success(paginationVO);
    }

    /**
     * 详情
     *
     * @param id
     * @return
     */
    @ApiOperation("详情")
    @GetMapping("/detail")
    public ActionResult<VisualInfoVO> info(String id) {
        VisualEntity visual = visualService.getInfo(id);
        VisualConfigEntity config = configService.getInfo(id);
        VisualInfoVO vo = new VisualInfoVO();
        vo.setVisual(JsonUtil.getJsonToBean(visual, VisualInfoModel.class));
        vo.setConfig(JsonUtil.getJsonToBean(config, VisualConfigInfoModel.class));
        return ActionResult.success(vo);
    }

    /**
     * 新增
     *
     * @param visualCrform
     * @return
     */
    @ApiOperation("新增")
    @PostMapping("/save")
    public ActionResult create(@RequestBody @Valid VisualCrform visualCrform) {
        VisualEntity visual = JsonUtil.getJsonToBean(visualCrform.getVisual(), VisualEntity.class);
        visual.setBackgroundUrl(VisusalImgUrl.url + "bg/bg1.png");
        VisualConfigEntity config = JsonUtil.getJsonToBean(visualCrform.getConfig(), VisualConfigEntity.class);
        visualService.create(visual, config);
        Map<String, String> data = new HashMap<>(16);
        data.put("id", visual.getId());
        return ActionResult.success(data);
    }

    /**
     * 修改
     *
     * @param categoryUpForm
     * @return
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    public ActionResult update(@RequestBody VisualUpform categoryUpForm) {
        VisualEntity visual = JsonUtil.getJsonToBean(categoryUpForm.getVisual(), VisualEntity.class);
        VisualConfigEntity config = JsonUtil.getJsonToBean(categoryUpForm.getConfig(), VisualConfigEntity.class);
        visualService.update(visual.getId(), visual, config);
        return ActionResult.success("更新成功");
    }

    /**
     * 删除
     *
     * @param ids
     * @return
     */
    @ApiOperation("删除")
    @PostMapping("/remove")
    public ActionResult delete(String ids) {
        VisualEntity entity = visualService.getInfo(ids);
        if (entity != null) {
            visualService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }

    /**
     * 复制
     *
     * @param id
     * @return
     */
    @ApiOperation("复制")
    @PostMapping("/copy")
    public ActionResult copy(String id) {
        VisualEntity entity = visualService.getInfo(id);
        VisualConfigEntity config = configService.getInfo(id);
        if (entity != null) {
            entity.setTitle(entity.getTitle() + "_复制");
            visualService.create(entity, config);
            return ActionResult.success("复制成功");
        }
        return ActionResult.fail("复制失败");
    }

    /**
     * 获取类型
     *
     * @return
     */
    @ApiOperation("获取类型")
    @GetMapping("/category")
    public ActionResult<VisualCategoryListVO> list() {
        List<VisualCategoryEntity> data = categoryService.getList();
        List<VisualCategoryListVO> list = JsonUtil.getJsonToList(data, VisualCategoryListVO.class);
        return ActionResult.success(list);
    }

    /**
     * 上传文件
     *
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("上传文件")
    @PostMapping(value = "/put-file/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult file(@RequestPart("file") MultipartFile file, @PathVariable("type") String type) {
        ImageVO vo = new ImageVO();
        VisualImgEnum imgEnum = VisualImgEnum.getByMessage(type);
        if (imgEnum != null) {
            String path = imgEnum.getMessage();
            String filePath = configValueUtil.getBiVisualPath() + path;
            String name = RandomUtil.uuId() + "." + UpUtil.getFileType(file);
            String fileName = name;
            if (StorageType.MINIO.equals(configValueUtil.getFileType())) {
                fileName = "/" + type + "/" + fileName;
            }
            //上传文件
            UploadUtil.uploadFile(configValueUtil.getFileType(), FileTypeEnum.BIVISUALPATH, fileName, file, filePath);
            vo.setOriginalName(file.getOriginalFilename());
            vo.setLink(VisusalImgUrl.url + path + "/" + name);
            vo.setName(VisusalImgUrl.url + path + "/" + name);
        }
        return ActionResult.success(vo);
    }

    /**
     * 获取图片列表
     *
     * @param type 哪个文件夹
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("获取图片列表")
    @GetMapping("/{type}")
    public ActionResult<ImageVO> getFile(@PathVariable("type") String type) {
        List<ImageVO> vo = new ArrayList<>();
        VisualImgEnum imgEnum = VisualImgEnum.getByMessage(type);
        if (imgEnum != null) {
            String path = configValueUtil.getBiVisualPath() + imgEnum.getMessage();
            vo = UploadUtil.getFileList(configValueUtil.getFileType(), FileTypeEnum.BIVISUALPATH, path, type);
        }
        return ActionResult.success(vo);
    }

    /**
     * 大屏下拉框
     *
     * @return
     */
    @ApiOperation("大屏下拉框")
    @GetMapping("/Selector")
    public ActionResult<VisualSelectorVO> selector() {
        List<VisualEntity> visualList = visualService.getList();
        List<VisualCategoryEntity> categoryList = categoryService.getList();
        List<VisualSelectorVO> listVos = new ArrayList<>();
        for (VisualCategoryEntity category : categoryList) {
            VisualSelectorVO categoryModel = new VisualSelectorVO();
            categoryModel.setId(category.getCategoryvalue());
            categoryModel.setFullName(category.getCategorykey());
            List<VisualEntity> visualAll = visualList.stream().filter(t -> t.getCategory().equals(category.getCategoryvalue())).collect(Collectors.toList());
            if (visualAll.size() > 0) {
                List<VisualSelectorVO> childList = new ArrayList<>();
                for (VisualEntity visual : visualAll) {
                    VisualSelectorVO visualModel = new VisualSelectorVO();
                    visualModel.setId(visual.getId());
                    visualModel.setFullName(visual.getTitle());
                    visualModel.setChildren(null);
                    visualModel.setHasChildren(false);
                    childList.add(visualModel);
                }
                categoryModel.setHasChildren(true);
                categoryModel.setChildren(childList);
                listVos.add(categoryModel);
            }
        }
        ListVO vo = new ListVO();
        vo.setList(listVos);
        return ActionResult.success(vo);
    }

    @ApiOperation("大屏导出")
    @PostMapping("/{id}/Actions/ExportData")
    public ActionResult exportData(@PathVariable("id") String id) {
        VisualEntity entity = visualService.getInfo(id);
        VisualConfigEntity configEntity = configService.getInfo(id);
        VisualModel model = new VisualModel();
        model.setEntity(entity);
        model.setConfigEntity(configEntity);
        DownloadVO downloadVO = fileExport.exportFile(model, configValueUtil.getTemporaryFilePath());
        return ActionResult.success(downloadVO);
    }

    @ApiOperation("大屏导入")
    @PostMapping(value = "/Model/Actions/ImportData", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult ImportData(@RequestPart("file") MultipartFile multipartFile) throws DataException {
        //判断是否为.json结尾
        if (FileUtil.existsSuffix(multipartFile)) {
            return ActionResult.fail("导入文件格式错误");
        }
        //获取文件内容
        String fileContent = FileUtil.getFileContent(multipartFile, configValueUtil.getTemporaryFilePath());
        VisualModel vo = JsonUtil.getJsonToBean(fileContent, VisualModel.class);
        visualService.createInport(vo.getEntity(),vo.getConfigEntity());
        return ActionResult.success("success");
    }

}
