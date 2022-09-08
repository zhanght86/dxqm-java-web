package jnpf.engine.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.util.FlowNature;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.util.DbTypeUtil;
import jnpf.engine.enums.*;
import jnpf.permission.entity.UserRelationEntity;
import jnpf.exception.WorkFlowException;
import jnpf.permission.service.UserRelationService;
import jnpf.util.*;
import jnpf.util.JsonUtil;
import jnpf.engine.entity.*;
import jnpf.engine.mapper.FlowTaskMapper;
import jnpf.engine.model.flowtask.FlowTaskWaitListModel;
import jnpf.engine.model.flowtask.PaginationFlowTask;
import jnpf.engine.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 流程任务
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Slf4j
@Service
public class FlowTaskServiceImpl extends ServiceImpl<FlowTaskMapper, FlowTaskEntity> implements FlowTaskService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private UserRelationService userRelationService;
    @Autowired
    private FlowDelegateService flowDelegateService;
    @Autowired
    private FlowTaskNodeService flowTaskNodeService;
    @Autowired
    private FlowTaskOperatorService flowTaskOperatorService;
    @Autowired
    private FlowTaskOperatorRecordService flowTaskOperatorRecordService;
    @Autowired
    private FlowTaskCirculateService flowTaskCirculateService;
    @Autowired
    private DataSourceUtil dataSourceUtil;

    @Override
    public List<FlowTaskEntity> getMonitorList(PaginationFlowTask paginationFlowTask) {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().gt(FlowTaskEntity::getStatus, FlowTaskStatusEnum.Draft.getCode());
        //关键字（流程名称、流程编码）
        String keyWord = paginationFlowTask.getKeyword() != null ? paginationFlowTask.getKeyword() : null;
        if (!StringUtils.isEmpty(keyWord)) {
            queryWrapper.lambda().and(
                    t -> t.like(FlowTaskEntity::getEnCode, keyWord)
                            .or().like(FlowTaskEntity::getFullName, keyWord)
            );
        }
        //日期范围（近7天、近1月、近3月、自定义）
        String startTime = paginationFlowTask.getStartTime() != null ? paginationFlowTask.getStartTime() : null;
        String endTime = paginationFlowTask.getEndTime() != null ? paginationFlowTask.getEndTime() : null;
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimes = DateUtil.stringToDate(DateUtil.daFormatYmd(Long.parseLong(startTime)) + " 00:00:00");
            Date endTimes = DateUtil.stringToDate(DateUtil.daFormatYmd(Long.parseLong(endTime)) + " 23:59:59");
            queryWrapper.lambda().ge(FlowTaskEntity::getCreatorTime, startTimes).le(FlowTaskEntity::getCreatorTime, endTimes);
        }
        //所属流程
        String flowId = paginationFlowTask.getFlowId() != null ? paginationFlowTask.getFlowId() : null;
        if (!StringUtils.isEmpty(flowId)) {
            queryWrapper.lambda().eq(FlowTaskEntity::getFlowId, flowId);
        }
        //所属分类
        String flowCategory = paginationFlowTask.getFlowCategory() != null ? paginationFlowTask.getFlowCategory() : null;
        if (!StringUtils.isEmpty(flowCategory)) {
            queryWrapper.lambda().eq(FlowTaskEntity::getFlowCategory, flowCategory);
        }
        //发起人员
        String creatorUserId = paginationFlowTask.getCreatorUserId() != null ? paginationFlowTask.getCreatorUserId() : null;
        if (!StringUtils.isEmpty(creatorUserId)) {
            queryWrapper.lambda().eq(FlowTaskEntity::getCreatorUserId, creatorUserId);
        }
        //排序
        if ("desc".equals(paginationFlowTask.getSort().toLowerCase())) {
            queryWrapper.lambda().orderByDesc(FlowTaskEntity::getCreatorTime);
        } else {
            queryWrapper.lambda().orderByAsc(FlowTaskEntity::getCreatorTime);
        }
        Page<FlowTaskEntity> page = new Page<>(paginationFlowTask.getCurrentPage(), paginationFlowTask.getPageSize());
        IPage<FlowTaskEntity> flowTaskEntityPage = this.page(page, queryWrapper);
        return paginationFlowTask.setData(flowTaskEntityPage.getRecords(), page.getTotal());
    }

    @Override
    public List<FlowTaskEntity> getLaunchList(PaginationFlowTask paginationFlowTask) {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        String userId = userProvider.get().getUserId();
        queryWrapper.lambda().eq(FlowTaskEntity::getCreatorUserId, userId);
        //关键字（流程名称、流程编码）
        String keyWord = paginationFlowTask.getKeyword() != null ? paginationFlowTask.getKeyword() : null;
        if (!StringUtils.isEmpty(keyWord)) {
            queryWrapper.lambda().and(
                    t -> t.like(FlowTaskEntity::getEnCode, keyWord)
                            .or().like(FlowTaskEntity::getFullName, keyWord)
            );
        }
        //日期范围（近7天、近1月、近3月、自定义）
        String startTime = paginationFlowTask.getStartTime() != null ? paginationFlowTask.getStartTime() : null;
        String endTime = paginationFlowTask.getEndTime() != null ? paginationFlowTask.getEndTime() : null;
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimes = DateUtil.stringToDate(DateUtil.daFormatYmd(Long.parseLong(startTime)) + " 00:00:00");
            Date endTimes = DateUtil.stringToDate(DateUtil.daFormatYmd(Long.parseLong(endTime)) + " 23:59:59");
            queryWrapper.lambda().ge(FlowTaskEntity::getCreatorTime, startTimes).le(FlowTaskEntity::getCreatorTime, endTimes);
        }
        //所属流程
        String flowName = paginationFlowTask.getFlowId() != null ? paginationFlowTask.getFlowId() : null;
        if (!StringUtils.isEmpty(flowName)) {
            queryWrapper.lambda().eq(FlowTaskEntity::getFlowId, flowName);
        }
        //所属分类
        String flowCategory = paginationFlowTask.getFlowCategory() != null ? paginationFlowTask.getFlowCategory() : null;
        if (!StringUtils.isEmpty(flowCategory)) {
            queryWrapper.lambda().eq(FlowTaskEntity::getFlowCategory, flowCategory);
        }
        //排序
        if ("asc".equals(paginationFlowTask.getSort().toLowerCase())) {
            queryWrapper.lambda().orderByAsc(FlowTaskEntity::getStatus).orderByAsc(FlowTaskEntity::getCreatorTime);
        } else {
            queryWrapper.lambda().orderByAsc(FlowTaskEntity::getStatus).orderByDesc(FlowTaskEntity::getCreatorTime);
        }
        Page<FlowTaskEntity> page = new Page<>(paginationFlowTask.getCurrentPage(), paginationFlowTask.getPageSize());
        IPage<FlowTaskEntity> flowTaskEntityPage = this.page(page, queryWrapper);
        return paginationFlowTask.setData(flowTaskEntityPage.getRecords(), page.getTotal());
    }

    @Override
    public List<FlowTaskEntity> getWaitList(PaginationFlowTask paginationFlowTask) {
        String userId = userProvider.get().getUserId();
        StringBuilder dbSql = new StringBuilder();
        //查询自己的待办
        dbSql.append(" AND (");
        dbSql.append("o.F_HandleId = '" + userId + "' ");
        //委托审核
        List<FlowDelegateEntity> flowDelegateList = flowDelegateService.getUser(userId);
        if (flowDelegateList.size() > 0) {
            dbSql.append(" OR ");
            for (int i = 0; i < flowDelegateList.size(); i++) {
                FlowDelegateEntity delegateEntity = flowDelegateList.get(i);
                //委托的人
                dbSql.append(" o.F_HandleId = '" + delegateEntity.getCreatorUserId() + "' ");
                if (flowDelegateList.size() - 1 > i) {
                    dbSql.append(" OR ");
                }
            }
            dbSql.append(")");
        } else {
            dbSql.append(")");
        }
        //关键字（流程名称、流程编码）
        String keyWord = paginationFlowTask.getKeyword() != null ? paginationFlowTask.getKeyword() : null;
        if (!StringUtils.isEmpty(keyWord)) {
            dbSql.append(" AND (t.F_EnCode like '%" + keyWord + "%' or t.F_FullName like '%" + keyWord + "%') ");
        }
        //日期范围（近7天、近1月、近3月、自定义）
        String startTime = paginationFlowTask.getStartTime() != null ? paginationFlowTask.getStartTime() : null;
        String endTime = paginationFlowTask.getEndTime() != null ? paginationFlowTask.getEndTime() : null;
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            if (DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE)) {
                String startTimes = DateUtil.daFormatYmd(Long.parseLong(startTime)) + " 00:00:00";
                String endTimes = DateUtil.daFormatYmd(Long.parseLong(endTime)) + " 23:59:59";
                dbSql.append(" AND o.F_CreatorTime Between TO_DATE('" + startTimes + "','yyyy-mm-dd HH24:mi:ss') AND TO_DATE('" + endTimes + "','yyyy-mm-dd HH24:mi:ss') ");
            } else {
                String startTimes = DateUtil.daFormatYmd(Long.parseLong(startTime)) + " 00:00:00";
                String endTimes = DateUtil.daFormatYmd(Long.parseLong(endTime)) + " 23:59:59";
                dbSql.append(" AND o.F_CreatorTime Between '" + startTimes + "' AND '" + endTimes + "' ");
            }
        }
        //所属流程
        String flowId = paginationFlowTask.getFlowId() != null ? paginationFlowTask.getFlowId() : null;
        if (!StringUtils.isEmpty(flowId)) {
            dbSql.append(" AND t.F_FlowId = '" + flowId + "'");
        }
        //所属分类
        String flowCategory = paginationFlowTask.getFlowCategory() != null ? paginationFlowTask.getFlowCategory() : null;
        if (!StringUtils.isEmpty(flowCategory)) {
            dbSql.append(" AND t.F_FlowCategory = '" + flowCategory + "'");
        }
        //发起人员
        String creatorUserId = paginationFlowTask.getCreatorUserId() != null ? paginationFlowTask.getCreatorUserId() : null;
        if (!StringUtils.isEmpty(creatorUserId)) {
            dbSql.append(" AND t.F_CreatorUserId = '" + creatorUserId + "'");
        }
        //排序
        StringBuilder orderBy = new StringBuilder();
        if ("desc".equals(paginationFlowTask.getSort().toLowerCase())) {
            orderBy.append(" Order by F_CreatorTime DESC");
        } else {
            orderBy.append(" Order by F_CreatorTime ASC");
        }
        String sql = dbSql.toString() + " " + orderBy.toString();
        List<FlowTaskWaitListModel> data = this.baseMapper.getWaitList(sql);
        List<FlowTaskEntity> result = new LinkedList<>();
        for (FlowTaskWaitListModel model : data) {
            List<Date> list = StringUtil.isNotEmpty(model.getDescription()) ? JsonUtil.getJsonToList(model.getDescription(), Date.class) : new ArrayList<>();
            FlowTaskEntity entity = JsonUtil.getJsonToBean(model, FlowTaskEntity.class);
            boolean delegate = true;
            boolean isuser = model.getHandleId().equals(userId);
            entity.setFullName(!isuser ? entity.getFullName() + "(委托)" : entity.getFullName());
            List<FlowDelegateEntity> flowList = flowDelegateList.stream().filter(t -> t.getFlowId().equals(model.getFlowId())).collect(Collectors.toList());
            //判断是否有自己审核
            if (!isuser) {
                //是否委托当前流程引擎 true是 flas否
                delegate = flowList.stream().filter(t -> t.getCreatorUserId().equals(model.getHandleId())).count() > 0;
            }
            if (delegate) {
                result.add(entity);
                Date date = new Date();
                boolean del = list.stream().filter(t -> t.getTime() > date.getTime()).count() > 0;
                if (del) {
                    result.remove(entity);
                }
            }
        }
        //返回数据
        return paginationFlowTask.setData(PageUtil.getListPage((int) paginationFlowTask.getCurrentPage(), (int) paginationFlowTask.getPageSize(), result), result.size());
    }

    @Override
    public List<FlowTaskEntity> getTrialList(PaginationFlowTask paginationFlowTask) {
        String userId = userProvider.get().getUserId();
        Map<String, Object> queryParam = new HashMap<>(16);
        StringBuilder dbSql = new StringBuilder();
        //关键字（流程名称、流程编码）
        String keyWord = paginationFlowTask.getKeyword() != null ? paginationFlowTask.getKeyword() : null;
        if (!StringUtils.isEmpty(keyWord)) {
            dbSql.append(" AND (t.F_EnCode like '%" + keyWord + "%' or t.F_FullName like '%" + keyWord + "%') ");
        }
        //日期范围（近7天、近1月、近3月、自定义）
        String startTime = paginationFlowTask.getStartTime() != null ? paginationFlowTask.getStartTime() : null;
        String endTime = paginationFlowTask.getEndTime() != null ? paginationFlowTask.getEndTime() : null;
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            if (DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE)) {
                String startTimes = DateUtil.daFormatYmd(Long.parseLong(startTime)) + " 00:00:00";
                String endTimes = DateUtil.daFormatYmd(Long.parseLong(endTime)) + " 23:59:59";
                dbSql.append(" AND r.F_HandleTime Between TO_DATE('" + startTimes + "','yyyy-mm-dd HH24:mi:ss') AND TO_DATE('" + endTimes + "','yyyy-mm-dd HH24:mi:ss') ");
            } else {
                String startTimes = DateUtil.daFormatYmd(Long.parseLong(startTime)) + " 00:00:00";
                String endTimes = DateUtil.daFormatYmd(Long.parseLong(endTime)) + " 23:59:59";
                dbSql.append(" AND r.F_HandleTime Between '" + startTimes + "' AND '" + endTimes + "' ");
            }
        }
        //所属流程
        String flowId = paginationFlowTask.getFlowId() != null ? paginationFlowTask.getFlowId() : null;
        if (!StringUtils.isEmpty(flowId)) {
            dbSql.append(" AND t.F_FlowId = '" + flowId + "' ");
        }
        //所属分类
        String flowCategory = paginationFlowTask.getFlowCategory() != null ? paginationFlowTask.getFlowCategory() : null;
        if (!StringUtils.isEmpty(flowCategory)) {
            dbSql.append(" AND t.F_FlowCategory = '" + flowCategory + "' ");
        }
        //发起人员
        String creatorUserId = paginationFlowTask.getCreatorUserId() != null ? paginationFlowTask.getCreatorUserId() : null;
        if (!StringUtils.isEmpty(creatorUserId)) {
            dbSql.append(" AND t.F_CreatorUserId = '" + creatorUserId + "' ");
        }
        //排序
        if ("desc".equals(paginationFlowTask.getSort().toLowerCase())) {
            dbSql.append(" Order by t.F_LastModifyTime DESC ");
        } else {
            dbSql.append(" Order by t.F_LastModifyTime ASC ");
        }
        dbSql.append(", F_CreatorTime desc");
        queryParam.put("handleId", userId);
        queryParam.put("sql", dbSql.toString());
        List<FlowTaskEntity> data = this.baseMapper.getTrialList(queryParam);
        return paginationFlowTask.setData(PageUtil.getListPage((int) paginationFlowTask.getCurrentPage(), (int) paginationFlowTask.getPageSize(), data), data.size());
    }

    @Override
    public List<FlowTaskEntity> getTrialList() {
        String userId = userProvider.get().getUserId();
        Map<String, Object> queryParam = new HashMap<>(16);
        StringBuilder dbSql = new StringBuilder();
        queryParam.put("handleId", userId);
        queryParam.put("sql", dbSql.toString());
        List<FlowTaskEntity> data = this.baseMapper.getTrialList(queryParam);
        return data;
    }

    @Override
    public List<FlowTaskEntity> getWaitList() {
        String userId = userProvider.get().getUserId();
        StringBuilder dbSql = new StringBuilder();
        //查询自己的待办
        dbSql.append(" AND (");
        dbSql.append("o.F_HandleId = '" + userId + "' ");
        //委托审核
        List<FlowDelegateEntity> flowDelegateList = flowDelegateService.getUser(userId);
        if (flowDelegateList.size() > 0) {
            dbSql.append(" OR ");
            for (int i = 0; i < flowDelegateList.size(); i++) {
                FlowDelegateEntity delegateEntity = flowDelegateList.get(i);
                //委托的人
                dbSql.append(" o.F_HandleId = '" + delegateEntity.getCreatorUserId() + "' ");
                if (flowDelegateList.size() - 1 > i) {
                    dbSql.append(" OR ");
                }
            }
            dbSql.append(")");
        } else {
            dbSql.append(")");
        }
        List<FlowTaskWaitListModel> data = this.baseMapper.getWaitList(dbSql.toString());
        //返回数据
        List<FlowTaskEntity> result = JsonUtil.getJsonToList(data, FlowTaskEntity.class);
        return result;
    }

    @Override
    public List<FlowTaskEntity> getAllWaitList() {
        StringBuilder dbSql = new StringBuilder();
        List<FlowTaskWaitListModel> data = this.baseMapper.getWaitList(dbSql.toString());
        List<FlowTaskEntity> result = JsonUtil.getJsonToList(data, FlowTaskEntity.class);
        return result;
    }

    @Override
    public List<FlowTaskEntity> getCirculateList(PaginationFlowTask paginationFlowTask) {
        String userId = userProvider.get().getUserId();
        QueryWrapper<UserRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserRelationEntity::getUserId, userId);
        List<UserRelationEntity> list = userRelationService.list(queryWrapper);
        List<String> userRelationList = list.stream().map(u -> u.getObjectId()).collect(Collectors.toList());
        String[] objectId = (String.join(",", userRelationList) + "," + userId).split(",");
        //传阅人员
        StringBuilder dbSql = new StringBuilder();
        dbSql.append(" AND (");
        for (int i = 0; i < objectId.length; i++) {
            dbSql.append("c.F_ObjectId = '" + objectId[i] + "'");
            if (objectId.length - 1 > i) {
                dbSql.append(" OR ");
            }
        }
        dbSql.append(")");
        //关键字（流程名称、流程编码）
        String keyWord = paginationFlowTask.getKeyword() != null ? paginationFlowTask.getKeyword() : null;
        if (!StringUtils.isEmpty(keyWord)) {
            dbSql.append(" AND (t.F_EnCode like " + " '%" + keyWord + "%' " + " or t.F_FullName like" + " '%" + keyWord + "%') ");
        }
        //日期范围（近7天、近1月、近3月、自定义）
        String startTime = paginationFlowTask.getStartTime() != null ? paginationFlowTask.getStartTime() : null;
        String endTime = paginationFlowTask.getEndTime() != null ? paginationFlowTask.getEndTime() : null;
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            if (DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE)) {
                String startTimes = DateUtil.daFormatYmd(Long.parseLong(startTime)) + " 00:00:00";
                String endTimes = DateUtil.daFormatYmd(Long.parseLong(endTime)) + " 23:59:59";
                dbSql.append(" AND c.F_CreatorTime Between TO_DATE('" + startTimes + "','yyyy-mm-dd HH24:mi:ss') AND TO_DATE('" + endTimes + "','yyyy-mm-dd HH24:mi:ss') ");
            } else {
                String startTimes = DateUtil.daFormatYmd(Long.parseLong(startTime)) + " 00:00:00";
                String endTimes = DateUtil.daFormatYmd(Long.parseLong(endTime)) + " 23:59:59";
                dbSql.append(" AND c.F_CreatorTime Between  '" + startTimes + "' AND '" + endTimes + "' ");
            }
        }
        //所属流程
        String flowId = paginationFlowTask.getFlowId() != null ? paginationFlowTask.getFlowId() : null;
        if (!StringUtils.isEmpty(flowId)) {
            dbSql.append(" AND t.F_FlowId = '" + flowId + "'");
        }
        //所属分类
        String flowCategory = paginationFlowTask.getFlowCategory() != null ? paginationFlowTask.getFlowCategory() : null;
        if (!StringUtils.isEmpty(flowCategory)) {
            dbSql.append(" AND t.F_FlowCategory = '" + flowCategory + "'");
        }
        //发起人员
        String creatorUserId = paginationFlowTask.getCreatorUserId() != null ? paginationFlowTask.getCreatorUserId() : null;
        if (!StringUtils.isEmpty(creatorUserId)) {
            dbSql.append(" AND t.F_CreatorUserId = '" + creatorUserId + "'");
        }
        //排序
        if ("desc".equals(paginationFlowTask.getSort().toLowerCase())) {
            dbSql.append(" Order by F_LastModifyTime DESC");
        } else {
            dbSql.append(" Order by F_LastModifyTime ASC");
        }
        List<FlowTaskEntity> data = this.baseMapper.getCirculateList(dbSql.toString());
        return paginationFlowTask.setData(PageUtil.getListPage((int) paginationFlowTask.getCurrentPage(), (int) paginationFlowTask.getPageSize(), data), data.size());
    }

    @Override
    public FlowTaskEntity getInfo(String id) throws WorkFlowException {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(
                t -> t.eq(FlowTaskEntity::getId, id)
                        .or().eq(FlowTaskEntity::getProcessId, id)
        );
        FlowTaskEntity entity = this.getOne(queryWrapper);
        if (entity == null) {
            throw new WorkFlowException("未找到流程任务");
        }
        return entity;
    }

    @Override
    public FlowTaskEntity getInfoSubmit(String id) {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().and(
                t -> t.eq(FlowTaskEntity::getId, id)
                        .or().eq(FlowTaskEntity::getProcessId, id)
        );
        return this.getOne(queryWrapper);
    }

    @Override
    public void delete(FlowTaskEntity entity) throws WorkFlowException {
        if (!checkStatus(entity.getStatus())) {
            throw new WorkFlowException("当前流程正在运行不能删除");
        } else {
            this.removeById(entity.getId());
            QueryWrapper<FlowTaskNodeEntity> node = new QueryWrapper<>();
            node.lambda().eq(FlowTaskNodeEntity::getTaskId, entity.getId());
            flowTaskNodeService.remove(node);
            QueryWrapper<FlowTaskOperatorEntity> operator = new QueryWrapper<>();
            operator.lambda().eq(FlowTaskOperatorEntity::getTaskId, entity.getId());
            flowTaskOperatorService.remove(operator);
            QueryWrapper<FlowTaskOperatorRecordEntity> record = new QueryWrapper<>();
            record.lambda().eq(FlowTaskOperatorRecordEntity::getTaskId, entity.getId());
            flowTaskOperatorRecordService.remove(record);
            QueryWrapper<FlowTaskCirculateEntity> circulate = new QueryWrapper<>();
            circulate.lambda().eq(FlowTaskCirculateEntity::getTaskId, entity.getId());
            flowTaskCirculateService.remove(circulate);
        }
    }

    @Override
    public void deleteChild(FlowTaskEntity entity) {
        this.removeById(entity.getId());
        QueryWrapper<FlowTaskNodeEntity> node = new QueryWrapper<>();
        node.lambda().eq(FlowTaskNodeEntity::getTaskId, entity.getId());
        flowTaskNodeService.remove(node);
        QueryWrapper<FlowTaskOperatorEntity> operator = new QueryWrapper<>();
        operator.lambda().eq(FlowTaskOperatorEntity::getTaskId, entity.getId());
        flowTaskOperatorService.remove(operator);
        QueryWrapper<FlowTaskOperatorRecordEntity> record = new QueryWrapper<>();
        record.lambda().eq(FlowTaskOperatorRecordEntity::getTaskId, entity.getId());
        flowTaskOperatorRecordService.remove(record);
        QueryWrapper<FlowTaskCirculateEntity> circulate = new QueryWrapper<>();
        circulate.lambda().eq(FlowTaskCirculateEntity::getTaskId, entity.getId());
        flowTaskCirculateService.remove(circulate);
    }

    @Override
    public void delete(String[] ids) throws WorkFlowException {
        if (ids.length > 0) {
            List<FlowTaskEntity> flowTaskList  = getOrderStaList(Arrays.asList(ids));
            boolean isDel = flowTaskList.stream().filter(t->t.getFlowType()==1 ).count()>0;
            if(isDel){
                throw new WorkFlowException("功能流程不能删除");
            }
            isDel = flowTaskList.stream().filter(t->!FlowNature.ParentId.equals(t.getParentId()) && StringUtil.isNotEmpty(t.getParentId())).count()>0;
            if(isDel){
                throw new WorkFlowException("子表数据不能删除");
            }
            QueryWrapper<FlowTaskEntity> task = new QueryWrapper<>();
            task.lambda().in(FlowTaskEntity::getId, ids);
            this.remove(task);
            QueryWrapper<FlowTaskNodeEntity> node = new QueryWrapper<>();
            node.lambda().in(FlowTaskNodeEntity::getTaskId, ids);
            flowTaskNodeService.remove(node);
            QueryWrapper<FlowTaskOperatorEntity> operator = new QueryWrapper<>();
            operator.lambda().in(FlowTaskOperatorEntity::getTaskId, ids);
            flowTaskOperatorService.remove(operator);
            QueryWrapper<FlowTaskOperatorRecordEntity> record = new QueryWrapper<>();
            record.lambda().in(FlowTaskOperatorRecordEntity::getTaskId, ids);
            flowTaskOperatorRecordService.remove(record);
            QueryWrapper<FlowTaskCirculateEntity> circulate = new QueryWrapper<>();
            circulate.lambda().in(FlowTaskCirculateEntity::getTaskId, ids);
            flowTaskCirculateService.remove(circulate);
        }
    }

    @Override
    public List<FlowTaskEntity> getTaskList(String id) {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FlowTaskEntity::getFlowId, id);
        return this.list(queryWrapper);
    }

    @Override
    public List<FlowTaskEntity> getOrderStaList(List<String> id) {
        List<FlowTaskEntity> list = new ArrayList<>();
        if (id.size() > 0) {
            QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().in(FlowTaskEntity::getId, id);
            list = this.list(queryWrapper);
        }
        return list;
    }

    @Override
    public List<FlowTaskEntity> getChildList(String id) {
        QueryWrapper<FlowTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().in(FlowTaskEntity::getParentId, id);
        return this.list(queryWrapper);
    }

    /**
     * 验证有效状态
     *
     * @param status 状态编码
     * @return
     */
    private boolean checkStatus(int status) {
        if (status == FlowTaskStatusEnum.Draft.getCode() || status == FlowTaskStatusEnum.Reject.getCode() || status == FlowTaskStatusEnum.Revoke.getCode()) {
            return true;
        } else {
            return false;
        }
    }

}
