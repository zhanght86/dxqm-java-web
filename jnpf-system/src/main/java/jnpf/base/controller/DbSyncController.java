package jnpf.base.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.service.DbSyncService;
import jnpf.base.model.dbsync.DbSyncForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;

/**
 * 数据同步
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "数据同步", value = "DataSync")
@RestController
@RequestMapping("/api/system/DataSync/Actions/Execute")
public class DbSyncController {

    @Autowired
    private DbSyncService dbSyncService;

    /**
     * 执行数据同步
     * @param dbSyncForm dto实体
     * @return
     */
    @PostMapping
    @ApiOperation("执行数据同步")
    public ActionResult execute(@RequestBody DbSyncForm dbSyncForm) throws SQLException {
        if (dbSyncForm.getDbConnectionFrom().equals(dbSyncForm.getDbConnectionTo())){
            return ActionResult.fail("请检查数据库连接是否相同");
        }
        String data = dbSyncService.importTableData(dbSyncForm.getDbConnectionFrom(), dbSyncForm.getDbConnectionTo(), dbSyncForm.getDbTable());
        if("ok".equals(data)){
            return ActionResult.success("同步成功");
        }
        if(data==null){
            return ActionResult.success("同步失败,目标表不存在");
        }
        return ActionResult.success(data);
    }
}
