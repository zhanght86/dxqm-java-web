package jnpf.scheduletask.model;


import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class FrequencyModel {
    /**
    * 执行日 1.每日 2.每周 3.每月
    */
    private String type;

    /**
    * 小时 1,2,3
    */
    private String hours;

    /**
    * 分 1,2,3
    */
    private String minute;

    /**
    * 周 2
    */
    private String week;

    /**
    * 天 3
    */
    private String day;

    /**
    * 执行月
    */
    private String month;

}
