package jnpf.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * token
 *
 * @author JNPF开发平台组
 * @version V1.2.191207
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Data
public class HeadersModel {
    @JSONField(name = "Token")
    private String token;
    @JSONField(name = "ModuleId")
    private String moduleId;
}
