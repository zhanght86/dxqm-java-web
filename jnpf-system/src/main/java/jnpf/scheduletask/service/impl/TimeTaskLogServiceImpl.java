package jnpf.scheduletask.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.scheduletask.entity.TimeTaskLogEntity;
import jnpf.scheduletask.mapper.TimeTaskLogMapper;
import jnpf.scheduletask.service.TimeTaskLogService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


/**
 * 执行记录
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class TimeTaskLogServiceImpl extends ServiceImpl<TimeTaskLogMapper, TimeTaskLogEntity> implements TimeTaskLogService {

    @Override
    public List<TimeTaskLogEntity> getTaskList(String taskId) {
        QueryWrapper<TimeTaskLogEntity> taskLog = new QueryWrapper<>();
        taskLog.lambda().in(TimeTaskLogEntity::getTaskId, taskId);
        return this.list(taskLog);
    }

    @Override
    public List<TimeTaskLogEntity> getTaskList(List<String> taskId) {
        List<TimeTaskLogEntity> list = new ArrayList<>();
        if (taskId.size() > 0) {
            QueryWrapper<TimeTaskLogEntity> taskLog = new QueryWrapper<>();
            taskLog.lambda().in(TimeTaskLogEntity::getTaskId, taskId);
            list = this.list(taskLog);
        }
        return list;
    }

    @Override
    public void delete(String taskId) {
        QueryWrapper<TimeTaskLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TimeTaskLogEntity::getTaskId, taskId);
        this.remove(queryWrapper);
    }


}
