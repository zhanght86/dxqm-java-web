package jnpf.base.model.dblink;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import jnpf.util.treeutil.SumTree;
import lombok.Data;

@Data
public class DbLinkModel extends SumTree {
    private String fullName;
    private String dbType;
    private String host;
    private String port;
    private Long creatorTime;
    @JSONField(name = "creatorUserId")
    private String creatorUser;
    private String id;
    private Long lastModifyTime;
    @JSONField(name = "lastModifyUserId")
    private String lastModifyUser;
    private Integer enabledMark;
    private Long sortCode;
    private Long num;
}
