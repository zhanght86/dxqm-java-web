package jnpf.model.home;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class EmailVO {
    private String id;
    @JSONField(name="subject")
    private String fullName;
    private Long creatorTime;
}
