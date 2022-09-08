package jnpf.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.base.ActionResult;
import jnpf.engine.entity.FlowEngineVisibleEntity;
import jnpf.engine.model.flowengine.FlowEngineListVO;
import jnpf.engine.model.flowengine.FlowExportModel;
import jnpf.exception.WorkFlowException;
import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.model.flowengine.PaginationFlowEngine;

import java.util.List;

/**
 * 流程引擎
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface FlowEngineService extends IService<FlowEngineEntity> {

    /**
     * 列表
     *
     * @param pagination 分页
     * @return
     */
    List<FlowEngineEntity> getList(PaginationFlowEngine pagination);

    /**
     * 列表
     *
     * @return
     */
    List<FlowEngineEntity> getList();

    /**
     * 列表
     *
     * @param id          流程id
     * @param visibleType 可见类型
     * @return
     */
    List<FlowEngineEntity> getListAll(List<String> id, String visibleType);

    /**
     * 列表
     *
     * @return
     */
    List<FlowEngineEntity> getFlowFormList();

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     * @throws WorkFlowException 异常
     */
    FlowEngineEntity getInfo(String id) throws WorkFlowException;

    /**
     * 信息
     *
     * @param enCode 流程编码
     * @return
     * @throws WorkFlowException 异常
     */
    FlowEngineEntity getInfoByEnCode(String enCode) throws WorkFlowException;

    /**
     * 验证名称
     *
     * @param fullName 名称
     * @param id       主键值
     * @return
     */
    boolean isExistByFullName(String fullName, String id);

    /**
     * 验证编码
     *
     * @param enCode 编码
     * @param id     主键值
     * @return
     */
    boolean isExistByEnCode(String enCode, String id);

    /**
     * 删除
     *
     * @param entity 实体对象
     */
    void delete(FlowEngineEntity entity);

    /**
     * 创建
     *
     * @param entity 实体对象
     */
    void create(FlowEngineEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     * @return
     */
    boolean updateVisible(String id, FlowEngineEntity entity);

    /**
     * 更新
     *
     * @param id     主键值
     * @param entity 实体对象
     */
    void update(String id, FlowEngineEntity entity);

    /**
     * 上移
     *
     * @param id 主键值
     * @return
     */
    boolean first(String id);

    /**
     * 下移
     *
     * @param id 主键值
     * @return
     */
    boolean next(String id);

    /**
     * 流程设计列表
     * @param pagination
     * @param isList
     * @return
     */
    List<FlowEngineListVO> getTreeList(PaginationFlowEngine pagination,boolean isList);

    /**
     * 导入创建
     *
     * @param id 导出主键
     */
    FlowExportModel exportData (String id) throws WorkFlowException;

    /**
     * 工作流导入
     *
     * @param entity      实体对象
     * @param visibleList 可见
     * @return
     * @throws WorkFlowException
     */
    ActionResult ImportData (FlowEngineEntity entity, List<FlowEngineVisibleEntity> visibleList) throws WorkFlowException;
}
