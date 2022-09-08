package jnpf.scheduletask.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.Pagination;
import jnpf.base.UserInfo;
import jnpf.base.service.DblinkService;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.util.jobutil.TimeUtil;
import jnpf.scheduletask.entity.TimeTaskEntity;
import jnpf.scheduletask.entity.TimeTaskLogEntity;
import jnpf.scheduletask.mapper.TimeTaskMapper;
import jnpf.scheduletask.model.ContentNewModel;
import jnpf.scheduletask.model.TaskPage;
import jnpf.scheduletask.service.TimeTaskLogService;
import jnpf.scheduletask.service.TimetaskService;
import jnpf.util.*;
import jnpf.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 定时任务
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class TimetaskServiceImpl extends ServiceImpl<TimeTaskMapper, TimeTaskEntity> implements TimetaskService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private DataSourceUtil dataSourceUtil;
    @Autowired
    private TimeTaskLogService timeTaskLogService;
    @Autowired
    private DblinkService dblinkService;

    @Override
    public List<TimeTaskEntity> getList() {
        QueryWrapper<TimeTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TimeTaskEntity::getEnabledMark, 1);
        return this.list(queryWrapper);
    }

    @Override
    public List<TimeTaskEntity> getList(Pagination pagination) {
        QueryWrapper<TimeTaskEntity> queryWrapper = new QueryWrapper<>();
        if (pagination.getKeyword() != null) {
            queryWrapper.lambda().and(
                    t -> t.like(TimeTaskEntity::getEnCode, pagination.getKeyword())
                            .or().like(TimeTaskEntity::getFullName, pagination.getKeyword())
            );
        }
        //排序
        queryWrapper.lambda().orderByDesc(TimeTaskEntity::getCreatorTime);
        Page page = new Page(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<TimeTaskEntity> iPage = this.page(page, queryWrapper);
        List<String> list = iPage.getRecords().stream().map(t -> t.getId()).collect(Collectors.toList());
        List<TimeTaskLogEntity> taskList = timeTaskLogService.getTaskList(list);
        for (TimeTaskEntity entity : iPage.getRecords()) {
            List<TimeTaskLogEntity> collect = taskList.stream().filter(t -> t.getTaskId().equals(entity.getId())).collect(Collectors.toList());
            entity.setRunCount(collect.size());
        }
        return pagination.setData(iPage.getRecords(), page.getTotal());
    }

    @Override
    public List<TimeTaskLogEntity> getTaskLogList(String taskId, TaskPage pagination) {
        QueryWrapper<TimeTaskLogEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TimeTaskLogEntity::getTaskId, taskId);
        //关键字查询
        if (pagination.getKeyword() != null) {
            queryWrapper.lambda().and(
                    t -> t.like(TimeTaskLogEntity::getDescription, pagination.getKeyword())
                            .or().like(TimeTaskLogEntity::getRunResult, pagination.getKeyword())
                            .or().like(TimeTaskLogEntity::getRunTime, pagination.getKeyword())
            );
        }
        //日期范围（近7天、近1月、近3月、自定义）
        String startTime = pagination.getStartTime() != null ? pagination.getStartTime() : null;
        String endTime = pagination.getEndTime() != null ? pagination.getEndTime() : null;
        if (!StringUtils.isEmpty(startTime) && !StringUtils.isEmpty(endTime)) {
            Date startTimes = DateUtil.stringToDate(DateUtil.daFormatYmd(Long.parseLong(startTime)) + " 00:00:00");
            Date endTimes = DateUtil.stringToDate(DateUtil.daFormatYmd(Long.parseLong(endTime)) + " 23:59:59");
            queryWrapper.lambda().ge(TimeTaskLogEntity::getRunTime, startTimes).le(TimeTaskLogEntity::getRunTime, endTimes);
        }
        //排序
        if (StringUtil.isEmpty(pagination.getSidx())) {
            queryWrapper.lambda().orderByDesc(TimeTaskLogEntity::getRunTime);
        } else {
            queryWrapper = "asc".equals(pagination.getSort().toLowerCase()) ? queryWrapper.orderByAsc(pagination.getSidx()) : queryWrapper.orderByDesc(pagination.getSidx());
        }
        if (pagination.getRunResult() != null) {
            queryWrapper.lambda().eq(TimeTaskLogEntity::getRunResult, pagination.getRunResult());
        }
        Page page = new Page(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<TimeTaskLogEntity> iPage = timeTaskLogService.page(page, queryWrapper);
        return pagination.setData(iPage.getRecords(), page.getTotal());
    }

    @Override
    public TimeTaskEntity getInfo(String id) {
        QueryWrapper<TimeTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TimeTaskEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<TimeTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TimeTaskEntity::getFullName, fullName);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(TimeTaskEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String enCode, String id) {
        QueryWrapper<TimeTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(TimeTaskEntity::getEnCode, enCode);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(TimeTaskEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public void create(TimeTaskEntity entity) {
        UserInfo userInfo = userProvider.get();
        String token = UserProvider.getToken();
        entity.setId(RandomUtil.uuId());
        entity.setEnabledMark(1);
        entity.setCreatorUserId(userInfo.getUserId());
        ContentNewModel model = JsonUtil.getJsonToBean(entity.getExecuteContent(), ContentNewModel.class);
        start(model, entity, userInfo, token);
        this.save(entity);
    }

    @Override
    public boolean update(String id, TimeTaskEntity entity) {
        UserInfo userInfo = userProvider.get();
        String token = UserProvider.getToken();
        entity.setId(id);
        entity.setLastModifyTime(DateUtil.getNowDate());
        entity.setLastModifyUserId(userInfo.getUserId());
        if (isjson(entity.getExecuteContent()) && entity.getEnabledMark() == 1) {
            ContentNewModel model = JsonUtil.getJsonToBean(entity.getExecuteContent(), ContentNewModel.class);
            start(model, entity, userInfo, token);
        }else {
            del(entity, userInfo);
        }
        return this.updateById(entity);
    }


    @Override
    public void delete(TimeTaskEntity entity) {
        UserInfo userInfo = userProvider.get();
        del(entity, userInfo);
        this.removeById(entity.getId());
        timeTaskLogService.delete(entity.getId());
    }

    @Override
    @Transactional
    public boolean first(String id) {
        boolean isOk = false;
        //获取要上移的那条数据的信息
        TimeTaskEntity upEntity = this.getById(id);
        Long upSortCode = upEntity.getSortCode() == null ? 0 : upEntity.getSortCode();
        //查询上几条记录
        QueryWrapper<TimeTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .lt(TimeTaskEntity::getSortCode, upSortCode)
                .orderByDesc(TimeTaskEntity::getSortCode);
        List<TimeTaskEntity> downEntity = this.list(queryWrapper);
        if (downEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = upEntity.getSortCode();
            upEntity.setSortCode(downEntity.get(0).getSortCode());
            downEntity.get(0).setSortCode(temp);
            this.updateById(downEntity.get(0));
            this.updateById(upEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    @Transactional
    public boolean next(String id) {
        boolean isOk = false;
        //获取要下移的那条数据的信息
        TimeTaskEntity downEntity = this.getById(id);
        Long upSortCode = downEntity.getSortCode() == null ? 0 : downEntity.getSortCode();
        //查询下几条记录
        QueryWrapper<TimeTaskEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .gt(TimeTaskEntity::getSortCode, upSortCode)
                .orderByAsc(TimeTaskEntity::getSortCode);
        List<TimeTaskEntity> upEntity = this.list(queryWrapper);
        if (upEntity.size() > 0) {
            //交换两条记录的sort值
            Long temp = downEntity.getSortCode();
            downEntity.setSortCode(upEntity.get(0).getSortCode());
            upEntity.get(0).setSortCode(temp);
            this.updateById(upEntity.get(0));
            this.updateById(downEntity);
            isOk = true;
        }
        return isOk;
    }

    @Override
    public void createTaskLog(TimeTaskLogEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setRunTime(DateUtil.getNowDate());
        timeTaskLogService.save(entity);
    }

    /**
     * 判断是否是json格式
     *
     * @param string
     * @return
     */
    private boolean isjson(String string) {
        try {
            JSONObject jsonStr = JSONObject.parseObject(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 启动任务调度
     *
     * @param model
     * @param entity
     * @param userInfo
     */
    private void start(ContentNewModel model, TimeTaskEntity entity, UserInfo userInfo, String token) {
        DbLinkEntity link = dblinkService.getInfo(model.getDatabase());
        String dbName = StringUtil.isEmpty(userInfo.getTenantId()) ? dataSourceUtil.getDbName() : userInfo.getTenantDbConnectionString();
        TimeUtil.database(entity, model, dbName, link, dataSourceUtil);
        TimeUtil.task(model, entity);
        TimeUtil.startJob(model, userInfo, entity,token);
    }

    /**
     * 删除任务调度
     *
     * @param entity
     * @param userInfo
     */
    private void del(TimeTaskEntity entity, UserInfo userInfo){
        TimeUtil.removeJob(entity, userInfo);
    }
}
