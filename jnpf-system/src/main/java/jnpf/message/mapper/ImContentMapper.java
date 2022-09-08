package jnpf.message.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jnpf.message.entity.ImContentEntity;
import jnpf.message.model.ImUnreadNumModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 聊天内容
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public interface ImContentMapper extends BaseMapper<ImContentEntity> {

    List<ImUnreadNumModel> getUnreadList(@Param("receiveUserId") String receiveUserId);

    List<ImUnreadNumModel> getUnreadLists(@Param("receiveUserId") String receiveUserId);

    int readMessage(@Param("map") Map<String, String> map);
}

