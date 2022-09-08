package jnpf.permission.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import jnpf.permission.entity.AuthorizeEntity;
import jnpf.base.model.button.ButtonModel;
import jnpf.base.model.column.ColumnModel;
import jnpf.base.model.module.ModuleModel;
import jnpf.base.model.resource.ResourceModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:27
 */
public interface AuthorizeMapper extends BaseMapper<AuthorizeEntity> {


    List<ModuleModel> findModule(@Param("objectId") String objectId);

    List<ButtonModel> findButton(@Param("objectId") String objectId);

    List<ColumnModel> findColumn(@Param("objectId") String objectId);

    List<ResourceModel> findResource(@Param("objectId") String objectId);

    List<ModuleModel> findModuleAdmin(@Param("mark") Integer mark);

    List<ButtonModel> findButtonAdmin(@Param("mark") Integer mark);

    List<ColumnModel> findColumnAdmin(@Param("mark") Integer mark);

    List<ResourceModel> findResourceAdmin(@Param("mark") Integer mark);

    void saveBatch(@Param("values") String values);

    void savaAuth(AuthorizeEntity authorizeEntity);
}
