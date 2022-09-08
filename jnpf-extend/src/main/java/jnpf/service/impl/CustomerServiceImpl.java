package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.Pagination;
import jnpf.entity.CustomerEntity;
import jnpf.mapper.CustomerMapper;
import jnpf.service.CustomerService;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * 客户信息
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 14:09:05
 */
@Service

public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, CustomerEntity> implements CustomerService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<CustomerEntity> getList(Pagination pagination) {
        QueryWrapper<CustomerEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(pagination.getKeyword())) {
            queryWrapper.lambda().and(
                    t->t.like(CustomerEntity::getAddress, pagination.getKeyword())
                            .or().like(CustomerEntity::getName, pagination.getKeyword())
                            .or().like(CustomerEntity::getCode, pagination.getKeyword())
            );
        }
        Page<CustomerEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<CustomerEntity> userIPage = this.page(page, queryWrapper);
        return pagination.setData(userIPage.getRecords(), userIPage.getTotal());
    }

    @Override
    public CustomerEntity getInfo(String id) {
        QueryWrapper<CustomerEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CustomerEntity::getId, id);
        return this.getOne(queryWrapper);

    }

    @Override
    public void create(CustomerEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(userProvider.get().getUserId());
        entity.setCreatorTime(new Date());
        this.save(entity);
    }

    @Override
    public boolean update(String id, CustomerEntity entity) {
        entity.setId(id);
        entity.setLastModifyUserId(userProvider.get().getUserId());
        entity.setLastModifyTime(new Date());
        return this.updateById(entity);
    }

    @Override
    public void delete(CustomerEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }
}