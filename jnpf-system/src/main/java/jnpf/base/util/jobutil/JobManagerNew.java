package jnpf.base.util.jobutil;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Slf4j
public class JobManagerNew {

    private SchedulerFactory schedulerFactory = new StdSchedulerFactory();

    private String trigger = "tri_";

    /**
     * 新增任务
     *
     * @param jobName      任务名
     * @param jobGroupName 分组名
     * @param cron         表达式
     * @param jobDataMap   数据
     * @param startDate    开始时间
     * @param endDate      结束时间
     */
    public void addJob(String jobName, String jobGroupName, String cron, JobDataMap jobDataMap, Date startDate, Date endDate,Class<? extends Job> jobClass) {
        this.addJob(jobName, jobGroupName, jobClass, cron, jobDataMap, startDate, endDate);
    }

    /**
     * 修改任务
     *
     * @param jobName      任务名
     * @param jobGroupName 分组名
     * @param cron         表达式
     * @param endDate      结束时间
     */
    public void updateJob(String jobName, String jobGroupName, String cron, JobDataMap jobDataMap, Date startDate, Date endDate) {
        this.updateJob(jobName, jobGroupName, trigger + jobName, trigger + jobGroupName, cron, jobDataMap, startDate, endDate);
    }

    /**
     * 移除任务
     *
     * @param jobName      任务名
     * @param jobGroupName 分组名
     */
    public void removeJob(String jobName, String jobGroupName) {
        this.removeJob(jobName, jobGroupName, trigger + jobName, trigger + jobGroupName);
    }

    /**
     * 添加任务
     *
     * @param jobName      任务名
     * @param jobGroupName 分组名
     * @param jobClass     任务
     * @param cron         表达式
     * @param jobDataMap   数据
     * @param endDate      结束时间
     */
    private void addJob(String jobName, String jobGroupName, Class<? extends Job> jobClass, String cron, JobDataMap jobDataMap, Date startDate, Date endDate) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = null;
            if (jobDataMap != null) {
                jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).setJobData(jobDataMap).build();
            } else {
                jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
            }
            // 触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            // 触发器名,触发器组
            triggerBuilder.withIdentity(trigger + jobName, trigger + jobGroupName);
            //开始时间
            triggerBuilder.startNow();
            // 触发器时间设定
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            //结束时间
            if (endDate != null) {
                triggerBuilder.endAt(endDate);
            }
            // 创建Trigger对象
            CronTrigger trigger = (CronTrigger) triggerBuilder.build();
            // 调度容器设置JobDetail和Trigger
            sched.scheduleJob(jobDetail, trigger);
            // 启动
            if (!sched.isShutdown()) {
                sched.start();
                log.info("添加定时任务成功");
            }
        } catch (Exception e) {
            log.error("添加定时任务失败:{}", e.getMessage());
        }
    }


    /**
     * 移除任务
     *
     * @param jobName          任务名
     * @param jobGroupName     分组名
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     */
    private void removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            // 停止触发器
            sched.pauseTrigger(triggerKey);
            // 移除触发器
            sched.unscheduleJob(triggerKey);
            // 删除任务
            sched.deleteJob(JobKey.jobKey(jobName, jobGroupName));
        } catch (Exception e) {
            log.error("移除定时任务失败:{}", e.getMessage());
        }
    }

    /**
     * 修改任务
     *
     * @param jobName          任务名
     * @param jobGroupName     分组名
     * @param triggerName      触发器名
     * @param triggerGroupName 触发器组名
     * @param cron             表达式
     * @param endDate          结束时间
     */
    private void updateJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName, String cron, JobDataMap jobDataMap, Date startDate, Date endDate) {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
            if (trigger == null) {
                return;
            }
            String oldTime = trigger.getCronExpression();
            if (!oldTime.equalsIgnoreCase(cron)) {
                /** 方式一 ：调用 rescheduleJob 开始 */
                // 触发器
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
                // 触发器名,触发器组
                if (jobDataMap != null) {
                    triggerBuilder.withIdentity(triggerName, triggerGroupName).usingJobData(jobDataMap);
                } else {
                    triggerBuilder.withIdentity(triggerName, triggerGroupName);
                }
                //判断是否有开始时间
                triggerBuilder.startNow();
                // 触发器时间设定
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
                //判断是否有结束时间
                if (endDate != null) {
                    triggerBuilder.endAt(endDate);
                }
                // 创建Trigger对象
                trigger = (CronTrigger) triggerBuilder.build();
                // 方式一 ：修改一个任务的触发时间
                sched.rescheduleJob(triggerKey, trigger);
            }
        } catch (Exception e) {
            log.error("修改定时任务失败:{}", e.getMessage());
        }
    }

    /**
     * 判断是否存在
     *
     * @param jobName      计划名称
     * @param jobGroupName 分组名称
     * @return
     */
    private boolean isTriKey(String jobName, String jobGroupName) {
        boolean flag = false;
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            TriggerKey triggerKey = TriggerKey.triggerKey(trigger + jobName, trigger + jobGroupName);
            CronTrigger trigger = (CronTrigger) sched.getTrigger(triggerKey);
            if (trigger != null) {
                flag = true;
            }
        } catch (Exception e) {
            log.error("判断任务调度触发器是否存在:{}", e.getMessage());
        }
        return flag;
    }

    /**
     * 启动所有定时任务
     */
    public void startJobs() {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            sched.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 关闭所有定时任务
     */
    public void shutdownJobs() {
        try {
            Scheduler sched = schedulerFactory.getScheduler();
            if (!sched.isShutdown()) {
                sched.shutdown();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
