package jnpf.base.util;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Set;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
public class ClassUtil {

    private static void getProxyPojoValue(Object object, Set<String> key1){
        String id = null;
        // 返回参数
        HashMap<String,Object> hashMap = new HashMap<>(16);
        for (String s : key1) {
            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);

                // 获取表名
                TableName table = object.getClass().getAnnotation(TableName.class);
                if (table != null) {
                    String tableName = table.value();
                    hashMap.putIfAbsent("tableName", tableName);
                }
                // 获取主键id
                if (id == null) {
                    boolean isIdField = field.isAnnotationPresent(TableId.class);
                    if (isIdField) {
                        TableField tableField = field.getAnnotation(TableField.class);
                        if (s.toLowerCase().equals(field.getName().toLowerCase())) {
                            String tableId = tableField.value();
                            hashMap.put(s,tableId);
                            id = tableId;
                        }
                    }
                }

                // 获取字段的值
                boolean isTableField = field.isAnnotationPresent(TableField.class);
                if (isTableField) {
                    TableField tableField = field.getAnnotation(TableField.class);
                    if (s.toLowerCase().equals(field.getName().toLowerCase())) {
                        String fieldValue = tableField.value();
                        hashMap.put(s,fieldValue);
                    }
                }
            }
        }
        System.out.println(hashMap);
    }
}
