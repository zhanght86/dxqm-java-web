package jnpf.engine.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jnpf.engine.entity.FlowTaskNodeEntity;
import org.apache.ibatis.annotations.Param;

/**
 * 流程节点
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface FlowTaskNodeMapper extends BaseMapper<FlowTaskNodeEntity> {

    /**
     * 更新驳回开始流程节点
     *
     * @param taskId 节点id
     */
    void updateState(@Param("taskId") String taskId);

}
