package jnpf.engine.model.flowengine;

import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.entity.FlowEngineVisibleEntity;
import lombok.Data;

import java.util.List;

/**
 *
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/15 9:17
 */
@Data
public class FlowExportModel {

    private FlowEngineEntity flowEngine;

    private List<FlowEngineVisibleEntity> visibleList;

}
