package jnpf.generater.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.entity.VisualdevEntity;
import jnpf.model.visiual.DownloadCodeForm;
import jnpf.model.visiual.FormDataModel;
import jnpf.database.model.DataSourceUtil;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
public interface VisualdevGenService extends IService<VisualdevEntity> {


    void  htmlTemplates(DownloadCodeForm downloadCodeForm,String fileName, VisualdevEntity entity, FormDataModel model, FormDataModel htmlModel, String templatePath, List<String> childTable, String pKeyName) throws SQLException;

    void  modelTemplates(String fileName, VisualdevEntity entity,FormDataModel model, String templatePath,List<String> childTable,String pKeyName) throws SQLException;

    void generate(VisualdevEntity entity, FormDataModel model, DataSourceUtil dataSourceUtil, String templateCodePath, String fileName, DownloadCodeForm downloadCodeForm, List<String> childTable, String pKeyName, Map<String,Object> childpKeyMap,String templatePath) throws SQLException;

    String codeGengerate(String id, DownloadCodeForm downloadCodeForm) throws SQLException;
}

