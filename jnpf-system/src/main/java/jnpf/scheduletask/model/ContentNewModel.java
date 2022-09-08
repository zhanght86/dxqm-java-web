package jnpf.scheduletask.model;

import jnpf.database.model.DbLinkEntity;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class ContentNewModel {
    //表达式设置
    private String cron;

    //请求类型
    private String interfaceType = "GET";
    //请求路径
    private String interfaceUrl = "";
    //请求参数
    private List<Map<String, Object>> parameter = new ArrayList<>();
    //数据库
    private String database;
    //数据库连接
    private DbLinkEntity link;
    //数据库密码
    private String password;
    //数据库账号
    private String userName;
    //数据库url
    private String url;

    //存储名称
    private String stored;
    //存储参数
    private List<Map<String, Object>> storedParameter = new ArrayList<>();

}
