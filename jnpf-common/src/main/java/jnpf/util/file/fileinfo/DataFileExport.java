package jnpf.util.file.fileinfo;

import jnpf.base.vo.DownloadVO;
import jnpf.config.ConfigValueUtil;
import jnpf.util.*;
import jnpf.util.enums.FileTypeEnum;
import jnpf.util.file.FileExport;
import jnpf.util.file.UploadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 数据接口文件导入导出
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-04
 */
@Component
@Slf4j
public class DataFileExport implements FileExport {
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;

    @Override
    public DownloadVO exportFile(Object obj, String filePath) {
        /** 1.model拼凑成Json字符串 */
        String json = JsonUtil.getObjectToString(obj);
        /** 2.写入到文件中 */
        String fileName = RandomUtil.uuId() + ".json";
        FileUtil.createFile(filePath, fileName);
        FileUtil.writeToFile(json, filePath, fileName);
        /** 是否需要上产到minio */
        try {
            UploadUtil.uploadFile(configValueUtil.getFileType(), filePath + fileName, FileTypeEnum.EXPORT, fileName);
        } catch (IOException e) {
            log.error("上传文件失败，错误" + e.getMessage());
        }
        /** 生成下载下载文件路径 */
        DownloadVO vo = DownloadVO.builder().name(fileName).url(UploaderUtil.uploaderFile(userProvider.get().getId() + "#" + fileName + "#" + "export")).build();
        return vo;
    }

}
