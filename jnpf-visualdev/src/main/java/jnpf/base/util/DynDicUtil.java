package jnpf.base.util;

import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DictionaryDataService;
import jnpf.base.entity.DictionaryDataEntity;
import jnpf.util.CacheKeyUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RedisUtil;
import jnpf.util.StringUtil;
import jnpf.util.context.SpringContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
@Component
public class DynDicUtil {

    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CacheKeyUtil cacheKeyUtil;
    @Autowired
    private DictionaryDataService dictionaryDataService;
    @Autowired
    private DataInterfaceService dataInterfaceService;


    public final String regEx="[\\[\\]\"]";

    /**
     * 获取数据字典数据
     * @param feild
     * @return
     */
    public  String getDicName(String feild){
        if(redisUtil.exists(cacheKeyUtil.getDictionary()+feild)){
            return redisUtil.getString(cacheKeyUtil.getDictionary()+feild).toString();
        }
        if(StringUtil.isNotEmpty(feild)){
        //去除中括号以及双引号
        feild=feild.replaceAll(regEx,"");
        //判断多选框
        String[] feilds=feild.split(",");
        if(feilds.length>1){
            StringBuilder feildsValue=new StringBuilder();
            DictionaryDataEntity dictionaryDataEntity;
            for(String feil:feilds){
                dictionaryDataEntity=dictionaryDataService.getInfo(feil);
                if(dictionaryDataEntity!=null){
                    feildsValue.append(dictionaryDataEntity.getFullName()+"/");
                }
            }
            String finalValue ;
            if (StringUtil.isEmpty(feildsValue) || feildsValue.equals("")){
                finalValue = feildsValue.toString();
            }else {
                finalValue = feildsValue.substring(0, feildsValue.length() - 1);
            }
            redisUtil = SpringContext.getBean(RedisUtil.class);
            redisUtil.insert(cacheKeyUtil.getDictionary()+feild,finalValue,20);
            return finalValue;
        }
        DictionaryDataEntity dictionaryDataentity=dictionaryDataService.getInfo(feild);
        if(dictionaryDataentity!=null){
            redisUtil = SpringContext.getBean(RedisUtil.class);
            redisUtil.insert(cacheKeyUtil.getDictionary()+feild,dictionaryDataentity.getFullName(),20);
            return dictionaryDataentity.getFullName();
        }
            return feild;
        }
        return feild;
    }

    /**
     * 获取远端数据
     * @param urlId
     * @param label
     * @param value
     * @param feildValue
     * @return
     * @throws IOException
     */
    public  String getDynName(String urlId,String label,String value,String feildValue) throws IOException {
        String rediskey= cacheKeyUtil.getDynamic()+"_"+urlId+"_"+feildValue;
        if (redisUtil.exists(rediskey)) {
            return redisUtil.getString(rediskey).toString();
        }
        if (StringUtil.isNotEmpty(feildValue)) {
            //去除中括号以及双引号
            feildValue = feildValue.replaceAll(regEx, "");
            //获取远端数据
            Object object = dataInterfaceService.infoToId(urlId);
            Map<String, Object> dynamicMap = JsonUtil.entityToMap(object);
            if (dynamicMap.get("data") != null) {
                List<Map<String, Object>> dataList = JsonUtil.getJsonToListMap(dynamicMap.get("data").toString());
                //判断是否多选
                String[] feildValues = feildValue.split(",");
                if (feildValues.length > 0) {
                    //转换的真实值
                    StringBuilder feildVa = new StringBuilder();
                    for (String feild : feildValues) {
                        for (Map<String, Object> data : dataList) {
                            if (String.valueOf(data.get(value)).equals(feild)) {
                                feildVa.append(data.get(label) + "/");
                            }
                        }
                    }
                    String finalValue ;
                    if (StringUtil.isEmpty(feildVa) || feildVa.equals("")){
                        finalValue = feildVa.toString();
                    }else {
                        finalValue = feildVa.substring(0, feildVa.length() - 1);
                    }
                    redisUtil = SpringContext.getBean(RedisUtil.class);
                    redisUtil.insert(rediskey, finalValue, 20);
                    return finalValue;
                }
                for (Map<String, Object> data : dataList) {
                    if (feildValue.equals(String.valueOf(data.get(value)))) {
                        redisUtil = SpringContext.getBean(RedisUtil.class);
                        redisUtil.insert(rediskey, data.get(label).toString(), 20);
                        return data.get(label).toString();
                    }
                    return feildValue;
                }
            }
            return feildValue;
        }
        return feildValue;
    }
}
