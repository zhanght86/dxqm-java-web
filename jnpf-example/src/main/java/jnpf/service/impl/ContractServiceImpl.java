package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.Pagination;
import jnpf.base.UserInfo;
import jnpf.mapper.ContractMapper;
import jnpf.service.ContractService;
import jnpf.entity.ContractEntity;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 9:47
 */
@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, ContractEntity> implements ContractService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<ContractEntity> getlist(Pagination pagination){
        //通过UserProvider获取用户信息
        UserInfo userProvider = this.userProvider.get();
        QueryWrapper<ContractEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            queryWrapper.lambda().and(
                t -> t.like(ContractEntity::getContractName, pagination.getKeyword())
                .or().like(ContractEntity::getMytelePhone, pagination.getKeyword())
            );
        }
        //排序
        if (StringUtil.isEmpty(pagination.getSidx())) {
        } else {
            queryWrapper = "asc".equals(pagination.getSort().toLowerCase()) ? queryWrapper.orderByAsc(pagination.getSidx()) : queryWrapper.orderByDesc(pagination.getSidx());
        }
        Page<ContractEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<ContractEntity> userPage = this.page(page, queryWrapper);
        return pagination.setData(userPage.getRecords(), page.getTotal());
    }

    @Override
    public ContractEntity getInfo(String id){
        QueryWrapper<ContractEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ContractEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    @Transactional
    public void create(ContractEntity entity){
        entity.setId(RandomUtil.uuId());
        this.save(entity);
    }

    @Override
    @Transactional
    public void update(String id, ContractEntity entity){
        entity.setId(id);
        this.updateById(entity);
    }

    @Override
    public void delete(ContractEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }
}
