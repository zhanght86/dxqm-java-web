package jnpf.base.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jnpf.base.ActionResultCode;
import jnpf.base.UserInfo;
import jnpf.base.service.DataInterfaceLogService;
import jnpf.database.exception.DataException;
import jnpf.base.ActionResult;
import jnpf.base.mapper.DataInterfaceMapper;
import jnpf.base.service.DataInterfaceService;
import jnpf.base.service.DblinkService;
import jnpf.base.entity.DataInterfaceEntity;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.model.dataInterface.PaginationDataInterface;
import jnpf.database.util.JdbcUtil;
import jnpf.util.*;
import jnpf.util.JsonUtil;
import jnpf.util.jwt.JwtUtil;
import jnpf.util.wxutil.HttpUtil;
import lombok.Cleanup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Service
public class DataInterfaceServiceImpl extends ServiceImpl<DataInterfaceMapper, DataInterfaceEntity> implements DataInterfaceService {
    @Autowired
    private UserProvider userProvider;
    @Autowired
    private DblinkService dblinkService;
    @Autowired
    private DataSourceUtil dataSourceUtils;
    @Autowired
    private DataInterfaceLogService dataInterfaceLogService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<DataInterfaceEntity> getList(PaginationDataInterface pagination) {
        QueryWrapper<DataInterfaceEntity> queryWrapper = new QueryWrapper<>();
        //关键字
        if (!StringUtil.isEmpty(pagination.getKeyword())) {
            queryWrapper.lambda().and(
                    t -> t.like(DataInterfaceEntity::getFullName, pagination.getKeyword())
                            .or().like(DataInterfaceEntity::getEnCode, pagination.getKeyword())
            );
        }
        //分类
        queryWrapper.lambda().eq(DataInterfaceEntity::getCategoryId, pagination.getCategoryId());
        //排序
        queryWrapper.lambda().orderByAsc(DataInterfaceEntity::getSortCode);
        Page<DataInterfaceEntity> page = new Page<>(pagination.getCurrentPage(), pagination.getPageSize());
        IPage<DataInterfaceEntity> iPage = this.page(page, queryWrapper);
        return pagination.setData(iPage.getRecords(), iPage.getTotal());
    }

    @Override
    public List<DataInterfaceEntity> getList() {
        QueryWrapper<DataInterfaceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataInterfaceEntity::getEnabledMark, "1");
        return baseMapper.selectList(queryWrapper);
    }

    @Override
    public DataInterfaceEntity getInfo(String id) {
        QueryWrapper<DataInterfaceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataInterfaceEntity::getId, id);
        return this.getOne(queryWrapper);
    }

    @Override
    public void create(DataInterfaceEntity entity) {
        if (entity.getId() == null) {
            entity.setId(RandomUtil.uuId());
            entity.setCreatorUser(userProvider.get().getUserId());
            entity.setCreatorTime(DateUtil.getNowDate());
            entity.setLastModifyTime(DateUtil.getNowDate());
        }
        this.save(entity);
    }

    @Override
    public boolean update(DataInterfaceEntity entity, String id) throws DataException {
        entity.setId(id);
        entity.setLastModifyUser(userProvider.get().getUserId());
        entity.setLastModifyTime(DateUtil.getNowDate());
        return this.updateById(entity);
    }

    @Override
    public void delete(DataInterfaceEntity entity) {
        this.removeById(entity.getId());
    }

    @Override
    public boolean isExistByFullName(String fullName, String id) {
        QueryWrapper<DataInterfaceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataInterfaceEntity::getFullName, fullName);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(DataInterfaceEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public boolean isExistByEnCode(String enCode, String id) {
        QueryWrapper<DataInterfaceEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(DataInterfaceEntity::getEnCode, enCode);
        if (!StringUtil.isEmpty(id)) {
            queryWrapper.lambda().ne(DataInterfaceEntity::getId, id);
        }
        return this.count(queryWrapper) > 0 ? true : false;
    }

    @Override
    public List<Map<String, Object>> get(String id, String sql) throws DataException {
        DataInterfaceEntity entity = this.getInfo(id);
        List<Map<String, Object>> mapList = connection(entity.getDbLinkId(), sql);
        return mapList;
    }

    @Override
    public ActionResult infoToId(String id) {
        DataInterfaceEntity entity = this.getInfo(id);
        //判读验证类型
        int checkType = entity.getCheckType() != null ? entity.getCheckType() : 0;
        //token验证
        if (checkType == 1) {
            //获取token
            String token = ServletUtil.getRequest().getHeader(Constants.AUTHORIZATION);
            if (StringUtil.isNotEmpty(token)) {
                String realToken = JwtUtil.getRealToken(token);
                if (!redisUtil.exists(realToken)) {
                    return ActionResult.fail(ActionResultCode.SessionOverdue.getMessage());
                }
            } else {
                return ActionResult.fail(ActionResultCode.SessionOverdue.getMessage());
            }
        }
        //跨域验证
        if (checkType == 2) {
            //跨域参数获取
            String requestHeaders = StringUtil.isNotEmpty(entity.getRequestHeaders()) ? entity.getRequestHeaders() : "";
            //获取数据库中参数
            String ipAddr = IpUtil.getIpAddr();
            String[] ips = requestHeaders.split(",");
            for (String ip : ips) {
                if (!ip.equals(ipAddr)) {
                    return ActionResult.fail("跨域验证失败");
                }
            }
        }
        //开始调用的时间
        LocalDateTime dateTime = LocalDateTime.now();
        //调用时间
        int invokWasteTime = 0;
        try {
            //如果是静态数据
            if (entity.getDataType() == 2) {
                try {
                    Map<String, Object> map = JsonUtil.stringToMap(entity.getQuery());
                    //调用时间
                    invokWasteTime = invokTime(dateTime);
                    //添加调用日志
                    dataInterfaceLogService.create(id, invokWasteTime);
                    return ActionResult.success(map);
                } catch (Exception e) {
                    try {
                        List<Map<String, Object>> list = JsonUtil.getJsonToListMap(entity.getQuery());
                        //调用时间
                        invokWasteTime = invokTime(dateTime);
                        //添加调用日志
                        dataInterfaceLogService.create(id, invokWasteTime);
                        return ActionResult.success(list);
                    } catch (Exception exception) {
                        Object obj = entity.getQuery() != null ? entity.getQuery() : null;
                        //调用时间
                        invokWasteTime = invokTime(dateTime);
                        //添加调用日志
                        dataInterfaceLogService.create(id, invokWasteTime);
                        return ActionResult.success(obj);
                    }
                }
            } else if (entity.getDataType() == 3) {
                //HTTP调用或HTTPS调用
                JSONObject get = null;
                String path = entity.getPath();
                //判断是否为http或https
                if ("https".equalsIgnoreCase(path.substring(0, 5)) || "http".equalsIgnoreCase(path.substring(0, 4))) {
                    //请求参数解析
                    List<Map<String, Object>> jsonToListMap = JsonUtil.getJsonToListMap(entity.getRequestParameters());
                    if (jsonToListMap != null) {
                        path = !path.contains("?") ? path += "?" : path + "&";
                        for (Map<String, Object> map : jsonToListMap) {
                            if (map != null) {
                                String field = String.valueOf(map.get("field"));
                                String value = String.valueOf(map.get("value"));
                                path = path + field + "=" + value + "&";
                            }
                        }
                    }
                    if ("https".equalsIgnoreCase(path.substring(0, 5))) {
                        get = HttpUtil.httpRequest(entity.getPath(), "GET", null);
                    } else if ("http".equalsIgnoreCase(path.substring(0, 4))) {
                        get = HttpUtil.httpRequest(path, "GET", null);
                    }
                } else {
                    return ActionResult.fail("外部接口暂时只支持HTTP和HTTPS方式");
                }
                //调用时间
                invokWasteTime = invokTime(dateTime);
                //添加调用日志
                dataInterfaceLogService.create(id, invokWasteTime);
                return ActionResult.success(get);
            } else if (entity.getDataType() == 1) {
                //SQL语句查询
                //判断只能使用select
                if (entity.getQuery().length() < 6 || !"select".equalsIgnoreCase(entity.getQuery().trim().substring(0, 6))) {
                    return ActionResult.fail("该功能只支持Select语句");
                }
                //判断返回值不能为*
                if ("*".equals(entity.getQuery().trim().substring(6, 7))) {
                    return ActionResult.fail("返回值不能为*");
                }
                //判断只有一个SQL语句
                if (entity.getQuery().trim().contains(";")) {
                    int i = entity.getQuery().indexOf(";");
                    if (!"".equals(entity.getQuery().trim().substring(i + 1).trim())) {
                        return ActionResult.fail("只能输入一个sql语句哦");
                    }
                }
                List<Map<String, Object>> SQLMapList = this.get(id, entity.getQuery());
                //调用时间
                invokWasteTime = invokTime(dateTime);
                //添加调用日志
                dataInterfaceLogService.create(id, invokWasteTime);
                return ActionResult.success(SQLMapList);
            }
        } catch (Exception e) {
            return ActionResult.fail("调用接口失败，请检查接口路径、参数或SQL语句");
        }
        return null;
    }

    /**
     * 拼接SQL语句并执行
     *
     * @param dbLinkId
     * @param sql
     * @return
     * @throws DataException
     */
    public List<Map<String, Object>> connection(String dbLinkId, String sql) throws DataException {
            DbLinkEntity linkEntity = dblinkService.getInfo(dbLinkId);
            try {
                @Cleanup Connection conn = null;
                if (linkEntity != null) {
                    conn = JdbcUtil.getConn(linkEntity);
                } else {
                    conn = JdbcUtil.getConn(dataSourceUtils,null);
                }
                UserInfo userInfo = userProvider.get();
                if (userInfo == null) {
                    return null;
                }
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(sql.replaceAll("@user", "'" + userInfo.getUserId() + "'")
                        .replaceAll("@department", "'" + userInfo.getDepartmentId() + "'")
                        .replaceAll("@organize", "'" + userInfo.getOrganizeId() + "'")
                        .replaceAll("@postion", "'" + userInfo.getPositionIds() != null ? userInfo.getPositionIds()[0] : null + "'"));
                ResultSet query = JdbcUtil.query(conn, stringBuffer.toString());
                return JdbcUtil.convertList2(query);
            }catch (Exception e){
                e.getStackTrace();
            }
            return null;
    }

    /**
     * 计算执行时间
     *
     * @param dateTime
     * @return
     */
    public int invokTime(LocalDateTime dateTime) {
        //调用时间
        int invokWasteTime = Integer.valueOf((int) (System.currentTimeMillis() - dateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli()));
        return invokWasteTime;
    }

}
