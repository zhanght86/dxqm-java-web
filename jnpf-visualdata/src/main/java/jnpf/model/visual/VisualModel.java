package jnpf.model.visual;

import jnpf.entity.VisualConfigEntity;
import jnpf.entity.VisualEntity;
import lombok.Data;

/**
 * 大屏导出
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年7月10日
 */
@Data
public class VisualModel {

    private VisualEntity entity;

    private VisualConfigEntity configEntity;
}
