package jnpf.database.util;

import com.baomidou.mybatisplus.annotation.DbType;
import jnpf.database.enums.DbSttEnum;
import jnpf.database.exception.DataException;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.source.DbBase;
import jnpf.database.source.impl.DbDm;
import jnpf.database.source.impl.DbKingbase;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.model.DataSourceUtil;

import java.sql.Connection;
import java.sql.SQLException;

public class DbTypeUtil {

    /**
     * 出现编码不符合规范的地方
     */

    public static final String CONFIG = "项目配置中";
    public static final String LINK = "数据连接中";
    public static final String URL = "URL中";

    /**===========================数据库对象（重载）=====================================**/

    /**
     * 根据数据库名获取数据库对象
     * Case insensitive 大小写不敏感
     * @param dataSourceUtil 数据库小写名
     * @return  DbTableEnum2 数据表枚举类
     */
    public static DbBase getDb(DataSourceUtil dataSourceUtil) throws DataException{
        String DataSourDbEncode = getEncode(dataSourceUtil);
        return getDbCommon(DataSourDbEncode);
    }

    public static DbBase getDb(DbLinkEntity link)throws DataException{
        String LinkDbEncode = getEncode(link);
        return getDbCommon(LinkDbEncode);
    }

    public static DbBase getDb(Connection conn) {
        try {
            return getDb(conn.getMetaData().getURL());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DbBase getDb(String url) {
        String dbType = url.split(":")[1];
        for(DbBase dbBase : DbBase.DB_BASES){
            if(dbType.equals(dbBase.getConnUrlEncode())){
                return dbBase;
            }
        }
        return null;
    }

    /**==========================获取URL(重载)===============================**/
    public static String getUrl(DataSourceUtil dataSourceUtil,String dbName){
        try {
            //当dbName为空的时候
            if(dbName==null){
                dbName = dataSourceUtil.getDbName();
            }
            DbBase dbBase = getDb(dataSourceUtil);
            return dbBase.getConnectionUrl(
                    dataSourceUtil.getHost(),
                    Integer.parseInt(dataSourceUtil.getPort()),
                    dbName);
        } catch (DataException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getUrl(DbLinkEntity dbLinkEntity)throws DataException {
        DbBase dbBase = getDb(dbLinkEntity);
        String dbName = dbLinkEntity.getServiceName();
        //如果没有数据库，就是schema作为数据库名
        if(dbName == null || dbName.equals("")){
            dbName = dbLinkEntity.getUserName();
        }
        return dbBase.getConnectionUrl(
                dbLinkEntity.getHost(),
                dbLinkEntity.getPort(),
                dbName);
    }
    /**===========================校验数据库类型=============================**/
    /**
     * IOC控制反转
     * @return 是否匹配
     */
    public static Boolean checkDb(DataSourceUtil dataSourceUtil,String encode){
        String DataSourDbEncode = null;
        try {
            DataSourDbEncode = getEncode(dataSourceUtil);
        } catch (DataException e) {
            e.printStackTrace();
        }
        return DataSourDbEncode.equals(encode);
    }

    public static Boolean checkDb(DbLinkEntity link,String encode){
        String LinkDbEncode = null;
        try {
            LinkDbEncode = getEncode(link);
        } catch (DataException e) {
            e.printStackTrace();
        }
        return LinkDbEncode.equals(encode);
    }

    /**============================专用代码区域=========================**/

    /**
     * MybatisPlusConfig
     */
    public static DbType getMybatisEnum(DataSourceUtil dataSourceUtil) throws DataException{
        return getDb(dataSourceUtil).getMpDbType();
    }

    /**
     * 默认数据库与数据连接判断
     */
    public static Boolean compare(String dbType1,String dbType2) throws DataException{
        dbType1 = checkDbTypeExist(dbType1,null,false);
        dbType2 = checkDbTypeExist(dbType2,null,false);
        return dbType1.equals(dbType2);
    }

    /**=========================内部复用代码================================**/

    /**====标准类型（重载）==**/

    /**
     * 获取标准类型编码
     * 根据URL
     * @param dataSourceUtil 数据源
     * @return String
     */
    private static String getEncode(DataSourceUtil dataSourceUtil)throws DataException{
        return checkDbTypeExist(dataSourceUtil.getDataType(),CONFIG,true);
    }

    /**
     * 获取标准类型编码
     * 根据Link里的DateType
     * @param link 数据连接
     * @return String
     */
    private static String getEncode(DbLinkEntity link)throws DataException{
        return checkDbTypeExist(link.getDbType(),LINK,true);
    }
    /**============**

    /**
     * 获取数据库对象
     * @param encode
     * @return
     */
    private static DbBase getDbCommon(String encode){
        for (DbBase db : DbBase.DB_BASES) {
            if (db.getDbEncode().equalsIgnoreCase(encode)) {
                return db;
            }
        }
        return null;
    }

    /**
     * 0、校验数据类型是否符合编码标准（包含即可）
     * @param dbType 数据类型
     * @param dbTypeSourceInfo 数据编码来源信息
     * @return 数据标准编码
     * @throws DataException 数据库类型不符合编码
     */
    private static String checkDbTypeExist(String dbType,String dbTypeSourceInfo,Boolean exceptionOnOff) throws DataException {
        for(String enEncode : DbBase.DB_ENCODES){
            if(enEncode.equals(dbType)){
                return enEncode;
            }
        }
        if(exceptionOnOff){
            throw DataException.DbTypeCompare(dbTypeSourceInfo);
        }
        return null;
    }



}
