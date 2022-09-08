package jnpf.permission.model.authorize;

import jnpf.util.treeutil.SumTree;
import lombok.Data;

import java.util.Date;

/**
 * 数据权限
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:04
 */
@Data
public class AuthorizeDataModel extends SumTree {
    private  String id;
    private String fullName;
    private String icon;
    private Boolean showcheck;
    private Integer checkstate;
    private String title;
    private String moduleId;
    private String type;
    private Date creatorTime;
    private Long sortCode=9999L;
}
