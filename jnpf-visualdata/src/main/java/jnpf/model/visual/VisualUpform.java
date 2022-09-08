package jnpf.model.visual;

import jnpf.model.visualconfig.VisualConfigCrForm;
import jnpf.model.visualconfig.VisualConfigUpForm;
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
public class VisualUpform {
    private VisualUpModel visual;
    private VisualConfigUpForm config;
}
