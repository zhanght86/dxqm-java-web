package jnpf.portal.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.UserInfo;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.permission.entity.AuthorizeEntity;
import jnpf.permission.model.user.UserAllModel;
import jnpf.permission.service.AuthorizeService;
import jnpf.permission.service.UserService;
import jnpf.portal.entity.PortalEntity;
import jnpf.portal.model.PortalPagination;
import jnpf.portal.mapper.PortalMapper;
import jnpf.portal.model.PortalSelectModel;
import jnpf.portal.service.PortalService;
import jnpf.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/3/16
 */
@Service
public class PortalServiceImpl extends ServiceImpl<PortalMapper, PortalEntity> implements PortalService {

    @Autowired
    private PortalService portalService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthorizeService authorizeService;
    @Autowired
    private DictionaryDataService dictionaryDataService;

    @Override
    public List<PortalEntity> getList(PortalPagination portalPagination) {
        QueryWrapper<PortalEntity> queryWrapper = new QueryWrapper<>();
        if (!StringUtil.isEmpty(portalPagination.getKeyword())) {
            queryWrapper.lambda().like(PortalEntity::getFullName, portalPagination.getKeyword());
        }
        queryWrapper.lambda().orderByDesc(PortalEntity::getCreatorTime);
        return list(queryWrapper);
    }


    @Override
    public List<PortalEntity> getList() {
        QueryWrapper<PortalEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(PortalEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public PortalEntity getInfo(String id) {
        QueryWrapper<PortalEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(PortalEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(PortalEntity entity) {
        if (StringUtil.isEmpty(entity.getId())){
            entity.setId(RandomUtil.uuId());
        }
        this.save(entity);
    }

    @Override
    public boolean update(String id, PortalEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(DateUtil.getNowDate());
        return this.updateById(entity);
    }

    @Override
    public void delete(PortalEntity entity) {
        if (entity != null) {
            this.removeById(entity.getId());
        }
    }

    @Override
    public List<PortalSelectModel> getList(String type) {
        List<PortalEntity> list = portalService.getList().stream().filter(t->"1".equals(String.valueOf(t.getEnabledMark()))).collect(Collectors.toList());
        UserInfo userInfo=userProvider.get();
        if("1".equals(type)&&userInfo.getIsAdministrator()!=true){
            List<UserAllModel> model = userService.getDbUserAll().stream().filter(t->t.getId().equals(userProvider.get().getUserId())).collect(Collectors.toList());

            if(model.size()>0){
                List<String> roleIds;
                List<String> itemIds =new ArrayList<>();
                Set<String> set = new HashSet<>(16);
                List<PortalEntity> newPortalList =new ArrayList<>();

                roleIds= Arrays.asList(model.get(0).getRoleId().split(","));
                if(roleIds.size()>0){
                    for(String ids:roleIds){
                        List<AuthorizeEntity> authorizeEntityList=authorizeService.getListByObjectId(ids).stream().filter(t->"portal".equals(t.getItemType())).collect(Collectors.toList());
                        if(authorizeEntityList.size()>0){
                            for(AuthorizeEntity authorizeEntity:authorizeEntityList){
                                itemIds.add(authorizeEntity.getItemId());
                            }
                        }
                    }
                }
                set.addAll(itemIds);
                for(PortalEntity entity:list){
                    for(String iid:set){
                        if(iid.equals(entity.getId())){
                            newPortalList.add(entity);
                        }
                    }
                }
                list=newPortalList;
            }else {
                list = new ArrayList<>();
            }
        }
        List<PortalSelectModel> modelList = JsonUtil.getJsonToList(list, PortalSelectModel.class);

        for(PortalEntity portalEntity:list){
            DictionaryDataEntity dictionaryDataEntity=dictionaryDataService.getInfo(portalEntity.getCategory());
            if(dictionaryDataEntity!=null){
                PortalSelectModel model=new PortalSelectModel();
                model.setId(dictionaryDataEntity.getId());
                model.setFullName(dictionaryDataEntity.getFullName());
                model.setParentId("0");
                if(!modelList.contains(model)){
                    modelList.add(model);
                }
            }
        }
        return modelList;
    }

}
