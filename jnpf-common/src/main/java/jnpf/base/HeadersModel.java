package jnpf.base;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * token
 *
 * @author 余家三少
 * @version V3.1.0
 * @copyright 上海引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Data
public class HeadersModel {
    @JSONField(name = "Token")
    private String token;
}
