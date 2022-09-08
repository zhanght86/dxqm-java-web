package jnpf.model.document;

import lombok.Data;

import java.util.Date;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:58
 */
@Data
public class DocumentSuserListVO {
    private String id;
    private String documentId;
    private String shareUserId;
    private Date shareTime;
}
