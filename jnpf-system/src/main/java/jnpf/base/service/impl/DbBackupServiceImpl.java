package jnpf.base.service.impl;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.Pagination;
import jnpf.base.entity.DbBackupEntity;
import jnpf.base.mapper.DbBackupMapper;
import jnpf.base.service.DbBackupService;
import jnpf.config.ConfigValueUtil;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.source.impl.DbDm;
import jnpf.database.source.impl.DbMysql;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.source.impl.DbSqlserver;
import jnpf.database.util.DbTypeUtil;
import jnpf.util.*;
import jnpf.util.enums.FileTypeEnum;
import jnpf.util.file.UploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * 数据备份
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Service
public class DbBackupServiceImpl extends ServiceImpl<DbBackupMapper, DbBackupEntity> implements DbBackupService {

    @Autowired
    private UserProvider userProvider;
    @Autowired
    private DataSourceUtil dataSourceUtils;
    @Autowired
    private ConfigValueUtil configValueUtil;

    @Override
    public List<DbBackupEntity> getList(Pagination pagination) {
        QueryWrapper<DbBackupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().orderByAsc(DbBackupEntity::getSortCode);
        if (pagination.getKeyword() != null) {
            queryWrapper.lambda().and(
                    t -> t.like(DbBackupEntity::getFileName, pagination.getKeyword())
                            .or().like(DbBackupEntity::getBackupDbName, pagination.getKeyword())
            );
        }
        //排序
        queryWrapper.lambda().orderByDesc(DbBackupEntity::getCreatorTime);
        Page<DbBackupEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<DbBackupEntity> iPage = this.page(page, queryWrapper);
        return pagination.setData(iPage.getRecords(), page.getTotal());
    }

    @Override
    public DbBackupEntity getInfo(String id) {
        QueryWrapper<DbBackupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DbBackupEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void delete(DbBackupEntity entity) {
        if (entity != null) {
            //删除文件
            FileUtil.deleteFile(configValueUtil.getDataBackupFilePath() + entity.getFileName());
            this.removeById(entity.getId());
            //删除文件
            UploadUtil.removeFile(configValueUtil.getFileType(), FileTypeEnum.DATABACKUP, entity.getFileName());
        }
    }

    @Override
    public void create(DbBackupEntity entity) {
        entity.setId(RandomUtil.uuId());
        entity.setSortCode(RandomUtil.parses());
        entity.setCreatorUserId(userProvider.get().getUserId());
        this.save(entity);
    }

    @Override
    public boolean dbBackup() {
        boolean flag = false;
        String path = configValueUtil.getDataBackupFilePath();
        String filePath = null;
        String fileName = null;
        if (DbTypeUtil.checkDb(dataSourceUtils,DbMysql.DB_ENCODE)) {
            fileName = RandomUtil.uuId() + ".sql";
            String url = DbTypeUtil.getUrl(dataSourceUtils,null);
            String[] dataUrl = url.substring(0, url.lastIndexOf("?")).split("/");
            String host = dataUrl[2].split(":")[0];
            String dbName = dataSourceUtils.getDbName();
            if (Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
                dbName = userProvider.get().getTenantDbConnectionString();
            }
            //备份数据
            filePath = DbMysql.mysqlBackUp(host, dataSourceUtils.getUserName(), dataSourceUtils.getPassword(), dbName, path, fileName);
            File file = new File(path + fileName);
            if (file.isFile()) {
                //备份记录
                DbBackupEntity entity = new DbBackupEntity();
                entity.setId(RandomUtil.uuId());
                entity.setFileName(fileName);
                entity.setFilePath(path + fileName);
                entity.setFileSize(FileUtil.getSize(String.valueOf(file.length())));
                entity.setCreatorUserId(userProvider.get().getUserId());
                this.save(entity);
                flag = true;
            }
        } else if (DbTypeUtil.checkDb(dataSourceUtils,DbSqlserver.DB_ENCODE)) {
            fileName = RandomUtil.uuId() + ".bak";
            String url = DbTypeUtil.getUrl(dataSourceUtils,null);
            String[] dataUrl = url.substring(0, url.lastIndexOf(";")).split("/");
            String host = dataUrl[2].split(":")[0];
            String post = dataUrl[2].split(":")[1];
            String dataseName = dataSourceUtils.getDbName();
            if (Boolean.parseBoolean(configValueUtil.getMultiTenancy())) {
                dataseName = userProvider.get().getTenantDbConnectionString();
            }
            //备份数据
            filePath = DbSqlserver.serverBackUp(dataSourceUtils.getUserName(), dataSourceUtils.getPassword(), host, Integer.parseInt(post), path, fileName, dataseName);
            File file = new File(path + fileName);
            if (file.isFile()) {
                //备份记录
                DbBackupEntity entity = new DbBackupEntity();
                entity.setId(RandomUtil.uuId());
                entity.setFileName(fileName);
                entity.setFilePath(path + fileName);
                entity.setFileSize(FileUtil.getSize(String.valueOf(file.length())));
                entity.setCreatorUserId(userProvider.get().getUserId());
                this.save(entity);
                flag = true;
            }
        } else if (DbTypeUtil.checkDb(dataSourceUtils,DbOracle.DB_ENCODE)) {
            fileName = RandomUtil.uuId() + ".dmp";
            String url = DbTypeUtil.getUrl(dataSourceUtils,null);
            String[] dataUrl = url.split("@")[1].split(":");
            String host = dataUrl[0];
            String sid = dataUrl[2];
            //备份数据
            filePath = DbOracle.oracleBackUp(dataSourceUtils.getUserName(), dataSourceUtils.getPassword(), host, sid, path, fileName);
            File file = new File(path + fileName);
            if (file.isFile()) {
                //备份记录
                DbBackupEntity entity = new DbBackupEntity();
                entity.setId(RandomUtil.uuId());
                entity.setFileName(fileName);
                entity.setFilePath(path + fileName);
                entity.setFileSize(FileUtil.getSize(String.valueOf(file.length())));
                entity.setCreatorUserId(userProvider.get().getUserId());
                this.save(entity);
                flag = true;
            }
        }else if(DbTypeUtil.checkDb(dataSourceUtils,DbDm.DB_ENCODE)){
            fileName = RandomUtil.uuId() + ".dmp";
            String url = DbTypeUtil.getUrl(dataSourceUtils,null);
            String[] dataUrl = url.substring(0, url.lastIndexOf("?")).split("/");
            String host = dataUrl[2].split(":")[0];
            String post = dataUrl[2].split(":")[1];
            //备份数据
            filePath = DbDm.dmBackUp(dataSourceUtils.getUserName(), dataSourceUtils.getPassword(), host, post, path, fileName);
            File file = new File(path + fileName);
            if (file.isFile()) {
                //备份记录
                DbBackupEntity entity = new DbBackupEntity();
                entity.setId(RandomUtil.uuId());
                entity.setFileName(fileName);
                entity.setFilePath(path + fileName);
                entity.setFileSize(FileUtil.getSize(String.valueOf(file.length())));
                entity.setCreatorUserId(userProvider.get().getUserId());
                this.save(entity);
                flag = true;
            }
        }
        //上传文件
        try {
            UploadUtil.uploadFile(configValueUtil.getFileType(), filePath, FileTypeEnum.DATABACKUP, fileName);
        } catch (IOException e) {
            log.error("文件上传失败");
        }
        return flag;
    }
}
