package jnpf.engine.service;

import jnpf.engine.entity.FlowTaskEntity;
import jnpf.engine.entity.FlowTaskOperatorEntity;
import jnpf.engine.entity.FlowTaskOperatorRecordEntity;
import jnpf.engine.model.FlowHandleModel;
import jnpf.engine.model.flowbefore.FlowBeforeInfoVO;
import jnpf.engine.model.flowengine.FlowModel;
import jnpf.exception.WorkFlowException;

/**
 * 流程引擎
 *
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年4月27日 上午9:18
 */
public interface FlowTaskNewService {

    /**
     * 保存
     *
     * @param flowModel 保存数据
     * @return
     * @throws WorkFlowException 异常
     */
    FlowTaskEntity saveIsAdmin(FlowModel flowModel) throws WorkFlowException;

    /**
     * 保存
     *
     * @param flowModel 保存数据
     * @return
     * @throws WorkFlowException 异常
     */
    FlowTaskEntity save(FlowModel flowModel) throws WorkFlowException;

    /**
     * 提交
     *
     * @param flowModel 提交数据
     * @throws WorkFlowException 异常
     */
    void submit(FlowModel flowModel) throws WorkFlowException;

    /**
     * 审批
     *
     * @param id        待办主键
     * @param flowModel 提交数据
     * @throws WorkFlowException 异常
     */
    void audit(String id, FlowModel flowModel) throws WorkFlowException;

    /**
     * 审批
     *
     * @param flowTask  流程实例
     * @param operator  流程经办
     * @param flowModel 提交数据
     * @throws WorkFlowException
     */
    void audit(FlowTaskEntity flowTask, FlowTaskOperatorEntity operator, FlowModel flowModel) throws WorkFlowException;

    /**
     * 驳回
     *
     * @param id        待办主键
     * @param flowModel 提交数据
     * @return
     * @throws WorkFlowException 异常
     */
    void reject(String id, FlowModel flowModel) throws WorkFlowException;

    /**
     * 驳回
     *
     * @param flowTask  流程实例
     * @param operator  流程经办
     * @param flowModel 提交数据
     * @throws WorkFlowException
     */
    void reject(FlowTaskEntity flowTask, FlowTaskOperatorEntity operator, FlowModel flowModel) throws WorkFlowException;

    /**
     * 已办撤回
     *
     * @param id             已办id
     * @param operatorRecord 经办记录
     * @param flowModel      提交数据
     * @throws WorkFlowException 异常
     */
    void recall(String id, FlowTaskOperatorRecordEntity operatorRecord, FlowModel flowModel) throws WorkFlowException;

    /**
     * 发起撤回
     *
     * @param flowTask  流程实例
     * @param flowModel 提交数据
     */
    void revoke(FlowTaskEntity flowTask, FlowModel flowModel);

    /**
     * 终止
     *
     * @param flowTask  流程实例
     * @param flowModel 提交数据
     */
    void cancel(FlowTaskEntity flowTask, FlowModel flowModel);

    /**
     * 指派
     *
     * @param id
     * @param flowHandleModel 提交数据
     * @return
     */
    boolean assign(String id, FlowHandleModel flowHandleModel);

    /**
     * 转办
     *
     * @param taskOperator 经办数据
     */
    void transfer(FlowTaskOperatorEntity taskOperator);

    /**
     * 获取任务详情
     *
     * @param id         主键
     * @param taskNodeId
     * @return
     * @throws WorkFlowException 异常
     */
    FlowBeforeInfoVO getBeforeInfo(String id, String taskNodeId) throws WorkFlowException;
}
