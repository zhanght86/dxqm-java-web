package jnpf.portal.model;


import jnpf.base.model.VisualDevListVO;
import lombok.Data;

import java.util.List;

/**
 *
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @author 管理员/admin
 * @date 2020-10-21 14:23:30
 */
@Data
public class PortalListVO{
    private String id;
    private Long num;
    private String fullName;
    private String enCode;
    private Integer enabledMark;
    private Long creatorTime;
    private String creatorUser;
    private Long lastModifyTime;
    private String lastModifyUser;
    private List<PortalListVO> children;
}
