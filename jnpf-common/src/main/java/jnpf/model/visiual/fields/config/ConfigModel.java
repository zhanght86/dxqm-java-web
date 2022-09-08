package jnpf.model.visiual.fields.config;


import jnpf.model.visiual.fields.FieLdsModel;
import lombok.Data;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:47
 */
@Data
public class ConfigModel {
    private String label;
    private String labelWidth;
    private Boolean showLabel;
    private Boolean changeTag;
    private Boolean border;
    private String tag;
    private String tagIcon;
    private Boolean required;
    private String layout;
    private String dataType;
    private Integer span;
    private String jnpfKey;
    private String dictionaryType;
    private Integer formId;
    private Long renderKey;
    private Integer columnWidth;
    private List<RegListModel> regList;
    private Object defaultValue;
    private String active;
    /**
     * app静态数据
     */
    private String options;
    /**
     * 判断defaultValue类型
     */
    private String valueType;
    private String propsUrl;
    private String optionType;
    private ConfigPropsModel props;
    /**
     * 子表添加字段
     */
    private Boolean showTitle;
    private String tableName;
    private List<FieLdsModel> children;
    /**
     * 单据规则使用
     */
    private String rule;

    /**
     * 验证规则触发方式
     */
    private String trigger="blur";
    /**
     * 隐藏
     */
    private Boolean noShow=false;
}
