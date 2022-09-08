package jnpf.base.controller;

import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.database.exception.DataException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.*;
import jnpf.base.service.DbBackupService;
import jnpf.config.ConfigValueUtil;
import jnpf.base.entity.DbBackupEntity;
import jnpf.base.model.dbbackup.DbBackupListVO;
import jnpf.util.FileUtil;
import jnpf.util.JsonUtil;
import jnpf.util.UploaderUtil;
import jnpf.util.UserProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据备份
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "数据备份", value = "DataBackup")
@RestController
@RequestMapping("/api/system/DataBackup")
public class DbBackupController {

    @Autowired
    private DbBackupService dbBackupService;
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private ConfigValueUtil configValueUtil;

    /**
     * 列表
     *
     * @param pagination
     * @return
     */
    @ApiOperation("获取数据备份列表(带分页)")
    @GetMapping
    public ActionResult<PageListVO<DbBackupListVO>> list(Pagination pagination) {
        UserInfo userInfo = userProvider.get();
        List<DbBackupEntity> list = dbBackupService.getList(pagination);

        PaginationVO paginationVO = JsonUtil.getJsonToBean(pagination, PaginationVO.class);
        List<DbBackupListVO> listVos = JsonUtil.getJsonToList(list, DbBackupListVO.class);
        for (DbBackupListVO dbList : listVos) {
            String filePath = configValueUtil.getDataBackupFilePath() + dbList.getFileName();
            if (FileUtil.fileIsFile(filePath)) {
                dbList.setFileUrl(UploaderUtil.uploaderFile(userInfo.getId() + "#" + dbList.getFileName() + "#dataBackup"));
            }
        }
        return ActionResult.page(listVos, paginationVO);
    }

    /**
     * 创建备份
     *
     * @return
     */
    @ApiOperation("添加数据备份")
    @PostMapping
    public ActionResult create() {
        boolean flag = dbBackupService.dbBackup();
        if (flag) {
            return ActionResult.success("备份成功");
        } else {
            return ActionResult.fail("备份失败");
        }
    }

    /**
     * 删除
     *
     * @param id 主键值
     * @return
     */
    @ApiOperation("删除数据备份")
    @DeleteMapping("/{id}")
    public ActionResult delete(@PathVariable("id") String id) throws DataException {
        DbBackupEntity entity = dbBackupService.getInfo(id);
        if (entity != null) {
            dbBackupService.delete(entity);
            return ActionResult.success("删除成功");
        }
        return ActionResult.fail("删除失败，数据不存在");
    }
}
