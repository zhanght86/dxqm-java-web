package jnpf.engine.service;

import com.baomidou.mybatisplus.extension.service.IService;
import jnpf.exception.WorkFlowException;
import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskNodeEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.entity.FlowTaskOperatorRecordEntity;
import jnpf.engine.model.FlowHandleModel;
import jnpf.engine.model.flowtask.PaginationFlowTask;

import java.util.List;

/**
 * 流程任务
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface FlowTaskService extends IService<FlowTaskEntity> {

    /**
     * 列表（流程监控）
     *
     * @param paginationFlowTask
     * @return
     */
    List<FlowTaskEntity> getMonitorList(PaginationFlowTask paginationFlowTask);

    /**
     * 列表（我发起的）
     *
     * @param paginationFlowTask
     * @return
     */
    List<FlowTaskEntity> getLaunchList(PaginationFlowTask paginationFlowTask);

    /**
     * 列表（待我审批）
     *
     * @param paginationFlowTask
     * @return
     */
    List<FlowTaskEntity> getWaitList(PaginationFlowTask paginationFlowTask);

    /**
     * 列表（我已审批）
     *
     * @return
     */
    List<FlowTaskEntity> getTrialList();


    /**
     * 列表（待我审批）
     *
     * @return
     */
    List<FlowTaskEntity> getWaitList();


    /**
     * 列表（待我审批）
     *
     * @return
     */
    List<FlowTaskEntity> getAllWaitList();

    /**
     * 列表（我已审批）
     *
     * @param paginationFlowTask
     * @return
     */
    List<FlowTaskEntity> getTrialList(PaginationFlowTask paginationFlowTask);


    /**
     * 列表（抄送我的）
     *
     * @param paginationFlowTask
     * @return
     */
    List<FlowTaskEntity> getCirculateList(PaginationFlowTask paginationFlowTask);

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     * @throws WorkFlowException 异常
     */
    FlowTaskEntity getInfo(String id) throws WorkFlowException;

    /**
     * 信息
     *
     * @param id 主键值
     * @return
     */
    FlowTaskEntity getInfoSubmit(String id);

    /**
     * 删除
     *
     * @param entity 实体对象
     * @throws WorkFlowException 异常
     */
    void delete(FlowTaskEntity entity) throws WorkFlowException;

    /**
     * 删除
     *
     * @param entity 实体对象
     * @throws WorkFlowException 异常
     */
    void deleteChild(FlowTaskEntity entity);

    /**
     * 批量删除流程
     *
     * @param ids
     */
    void delete(String[] ids) throws WorkFlowException;

    /**
     * 通过流程引擎id获取流程列表
     *
     * @param id
     * @return
     */
    List<FlowTaskEntity> getTaskList(String id);

    /**
     * 查询订单状态
     *
     * @param id
     * @return
     */
    List<FlowTaskEntity> getOrderStaList(List<String> id);

    /**
     * 查询子流程
     *
     * @param id
     * @return
     */
    List<FlowTaskEntity> getChildList(String id);
}
