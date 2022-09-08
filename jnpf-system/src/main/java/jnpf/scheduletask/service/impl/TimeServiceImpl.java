package jnpf.scheduletask.service.impl;

import jnpf.database.model.DbLinkEntity;
import jnpf.database.util.DbTypeUtil;
import jnpf.database.util.JdbcUtil;
import jnpf.scheduletask.entity.TimeTaskEntity;
import jnpf.scheduletask.entity.TimeTaskLogEntity;
import jnpf.scheduletask.model.ContentNewModel;
import jnpf.scheduletask.service.TimeService;
import jnpf.scheduletask.service.TimeTaskLogService;
import jnpf.scheduletask.service.TimetaskService;
import jnpf.util.*;
import jnpf.util.wxutil.HttpUtil;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Slf4j
@Service
public class TimeServiceImpl implements TimeService {

    @Autowired
    private TimeTaskLogService taskLogService;
    @Autowired
    private TimetaskService taskService;

    @Async
    @Override
    public void storage(ContentNewModel model, String id) {
        System.out.println("进入存储");
        Date startDate = new Date();
        StringBuffer history = new StringBuffer();
        history.append("【" + DateUtil.getNow("+8") + "】" + "【执行开始】 ");
        boolean falg = true;
        String msg = "";
        try {
            StringBuilder sql = new StringBuilder();
            sql.append("{call " + model.getStored() + "(");
            int parameter = model.getStoredParameter().size();
            for (int i = 0; i < parameter; i++) {
                sql.append("?,");
            }
            if (parameter > 0) {
                sql.deleteCharAt(sql.length() - 1);
            }
            sql.append(")}");
            @Cleanup Connection con = null;
            if ("0".equals(model.getDatabase())) {
                con = JdbcUtil.getConn(model.getUserName(), model.getPassword(), model.getUrl());
            } else {
                DbLinkEntity link = model.getLink();
                con = JdbcUtil.getConn(link);
            }
            CallableStatement callStmt = null;
            if (con != null) {
                callStmt = con.prepareCall(sql.toString());
                if (callStmt != null) {
                    for (int i = 0; i < model.getStoredParameter().size(); i++) {
                        Map<String, Object> paramter = model.getStoredParameter().get(i);
                        String value = String.valueOf(paramter.get("value"));
                        callStmt.setString(i + 1, value);
                    }
                    callStmt.execute();
                }
            }
        } catch (Exception e) {
            falg = false;
            msg = e.getMessage();
        }
        history.append("【" + DateUtil.getNow("+8") + "】");
        history.append(falg ? "【执行成功:存储过程调用成功】 " : "【执行失败:" + msg + "】");
        history.append("【" + DateUtil.getNow("+8") + "】" + "【执行结束】");
        TimeTaskLogEntity baskLog = new TimeTaskLogEntity();
        baskLog.setId(RandomUtil.uuId());
        baskLog.setTaskId(id);
        baskLog.setRunTime(startDate);
        baskLog.setDescription(history.toString());
        baskLog.setRunResult(falg ? 0 : 1);
        taskLogService.save(baskLog);
        TimeTaskEntity entity = taskService.getInfo(id);
        List<TimeTaskLogEntity> taskList = taskLogService.getTaskList(id);
        entity.setRunCount(taskList.size());
        entity.setLastModifyTime(startDate);
        entity.setLastRunTime(startDate);
        Date nextTime = DateUtil.getNextCronDate(entity.getExecuteCycleJson(), null);
        entity.setNextRunTime(nextTime);
        taskService.update(id, entity);
    }

    @Async
    @Override
    public void connector(ContentNewModel model, String id, String token) {
        System.out.println("进入接口");
        Date startDate = new Date();
        StringBuffer history = new StringBuffer();
        history.append("【" + DateUtil.getNow("+8") + "】" + "【执行开始】 ");
        String json = null;
        if (model.getParameter() != null) {
            if (model.getParameter().size() != 0) {
                json = JsonUtil.getObjectToString(model.getParameter());
            }
        }
        String faceurl = model.getInterfaceUrl();
        if ("get".equals(model.getInterfaceType().toLowerCase())) {
            faceurl = getUrl(model.getInterfaceUrl(), model.getParameter());
            json = null;
        }
        boolean falg = HttpUtil.httpCronRequest(faceurl, model.getInterfaceType(), json, token);
        history.append("【" + DateUtil.getNow("+8") + "】");
        history.append(falg ? "【执行成功】 " : "【执行失败:无接口】");
        history.append("【" + DateUtil.getNow("+8") + "】" + "【执行结束】");
        TimeTaskLogEntity baskLog = new TimeTaskLogEntity();
        baskLog.setId(RandomUtil.uuId());
        baskLog.setTaskId(id);
        baskLog.setRunTime(startDate);
        baskLog.setDescription(history.toString());
        baskLog.setRunResult(falg ? 0 : 1);
        taskLogService.save(baskLog);
        TimeTaskEntity entity = taskService.getInfo(id);
        List<TimeTaskLogEntity> taskList = taskLogService.getTaskList(id);
        entity.setRunCount(taskList.size());
        entity.setLastModifyTime(startDate);
        entity.setLastRunTime(startDate);
        Date nextTime = DateUtil.getNextCronDate(entity.getExecuteCycleJson(), null);
        entity.setNextRunTime(nextTime);
        taskService.update(id, entity);
    }

    /**
     * get的参数拼接到url
     *
     * @param url
     * @param params
     * @return
     */
    private static String getUrl(String url, List<Map<String, Object>> params) {
        StringBuilder urlStringBuilder = new StringBuilder(url);
        try {
            if (params.size() > 0) {
                urlStringBuilder.append("?");
                for (Map<String, Object> param : params) {
                    if (StringUtil.isNotEmpty(String.valueOf(param.get("key")))) {
                        urlStringBuilder.append(param.get("key")).append("=").append(param.get("value")).append("&");
                    }
                }
                String substring = urlStringBuilder.substring(0, urlStringBuilder.length() - 1);
                return substring;
            }
        } catch (Exception e) {
            log.error("url错误{}", e.getMessage());
        }
        return url;
    }
}
