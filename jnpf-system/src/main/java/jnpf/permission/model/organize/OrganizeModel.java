package jnpf.permission.model.organize;

import com.alibaba.fastjson.annotation.JSONField;
import jnpf.util.treeutil.SumTree;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class OrganizeModel extends SumTree {
    private String fullName;
    private String enCode;
    private Long creatorTime;
    private String manager;
    private String description;
    private int enabledMark;
    private String icon;
    @JSONField(name="category")
    private String  type;
    private long sortCode;
}
