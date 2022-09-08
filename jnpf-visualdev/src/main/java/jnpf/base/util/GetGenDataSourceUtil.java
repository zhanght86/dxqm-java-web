package jnpf.base.util;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import jnpf.base.UserInfo;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.service.DblinkService;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.util.DbTypeUtil;
import jnpf.util.UserProvider;
import jnpf.util.context.SpringContext;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021年3月17日14:06:29
 */

public class GetGenDataSourceUtil {
    private static DblinkService dblinkService;
    private static UserProvider userProvider;
    private static DataSourceUtil dataSourceUtil;

    public static DataSourceConfig getGenDataSource(String dataSourceId){

        // 数据源配置
        SourceUtil sourceUtil = new SourceUtil();

        dblinkService = SpringContext.getBean(DblinkService.class);
        DataSourceConfig dsc;
        DbLinkEntity linkEntity = dblinkService.getInfo(dataSourceId);
        if (linkEntity != null) {
            return sourceUtil.dbConfig(linkEntity);
        }

        userProvider=SpringContext.getBean(UserProvider.class);
        dataSourceUtil=SpringContext.getBean(DataSourceUtil.class);
        UserInfo userInfo=userProvider.get();
         dsc = sourceUtil.dbConfig(userInfo.getTenantDbConnectionString());
        if (DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE)) {
            String schema = dataSourceUtil.getUserName();
            //oracle 默认 schema=username
            dsc.setSchemaName(schema.toUpperCase());
        }
        return dsc;
    }

}
