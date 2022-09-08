package jnpf.permission.model.authorize;

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
public class ConditionModel {
    private String logic;
    private List<ConditionItemModel> groups;

    /**
     * 数据权限条件字段
     */
    @Data
    public class ConditionItemModel{
        private String id;
        private String field;
        private String type;
        private String op;
        private String value;
    }
}
