package jnpf.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.permission.service.UserRelationService;
import jnpf.engine.entity.FlowEngineVisibleEntity;
import jnpf.engine.mapper.FlowEngineVisibleMapper;
import jnpf.engine.service.FlowEngineVisibleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 流程可见
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class FlowEngineVisibleServiceImpl extends ServiceImpl<FlowEngineVisibleMapper, FlowEngineVisibleEntity> implements FlowEngineVisibleService {

    @Autowired
    private UserRelationService userRelationService;

    @Override
    public List<FlowEngineVisibleEntity> getList(String flowId) {
        QueryWrapper<FlowEngineVisibleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowEngineVisibleEntity::getFlowId, flowId).orderByAsc(FlowEngineVisibleEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<FlowEngineVisibleEntity> getVisibleFlowList(String userId) {
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getUserId, userId);
        List<UserRelationEntity> list = userRelationService.list(queryWrapper);
        List<String> userRelationList = list.stream().map(u -> u.getObjectId()).collect(Collectors.toList());
        userRelationList.add(userId);
        StringBuilder sql = new StringBuilder();
        for (int i = 0; i < userRelationList.size(); i++) {
            if (i != userRelationList.size() - 1) {
                sql.append("'" + userRelationList.get(i) + "',");
            } else {
                sql.append("'" + userRelationList.get(i) + "'");
            }
        }
        return this.baseMapper.getVisibleFlowList(sql.toString());
    }
}
