package jnpf.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.Pagination;
import jnpf.base.entity.DataInterfaceLogEntity;
import jnpf.base.mapper.DataInterfaceLogMapper;
import jnpf.base.service.DataInterfaceLogService;
import jnpf.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021-06-03
 */
@Service
public class DataInterfaceLogServiceImpl extends ServiceImpl<DataInterfaceLogMapper, DataInterfaceLogEntity> implements DataInterfaceLogService {
    @Autowired
    private UserProvider userProvider;

    @Override
    public void create(String dateInterfaceId, Integer invokWasteTime) {
        DataInterfaceLogEntity entity = new DataInterfaceLogEntity();
        entity.setId(RandomUtil.uuId());
        entity.setInvokTime(DateUtil.getNowDate());
        entity.setUserId(userProvider.get().getUserId());
        entity.setInvokId(dateInterfaceId);
        entity.setInvokIp(IpUtil.getIpAddr());
        entity.setInvokType("GET");
        entity.setInvokDevice(ServletUtil.getUserAgent());
        entity.setInvokWasteTime(invokWasteTime);
        this.save(entity);
    }

    @Override
    public List<DataInterfaceLogEntity> getList(String invokId, Pagination pagination) {
        QueryWrapper<DataInterfaceLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataInterfaceLogEntity::getInvokId, invokId).orderByDesc(DataInterfaceLogEntity::getInvokTime);
        if (StringUtil.isNotEmpty(pagination.getKeyword())){
            queryWrapper.lambda().and(
                    t->t.like(DataInterfaceLogEntity::getUserId, pagination.getKeyword())
                    .or().like(DataInterfaceLogEntity::getInvokIp, pagination.getKeyword())
                    .or().like(DataInterfaceLogEntity::getInvokDevice, pagination.getKeyword())
                    .or().like(DataInterfaceLogEntity::getInvokType, pagination.getKeyword())
            );
        }
        Page<DataInterfaceLogEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<DataInterfaceLogEntity> iPage = this.page(page, queryWrapper);
        return pagination.setData(iPage.getRecords(), page.getTotal());
    }

}
