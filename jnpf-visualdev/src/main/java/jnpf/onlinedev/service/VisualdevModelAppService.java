package jnpf.onlinedev.service;

import jnpf.base.ActionResult;
import jnpf.base.entity.VisualdevEntity;
import jnpf.database.exception.DataException;
import jnpf.onlinedev.model.PaginationModel;

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
public interface VisualdevModelAppService {

    /**
     * 列表
     *
     * @param modelId         模板id
     * @param paginationModel 条件
     * @return
     */
    List<Map<String, Object>> resultList(String modelId, PaginationModel paginationModel) throws DataException, ParseException, SQLException, IOException;

    /**
     * 新增
     *
     * @param entity 实体
     * @param data   数据
     */
    void create(VisualdevEntity entity, String data) throws DataException, SQLException;

    /**
     * 修改
     *
     * @param id     主键
     * @param entity 实体
     * @param data   数据
     */
    ActionResult update(String id, VisualdevEntity entity, String data) throws DataException, SQLException;

    /**
     * 删除
     *
     * @param id     主键
     * @param entity 实体
     */
    boolean delete(String id, VisualdevEntity entity) throws DataException, SQLException;

    /**
     * 信息
     *
     * @param id     主键
     * @param entity 实体
     * @return
     */
    Map<String, Object> info(String id, VisualdevEntity entity) throws DataException, ParseException, SQLException, IOException;
}
