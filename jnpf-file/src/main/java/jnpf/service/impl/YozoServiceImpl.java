package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jnpf.entity.FileEntity;
import jnpf.base.ActionResult;
import jnpf.base.vo.PaginationVO;
import jnpf.mapper.FileMapper;
import jnpf.model.YozoFileParams;
import jnpf.model.YozoParams;
import jnpf.service.YozoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/5/13
 */
@Service
public class YozoServiceImpl implements YozoService {

    @Autowired
    private FileMapper fileMapper;

    @Override
    public String getPreviewUrl(YozoFileParams params) {
        StringBuilder paramsUrl = new StringBuilder();
        if (!StringUtils.isEmpty(params.getNoCache())) {
            paramsUrl.append("&noCache=" + params.getNoCache());
        }
        if (!StringUtils.isEmpty(params.getWatermark())) {
            paramsUrl.append("&watermark=" + params.getWatermark());
        }
        if (!StringUtils.isEmpty(params.getIsCopy())) {
            paramsUrl.append("&isCopy=" + params.getIsCopy());
        }
        if (!StringUtils.isEmpty(params.getPageStart())) {
            paramsUrl.append("&pageStart=" + params.getPageStart());
        }
        if (!StringUtils.isEmpty(params.getPageEnd())) {
            paramsUrl.append("&pageEnd=" + params.getPageEnd());
        }
        if (!StringUtils.isEmpty(params.getType())) {
            paramsUrl.append("&type=" + params.getType());
        }
        String s = paramsUrl.toString();
        String previewUrl = YozoParams.DOMAIN + "?k=" + YozoParams.DOMAIN_KEY + "&url=" + params.getUrl() + s;
        return previewUrl;
    }

    @Override
    public ActionResult saveFileId(String fileVersionId, String fileId, String fileName) {

        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileId(fileId);
        fileEntity.setFileName(fileName);
        fileEntity.setFileVersionId(fileVersionId);
        fileEntity.setType("create");

        fileMapper.insert(fileEntity);

        return ActionResult.success("新建文档成功");
    }

    @Override
    public FileEntity selectByName(String fileNa) {
        QueryWrapper<FileEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("F_FileName", fileNa);
        FileEntity fileEntity = fileMapper.selectOne(wrapper);
        return fileEntity;
    }

    @Override
    public ActionResult saveFileIdByHttp(String fileVersionId, String fileId, String fileUrl) {
        String fileName = "";
        String url = "";
        String name = "";
        try {
            url = URLDecoder.decode(fileUrl, "UTF-8");
            if (url.contains("/")) {
                fileName = url.substring(url.lastIndexOf("/") + 1);
            } else {
                fileName = url.substring(url.lastIndexOf("\\") + 1);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //同一url文件数
        QueryWrapper<FileEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("F_Url", url);
        Integer total = fileMapper.selectCount(wrapper);
        if (total == 0) {
            name = fileName;
        } else {
            String t = total.toString();
            name = fileName + "(" + t + ")";
        }
        FileEntity fileEntity = new FileEntity();
        fileEntity.setType(url.contains("http") ? "http" : "local");
        fileEntity.setFileVersionId(fileVersionId);
        fileEntity.setFileId(fileId);
        fileEntity.setFileName(name);
        fileEntity.setUrl(url);
        fileMapper.insert(fileEntity);
        return ActionResult.success("新建文档成功");
    }

    @Override
    public ActionResult deleteFileByVersionId(String versionId) {
        QueryWrapper<FileEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("F_FileVersion", versionId);
        int i = fileMapper.delete(wrapper);
        if (i == 1) {
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败");
    }

    @Override
    public FileEntity selectByVersionId(String fileVersionId) {
        QueryWrapper<FileEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("F_FileVersion", fileVersionId);
        FileEntity fileEntity = fileMapper.selectOne(wrapper);
        return fileEntity;
    }

    @Override
    public ActionResult deleteBatch(String[] versions) {
        for (String version : versions) {
            QueryWrapper<FileEntity> wrapper = new QueryWrapper<>();
            wrapper.eq("F_FileVersion", version);
            int i = fileMapper.delete(wrapper);
            if (i == 0) {
                return ActionResult.fail("删除文件:" + version + "失败");
            }
        }
        return ActionResult.success("删除成功");

    }

    @Override
    public void editFileVersion(String oldFileId, String newFileId) {
        UpdateWrapper<FileEntity> wrapper = new UpdateWrapper<>();
        wrapper.eq("F_FileVersion", oldFileId);
        FileEntity fileEntity = new FileEntity();
        fileEntity.setFileVersionId(newFileId);
        fileEntity.setOldFileVersionId(oldFileId);
        fileMapper.update(fileEntity, wrapper);
    }

    @Override
    public List<FileEntity> getAllList(PaginationVO pageModel) {
        Page page = new Page(pageModel.getCurrentPage(), pageModel.getPageSize());
        IPage<FileEntity> iPage = fileMapper.selectPage(page, null);
        return iPage.getRecords();
    }

}
