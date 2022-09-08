package jnpf.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import jnpf.base.ActionResult;
import jnpf.base.NoDataSourceBind;
import jnpf.base.Page;
import jnpf.base.vo.ListVO;
import jnpf.config.ConfigValueUtil;

import jnpf.enums.FilePreviewTypeEnum;
import jnpf.database.exception.DataException;

import jnpf.model.YozoFileParams;
import jnpf.model.YozoParams;
import jnpf.model.documentpreview.FileListVO;
import jnpf.model.FileModel;
import jnpf.util.DownUtil;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;;
import jnpf.util.enums.FileTypeEnum;
import jnpf.util.file.UploadUtil;
import jnpf.utils.SplicingUrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文档在线预览
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@NoDataSourceBind()
@Api(tags = "文档在线预览", value = "DocumentPreview")
@RestController
@RequestMapping("/api/extend/DocumentPreview")
public class DocumentPreviewController {

    @Autowired
    private ConfigValueUtil configValueUtil;

    /**
     * 列表
     *
     * @param page
     * @return
     */
    @ApiOperation("获取文档列表")
    @GetMapping
    public ActionResult<ListVO<FileListVO>> list(Page page) {
        String path = configValueUtil.getDocumentPreviewPath();
        List<FileModel> data = UploadUtil.getFileList(configValueUtil.getFileType(), FileTypeEnum.DOCUMENTPREVIEWPATH, path, page.getKeyword(), true);
        List<FileListVO> list = JsonUtil.getJsonToList(data, FileListVO.class);
        return ActionResult.success(list);
    }

    /**
     * 文档预览
     *
     * @param fileId 文件标记
     * @return
     */
    @ApiOperation("文档在线预览")
    @GetMapping("/{fileId}/Preview")
    public ActionResult list(@PathVariable("fileId") Integer fileId, YozoFileParams params, String previewType) throws UnsupportedEncodingException {
        File file = null;
        String filePath = configValueUtil.getDocumentPreviewPath();
        if (configValueUtil.getFileType().equals("minio")){
            //下载到本地
            List<FileModel> fileList = UploadUtil.getFileList(configValueUtil.getFileType(), FileTypeEnum.DOCUMENTPREVIEWPATH, filePath, null, false);
            if (fileList.get(fileId) == null){
                return ActionResult.fail("文件找不到!");
            }
            String url = YozoParams.JNPF_DOMAINS + "/api/extend/DocumentPreview/down/" + fileList.get(fileId).getFileName();
            String urlPath;
            if (previewType.equals(FilePreviewTypeEnum.YOZO_ONLINE_PREVIEW.getType())){
                params.setUrl(url);
                urlPath = SplicingUrlUtil.getPreviewUrl(params);
                return ActionResult.success("success",urlPath);
            }
            if(previewType.equals(FilePreviewTypeEnum.LOCAL_PREVIEW.getType())){
                return ActionResult.success("success",url);
            }

        }else {
            File filePaths = new File(filePath);
            List<File> files = FileUtil.getFile(filePaths);
            if (fileId > files.size()) {
                return ActionResult.fail("文件找不到!");
            }
            file = files.get(fileId);
            if (file != null) {
                String url = YozoParams.JNPF_DOMAINS + "/api/extend/DocumentPreview/down/" + file.getName();
                String urlPath;
                if (previewType.equals(FilePreviewTypeEnum.YOZO_ONLINE_PREVIEW.getType())){
                    params.setUrl(url);
                    urlPath = SplicingUrlUtil.getPreviewUrl(params);
                    return ActionResult.success("success",urlPath);
                }
                if(previewType.equals(FilePreviewTypeEnum.LOCAL_PREVIEW.getType())){
                    return ActionResult.success("success",url);
                }
            }
        }
        return ActionResult.fail("文件找不到!");
    }

    /**
     * 下载文件
     *
     * @param fileName
     */
    @NoDataSourceBind
    @GetMapping("/down/{fileName}")
    public void down(@PathVariable("fileName") String fileName) throws DataException {
        String filePath = configValueUtil.getDocumentPreviewPath();
        //下载文件
        List<FileModel> fileList = UploadUtil.getFileList(configValueUtil.getFileType(), FileTypeEnum.DOCUMENTPREVIEWPATH, filePath, null, false);
        //判断文件是否存在
        List<FileModel> collect = fileList.stream().filter(t -> fileName.equals(t.getFileName())).collect(Collectors.toList());
        if (collect.size() > 0){
            UploadUtil.streamToDown(configValueUtil.getFileType(), fileName, FileTypeEnum.DOCUMENTPREVIEWPATH, filePath);
        }
        filePath += fileName;
        if (FileUtil.fileIsFile(filePath)) {
            if (!DownUtil.dowloadFile(filePath, fileName)) {
                throw new DataException("下载失败");
            }
        }
    }

}
