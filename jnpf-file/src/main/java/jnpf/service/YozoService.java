package jnpf.service;

import jnpf.entity.FileEntity;
import jnpf.base.ActionResult;
import jnpf.base.vo.PaginationVO;

import jnpf.model.YozoFileParams;
import org.springframework.stereotype.Service;

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
public interface YozoService {
    /**
     * 生成文件预览url
     * @param params
     * @return
     */
    String getPreviewUrl(YozoFileParams params);

    /**
     * 新建文档保存versionId
     * @param fileVersionId
     * @param fileId
     * @param fileName
     * @return
     */
    ActionResult saveFileId(String fileVersionId, String fileId, String fileName);

    /**
     * 根据文件名查询
     * @param fileNa
     * @return
     */
    FileEntity selectByName(String fileNa);

    /**
     * 上传文件到永中
     * @param fileVersionId
     * @param fileId
     * @param fileUrl
     * @return
     */
    ActionResult saveFileIdByHttp(String fileVersionId, String fileId, String fileUrl);

    /**
     * 删除文件
     * @param versionId
     * @return
     */
    ActionResult deleteFileByVersionId(String versionId);

    /**
     * 根据versionId查询文件
     * @param fileVersionId
     * @return
     */
    FileEntity selectByVersionId(String fileVersionId);

    /**
     * 批量删除
     * @param versions
     * @return
     */
    ActionResult deleteBatch(String[] versions);

    /**
     * 更新versionId
     * @param oldFileId
     * @param newFileId
     */
    void editFileVersion(String oldFileId, String newFileId);

    List<FileEntity> getAllList(PaginationVO pageModel);
}
