package jnpf.model.visual;

import jnpf.model.visualconfig.VisualConfigInfoModel;
import lombok.Data;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年6月15日
 */
@Data
public class VisualInfoVO {
    private VisualInfoModel visual;
    private VisualConfigInfoModel config;
}
