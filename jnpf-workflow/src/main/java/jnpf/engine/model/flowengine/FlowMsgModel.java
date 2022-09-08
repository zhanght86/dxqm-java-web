package jnpf.engine.model.flowengine;

import jnpf.engine.entity.FlowTaskCirculateEntity;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import lombok.Data;

import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/7/14 9:17
 */
@Data
public class FlowMsgModel {
    private FlowTaskEntity taskEntity;
    private List<FlowTaskNodeEntity> nodeList;
    private List<FlowTaskOperatorEntity> operatorList;
    private List<FlowTaskCirculateEntity> circulateList;
    private String msgTitel;
}
