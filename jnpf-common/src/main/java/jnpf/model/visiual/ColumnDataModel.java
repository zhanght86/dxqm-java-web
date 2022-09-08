package jnpf.model.visiual;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:47
 */
@Data
public class ColumnDataModel {
    private String searchList;
    private String columnList;
    private String sortList;
    private Integer type;
    private String defaultSidx;
    private String sort;
    private String hasPage;
    private Integer pageSize;
    private String treeTitle;
    private String treeDataSource;
    private String treeDictionary;
    private String treeRelation;
    private String treePropsUrl;
    private String treePropsValue;
    private String treePropsChildren;
    private String treePropsLabel;
    private String groupField;
    private String btnsList;
    private String columnBtnsList;
    private Boolean useColumnPermission;
    private Boolean useBtnPermission;
    private Boolean useDataPermission;
}
