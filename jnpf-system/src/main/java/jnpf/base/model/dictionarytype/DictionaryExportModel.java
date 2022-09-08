package jnpf.base.model.dictionarytype;

import jnpf.base.entity.DictionaryTypeEntity;
import jnpf.base.model.dictionarydata.DictionaryDataExportModel;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 数据字典导入导出模板
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-11
 */
@Data
public class DictionaryExportModel implements Serializable {

    /**
     * 字典分类
     */
    private List<DictionaryTypeEntity> list;

    /**
     * 数据集合
     */
    private List<DictionaryDataExportModel> modelList;

}
