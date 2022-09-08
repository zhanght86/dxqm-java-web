package jnpf.base.util.jobutil;

import jnpf.base.UserInfo;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.util.DbTypeUtil;
import jnpf.scheduletask.entity.TimeTaskEntity;
import jnpf.scheduletask.model.ContentNewModel;
import jnpf.database.model.DataSourceUtil;
import jnpf.util.StringUtil;
import org.quartz.JobDataMap;


/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
public class TimeUtil {

    /**
     * cron赋值
     *
     * @param model  调度json
     * @param entity 实体
     */
    public static void task(ContentNewModel model, TimeTaskEntity entity) {
        entity.setExecuteCycleJson(model.getCron());
    }

    /**
     * 数据库连接赋值
     *
     * @param entity         实体
     * @param model          调度json
     * @param dbName         数据库名
     * @param link           数据库实体
     * @param dataSourceUtil 配置
     */
    public static void database(TimeTaskEntity entity, ContentNewModel model, String dbName, DbLinkEntity link, DataSourceUtil dataSourceUtil) {
        if ("2".equals(entity.getExecuteType())) {
            model.setLink(link);
            String url = DbTypeUtil.getUrl(dataSourceUtil,dbName);
            model.setUrl(url);
            model.setUserName(dataSourceUtil.getUserName());
            model.setPassword(dataSourceUtil.getPassword());
        }
    }

    /**
     * 新增调度
     *
     * @param model    调度json
     * @param entity   实体
     * @param userInfo 用户信息
     */
    public static void startJob(ContentNewModel model, UserInfo userInfo, TimeTaskEntity entity, String token) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("content", model);
        jobDataMap.put("tenantId", userInfo.getTenantId());
        jobDataMap.put("tenantDbConnectionString", userInfo.getTenantDbConnectionString());
        jobDataMap.put("id", entity.getId());
        jobDataMap.put("type", entity.getExecuteType());
        jobDataMap.put("token", token);
        if ("1".equals(String.valueOf(entity.getEnabledMark()))) {
            String jobName = entity.getId();
            String jobGroupName = StringUtil.isNotEmpty(userInfo.getTenantId()) ? userInfo.getTenantId() : "jnpf";
            String cron = entity.getExecuteCycleJson();
            if (StringUtil.isNotEmpty(userInfo.getId())) {
                JobManagerNew jobManager = new JobManagerNew();
                jobManager.removeJob(jobName, jobGroupName);
                jobManager.addJob(jobName, jobGroupName, cron, jobDataMap, null, null, TimeJob.class);
            }
        }
    }

    /**
     * 删除调度
     *
     * @param entity   实体
     * @param userInfo 用户信息
     */
    public static void removeJob(TimeTaskEntity entity, UserInfo userInfo) {
        JobManagerNew jobManager = new JobManagerNew();
        String jobName = entity.getId();
        String jobGroupName = StringUtil.isNotEmpty(userInfo.getTenantId()) ? userInfo.getTenantId() : "jnpf";
        if (StringUtil.isNotEmpty(userInfo.getId())) {
            jobManager.removeJob(jobName, jobGroupName);
        }
    }

}
