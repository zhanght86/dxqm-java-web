package jnpf.onlinedev.util;

import jnpf.database.model.DbLinkEntity;
import jnpf.base.util.VisualUtils;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/12
 */
@Slf4j
public class VisualJDBCUtils {
	public static void custom(DbLinkEntity linkEntity, String sql)throws SQLException{
	  @Cleanup Connection conn =VisualUtils.getTableConn();
		if (linkEntity!=null){
			conn = VisualUtils.getDataConn(linkEntity);
		}
		try {
			@Cleanup PreparedStatement preparedStatement = null;
			//开启事务
			conn.setAutoCommit(false);
			preparedStatement = conn.prepareStatement(sql);
			preparedStatement.executeUpdate();
			//提交事务
			conn.commit();
		} catch (Exception e) {
			log.error(e.getMessage());
				conn.rollback();
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
				log.error(e.getMessage());
			}
		}
	}
}
