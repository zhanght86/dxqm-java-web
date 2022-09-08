package jnpf.base.model;

import jnpf.engine.model.flowengine.FlowEngineListVO;
import lombok.Data;

import java.util.List;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Data
public class VisualDevListVO {
    private String id;
    private Long num;
    private String fullName;
    private Integer state;
    private String enCode;
    private String type;
    private String tables;
    private Long creatorTime;
    private String creatorUser;
    private Long lastModifyTime;
    private String lastModifyUser;
    private List<VisualDevListVO> children;
}
