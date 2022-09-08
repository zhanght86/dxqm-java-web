package jnpf.base.util;

import jnpf.config.ConfigValueUtil;
import jnpf.model.FileModel;
import jnpf.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Component
public class FileManageUtil {

    @Autowired
    private ConfigValueUtil configValueUtil;

    // 添加附件：将临时文件夹的文件拷贝到正式文件夹里面

    /**
     * 添加附件：将临时文件夹的文件拷贝到正式文件夹里面
     * @param data list集合
     */
    public void createFile(List<FileModel> data) {
        if (data != null && data.size() > 0) {
            String temporaryFilePath = configValueUtil.getTemporaryFilePath();
            String systemFilePath = configValueUtil.getSystemFilePath();
            for (FileModel item : data) {
                FileUtil.copyFile(temporaryFilePath + item.getFileId(), systemFilePath + item.getFileId());
            }
        }
    }

    /**
     * 更新附件
     * @param data list集合
     */
    public void updateFile(List<FileModel> data) {
        if (data != null && data.size() > 0) {
            String temporaryFilePath = configValueUtil.getTemporaryFilePath();
            String systemFilePath = configValueUtil.getSystemFilePath();
            for (FileModel item : data) {
                if ("add".equals(item.getFileType())) {
                    FileUtil.copyFile(temporaryFilePath + item.getFileId(), systemFilePath + item.getFileId());
                } else if ("delete".equals(item.getFileType())) {
                    FileUtil.deleteFile(systemFilePath + item.getFileId());
                }
            }
        }
    }

    /**
     * 删除附件
     * @param data list集合
     */
    public void deleteFile(List<FileModel> data) {
        if (data != null && data.size() > 0) {
            String systemFilePath = configValueUtil.getSystemFilePath();
            for (FileModel item : data) {
                FileUtil.deleteFile(systemFilePath + item.getFileId());
            }
        }
    }
}
