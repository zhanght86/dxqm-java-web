package jnpf.base.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.entity.ModuleDataAuthorizeSchemeEntity;
import jnpf.base.mapper.ModuleDataAuthorizeSchemeMapper;
import jnpf.base.service.ModuleDataAuthorizeSchemeService;
import jnpf.util.DateUtil;
import jnpf.util.RandomUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据权限方案
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class ModuleDataAuthorizeSchemeServiceImpl extends ServiceImpl<ModuleDataAuthorizeSchemeMapper, ModuleDataAuthorizeSchemeEntity> implements ModuleDataAuthorizeSchemeService {

    @Autowired
    private UserProvider userProvider;

    @Override
    public List<ModuleDataAuthorizeSchemeEntity> getList() {
        QueryWrapper<ModuleDataAuthorizeSchemeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(ModuleDataAuthorizeSchemeEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<ModuleDataAuthorizeSchemeEntity> getList(String moduleId) {
        QueryWrapper<ModuleDataAuthorizeSchemeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleDataAuthorizeSchemeEntity::getModuleId, moduleId).orderByAsc(ModuleDataAuthorizeSchemeEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public ModuleDataAuthorizeSchemeEntity getInfo(String id) {
        QueryWrapper<ModuleDataAuthorizeSchemeEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(ModuleDataAuthorizeSchemeEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(ModuleDataAuthorizeSchemeEntity entity) {
        if (entity.getId() == null) {
            entity.setId(RandomUtil.uuId());
            entity.setEnabledMark(1);
            entity.setSortCode(RandomUtil.parses());
        }
        this.save(entity);
    }

    @Override
    public boolean update(String id, ModuleDataAuthorizeSchemeEntity entity) {
        entity.setId(id);
        entity.setEnabledMark(1);
        entity.setLastModifyTime(DateUtil.getNowDate());
        return  this.updateById(entity);
    }

    @Override
    public void delete(ModuleDataAuthorizeSchemeEntity entity) {
        this.removeById(entity.getId());
    }


}
