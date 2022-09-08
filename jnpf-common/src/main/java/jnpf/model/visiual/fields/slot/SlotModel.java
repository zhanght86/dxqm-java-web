package jnpf.model.visiual.fields.slot;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:57
 */
@Data
public class SlotModel {
    private String prepend;
    private String append;
    @JSONField(name = "default")
    private String defaultName;
    private String options;
    private String appOptions;
}
