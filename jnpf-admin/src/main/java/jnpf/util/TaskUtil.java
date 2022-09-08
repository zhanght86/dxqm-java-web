package jnpf.util;

import com.alibaba.fastjson.JSONObject;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.service.DblinkService;
import jnpf.base.util.jobutil.JobManagerNew;
import jnpf.base.util.jobutil.TimeJob;
import jnpf.config.ConfigValueUtil;
import jnpf.database.util.DbTypeUtil;
import jnpf.scheduletask.entity.TimeTaskEntity;
import jnpf.scheduletask.model.ContentNewModel;
import jnpf.scheduletask.service.TimetaskService;
import jnpf.util.context.SpringContext;
import org.quartz.CronExpression;
import org.quartz.JobDataMap;

import java.util.List;

public class TaskUtil {

    public static void task() {
        //多租户下不启动任务调度
        ConfigValueUtil configValueUtil = SpringContext.getBean(ConfigValueUtil.class);
        if (!Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
            TimetaskService timetaskService = SpringContext.getBean(TimetaskService.class);
            DataSourceUtil dataSourceUtil = SpringContext.getBean(DataSourceUtil.class);
            DblinkService dblinkService = SpringContext.getBean(DblinkService.class);
            List<TimeTaskEntity> list = timetaskService.getList();
            for (TimeTaskEntity entity : list) {
                if (isjson(entity.getExecuteContent())) {
                    ContentNewModel model = JsonUtil.getJsonToBean(entity.getExecuteContent(), ContentNewModel.class);
                    model.setPassword(dataSourceUtil.getPassword());
                    model.setUserName(dataSourceUtil.getUserName());
                    if ("0".equals(model.getDatabase())) {
                        String dbName = dataSourceUtil.getDbName();
                        String url = DbTypeUtil.getUrl(dataSourceUtil,dbName);
                        model.setUrl(url);
                    } else {
                        DbLinkEntity link = dblinkService.getInfo(model.getDatabase());
                        model.setLink(link);
                    }
                    JobDataMap jobDataMap = new JobDataMap();
                    jobDataMap.put("content", model);
                    jobDataMap.put("tenantId", "");
                    jobDataMap.put("tenantDbConnectionString", "");
                    jobDataMap.put("id", entity.getId());
                    jobDataMap.put("type", entity.getExecuteType());
                    String jobName = entity.getId();
                    String jobGroupName = "jnpf";
                    String cron = entity.getExecuteCycleJson();
                    if (filterWithCronTime(cron)) {
                        JobManagerNew jobManager = new JobManagerNew();
                        jobManager.addJob(jobName, jobGroupName, cron, jobDataMap, null, null, TimeJob.class);
                    }
                }
            }
        }
    }

    private static Boolean filterWithCronTime(String cron) {
        boolean validExpression = false;
        if (StringUtil.isNotEmpty(cron)) {
            validExpression = CronExpression.isValidExpression(cron);
        }
        return validExpression;
    }

    private static boolean isjson(String string) {
        try {
            JSONObject jsonStr = JSONObject.parseObject(string);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
