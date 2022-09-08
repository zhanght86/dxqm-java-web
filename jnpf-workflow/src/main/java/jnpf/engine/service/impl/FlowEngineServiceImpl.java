package jnpf.engine.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.base.service.DictionaryDataService;
import jnpf.engine.model.flowengine.FlowEngineListVO;
import jnpf.engine.model.flowengine.FlowExportModel;
import jnpf.engine.model.flowengine.FlowTreeModel;
import jnpf.engine.model.flowengine.shuntjson.childnode.ChildNode;
import jnpf.engine.model.flowengine.shuntjson.childnode.Properties;
import jnpf.exception.WorkFlowException;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.service.UserService;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;
import jnpf.util.UserProvider;
import jnpf.engine.entity.FlowEngineEntity;
import jnpf.engine.entity.FlowEngineVisibleEntity;
import jnpf.engine.mapper.FlowEngineMapper;
import jnpf.engine.model.flowengine.PaginationFlowEngine;
import jnpf.engine.service.FlowEngineService;
import jnpf.engine.service.FlowEngineVisibleService;
import jnpf.util.treeutil.SumTree;
import jnpf.util.treeutil.newtreeutil.TreeDotUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程引擎
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class FlowEngineServiceImpl extends ServiceImpl<FlowEngineMapper, FlowEngineEntity> implements FlowEngineService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserService userService;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private FlowEngineVisibleService flowEngineVisibleService;

    @Override
    public List<FlowEngineEntity> getList(PaginationFlowEngine pagination) {
        QueryWrapper<FlowEngineEntity> queryWrapper = new QueryWrapper<>();
        if (StringUtil.isNotEmpty(pagination.getKeyword())) {
            queryWrapper.lambda().like(FlowEngineEntity::getFullName, pagination.getKeyword());
        }
        if (StringUtil.isNotEmpty(pagination.getFormType())) {
            queryWrapper.lambda().like(FlowEngineEntity::getFormType, pagination.getFormType());
        }
        if (StringUtil.isNotEmpty(pagination.getEnabledMark())) {
            queryWrapper.lambda().like(FlowEngineEntity::getEnabledMark, pagination.getEnabledMark());
        }
        //排序
        queryWrapper.lambda().orderByAsc(FlowEngineEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<FlowEngineEntity> getList() {
        QueryWrapper<FlowEngineEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByDesc(FlowEngineEntity::getCreatorTime);
        return this.list(queryWrapper);
    }

    @Override
    public List<FlowEngineEntity> getListAll(List<String> id, String visibleType) {
        QueryWrapper<FlowEngineEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowEngineEntity::getEnabledMark, 1).eq(FlowEngineEntity::getType, 0);
        if (id.size() > 0) {
            queryWrapper.lambda().and(
                    t -> t.in(FlowEngineEntity::getId, id)
                            .or().like(FlowEngineEntity::getVisibleType, visibleType)

            );
        } else if (StringUtil.isNotEmpty(visibleType)) {
            queryWrapper.lambda().eq(FlowEngineEntity::getVisibleType, visibleType);
        }
        queryWrapper.lambda().orderByAsc(FlowEngineEntity::getSortCode);
        return this.list(queryWrapper);
    }

    @Override
    public List<FlowEngineEntity> getFlowFormList() {
        UserInfo userInfo = userProvider.get();
        List<String> id = new ArrayList<>();
        String visibleType = "";
        if (!userInfo.getIsAdministrator()) {
            //部分看见(岗位和用户)
            id = flowEngineVisibleService.getVisibleFlowList(userInfo.getUserId()).stream().map(t -> t.getFlowId()).collect(Collectors.toList());
            visibleType = "0";
        }
        List<FlowEngineEntity> data = getListAll(id, visibleType);
        return data;
    }

    @Override
    public FlowEngineEntity getInfo(String id) throws WorkFlowException {
        QueryWrapper<FlowEngineEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowEngineEntity::getId, id);
        FlowEngineEntity flowEngineEntity = this.getOne(queryWrapper);
        if (flowEngineEntity == null) {
            throw new WorkFlowException("未找到流程引擎");
        }
        return flowEngineEntity;
    }

    @Override
    public FlowEngineEntity getInfoByEnCode(String enCode) throws WorkFlowException {
        QueryWrapper<FlowEngineEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowEngineEntity::getEnCode, enCode).eq(FlowEngineEntity::getEnabledMark, 1);
        FlowEngineEntity flowEngineEntity = this.getOne(queryWrapper);
        if (flowEngineEntity == null) {
            throw new WorkFlowException("未找到流程引擎");
        }
        return flowEngineEntity;
    }

    @Override
    public boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<FlowEngineEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowEngineEntity::getFullName, fullName);
        if (!StringUtils.isEmpty(id)) {
            queryWrapper.lambda().ne(FlowEngineEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String enCode, String id) {
        QueryWrapper<FlowEngineEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowEngineEntity::getEnCode, enCode);
        if (!StringUtils.isEmpty(id)) {
            queryWrapper.lambda().ne(FlowEngineEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public void delete(FlowEngineEntity entity) {
        this.removeById(entity.getId());
        QueryWrapper<FlowEngineVisibleEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowEngineVisibleEntity::getFlowId, entity.getId());
        flowEngineVisibleService.remove(queryWrapper);
    }

    @Override
    public void create(FlowEngineEntity entity) {
        List<FlowEngineVisibleEntity> visibleList = visibleList(entity);
        if (entity.getId() == null) {
            entity.setId(RandomUtil.uuId());
        }
        entity.setCreatorUser(userProvider.get().getUserId());
        entity.setVisibleType(visibleList.size() == 0 ? 0 : 1);
        this.save(entity);
        for (int i = 0; i < visibleList.size(); i++) {
            visibleList.get(i).setId(RandomUtil.uuId());
            visibleList.get(i).setFlowId(entity.getId());
            visibleList.get(i).setSortCode(RandomUtil.parses());
            flowEngineVisibleService.save(visibleList.get(i));
        }
    }

    @Override
    public boolean updateVisible(String id, FlowEngineEntity entity) {
        List<FlowEngineVisibleEntity> visibleList = visibleList(entity);
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUser(userProvider.get().getUserId());
        entity.setVisibleType(visibleList.size() == 0 ? 0 : 1);
        boolean flag = this.updateById(entity);
        if (flag == true) {
            QueryWrapper<FlowEngineVisibleEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(FlowEngineVisibleEntity::getFlowId, entity.getId());
            flowEngineVisibleService.remove(queryWrapper);
            for (int i = 0; i < visibleList.size(); i++) {
                visibleList.get(i).setId(RandomUtil.uuId());
                visibleList.get(i).setFlowId(entity.getId());
                visibleList.get(i).setSortCode(Long.parseLong(i + ""));
                flowEngineVisibleService.save(visibleList.get(i));
            }
        }
        return flag;
    }

    @Override
    public void update(String id, FlowEngineEntity entity) {
        entity.setId(id);
        entity.setLastModifyTime(new Date());
        entity.setLastModifyUser(userProvider.get().getUserId());
        this.updateById(entity);
    }

    @Override
    public boolean first(String id) {
        boolean isOk = false;
        //获取要上移的那条数据的信息
        FlowEngineEntity upEntity = this.getById(id);
        Long upSortCode = upEntity.getSortCode() == null ? 0 : upEntity.getSortCode();
        //查询上几条记录
        QueryWrapper<FlowEngineEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .lt(FlowEngineEntity::getSortCode, upSortCode)
                .eq(FlowEngineEntity::getCategory, upEntity.getCategory())
                .orderByDesc(FlowEngineEntity::getSortCode);
        List<FlowEngineEntity> downEntity = this.list(queryWrapper);
        if (downEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = upEntity.getSortCode();
            upEntity.setSortCode(downEntity.get(0).getSortCode());
            downEntity.get(0).setSortCode(temp);
            updateById(downEntity.get(0));
            updateById(upEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    public boolean next(String id) {
        boolean isOk = false;
        //获取要下移的那条数据的信息
        FlowEngineEntity downEntity = this.getById(id);
        Long upSortCode = downEntity.getSortCode() == null ? 0 : downEntity.getSortCode();
        //查询下几条记录
        QueryWrapper<FlowEngineEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .gt(FlowEngineEntity::getSortCode, upSortCode)
                .eq(FlowEngineEntity::getCategory, downEntity.getCategory())
                .orderByAsc(FlowEngineEntity::getSortCode);
        List<FlowEngineEntity> upEntity = this.list(queryWrapper);
        if (upEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = downEntity.getSortCode();
            downEntity.setSortCode(upEntity.get(0).getSortCode());
            upEntity.get(0).setSortCode(temp);
            updateById(upEntity.get(0));
            updateById(downEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    public List<FlowEngineListVO> getTreeList(PaginationFlowEngine pagination, boolean isList) {
        List<FlowEngineEntity> data = new ArrayList<>();
        if (isList) {
            data = getList(pagination);
        } else {
            data = getFlowFormList();
        }
        List<DictionaryDataEntity> dictionList = dictionaryDataService.getList("507f4f5df86b47588138f321e0b0dac7");
        List<FlowTreeModel> modelAll = new LinkedList<>();
        List<UserEntity> userList = userService.getList();
        for (DictionaryDataEntity entity : dictionList) {
            FlowTreeModel model = new FlowTreeModel();
            model.setFullName(entity.getFullName());
            model.setId(entity.getId());
            long num = data.stream().filter(t -> entity.getEnCode().equals(t.getCategory())).count();
            model.setNum(num);
            if (num > 0) {
                modelAll.add(model);
            }
        }
        for (FlowEngineEntity entity : data) {
            FlowTreeModel model = JsonUtil.getJsonToBean(entity, FlowTreeModel.class);
            DictionaryDataEntity dataEntity = dictionList.stream().filter(t -> t.getEnCode().equals(entity.getCategory())).findFirst().orElse(null);
            if (dataEntity != null) {
                model.setParentId(dataEntity.getId());
                UserEntity creatorUser = userList.stream().filter(t -> t.getId().equals(model.getCreatorUser())).findFirst().orElse(null);
                if (creatorUser != null) {
                    model.setCreatorUser(creatorUser.getRealName() + "/" + creatorUser.getAccount());
                } else {
                    model.setCreatorUser("");
                }
                modelAll.add(model);
            }
        }
        List<SumTree<FlowTreeModel>> trees = TreeDotUtils.convertListToTreeDot(modelAll);
        List<FlowEngineListVO> list = JsonUtil.getJsonToList(trees, FlowEngineListVO.class);
        return list;
    }


    private List<FlowEngineVisibleEntity> visibleList(FlowEngineEntity entity) {
        List<FlowEngineVisibleEntity> visibleList = new ArrayList<>();
        if (entity.getFlowTemplateJson() != null) {
            ChildNode childNode = JsonUtil.getJsonToBean(entity.getFlowTemplateJson(), ChildNode.class);
            Properties properties = childNode.getProperties();
            //可见的用户
            for (String user : properties.getInitiator()) {
                FlowEngineVisibleEntity visible = new FlowEngineVisibleEntity();
                visible.setOperatorId(user);
                visible.setOperatorType("user");
                visibleList.add(visible);
            }
            //可见的部门
            for (String position : properties.getInitiatePos()) {
                FlowEngineVisibleEntity visible = new FlowEngineVisibleEntity();
                visible.setOperatorId(position);
                visible.setOperatorType("position");
                visibleList.add(visible);
            }
            //可见的角色
            List<String> roleList = properties.getInitiateRole() != null ? properties.getInitiateRole() : new ArrayList<>();
            for (String role : roleList) {
                FlowEngineVisibleEntity visible = new FlowEngineVisibleEntity();
                visible.setOperatorId(role);
                visible.setOperatorType("role");
                visibleList.add(visible);
            }
        }
        return visibleList;
    }

    @Override
    public FlowExportModel exportData(String id) throws WorkFlowException {
        FlowEngineEntity entity = getInfo(id);
        List<FlowEngineVisibleEntity> visibleList = flowEngineVisibleService.getVisibleFlowList(entity.getId());
        FlowExportModel model = new FlowExportModel();
        model.setFlowEngine(entity);
        model.setVisibleList(visibleList);
        return model;
    }

    @Override
    @Transactional
    public ActionResult ImportData(FlowEngineEntity entity, List<FlowEngineVisibleEntity> visibleList) throws WorkFlowException {
        if (entity != null) {
            if (isExistByFullName(entity.getFullName(), null)) {
                return ActionResult.fail("流程名称不能重复");
            }
            if (isExistByEnCode(entity.getEnCode(), null)) {
                return ActionResult.fail("流程编码不能重复");
            }
            try {
                this.save(entity);
                if (visibleList != null) {
                    for (int i = 0; i < visibleList.size(); i++) {
                        flowEngineVisibleService.save(visibleList.get(i));
                    }
                }
            } catch (Exception e) {
                throw new WorkFlowException("数据已存在");
            }
            return ActionResult.success("导入成功");
        }
        return ActionResult.fail("导入数据格式不正确");
    }
}
