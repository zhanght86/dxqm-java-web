package jnpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.entity.ProductclassifyEntity;
import jnpf.mapper.ProductclassifyMapper;
import jnpf.service.ProductclassifyService;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 *
 * 产品分类
 * 版本： V3.1.0
 * 版权： 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * 作者： JNPF开发平台组
 * 日期： 2021-07-10 14:34:04
 */
@Service
public class ProductclassifyServiceImpl extends ServiceImpl<ProductclassifyMapper, ProductclassifyEntity> implements ProductclassifyService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<ProductclassifyEntity> getList(){
        QueryWrapper<ProductclassifyEntity> queryWrapper=new QueryWrapper<>();
        return list(queryWrapper);
    }

    @Override
    public ProductclassifyEntity getInfo(String id){
        QueryWrapper<ProductclassifyEntity> queryWrapper=new QueryWrapper<>();
        queryWrapper.lambda().eq(ProductclassifyEntity::getId,id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(ProductclassifyEntity entity){
        entity.setId(RandomUtil.uuId());
        entity.setCreatorUserId(userProvider.get().getUserId());
        entity.setCreatorTime(new Date());
        this.save(entity);
    }

    @Override
    public boolean update(String id, ProductclassifyEntity entity){
        entity.setId(id);
        entity.setLastModifyUserId(userProvider.get().getUserId());
        entity.setLastModifyTime(new Date());
        return this.updateById(entity);
    }
    @Override
    public void delete(ProductclassifyEntity entity){
        if(entity!=null){
            this.removeById(entity.getId());
        }
    }

}