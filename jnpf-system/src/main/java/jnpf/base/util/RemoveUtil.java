package jnpf.base.util;

import com.alibaba.fastjson.JSONObject;
import jnpf.model.OnlineUserModel;
import jnpf.model.OnlineUserProvider;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
public class RemoveUtil {

    public static void removeOnline(String userId) {
        String id = userId;
        OnlineUserModel user = OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getUserId().equals(id)).findFirst().isPresent() ? OnlineUserProvider.getOnlineUserList().stream().filter(t -> t.getUserId().equals(id)).findFirst().get() : null;
        if (user != null) {
            OnlineUserProvider.getOnlineUserList().remove(user);
        }
        if (OnlineUserProvider.getOnlineUserList().stream().filter(t -> String.valueOf(t.getUserId()).equals(id)).count() == 0) {
            for (OnlineUserModel item : OnlineUserProvider.getOnlineUserList()) {
                if (!item.getUserId().equals(id)) {
                    JSONObject map = new JSONObject();
                    map.put("method", "Offline");
                    map.put("userId", id);
                    item.getWebSocket().getAsyncRemote().sendText(map.toJSONString());
                }
            }
        }
    }
}
