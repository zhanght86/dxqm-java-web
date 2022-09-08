package jnpf.controller;


import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.entity.FileEntity;
import jnpf.base.ActionResult;
import jnpf.base.vo.PaginationVO;
import jnpf.config.ConfigValueUtil;
import jnpf.database.exception.DataException;
import jnpf.model.FileForm;
import jnpf.model.YozoFileParams;
import jnpf.model.YozoParams;
import jnpf.service.YozoService;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.wxutil.HttpUtil;
import jnpf.utils.YozoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

;

/**
 * @author JNPF开发平台组
 */
@RestController
@RequestMapping
@Api(tags = "在线文档预览",value = "文件在线预览")
public class YozoFileController {

    @Autowired
    private YozoService yozoService;

    @Autowired
    private YozoUtils yozoUtil;

    @Autowired
    private ConfigValueUtil configValueUtil;

    @PostMapping("/api/file/getViewUrlWebPath")
    @ApiOperation("文档预览")
    public ActionResult getUrl(YozoFileParams params) {
        String previewUrl = yozoService.getPreviewUrl(params);
        return ActionResult.success("success",previewUrl);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    @ApiOperation("上传本地文件")
    public ActionResult upload (@RequestPart("multipartFile") MultipartFile file) throws IOException, DataException {
        //获取签名
        Map<String, String[]> parameter = new HashMap<String, String[]>();
        parameter.put("appId",new String[]{""});
        String sign = yozoUtil.generateSign(YozoParams.APP_ID, YozoParams.APP_KEY,parameter).getData();
        String resultString="";

        String url = YozoParams.CLOUD_DOMAIN + "/api/file/upload" ;

        //统一上传到本地目录
        String fileName = file.getOriginalFilename();
        String filePath =  configValueUtil.getDocumentPreviewPath();
        String path = filePath  + fileName;
        FileUtil.upFile(file,filePath,fileName);

        //文件流
        File fileUp = new File(path);
        resultString= yozoUtil.uploadFile(url,fileUp, YozoParams.APP_ID,sign);

        JSONObject.parseObject(resultString);
        Map<String,Object> maps= JSONObject.parseObject(resultString,Map.class);
        Map<String,String> fileMap= (Map<String, String>) maps.get("data");
        String fileVersionId = fileMap.get("fileVersionId");
        String fileId = fileMap.get("fileId");
        ActionResult back = yozoService.saveFileIdByHttp(fileVersionId,fileId,path);
        return back;
    }

    @GetMapping("/newCreate")
    @ApiOperation("新建文件")
    public ActionResult newCreate(String fileName, String templateType){
        String fileNa = yozoUtil.getFileName(fileName, templateType);
        if (fileNa==null){
            return ActionResult.fail("请输入正确的文件格式");
        }
        //判断文件是否创建过
        FileEntity fileEntity =yozoService.selectByName(fileNa);
        if (fileEntity!=null){
            return ActionResult.fail("存在同名文件！");
        }
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("templateType",new String[]{templateType});
        params.put("fileName",new String[]{fileName});
        String sign = yozoUtil.generateSign(YozoParams.APP_ID, YozoParams.APP_KEY,params).getData();
        String url = YozoParams.CLOUD_DOMAIN + "/api/file/template?templateType=" + templateType +
                "&fileName=" + fileName +
                "&appId=" +  YozoParams.APP_ID +
                "&sign=" + sign;
        String s = HttpUtil.sendHttpPost(url);
        Map<String,Object> maps= JSONObject.parseObject(s,Map.class);
        Map<String,String> fileMap= (Map<String, String>) maps.get("data");
        String fileVersionId = fileMap.get("fileVersionId");
        String fileId = fileMap.get("fileId");
        ActionResult back = yozoService.saveFileId(fileVersionId,fileId,fileNa);
        //在本地新建文件
        FileUtil.createFile(configValueUtil.getDocumentPreviewPath(),fileNa);
        return back;
    }

    @GetMapping("/uploadByHttp")
    @ApiOperation("http上传文件")
    public ActionResult uploadByHttp(String fileUrl) {
        //获取签名
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("fileUrl",new String[]{fileUrl});
        String sign = yozoUtil.generateSign( YozoParams.APP_ID, YozoParams.APP_KEY,params).getData();
        String url = YozoParams.CLOUD_DOMAIN + "/api/file/http?fileUrl=" + fileUrl +
                "&appId=" +  YozoParams.APP_ID +
                "&sign=" + sign;
        String s = HttpUtil.sendHttpPost(url);
        Map<String,Object> maps= JSONObject.parseObject(s,Map.class);
        Map<String,String> fileMap= (Map<String, String>) maps.get("data");
        String fileVersionId = fileMap.get("fileVersionId");
        String fileId = fileMap.get("fileId");
        ActionResult back = yozoService.saveFileIdByHttp(fileVersionId,fileId,fileUrl);
        return back;
    }

    @GetMapping("/downloadFile")
    @ApiOperation("永中下载文件")
    public String downloadFile(String fileVersionId){
        FileEntity fileEntity= yozoService.selectByVersionId(fileVersionId);
        if (fileEntity==null){
            return "不存在该文件";
        }
        //获取签名
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("fileVersionId",new String[]{fileVersionId});
        String sign = yozoUtil.generateSign( YozoParams.APP_ID, YozoParams.APP_KEY,params).getData();
        String url = YozoParams.CLOUD_DOMAIN + "/api/file/download?fileVersionId=" +fileVersionId+
                "&appId=" +  YozoParams.APP_ID +
                "&sign=" + sign;
        return url;
    }


    @GetMapping("/deleteVersionFile")
    @ApiOperation("删除文件版本")
    public ActionResult deleteVersion(String fileVersionId){
        //获取签名
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("fileVersionId",new String[]{fileVersionId});
        String sign = yozoUtil.generateSign( YozoParams.APP_ID, YozoParams.APP_KEY,params).getData();
        String url = YozoParams.CLOUD_DOMAIN + "/api/file/delete/version?fileVersionId=" +fileVersionId+
                "&appId=" +  YozoParams.APP_ID +
                "&sign=" + sign;
        String s = HttpUtil.sendHttpGet(url);
        Map<String,Object> maps= JSONObject.parseObject(s,Map.class);
        String fileName = yozoService.selectByVersionId(fileVersionId).getFileName();
        String path = configValueUtil.getDocumentPreviewPath()+fileName;
        if (FileUtil.fileIsFile(path)){
            File file =new File(path);
            file.delete();
        }
        String versionId=(String)maps.get("data");
        ActionResult back = yozoService.deleteFileByVersionId(versionId);
        return back;
    }

    @GetMapping("/batchDelete")
    @ApiOperation("批量删除文件版本")
    public ActionResult batchDelete(String[] fileVersionIds){
        //获取签名
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("fileVersionIds",fileVersionIds);
        String sign = yozoUtil.generateSign( YozoParams.APP_ID, YozoParams.APP_KEY,params).getData();

        StringBuilder fileVersionIdList=new StringBuilder();
        for(String s : fileVersionIds){
            String fileName = yozoService.selectByVersionId(s).getFileName();
            String path = configValueUtil.getDocumentPreviewPath() + fileName;
            File file =new File(path);
            file.delete();
            fileVersionIdList.append("fileVersionIds="+ s + "&");
        }
        String list =fileVersionIdList.toString();
        String url = YozoParams.CLOUD_DOMAIN + "/api/file/delete/versions?" +list+
                "appId=" +  YozoParams.APP_ID +
                "&sign=" +  sign;
        String s = HttpUtil.sendHttpGet(url);
        ActionResult back=yozoService.deleteBatch(fileVersionIds);
        return back;
    }

    @GetMapping("/editFile")
    @ApiOperation("在线编辑")
    public ActionResult editFile (String fileVersionId){
        //获取签名
        Map<String, String[]> params = new HashMap<String, String[]>();
        params.put("fileVersionId",new String[]{fileVersionId});
        String sign = yozoUtil.generateSign( YozoParams.APP_ID, YozoParams.APP_KEY,params).getData();
        String url = YozoParams.EDIT_DOMAIN + "/api/edit/file?fileVersionId=" +fileVersionId+
                "&appId=" +  YozoParams.APP_ID +
                "&sign=" + sign;
        return ActionResult.success("success",url);
    }

    /**
     * 永中回调
     * @param oldFileId
     * @param newFileId
     * @param message
     * @param errorCode
     * @return
     */
    @PostMapping("/3rd/edit/callBack")
    public Map<String,Object> editCallBack(String oldFileId,String newFileId,String message,Integer errorCode){

        yozoService.editFileVersion(oldFileId,newFileId);

        Map<String,Object> result =new HashMap<>();
        result.put("oldFileId",oldFileId);
        result.put("newFileId",newFileId);
        result.put("message",message);
        result.put("errorCode",errorCode);
        return result;
    }

    @PostMapping("/documentList")
    @ApiOperation("文档列表")
    public ActionResult documentList(@RequestBody PaginationVO pageModel){
        List<FileEntity> list = yozoService.getAllList(pageModel);
        List<FileForm> listVo = JsonUtil.getJsonToList(list, FileForm.class);
        return ActionResult.page(listVo,pageModel);
    }

    /**
     * 传入新的fileVersionId同步
     * @param fileVersionId
     * @return
     * @throws Exception
     */
    @GetMapping("/updateFile")
    @ApiOperation("/同步文件版本到本地")
    public ActionResult updateFile(String fileVersionId) throws Exception {
        FileEntity fileEntity = yozoService.selectByVersionId(fileVersionId);
        String fileName = fileEntity.getFileName();
        String path = configValueUtil.getDocumentPreviewPath() + fileName;
        if(FileUtil.fileIsFile(path)){
            File file =new File(path);
            file.delete();
        }
        String fileUrl= this.downloadFile(fileVersionId);
        yozoUtil.downloadFile(fileUrl,path);
        return ActionResult.success("更新完毕");
    }
}
