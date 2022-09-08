package jnpf.base.controller;

import com.google.common.base.CaseFormat;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jnpf.base.ActionResult;
import jnpf.base.DbTableModel;
import jnpf.base.Page;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.model.dbtable.*;
import jnpf.base.service.DbTableService;
import jnpf.base.service.DblinkService;
import jnpf.base.vo.ListVO;
import jnpf.base.vo.PageListVO;
import jnpf.base.vo.PaginationVO;
import jnpf.database.util.DbTypeUtil;
import jnpf.database.util.DbUtil;
import jnpf.database.exception.DataException;
import jnpf.database.model.DbTableDataForm;
import jnpf.database.model.DbTableFieldModel;
import jnpf.database.model.DataSourceUtil;
import jnpf.util.JsonUtil;
import jnpf.util.JsonUtilEx;
import jnpf.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 数据建模
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
@Api(tags = "数据建模", value = "DataModel")
@RestController
@RequestMapping("/api/system/DataModel")
@Slf4j
public class DbTableController {

    @Autowired
    private DbTableService dbTableService;
    @Autowired
    private DblinkService dblinkService;
    @Autowired
    private DataSourceUtil dataSourceUtils;

    /**
     * 列表
     *
     * @param id   连接id
     * @param page 关键词
     * @return
     */
    @ApiOperation("获取数据库表列表")
    @GetMapping("/{id}/Tables")
    public ActionResult<ListVO<List<DbTableInfoVO>>> getList(@PathVariable("id") String id, Page page) throws DataException {
        List<DbTableModel> data = dbTableService.
                getList(id).
                stream().
                //过滤不符条件的元素
                filter( t ->
                        //三目运算
                        !StringUtil.isEmpty(page.getKeyword()) ?
                        t.getDescription().toLowerCase().contains(page.getKeyword().toLowerCase()) ||
                                t.getTable().toLowerCase().contains(page.getKeyword().toLowerCase())
                        : t.getTable() != null
                        ).
                //排序
                sorted(Comparator.comparing(DbTableModel::getTable)).collect(Collectors.toList());
        ListVO vo = new ListVO();
        vo.setList(data);
        return ActionResult.success(vo);
    }


    /**
     * 预览数据库表
     *
     * @param dbTableDataForm 查询条件
     * @param linkId          连接Id
     * @param tableName       表名
     * @return
     */
    @ApiOperation("预览数据库表")
    @GetMapping("/{linkId}/Table/{tableName}/Preview")
    public ActionResult<PageListVO> data(DbTableDataForm dbTableDataForm, @PathVariable("linkId") String linkId, @PathVariable("tableName") String tableName) throws DataException {
        List<Map<String, Object>> data = dbTableService.getData(dbTableDataForm, linkId, tableName);
        PaginationVO paginationVO = JsonUtilEx.getJsonToBeanEx(dbTableDataForm, PaginationVO.class);
        return ActionResult.page(data, paginationVO);
    }


    /**
     * 列表
     *
     * @return
     */
    @GetMapping("/{linkId}/Tables/{tableName}/Fields/Selector")
    @ApiOperation("获取数据库表字段下拉框列表")
    public ActionResult<ListVO<DbTableFieldSeleVO>> selectorList(@PathVariable("linkId") String linkId, @PathVariable("tableName") String tableName) throws DataException {
        List<DbTableFieldModel> data = dbTableService.getFieldList(linkId, tableName);
        List<DbTableFieldSeleVO> vos = JsonUtil.getJsonToList(data, DbTableFieldSeleVO.class);
        ListVO vo = new ListVO();
        vo.setList(vos);
        return ActionResult.success(vo);
    }

    /**
     * 字段列表
     *
     * @param linkId    连接Id
     * @param tableName 表名
     * @return
     */
    @ApiOperation("获取数据库表字段列表")
    @GetMapping("/{linkId}/Tables/{tableName}/Fields")
    public ActionResult<ListVO<DbTableFieldVO>> fieldList(@PathVariable("linkId") String linkId, @PathVariable("tableName") String tableName, String type) throws DataException {
        List<DbTableFieldModel> data = dbTableService.getFieldList(linkId, tableName);
        List<DbTableFieldVO> vos = JsonUtil.getJsonToList(data, DbTableFieldVO.class);
        for (DbTableFieldVO vo : vos) {
            if("1".equals(type)){
                String name = vo.getField().toLowerCase().replaceAll("f_", "");
                vo.setField(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name));
            } else {
                vo.setField(vo.getField().toLowerCase());
            }
        }
        ListVO vo = new ListVO();
        vo.setList(vos);
        return ActionResult.success(vo);
    }

    /**
     * 信息
     *
     * @param linkId    连接Id
     * @param tableName 表名
     * @return
     */
    @ApiOperation("获取数据表")
    @GetMapping("/{linkId}/Table/{tableName}")
    public ActionResult<DbTableVO> get(@PathVariable("linkId") String linkId, @PathVariable("tableName") String tableName) throws DataException {
        DbTableModel dbTableModel = dbTableService.getList(linkId).stream().filter(m -> m.getTable().equals(tableName)).findFirst().get();
        //转换
        DbTableInfoVO tableInfo = JsonUtilEx.getJsonToBeanEx(dbTableModel, DbTableInfoVO.class);
        List<DbTableFieldModel> tableFieldList = dbTableService.getFieldList(linkId, tableName);
        List<DbTableFieldVO> fieldList = JsonUtil.getJsonToList(tableFieldList, DbTableFieldVO.class);
        DbTableVO vo = DbTableVO.builder().tableFieldList(fieldList).tableInfo(tableInfo).build();
        return ActionResult.success(vo);
    }

    /**
     * 新建
     *
     * @param linkId 连接Id
     * @return
     */
    @ApiOperation("新建")
    @PostMapping("{linkId}/Table")
    public ActionResult create(@PathVariable("linkId") String linkId, @RequestBody @Valid DbTableCrForm dbTableCrForm) throws DataException {
        //解析接收的新建字段与表信息
        DbTableModel dbTableModel = JsonUtil.getJsonToBean(dbTableCrForm.getTableInfo(), DbTableModel.class);
        List<DbTableFieldModel> list = JsonUtil.getJsonToList(dbTableCrForm.getTableFieldList(), DbTableFieldModel.class);

        if (dbTableService.isExistByFullName(linkId, dbTableCrForm.getTableInfo().getTable(), dbTableModel.getId())) {
            return ActionResult.fail("表名称不能重复");
        }

        return dbTableService.create(linkId, dbTableModel, list);
    }



    /**
     * 删除
     *
     * @param linkId    连接Id
     * @param tableName 表名
     * @return
     */
    @ApiOperation("删除")
    @DeleteMapping("/{linkId}/Table/{tableName}")
    public ActionResult delete(@PathVariable("linkId") String linkId, @PathVariable("tableName") String tableName) throws DataException {
        if (!sumExistFlag(linkId,tableName)) {
            if (checkRepetition(tableName)) {
                return ActionResult.fail("系统自带表,不允许被删除");
            }
            dbTableService.delete(linkId, tableName);
            return ActionResult.success("删除成功");
        } else {
            return ActionResult.fail("表已经被使用,不允许被删除");
        }
    }

    /**
     * 更新
     *
     * @param linkId 连接Id
     * @return
     */
    @ApiOperation("更新")
    @PutMapping("/{linkId}/Table")
    public ActionResult update(@PathVariable("linkId") String linkId, @RequestBody @Valid DbTableUpForm dbTableUpForm) throws DataException {
        DbTableModel dbTableModel = JsonUtil.getJsonToBean(dbTableUpForm.getTableInfo(), DbTableModel.class);
        List<DbTableFieldModel> list = JsonUtil.getJsonToList(dbTableUpForm.getTableFieldList(), DbTableFieldModel.class);
        if (!dbTableUpForm.getTableInfo().getNewTable().equals(dbTableUpForm.getTableInfo().getTable())) {
            if (dbTableService.isExistByFullName(linkId, dbTableUpForm.getTableInfo().getNewTable(), dbTableUpForm.getTableInfo().getTable())) {
                return ActionResult.fail("表名称不能重复");
            }
        }
        String oldTable = dbTableModel.getTable();
        if (!sumExistFlag(linkId,oldTable)) {
            if (checkRepetition(dbTableUpForm.getTableInfo().getNewTable())) {
                return ActionResult.fail("系统自带表,不允许被修改");
            }
            dbTableService.update(linkId, dbTableModel, list);
            return ActionResult.success("修改成功");
        } else {
            return ActionResult.fail("表已经被使用,不允许被编辑");
        }
    }

    /**=============复用代码================**/


    private Boolean checkRepetition(String tableName){
        String[] tables = DbUtil.BYO_TABLE.split(",");
        boolean exists;
        for (String table : tables) {
            exists = tableName.toLowerCase().equals(table);
            if (exists) {
                return true;
            }
        }
        return false;
    }

    private Boolean sumExistFlag(String linkId,String oldTable) throws DataException{
        //默认数据库判断
        /*if("0".equals(linkId)){
            boolean existLink = false;
            String dsDbUsName = dataSourceUtils.getUserName();
            String dsDbPassword = dataSourceUtils.getPassword();
            String dsDbName = dataSourceUtils.getDbName();
            String dsDbType = dataSourceUtils.getDataType();
            String dsDbHost = dataSourceUtils.getHost();
            String dsDbPort = dataSourceUtils.getPort();
            //获取所有设置过的连接
            List<DbLinkEntity> linkList = dblinkService.getList();
            for(DbLinkEntity dbLinkEntity:linkList){
                if (DbTypeUtil.compare(dsDbType,dbLinkEntity.getDbType()) &&
                        dsDbUsName.equals(dbLinkEntity.getUserName())&&
                        dsDbPassword.equals(dbLinkEntity.getPassword()) &&
                        dsDbName.equals(dbLinkEntity.getServiceName())&&
                        dsDbHost.equals(dbLinkEntity.getHost()) &&
                        dsDbPort.equals(dbLinkEntity.getPort())
                ){
                    existLink = true;
                    linkId = dbLinkEntity.getId();
                }
            }
            //未找到相对应的连接
            if(!existLink){throw DataException.configDbLoseLink();}
        }*/
        return checkCommon(linkId,oldTable);
    }


    /**
     * 获取表数据行数
     * 说明：检查表格是否存在数据（存在不可删除）
     * @param linkId 连接id
     * @param tableName 表名
     * @return Integer
     */
    private Boolean checkCommon(String linkId,String tableName) throws DataException {
        List<DbTableModel> tableList = dbTableService.getList(linkId);
        //筛选出指定表名的表
        Optional<DbTableModel> table = tableList.stream().filter(m -> m.getTable().equals(tableName)).findFirst();
        //表存在判断
        if(table.isPresent()){
            Integer sum = table.get().getSum();
            if(sum!=null){
                log.error("表的数据总数sum是：" + sum.toString());

            }else {
                sum = dbTableService.getSum(linkId,tableName);
                /*throw new DataException("数据库表总数出现异常，导致无法删除。");*/
            }
            if(sum!=0 && sum>0){
                return true;
            }else {
                log.error("表的数据表信息：" + table.get().toString());
                return false;
            }
        }else {
            throw new DataException("该表已被删除，或者不存在，请刷新页面后再尝试。");
        }


    }


}
