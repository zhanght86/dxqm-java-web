package jnpf.base.model.button;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ButtonTreeListVO {
    private Long sortCode;
    private String id;
    private String parentId;
    private String fullName;
    private String icon;
    private String enCode;
    private Integer enabledMark;
    private Boolean hasChildren;
    private List<ButtonTreeListVO> children;
}
