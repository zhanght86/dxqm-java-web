package jnpf.base.model.province;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class ProvinceListVO {
    private String id;

    private String fullName;

    private String enCode;

    private Integer enabledMark;

    private Boolean isLeaf;
    private Boolean hasChildren;
    private Long sortCode;
}
