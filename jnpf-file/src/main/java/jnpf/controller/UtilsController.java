package jnpf.controller;


import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.NoDataSourceBind;
import jnpf.base.UserInfo;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.util.OptimizeUtil;
import jnpf.base.vo.DownloadVO;
import jnpf.base.vo.ListVO;
import jnpf.config.ConfigValueUtil;
import jnpf.database.exception.DataException;
import jnpf.model.LanguageVO;
import jnpf.model.UploaderVO;
import jnpf.util.*;
import jnpf.util.enums.FileTypeEnum;
import jnpf.util.file.StorageType;
import jnpf.util.file.UploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

/**
 * 通用控制器
 *
 * @author JNPF开发平台组
 * @version V1.2.191207
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Api(tags = "公共", value = "file")
@RestController
@RequestMapping("/api/file")
public class UtilsController {

    @Autowired
    private ConfigValueUtil configValueUtil;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private DictionaryDataService dictionaryDataService;

    /**
     * 语言列表
     *
     * @return
     */
    @ApiOperation("语言列表")
    @GetMapping("/Language")
    public ActionResult<ListVO<LanguageVO>> getList() {
        String dictionaryTypeId = "dc6b2542d94b407cac61ec1d59592901";
        List<DictionaryDataEntity> list = dictionaryDataService.getList(dictionaryTypeId);
        List<LanguageVO> language = JsonUtil.getJsonToList(list, LanguageVO.class);
        ListVO vo = new ListVO();
        vo.setList(language);
        return ActionResult.success(vo);
    }

    /**
     * 图形验证码
     *
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("图形验证码")
    @GetMapping("/ImageCode/{timestamp}")
    public void imageCode(@PathVariable("timestamp") String timestamp) {
        DownUtil.downCode();
        redisUtil.insert(timestamp, ServletUtil.getSession().getAttribute(CodeUtil.RANDOMCODEKEY), 120);
    }

    /**
     * 上传文件/图片
     *
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("上传文件/图片")
    @PostMapping(value = "/Uploader/{type}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ActionResult uploader(@RequestPart("file") MultipartFile file, @PathVariable("type") String type) {
        String fileType = UpUtil.getFileType(file);
        //验证类型
        if (type.equalsIgnoreCase(FileTypeEnum.WEIXIN)) {
            if (!OptimizeUtil.fileType(configValueUtil.getWeChatUploadFileType(), fileType) && !OptimizeUtil.fileType(configValueUtil.getMpUploadFileType(), fileType)) {
                return ActionResult.fail("上传失败，文件格式不允许上传");
            }
            if (OptimizeUtil.fileSize(file.getSize(), 1024000)) {
                return ActionResult.fail("上传失败，文件大小超过1M");
            }
        } else {
            if (!OptimizeUtil.fileType(configValueUtil.getAllowUploadFileType(), fileType)) {
                return ActionResult.fail("上传失败，文件格式不允许上传");
            }
            if (OptimizeUtil.fileSize(file.getSize(), 1024000)) {
                return ActionResult.fail("上传失败，文件大小超过1M");
            }
        }
        String fileName = DateUtil.dateNow("yyyyMMdd") + "_" + RandomUtil.uuId() + "." + fileType;
        if(type.equals(FileTypeEnum.MAIL)) {
            type = FileTypeEnum.TEMPORARY;
        }
        String filePath = getFilePath(type.toLowerCase());
        UploaderVO vo = UploaderVO.builder().name(fileName).build();
        //上传文件
        UploadUtil.uploadFile(configValueUtil.getFileType(), type, fileName, file, filePath);
        if (type.equalsIgnoreCase(FileTypeEnum.USERAVATAR)) {
            vo.setUrl(UploaderUtil.uploaderImg(fileName));
        } else if (type.equalsIgnoreCase(FileTypeEnum.ANNEX)) {
            UserInfo userInfo = userProvider.get();
            fileName = userInfo.getId() + "#" + fileName;
            vo.setUrl(UploaderUtil.uploaderFile("/api/file/Download/", userInfo.getId() + "#" + fileName));
        } else if (type.equalsIgnoreCase(FileTypeEnum.ANNEXPIC)) {
            vo.setUrl(UploaderUtil.uploaderImg("/api/file/Image/annex/", fileName));
        }
        return ActionResult.success(vo);
    }

    /**
     * 获取下载文件链接
     *
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("获取下载文件链接")
    @GetMapping("/Download/{type}/{fileName}")
    public ActionResult<DownloadVO> downloadUrl(@PathVariable("type") String type, @PathVariable("fileName") String fileName) {
        UserInfo userInfo = userProvider.get();
        if (!configValueUtil.getFileType().equals(StorageType.STORAGE)) {
            DownloadVO vo = DownloadVO.builder().name(fileName).url(UploaderUtil.uploaderFile(userInfo.getId() + "#" + fileName + "#" + type)).build();
            return ActionResult.success(vo);
        }
        String filePath = getFilePath(type) + fileName;
        if (FileUtil.fileIsFile(filePath)) {
            DownloadVO vo = DownloadVO.builder().name(fileName).url(UploaderUtil.uploaderFile(userInfo.getId() + "#" + fileName + "#" + type)).build();
            return ActionResult.success(vo);
        }
        return ActionResult.fail("文件不存在");
    }

    /**
     * 下载文件链接
     *
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("下载文件链接")
    @GetMapping("/Download")
    public void downloadFile() throws DataException {
        HttpServletRequest request = ServletUtil.getRequest();
        String reqJson = request.getParameter("encryption");
        String fileNameAll = DesUtil.aesDecode(reqJson);
        if (!StringUtil.isEmpty(fileNameAll)) {
            String[] data = fileNameAll.split("#");
            String token = data.length > 0 ? data[0] : "";
            //验证token
            if (redisUtil.exists(token)) {
                String fileName = data.length > 1 ? data[1] : "";
                String type = data.length > 2 ? data[2] : "";
                String filePath = getFilePath(type.toLowerCase());
                //下载文件
                UploadUtil.downFile(configValueUtil.getFileType(), fileName, type, filePath);
            } else {
                throw new DataException("token验证失败");
            }
        }
    }

    /**
     * 下载文件链接
     *
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("下载模板文件链接")
    @GetMapping("/DownloadModel")
    public void downloadModel() throws DataException {
        HttpServletRequest request = ServletUtil.getRequest();
        String reqJson = request.getParameter("encryption");
        String fileNameAll = DesUtil.aesDecode(reqJson);
        if (!StringUtil.isEmpty(fileNameAll)) {
            String token = fileNameAll.split("#")[0];
            if (redisUtil.exists(token)) {
                String fileName = fileNameAll.split("#")[1];
                String filePath = configValueUtil.getTemplateFilePath();
                //下载文件
                UploadUtil.downFile(configValueUtil.getFileType(), fileName, FileTypeEnum.TEMPLATEFILE, filePath);
            }
        }
    }


    /**
     * 获取图片
     *
     * @param fileName
     * @param type
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("获取图片")
    @GetMapping("/Image/{type}/{fileName}")
    public void downLoadImg(@PathVariable("type") String type, @PathVariable("fileName") String fileName) {
        String filePath = getFilePath(type.toLowerCase());
        if (FileTypeEnum.IM.equalsIgnoreCase(type)){
            type = "imfile";
        }else if (FileTypeEnum.ANNEX.equalsIgnoreCase(type)){
            type = FileTypeEnum.ANNEXPIC;
        }
        //下载文件
        UploadUtil.downFile(configValueUtil.getFileType(), fileName, type.toLowerCase(), filePath);
    }

    /**
     * 获取IM聊天图片
     * 注意 后缀名前端故意把 .替换@
     *
     * @param fileName
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("获取IM聊天图片")
    @GetMapping("/IMImage/{fileName}")
    public void imImage(@PathVariable("fileName") String fileName) {
        //下载文件
        UploadUtil.downFile(configValueUtil.getFileType(), fileName, "imfile", getFilePath(FileTypeEnum.IM)+fileName);
    }

    /**
     * 查看图片
     *
     * @param type     哪个文件夹
     * @param fileName 文件名称
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("查看图片")
    @GetMapping("/{type}/{fileName}")
    public void img(@PathVariable("type") String type, @PathVariable("fileName") String fileName) {
        String filePath = configValueUtil.getBiVisualPath() + type + File.separator;
        if (StorageType.MINIO.equals(configValueUtil.getFileType())) {
            fileName = "/" + type + "/" + fileName;
            filePath = configValueUtil.getBiVisualPath().substring(0, configValueUtil.getBiVisualPath().length() - 1);
        }
        //下载文件
        UploadUtil.downFile(configValueUtil.getFileType(), fileName, FileTypeEnum.BIVISUALPATH, filePath);
    }

    /**
     * 获取IM聊天语音
     * 注意 后缀名前端故意把 .替换@
     *
     * @param fileName
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("获取IM聊天语音")
    @GetMapping("/IMVoice/{fileName}")
    public void imVoice(@PathVariable("fileName") String fileName) {
        String paths = getFilePath(FileTypeEnum.IM) + fileName.replaceAll("@", ".");
        UploadUtil.downFile(configValueUtil.getFileType(), fileName, "imfile", paths);
    }

    /**
     * app启动获取信息
     *
     * @param appName
     * @return
     */
    @NoDataSourceBind()
    @ApiOperation("app启动获取信息")
    @GetMapping("/AppStartInfo/{appName}")
    public ActionResult getAppStartInfo(@PathVariable("appName") String appName) {
        JSONObject object = new JSONObject();
        object.put("AppVersion", configValueUtil.getAppVersion());
        object.put("AppUpdateContent", configValueUtil.getAppUpdateContent());
        return ActionResult.success(object);
    }

    //----------大屏图片下载---------
    @NoDataSourceBind()
    @ApiOperation("获取图片")
    @GetMapping("/VisusalImg/{type}/{fileName}")
    public void downVisusalImg(@PathVariable("type") String type, @PathVariable("fileName") String fileName) {
        String filePath = getFilePath(FileTypeEnum.BIVISUALPATH) + type + File.separator;
        if (StorageType.MINIO.equals(configValueUtil.getFileType())) {
            fileName = "/" + type + "/" + fileName;
        }
        UploadUtil.downFile(configValueUtil.getFileType(),fileName,FileTypeEnum.BIVISUALPATH,filePath);
    }

    //----------------------

    /**
     * 通过fileType获取文件夹名称
     *
     * @param fileType 文件类型
     * @return
     */
    private String getFilePath(String fileType) {
        String filePath = null;
        //获取文件保存路径
        switch (fileType.toLowerCase()) {
            //用户头像存储路径
            case FileTypeEnum.USERAVATAR:
                filePath = configValueUtil.getUserAvatarFilePath();
                break;
            //邮件文件存储路径
            case FileTypeEnum.MAIL:
                filePath = configValueUtil.getEmailFilePath();
                break;
            //前端附件文件目录
            case FileTypeEnum.ANNEX:
                filePath = configValueUtil.getWebAnnexFilePath();
                break;
            case FileTypeEnum.ANNEXPIC:
                filePath = configValueUtil.getWebAnnexFilePath();
                break;
            //IM聊天图片+语音存储路径
            case FileTypeEnum.IM:
                filePath = configValueUtil.getImContentFilePath();
                break;
            //临时文件存储路径
            case FileTypeEnum.WEIXIN:
                filePath = configValueUtil.getMpMaterialFilePath();
                break;
            //临时文件存储路径
            case FileTypeEnum.WORKFLOW:
                filePath = configValueUtil.getTemporaryFilePath();
                break;
            //文档管理存储路径
            case FileTypeEnum.DOCUMENT:
                filePath = configValueUtil.getDocumentFilePath();
                break;
            //数据库备份文件路径
            case FileTypeEnum.DATABACKUP:
                filePath = configValueUtil.getDataBackupFilePath();
                break;
            //临时文件存储路径
            case FileTypeEnum.TEMPORARY:
                filePath = configValueUtil.getTemporaryFilePath();
                break;
            //允许上传文件类型
            case FileTypeEnum.ALLOWUPLOADFILETYPE:
                filePath = configValueUtil.getAllowUploadFileType();
                break;
            //文件在线预览存储pdf
            case FileTypeEnum.DOCUMENTPREVIEWPATH:
                filePath = configValueUtil.getDocumentPreviewPath();
                break;
            //文件模板存储路径
            case FileTypeEnum.TEMPLATEFILE:
                filePath = configValueUtil.getTemplateFilePath();
                break;
            //前端文件目录
            case FileTypeEnum.SERVICEDIRECTORY:
                break;
            //后端文件目录
            case FileTypeEnum.WEBDIRECTORY:
                filePath = configValueUtil.getCodeAreasName();
                break;
            //大屏
            case FileTypeEnum.BIVISUALPATH:
                filePath = configValueUtil.getBiVisualPath();
                break;
            //导出
            case FileTypeEnum.EXPORT:
                filePath = configValueUtil.getTemporaryFilePath();
                break;
            default:
                break;
        }
        return filePath;
    }

}
