package jnpf.base.util;


import jnpf.base.service.DataInterfaceService;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.util.CacheKeyUtil;
import jnpf.util.JsonUtil;
import jnpf.util.RedisUtil;
import jnpf.util.context.SpringContext;

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
public class DynamicUtil {

    private RedisUtil redisUtil;
    private CacheKeyUtil cacheKeyUtil;
    private DataInterfaceService dataInterfaceService;

    /**
     * 获取远端数据转换关键词返回
     * @param model
     * @param keyJsonMap
     * @return
     * @throws IOException
     */
    public Map<String, Object> dynamicKeyData(FieLdsModel model, Map<String, Object> keyJsonMap) throws IOException {

//        long startTime = System.currentTimeMillis(); //获取开始时间
        redisUtil = SpringContext.getBean(RedisUtil.class);
        cacheKeyUtil = SpringContext.getBean(CacheKeyUtil.class);
        if (redisUtil.exists(cacheKeyUtil.getDynamic() + model.getConfig().getPropsUrl())) {
            model = JsonUtil.getJsonToBean(redisUtil.getString(cacheKeyUtil.getDynamic() + model.getConfig().getPropsUrl()).toString(), FieLdsModel.class);
        } else {
            //获取远端数据
            model = dynamicData(model);
            redisUtil.insert(cacheKeyUtil.getDynamic() + model.getConfig().getPropsUrl(), model);
        }
        String keyStr = String.valueOf(keyJsonMap.get(model.getVModel()));

        if (model.getSlot() != null && model.getSlot().getOptions() != null) {
            List<Map<String, Object>> modelOpt = JsonUtil.getJsonToListMap(model.getSlot().getOptions());
            for (Map<String, Object> map : modelOpt) {
                if (map.get(model.getConfig().getProps().getValue()).toString().equals(keyStr)) {
                    keyJsonMap.put(model.getVModel(), map.get(model.getConfig().getProps().getLabel()).toString());
                }

            }
        }
        return keyJsonMap;
    }


    /**
     * 获取远端数据
     * @param model
     * @return
     * @throws IOException
     */
    public FieLdsModel dynamicData(FieLdsModel model) throws IOException {
        dataInterfaceService = SpringContext.getBean(DataInterfaceService.class);
        redisUtil = SpringContext.getBean(RedisUtil.class);
        cacheKeyUtil = SpringContext.getBean(CacheKeyUtil.class);
        if (redisUtil.exists(cacheKeyUtil.getDynamic() + model.getConfig().getPropsUrl())) {
            model = JsonUtil.getJsonToBean(String.valueOf(redisUtil.getString(cacheKeyUtil.getDynamic() + model.getConfig().getPropsUrl())), FieLdsModel.class);
        } else {
            //获取远端数据
            Object object = dataInterfaceService.infoToId(model.getConfig().getPropsUrl());
            Map<String, Object> dynamicMap= JsonUtil.entityToMap(object);
            if (dynamicMap.get("data") != null) {
                List<Map<String, Object>> dataList = JsonUtil.getJsonToListMap(dynamicMap.get("data").toString());
                if(model.getSlot()!=null){
                    model.getSlot().setOptions(JsonUtil.getObjectToString(dataList));
                }
            }
            redisUtil.insert(cacheKeyUtil.getDynamic() + model.getConfig().getPropsUrl(), model);
        }
        return model;
    }

}
