package jnpf.base.util.jobutil;

import jnpf.database.data.DataSourceContextHolder;
import jnpf.scheduletask.model.ContentNewModel;
import jnpf.scheduletask.service.TimeService;
import jnpf.util.StringUtil;
import jnpf.util.context.SpringContext;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;


/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Slf4j
public class TimeJob implements Job {

    @Override
    public void execute(JobExecutionContext context) {
        System.out.println("进入调度");
        JobDataMap dataMap = context.getMergedJobDataMap();
        ContentNewModel model = dataMap.get("content") != null ? (ContentNewModel) dataMap.get("content") : null;
        String type = dataMap.getString("type");
        String id = dataMap.getString("id");
        String tenantId = dataMap.getString("tenantId");
        String token = dataMap.getString("token");
        String tenantDbConnectionString = dataMap.getString("tenantDbConnectionString");
        if (model != null) {
            if (StringUtil.isNotEmpty(tenantId)) {
                DataSourceContextHolder.setDatasource(tenantId, tenantDbConnectionString);
            }
            TimeService timeService = SpringContext.getBean(TimeService.class);
            if ("1".equals(type)) {
                timeService.connector(model, id, token);
            } else {
                timeService.storage(model, id);
            }
        }
    }

}
