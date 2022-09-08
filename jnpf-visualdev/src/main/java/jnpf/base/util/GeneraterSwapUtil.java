package jnpf.base.util;

import jnpf.permission.entity.OrganizeEntity;
import jnpf.permission.entity.PositionEntity;
import jnpf.permission.entity.UserEntity;
import jnpf.permission.model.user.UserAllModel;
import jnpf.permission.service.OrganizeService;
import jnpf.permission.service.PositionService;
import jnpf.permission.service.UserService;
import jnpf.util.StringUtil;
import jnpf.util.context.SpringContext;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/3/16
 */
@Component
public class GeneraterSwapUtil {


    private OrganizeService organizeService;


    private PositionService positionService;


    private UserService userService;

    /**
     * 日期时间戳字符串转换
     *
     * @param date
     * @param format
     * @return
     */
    public String dateSwap(String date, String format) {
        if (StringUtil.isNotEmpty(date)) {
            DateTimeFormatter ftf = DateTimeFormatter.ofPattern(format);
            if (date.contains(",")) {
                String[] dates = date.split(",");
                long time1 = Long.parseLong(dates[0]);
                long time2 = Long.parseLong(dates[1]);
                String value1 = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time1), ZoneId.systemDefault()));
                String value2 = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time2), ZoneId.systemDefault()));
                return value1 + "至" + value2;
            }
            long time = Long.parseLong(date);
            String value = ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
            return value;
        }
        return date;
    }

    /**
     * 行政区划转换
     *
     * @param data
     * @return
     */
    public List<String> provinceData(String data) {
        if (StringUtil.isNotEmpty(data)) {
            String[] strs = data.split(",");
            List<String> provList = new ArrayList(Arrays.asList(strs));
            for (String str : strs) {
                provList.add(str);
            }
            return provList;
        }
        return new ArrayList<>();
    }

    /**
     * 公司部门id转名称
     *
     * @param id
     * @return
     */
    public String comSelectValue(String id) {
        if (StringUtil.isNotEmpty(id)) {
            organizeService = SpringContext.getBean(OrganizeService.class);
            List<OrganizeEntity> orgMapList = organizeService.getOrgRedisList();
            for (OrganizeEntity organizeEntity : orgMapList) {
                if (id.equals(organizeEntity.getId())) {
                    return organizeEntity.getFullName();
                }
            }
            return id;
        }
        return id;
    }

    /**
     * 公司部门id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String comSelectValues(String ids) {
        if (StringUtil.isNotEmpty(ids)) {
            String[] idList = ids.split(",");
            if (idList.length > 0) {
                organizeService = SpringContext.getBean(OrganizeService.class);
                StringBuilder value = new StringBuilder();
                List<OrganizeEntity> orgMapList = organizeService.getOrgRedisList();
                for (String id : idList) {
                    for (OrganizeEntity organizeEntity : orgMapList) {
                        if (id.equals(organizeEntity.getId())) {
                            value.append(organizeEntity.getFullName() + "/");
                        }
                    }
                }
                return value.substring(0, value.length() - 1);
            }
        }
        return ids;
    }


    /**
     * 岗位id转名称
     *
     * @param id
     * @return
     */
    public String posSelectValue(String id) {
        if (StringUtil.isNotEmpty(id)) {
            positionService = SpringContext.getBean(PositionService.class);
            List<PositionEntity> posMapList = positionService.getPosRedisList();
            for (PositionEntity positionEntity : posMapList) {
                if (id.equals(positionEntity.getId())) {
                    return positionEntity.getFullName();
                }
            }
            return id;
        }
        return id;
    }

    /**
     * 岗位id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String posSelectValues(String ids) {
        if (StringUtil.isNotEmpty(ids)) {
            String[] idList = ids.split(",");
            if (idList.length > 0) {
                positionService = SpringContext.getBean(PositionService.class);
                StringBuilder value = new StringBuilder();
                List<PositionEntity> posMapList = positionService.getPosRedisList();
                for (String id : idList) {
                    for (PositionEntity positionEntity : posMapList) {
                        if (id.equals(positionEntity.getId())) {
                            value.append(positionEntity.getFullName() + "/");
                        }
                    }
                }
                return value.substring(0, value.length() - 1);
            }
        }
        return ids;
    }

    /**
     * 用户id转名称
     *
     * @param id
     * @return
     */
    public String userSelectValue(String id) {
        if (StringUtil.isNotEmpty(id)) {
            userService = SpringContext.getBean(UserService.class);
            UserEntity userEntity = userService.getInfo(id);
            return userEntity.getRealName() + "/" + userEntity.getAccount();
        }
        return id;
    }

    /**
     * 用户id转名称(多选)
     *
     * @param ids
     * @return
     */
    public String userSelectValues(String ids) {
        if (StringUtil.isNotEmpty(ids)) {
            String[] idList = ids.split(",");
            if (idList.length > 0) {
                userService = SpringContext.getBean(UserService.class);
                List<UserAllModel> userMapList = userService.getAll();
                StringBuilder value = new StringBuilder();
                for (String id : idList) {
                    for (UserAllModel userAllModel : userMapList) {
                        if (id.equals(userAllModel.getId())) {
                            value.append(userAllModel.getRealName() + "/" + userAllModel.getAccount() + "-");
                        }
                    }
                }
                return value.substring(0, value.length() - 1);
            }
        }
        return ids;
    }

    /**
     * 开关
     * @param data
     * @return
     */
    public String switchSelectValue(String data){
        if (StringUtil.isNotEmpty(data)){
            if (data.equals("0")||data.equals("false")){
                return "关";
            }else if (data.equals("1")||data.equals("true")){
                return "开";
            }else {
                return data;
            }
        }
        return null;
    }
}
