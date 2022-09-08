package jnpf.onlinedev.service;


import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.ActionResult;
import jnpf.base.entity.VisualdevEntity;
import jnpf.database.exception.DataException;
import jnpf.exception.WorkFlowException;
import jnpf.onlinedev.model.*;
import jnpf.onlinedev.entity.VisualdevModelDataEntity;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
public interface VisualdevModelDataService extends IService<VisualdevModelDataEntity> {


    List<Map<String, Object>> getListResult(VisualdevEntity visualdevEntity , PaginationModel paginationModel) throws IOException, ParseException, DataException, SQLException;

    List<Map<String, Object>> getListResultAll(VisualdevEntity visualdevEntity) throws IOException, ParseException, DataException, SQLException;

    List<VisualdevModelDataEntity> getList(String modelId);

    VisualdevModelDataEntity getInfo(String id);

    List<VisualdevModelDataEntity> getModel(String id);

    VisualdevModelDataInfoVO infoDataChange(String id, VisualdevEntity visualdevEntity) throws IOException, ParseException, DataException, SQLException;

    VisualdevModelDataInfoVO tableInfo(String id, VisualdevEntity visualdevEntity) throws DataException, ParseException, SQLException, IOException;

    ActionResult create(VisualdevEntity visualdevEntity, VisualdevModelDataCrForm visualdevModelDataCrForm) throws DataException, SQLException;

    ActionResult update(String id,VisualdevEntity visualdevEntity, VisualdevModelDataUpForm visualdevModelDataUpForm) throws DataException, SQLException;

    void delete(VisualdevModelDataEntity entity);

    boolean tableDelete(String id,VisualdevEntity visualdevEntity) throws DataException, SQLException;

    ActionResult tableDeleteMore(List<String> id, VisualdevEntity visualdevEntity) throws DataException, SQLException;

    void  importData(List<VisualdevModelDataEntity> list);

    List<Map<String, Object>> exportData(String[] keys, PaginationModelExport paginationModelExport, VisualdevEntity visualdevEntity) throws IOException, ParseException, SQLException, DataException;

    VisualdevModelDataInfoVO tableInfoDataChange(String id, VisualdevEntity visualdevEntity) throws DataException, ParseException, IOException, SQLException;

    ActionResult visualCreate(VisualdevEntity visualdevEntity,Map<String, Object> dataMap,VisualdevModelDataCrForm visualdevModelDataCrForm,String mainId)throws WorkFlowException;

    ActionResult visualUpdate(String id,VisualdevEntity visualdevEntity, Map<String,Object> map,VisualdevModelDataUpForm visualdevModelDataUpForm) throws WorkFlowException;
}
