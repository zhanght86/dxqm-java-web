package jnpf.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.engine.enums.FlowNodeEnum;
import jnpf.util.RandomUtil;
import jnpf.engine.entity.FlowTaskOperatorRecordEntity;
import jnpf.engine.mapper.FlowTaskOperatorRecordMapper;
import jnpf.engine.service.FlowTaskOperatorRecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 流程经办
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class FlowTaskOperatorRecordServiceImpl extends ServiceImpl<FlowTaskOperatorRecordMapper, FlowTaskOperatorRecordEntity> implements FlowTaskOperatorRecordService {


    @Override
    public List<FlowTaskOperatorRecordEntity> getList(String taskId) {
        QueryWrapper<FlowTaskOperatorRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTaskOperatorRecordEntity::getTaskId, taskId).orderByAsc(FlowTaskOperatorRecordEntity::getHandleTime);
        return this.list(queryWrapper);
    }

    @Override
    public FlowTaskOperatorRecordEntity getInfo(String id) {
        QueryWrapper<FlowTaskOperatorRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTaskOperatorRecordEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void delete(FlowTaskOperatorRecordEntity entity) {
        QueryWrapper<FlowTaskOperatorRecordEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTaskOperatorRecordEntity::getId, entity.getId());
        this.remove(queryWrapper);
    }

    @Override
    public void create(FlowTaskOperatorRecordEntity entity) {
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    public void update(String id, FlowTaskOperatorRecordEntity entity) {
        entity.setId(id);
        this.updateById(entity);
    }

    @Override
    public void updateStatus(Set<String> taskNodeId, String taskId) {
        if (taskNodeId.size() > 0) {
            UpdateWrapper<FlowTaskOperatorRecordEntity> updateWrapper = new UpdateWrapper<>();
            updateWrapper.lambda().in(FlowTaskOperatorRecordEntity::getTaskNodeId, taskNodeId).eq(FlowTaskOperatorRecordEntity::getTaskId, taskId);
            updateWrapper.lambda().set(FlowTaskOperatorRecordEntity::getStatus, FlowNodeEnum.Futility.getCode());
            this.update(updateWrapper);
        }
    }
}
