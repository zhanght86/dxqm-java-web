package jnpf.base.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.entity.ModuleDataAuthorizeEntity;
import jnpf.base.mapper.ModuleDataAuthorizeMapper;
import jnpf.base.service.ModuleDataAuthorizeService;
import jnpf.util.DateUtil;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据权限配置
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class ModuleDataAuthorizeServiceImpl extends ServiceImpl<ModuleDataAuthorizeMapper, ModuleDataAuthorizeEntity> implements ModuleDataAuthorizeService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<ModuleDataAuthorizeEntity> getList() {
        QueryWrapper<ModuleDataAuthorizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(ModuleDataAuthorizeEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleDataAuthorizeEntity> getList(String moduleId) {
        QueryWrapper<ModuleDataAuthorizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleDataAuthorizeEntity::getModuleId, moduleId).orderByAsc(ModuleDataAuthorizeEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public ModuleDataAuthorizeEntity getInfo(String id) {
        QueryWrapper<ModuleDataAuthorizeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleDataAuthorizeEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(ModuleDataAuthorizeEntity entity) {
        if (entity.getId() == null){
            entity.setId(RandomUtil.uuId());
            entity.setEnabledMark(1);
            entity.setSortCode(RandomUtil.parses());
        }
        this.save(entity);
    }

    @Override
    public boolean update(String id, ModuleDataAuthorizeEntity entity) {
        entity.setId(id);
        entity.setEnabledMark(1);
        entity.setLastModifyTime(DateUtil.getNowDate());
       return this.updateById(entity);
    }

    @Override
    public void delete(ModuleDataAuthorizeEntity entity) {
        this.removeById(entity.getId());
    }


}
