package jnpf.model.document;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 10:28
 */
@Data
public class DocumentUploader {
    private String parentId;
    private MultipartFile file;
}
