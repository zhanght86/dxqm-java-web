package jnpf.database.model.dto;

import jnpf.database.exception.DataException;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.source.DbBase;
import jnpf.database.util.DbTypeUtil;
import lombok.Data;

import java.sql.Connection;

@Data
public class DbConnDTO {


    public DbConnDTO(DataSourceUtil dataSourceUtil,String name)throws DataException{
        this.dbType = dataSourceUtil.getDataType();
        this.dbDriver = DbTypeUtil.getDb(dataSourceUtil).getDriver();

        this.dbBase = DbTypeUtil.getDb(dataSourceUtil);
        this.host = dataSourceUtil.getHost();
        this.port = Integer.valueOf(dataSourceUtil.getPort());
        this.userName = dataSourceUtil.getUserName();
        this.password = dataSourceUtil.getPassword();
        if(name!=null){
            this.serviceName = name;
        }else {
            this.serviceName = dataSourceUtil.getDbName();
        }
        this.url = DbTypeUtil.getUrl(dataSourceUtil,this.serviceName);
        //默认：用户即模式
        this.dbSchema = dataSourceUtil.getUserName();
        this.tableSpace = dataSourceUtil.getTableSpace();
    }

    public DbConnDTO(DbLinkEntity link)throws DataException{
        this.dbType = link.getDbType();
        this.dbDriver = DbTypeUtil.getDb(link).getDriver();
        this.url = DbTypeUtil.getUrl(link);
        this.dbBase = DbTypeUtil.getDb(link);
        this.host = link.getHost();
        this.port = Integer.valueOf(link.getPort());
        this.userName = link.getUserName();
        this.password = link.getPassword();
        this.serviceName = link.getServiceName();
        //默认：用户即模式
        this.dbSchema = link.getUserName();
        this.tableSpace = link.getTableSpace();
    }

    /**
     * 数据库类型
     */
    private String dbType;

    /**
     * 驱动
     */
    private String dbDriver;

    /**
     * 主机名称
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 用户
     */
    private String userName;

    /**
     * 密码
     */
    private String password;

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 模式
     * 说明：默认与用户同名模式,用户跨模式查询，表名要用限定名
     *      注意：数据库大小写敏感问题
     */
    private String dbSchema;

    /**
     * 表空间
     */
    public String tableSpace;

    /**
     * 连接地址
     */
    public String url;

    /**
     * 数据库对象
     */
    public DbBase dbBase;

    /**
     * 数据连接
     */
    public Connection connection;
}
