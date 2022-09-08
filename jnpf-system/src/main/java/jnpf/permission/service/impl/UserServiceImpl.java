package jnpf.permission.service.impl;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.Pagination;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.source.impl.DbDm;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.util.DbTypeUtil;
import jnpf.permission.entity.*;
import jnpf.permission.model.user.UserAllModel;
import jnpf.permission.mapper.UserMapper;
import jnpf.permission.service.*;
import jnpf.util.*;
import jnpf.util.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户信息
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月26日 上午9:18
 */
@Service
@Transactional(isolation = Isolation.DEFAULT, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private OrganizeService organizeService;
    @Autowired
    private PositionService positionService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private DataSourceUtil dataSourceUtil;

    @Override
    public List<UserEntity> getList() {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(UserEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<UserEntity> getEnableMarkList(String enableMark) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getEnabledMark,Integer.valueOf(enableMark));
        queryWrapper.lambda().orderByAsc(UserEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<UserEntity> getList(Pagination pagination, String organizeId) {
        String userId = userProvider.get().getUserId();
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().ne(UserEntity::getId, userId);
        //组织机构
        if (!StringUtil.isEmpty(organizeId)) {
            String[] organizeIds = organizeId.split(",");
            if (DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE) || DbTypeUtil.checkDb(dataSourceUtil, DbDm.DB_ENCODE)) {
                queryWrapper.in("to_char(F_ORGANIZEID)", organizeIds);
            } else {
                queryWrapper.lambda().in(UserEntity::getOrganizeId, organizeIds);
            }
        }
        //关键字（账户、姓名、手机）
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            queryWrapper.lambda().and(
                    t -> t.like(UserEntity::getAccount, pagination.getKeyword())
                            .or().like(UserEntity::getRealName, pagination.getKeyword())
                            .or().like(UserEntity::getMobilePhone, pagination.getKeyword())
            );
        }
        //排序
        queryWrapper.lambda().orderByAsc(UserEntity::getSortCode).orderByDesc(UserEntity::getCreatorTime);
        Page<UserEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<UserEntity> iPage = this.page(page, queryWrapper);
        return pagination.setData(iPage.getRecords(), iPage.getTotal());
    }

    @Override
    public List<UserEntity> getList(String keyword) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getEnabledMark,1);
        //通过关键字查询
        queryWrapper.lambda().and(
                t -> t.like(UserEntity::getAccount, keyword)
                        .or().like(UserEntity::getRealName, keyword)
        );
        return this.list(queryWrapper);
    }

    @Override
    public List<UserEntity> getListByPositionId(String positionId) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getPositionId, positionId);
        return this.list(queryWrapper);
    }

    @Override
    public List<UserEntity> getListByManagerId(String managerId) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getManagerId, managerId);
        return this.list(queryWrapper);
    }

    @Override
    public UserEntity getInfo(String id) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public String getUsername(String id) {
        UserEntity entity = this.getInfo(id);
        if (entity != null) {
            return entity.getRealName() + "(" + entity.getAccount() + ")";
        } else {
            return "";
        }
    }

    @Override
    public boolean isExistByAccount(String account) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getAccount, account);
        UserEntity entity = this.getOne(queryWrapper);
        if (entity != null) {
            return true;
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(UserEntity entity) {
        //添加用户
        entity.setId(RandomUtil.uuId());
        entity.setQuickQuery(PinYinUtil.getFirstSpell(entity.getRealName()));
        entity.setSecretkey(RandomUtil.uuId());
        entity.setPassword(Md5Util.getStringMd5(entity.getPassword().toLowerCase() + entity.getSecretkey().toLowerCase()));
        entity.setIsAdministrator(0);
        entity.setCreatorUserId(userProvider.get().getUserId());
        this.save(entity);
        //添加用户关系
        List<UserRelationEntity> relationList = new ArrayList<>();
        //关联岗位
        if (entity.getPositionId() != null) {
            String[] position = entity.getPositionId().split(",");
            for (int i = 0; i < position.length; i++) {
                UserRelationEntity relationEntity = new UserRelationEntity();
                relationEntity.setId(RandomUtil.uuId());
                relationEntity.setObjectType("Position");
                relationEntity.setObjectId(position[i]);
                relationEntity.setSortCode(Long.parseLong(i + ""));
                relationEntity.setUserId(entity.getId());
                relationEntity.setCreatorTime(entity.getCreatorTime());
                relationEntity.setCreatorUserId(entity.getCreatorUserId());
                relationList.add(relationEntity);
            }
        }
        //关联角色
        if (entity.getRoleId() != null) {
            String[] position = entity.getRoleId().split(",");
            for (int i = 0; i < position.length; i++) {
                UserRelationEntity relationEntity = new UserRelationEntity();
                relationEntity.setId(RandomUtil.uuId());
                relationEntity.setObjectType("Role");
                relationEntity.setObjectId(position[i]);
                relationEntity.setSortCode(Long.parseLong(i + ""));
                relationEntity.setUserId(entity.getId());
                relationEntity.setCreatorTime(entity.getCreatorTime());
                relationEntity.setCreatorUserId(entity.getCreatorUserId());
                relationList.add(relationEntity);
            }
        }
        for (UserRelationEntity userRelationEntity : relationList) {
            userRelationService.save(userRelationEntity);
        }
//        if (relationList.size() > 0) {
//            userRelationService.saveBatch(relationList);
//        }
        //清理获取所有用户的redis缓存
        redisUtil.remove(cacheKeyUtil.getAllUser());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(String id, UserEntity entity) {
        //更新用户
        entity.setId(id);
        entity.setQuickQuery(PinYinUtil.getFirstSpell(entity.getRealName()));
        entity.setLastModifyTime(DateUtil.getNowDate());
        entity.setLastModifyUserId(userProvider.get().getUserId());
        entity.setBirthday("".equals(entity.getBirthday()) ? null : entity.getBirthday());
        entity.setEntryDate("".equals(entity.getEntryDate()) ? null : entity.getEntryDate());
        if (!this.updateById(entity)) {
            return false;
        }
        //更新用户关系
        List<UserRelationEntity> relationList = new ArrayList<>();
        //删除用户关联
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getUserId, entity.getId());
        userRelationService.remove(queryWrapper);
        //关联岗位
        if (entity.getPositionId() != null) {
            String[] position = entity.getPositionId().split(",");
            for (int i = 0; i < position.length; i++) {
                UserRelationEntity relationEntity = new UserRelationEntity();
                relationEntity.setId(RandomUtil.uuId());
                relationEntity.setObjectType("Position");
                relationEntity.setObjectId(position[i]);
                relationEntity.setSortCode(Long.parseLong(i + ""));
                relationEntity.setUserId(entity.getId());
                relationEntity.setCreatorTime(entity.getCreatorTime());
                relationEntity.setCreatorUserId(entity.getCreatorUserId());
                relationList.add(relationEntity);
            }
        }
        //关联角色
        if (entity.getRoleId() != null) {
            String[] position = entity.getRoleId().split(",");
            for (int i = 0; i < position.length; i++) {
                UserRelationEntity relationEntity = new UserRelationEntity();
                relationEntity.setId(RandomUtil.uuId());
                relationEntity.setObjectType("Role");
                relationEntity.setObjectId(position[i]);
                relationEntity.setSortCode(Long.parseLong(i + ""));
                relationEntity.setUserId(entity.getId());
                relationEntity.setCreatorTime(entity.getCreatorTime());
                relationEntity.setCreatorUserId(entity.getCreatorUserId());
                relationList.add(relationEntity);
            }
        }
        for (UserRelationEntity userRelationEntity : relationList) {
            userRelationService.save(userRelationEntity);
        }
        //清理获取所有用户的redis缓存
        redisUtil.remove(cacheKeyUtil.getAllUser());
        return true;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(UserEntity entity) {
        this.removeById(entity.getId());
        //删除用户关联
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getUserId, entity.getId());
        userRelationService.remove(queryWrapper);
    }


    @Override
    public void updatePassword(UserEntity entity) {
        entity.setSecretkey(RandomUtil.uuId());
        entity.setPassword(Md5Util.getStringMd5(entity.getPassword().toLowerCase() + entity.getSecretkey().toLowerCase()));
        entity.setChangePasswordDate(DateUtil.getNowDate());
        this.updateById(entity);
    }

    @Override
    public void settingMenu(String id, String menuId) {
        UserEntity entity = this.getInfo(id);
        if (entity != null) {
            entity.setCommonMenu(menuId);
            this.updateById(entity);
        }
    }

    @Override
    public List<UserEntity> getUserName(List<String> id) {
        List<UserEntity> list = new ArrayList<>();
        if (id.size() > 0) {
            QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(UserEntity::getId, id);
            list = this.list(queryWrapper);
        }
        return list;
    }

    /**
     * 有判断redis来获取所有用户信息
     *
     * @return
     */
    @Override
    public List<UserAllModel> getAll() {
        String catchKey = cacheKeyUtil.getAllUser();
        if (redisUtil.exists(catchKey)) {
            return JsonUtil.getJsonToList(redisUtil.getString(catchKey).toString(), UserAllModel.class);
        }
        List<UserEntity> list = this.getEnableMarkList("1");
        //获取全部部门信息
        List<OrganizeEntity> departmentList = organizeService.getList();
        //获取全部岗位信息
        List<PositionEntity> positionList = positionService.getList();
        //获取全部角色信息
        List<RoleEntity> roleList = roleService.getList();
        List<UserAllModel> models = JsonUtil.getJsonToList(list, UserAllModel.class);
        for (UserAllModel model : models) {
            //部门名称
            OrganizeEntity deptEntity = departmentList.stream().filter(t -> t.getId().equals(model.getOrganizeId())).findFirst().orElse(new OrganizeEntity());
            if (StringUtil.isNotEmpty(deptEntity.getFullName())) {
                model.setDepartment(deptEntity.getFullName());
                model.setDepartmentId(deptEntity.getId());
            }
            //组织名称
            OrganizeEntity organizeEntity = departmentList.stream().filter(t -> t.getId().equals(String.valueOf(deptEntity.getParentId()))).findFirst().orElse(new OrganizeEntity());
            if (organizeEntity != null) {
                model.setOrganizeId(organizeEntity.getId());
                model.setOrganize(organizeEntity.getFullName());
            }
            //岗位名称(多个)
            if (model.getPositionId() != null) {
                List<String> positionName = new ArrayList<>();
                for (String id : model.getPositionId().split(",")) {
                    String name = positionList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(new PositionEntity()).getFullName();
                    if (!StringUtil.isEmpty(name)) {
                        positionName.add(name);
                    }
                }
                model.setPositionName(String.join(",", positionName));
            }
            //角色名称(多个)
            if (model.getRoleId() != null) {
                List<String> roleName = new ArrayList<>();
                for (String id : model.getRoleId().split(",")) {
                    String name = roleList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(new RoleEntity()).getFullName();
                    if (!StringUtil.isEmpty(name)) {
                        roleName.add(name);
                    }
                }
                model.setRoleName(String.join(",", roleName));
            }
            //主管名称
            String managerName = list.stream().filter(t -> t.getId().equals(model.getManagerId())).findFirst().orElse(new UserEntity()).getRealName();
            if (StringUtil.isNotEmpty(managerName)) {
                model.setManagerName(managerName);
            }
            model.setHeadIcon(UploaderUtil.uploaderImg(model.getHeadIcon()));
        }
        String allUser = JsonUtil.getObjectToString(models);
        redisUtil.insert(cacheKeyUtil.getAllUser(), allUser, 300);
        return models;
    }

    /**
     * 直接从数据库获取所有用户信息（不过滤冻结账号）
     *
     * @return
     */
    @Override
    public List<UserAllModel> getDbUserAll() {
        List<UserEntity> list = this.getList();
        //获取全部部门信息
        List<OrganizeEntity> departmentList = organizeService.getList();
        //获取全部岗位信息
        List<PositionEntity> positionList = positionService.getList();
        //获取全部角色信息
        List<RoleEntity> roleList = roleService.getList();
        List<UserAllModel> models = JsonUtil.getJsonToList(list, UserAllModel.class);
        for (UserAllModel model : models) {
            //部门名称
            OrganizeEntity organize = departmentList.stream().filter(t -> t.getId().equals(model.getOrganizeId())).findFirst().orElse(new OrganizeEntity());
            if (StringUtil.isNotEmpty(organize.getFullName())) {
                model.setDepartment(organize.getFullName());
            }
            //组织名称
            String organizeName = departmentList.stream().filter(t -> t.getId().equals(String.valueOf(organize.getParentId()))).findFirst().orElse(new OrganizeEntity()).getFullName();
            if (StringUtil.isNotEmpty(organizeName)) {
                model.setOrganize(organizeName);
            }
            //岗位名称(多个)
            if (model.getPositionId() != null) {
                List<String> positionName = new ArrayList<>();
                for (String id : model.getPositionId().split(",")) {
                    String name = positionList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(new PositionEntity()).getFullName();
                    positionName.add(name);
                }
                model.setPositionName(String.join(",", positionName));
            }
            //角色名称(多个)
            if (model.getRoleId() != null) {
                List<String> roleName = new ArrayList<>();
                for (String id : model.getRoleId().split(",")) {
                    String name = roleList.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(new RoleEntity()).getFullName();
                    roleName.add(name);
                }
                model.setRoleName(String.join(",", roleName));
            }
            //主管名称
            String managerName = list.stream().filter(t -> t.getId().equals(model.getManagerId())).findFirst().orElse(new UserEntity()).getRealName();
            if (StringUtil.isNotEmpty(managerName)) {
                model.setManagerName(managerName);
            }
            model.setHeadIcon(UploaderUtil.uploaderImg(model.getHeadIcon()));
        }
        return models;
    }

    @Override
    public UserEntity getUserEntity(String account) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getAccount, account);
        return this.baseMapper.selectOne(queryWrapper);
    }
}
