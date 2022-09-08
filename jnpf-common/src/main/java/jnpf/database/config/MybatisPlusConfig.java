package jnpf.database.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.autoconfigure.SpringBootVFS;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.injector.ISqlInjector;
import com.baomidou.mybatisplus.extension.incrementer.H2KeyGenerator;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import jnpf.base.DbTableModel;
import jnpf.config.ConfigValueUtil;
import jnpf.database.exception.DataException;
import jnpf.database.model.dto.DbConnDTO;
import jnpf.database.source.DbBase;
import jnpf.database.util.DbModelUtil;
import jnpf.database.util.DbTypeUtil;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.util.JdbcUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.logging.slf4j.Slf4jImpl;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:53
 */
@Slf4j
@Configuration
@ComponentScan("jnpf")
@MapperScan(basePackages = {"jnpf.*.mapper","jnpf.mapper"})
public class MybatisPlusConfig {

    /**
     * 对接数据库的实体层
     */
    static final String ALIASES_PACKAGE = "jnpf.*.entity";



    @Autowired
    private DataSourceUtil dataSourceUtil;
    @Autowired
    private ConfigValueUtil configValueUtil;

    @Primary
    @Bean(name = "dataSourceSystem")
    public DataSource dataSourceOne() {
        return druidDataSource();
    }

    @Bean(name = "sqlSessionFactorySystem")
    public SqlSessionFactory sqlSessionFactoryOne(@Qualifier("dataSourceSystem") DataSource dataSource) throws Exception {
        return createSqlSessionFactory(dataSource);
    }

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        try{
            //判断是否多租户
            if (Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
                DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
                HashMap<String, TableNameHandler> map = new HashMap<>(150) ;
                //原来采用dbInt的方式
                /*Connection conn = JdbcUtil.getConn(dataSourceUtil,dataSourceUtil.getDbInit());*/
                Connection conn = JdbcUtil.getConn(dataSourceUtil,null);
                List<DbTableModel> dbTableModels=new ArrayList<>();
                DbBase dbBase = DbTypeUtil.getDb(dataSourceUtil);
                if (conn != null) {
                    String sql = dbBase.getListSql();
                    DbConnDTO connDTO = new DbConnDTO(dataSourceUtil,null);
                    sql = dbBase.getReplaceSql(sql,"",connDTO);
                    dbTableModels = DbModelUtil.getModelList(conn,dbBase,sql,DbTableModel.class);
                }
                log.error("con:"+conn);
                for(DbTableModel dbTableModel:dbTableModels){
                    dbBase.setDynamicMap(map,dbTableModel);
                }
                dynamicTableNameInnerInterceptor.setTableNameHandlerMap(map);
                interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
            }
            //新版本分页必须指定数据库，否则分页不生效
            interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbTypeUtil.getMybatisEnum(dataSourceUtil)));
        }catch (Exception e){
            e.printStackTrace();
        }
        return interceptor;
    }


    protected DataSource druidDataSource() {
        if (Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
            //多租户安全空库保护
            /*dbName = env.getProperty(DataSourceUtil.PREFIX + ".dbnull");*/
        }
        DbBase dbBase = null;
        try {
            dbBase = DbTypeUtil.getDb(dataSourceUtil);
        } catch (DataException e) {
            e.printStackTrace();
        }
        String url = DbTypeUtil.getUrl(dataSourceUtil,null);
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername(dataSourceUtil.getUserName());
        dataSource.setUrl(url);
        dataSource.setPassword(dataSourceUtil.getPassword());
        dataSource.setDriverClassName(dbBase.getDriver());
        return dataSource;
    }




    public Resource[] resolveMapperLocations() {
        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        List<String> mapperLocations = new ArrayList<>();
        mapperLocations.add("classpath:mapper/*/*.xml");
        mapperLocations.add("classpath:mapper/*/*/*.xml");
        List<Resource> resources = new ArrayList();
        if (mapperLocations != null) {
            for (String mapperLocation : mapperLocations) {
                try {
                    Resource[] mappers = resourceResolver.getResources(mapperLocation);
                    resources.addAll(Arrays.asList(mappers));
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return resources.toArray(new Resource[resources.size()]);
    }

    public SqlSessionFactory createSqlSessionFactory(DataSource dataSource) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        //配置填充器
        globalConfig.setMetaObjectHandler(new MybatisPlusMetaObjectHandler());
        bean.setGlobalConfig(globalConfig);

        bean.setVfs(SpringBootVFS.class);
        bean.setTypeAliasesPackage(ALIASES_PACKAGE);
        bean.setMapperLocations(resolveMapperLocations());
        bean.setConfiguration(configuration(dataSource));
        return bean.getObject();
    }

    public MybatisConfiguration configuration(DataSource dataSource){
        MybatisConfiguration mybatisConfiguration = new MybatisConfiguration();
        mybatisConfiguration.setMapUnderscoreToCamelCase(true);
        mybatisConfiguration.setCacheEnabled(false);
        mybatisConfiguration.addInterceptor(mybatisPlusInterceptor());
        mybatisConfiguration.setLogImpl(Slf4jImpl.class);
        mybatisConfiguration.setJdbcTypeForNull(JdbcType.NULL);
        return mybatisConfiguration;
    }
    @Bean
    public IKeyGenerator keyGenerator() {
        return new H2KeyGenerator();
    }

    @Bean
    public ISqlInjector sqlInjector() {
        return (builderAssistant, mapperClass) -> {

        };
    }

    /**
     * 数据权限插件
     *
     * @return DataScopeInterceptor
     */
//    @Bean
//    @ConditionalOnMissingBean
//    public DataScopeInterceptor dataScopeInterceptor(DataSource dataSource) {
//        return new DataScopeInterceptor(dataSource);
//    }


}
