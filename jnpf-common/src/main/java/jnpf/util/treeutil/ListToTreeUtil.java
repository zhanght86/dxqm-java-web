package jnpf.util.treeutil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
public class ListToTreeUtil {

    /**
     * 转换TreeView
     *
     * @param data
     * @return
     */
    public static List<TreeViewModel> toTreeView(List<TreeViewModel> data) {
        List<TreeViewModel> treeList = getChildNodeList(data, "0");
        return treeList;
    }

    /**
     * 递归
     *
     * @param data
     * @param parentId
     */
    private static List<TreeViewModel> getChildNodeList(List<TreeViewModel> data, String parentId) {
        List<TreeViewModel> treeList = new ArrayList<>();
        List<TreeViewModel> childNodeList = data.stream().filter(t -> String.valueOf(t.getParentId()).equals(parentId)).collect(Collectors.toList());
        for (TreeViewModel entity : childNodeList) {
            TreeViewModel model = new TreeViewModel();
            model.setId(entity.getId());
            model.setText(entity.getText());
            model.setParentId(entity.getParentId());
            model.setIsexpand(entity.getIsexpand());
            model.setComplete(entity.getComplete());
            model.setHasChildren(entity.getHasChildren() == null ? data.stream().filter(t -> String.valueOf(t.getParentId()).equals(String.valueOf(entity.getId()))).count() == 0 ? false : true : false);
            if (entity.getShowcheck()) {
                model.setCheckstate(entity.getCheckstate());
                model.setShowcheck(true);
            }
            if (entity.getImg() != null) {
                model.setImg(entity.getImg());
            }
            if (entity.getCssClass() != null) {
                model.setCssClass(entity.getCssClass());
            }
            if (entity.getClick() != null) {
                model.setClick(entity.getClick());
            }
            if (entity.getCode() != null) {
                model.setCode(entity.getCode());
            }
            if (entity.getTitle() != null) {
                model.setTitle(entity.getTitle());
            }
            if (entity.getHt() != null) {
                model.setHt(entity.getHt());
            }
            model.setChildNodes(getChildNodeList(data, entity.getId()));
            treeList.add(model);
        }
        return treeList;
    }

    /**
     * 转换树表
     *
     * @param data 数据
     * @return
     */
    public static Object listTree(List<TreeListModel> data) {
        List<Object> treeGridList = new ArrayList<>();
        getChildNodeList(data, -1, "0", treeGridList);
        Map map = new HashMap(16);
        map.put("rows", treeGridList);
        return map;
    }

    /**
     * 转换树表
     *
     * @param data     数据
     * @param parentId 父节点
     * @param level    层级
     * @return
     */
    public static Object listTree(List<TreeListModel> data, String parentId, int level) {
        List<Object> treeGridList = new ArrayList<>();
        getChildNodeList(data, level, parentId, treeGridList);
        Map map = new HashMap(16);
        map.put("rows", treeGridList);
        return map;
    }

    /**
     * 递归
     *
     * @param data          数据
     * @param level         层级
     * @param parentId      父节点
     * @param treeGridList 返回数据
     */
    private static void getChildNodeList(List<TreeListModel> data, int level, String parentId, List<Object> treeGridList) {
        List<TreeListModel> childNodeList = data.stream().filter(t -> t.getParentId().equals(parentId)).collect(Collectors.toList());
        if (childNodeList.size() > 0) {
            level++;
        }
        for (TreeListModel entity : childNodeList) {
            Map ht = new HashMap(16);
            if (entity.getHt() != null) {
                ht = entity.getHt();
            }
            ht.put("level", entity.getLevel() == null ? level : entity.getLevel());
            if (entity.getIsLeaf() != null) {
                ht.put("isLeaf", entity.getIsLeaf());
            } else {
                ht.put("isLeaf", data.stream().filter(t -> t.getParentId().equals(entity.getId())).count() == 0 ? true : false);
            }
            ht.put("parent", parentId);
            ht.put("expanded", entity.getExpanded());
            ht.put("loaded", entity.getLoaded());
            treeGridList.add(ht);
            getChildNodeList(data, level, entity.getId(), treeGridList);
        }
    }

    /**
     * 递归查询父节点
     *
     * @param data     条件的的数据
     * @param dataAll  所有的数据
     * @param id       id
     * @param parentId parentId
     * @param <T>
     * @return
     */
    public static <T> JSONArray treeWhere(List<T> data, List<T> dataAll, String id, String parentId) {
        JSONArray datas = JSONArray.parseArray(JSON.toJSONString(data));
        JSONArray result = new JSONArray();
        if (datas.size() == dataAll.size()) {
            return datas;
        }
        int datasSize=datas.size();
        for (int i = 0; i <datasSize ; i++) {
            JSONObject json = (JSONObject) datas.get(i);
            if (result.stream().filter(t -> t.equals(json)).count() == 0) {
                result.add(json);
            }
            if (!"-1".equals(json.getString(parentId))) {
                ListToTreeUtil.result(dataAll, json, result, id, parentId);
            }
        }
        return result;
    }

    /**
     * 递归查询父节点
     *
     * @param data     条件的的数据
     * @param dataAll  所有的数据
     * @param <T>
     * @return
     */
    public static <T> JSONArray treeWhere(List<T> data, List<T> dataAll) {
        String id = "id";
        String parentId = "parentId";
        JSONArray datas = JSONArray.parseArray(JSON.toJSONString(data));
        JSONArray result = new JSONArray();
        if (datas.size() == dataAll.size()) {
            return datas;
        }
        for (int i = 0; i < datas.size(); i++) {
            JSONObject json = (JSONObject) datas.get(i);
            if (result.stream().filter(t -> t.equals(json)).count() == 0) {
                result.add(json);
            }
            if (!"-1".equals(json.getString(parentId))) {
                ListToTreeUtil.result(dataAll, json, result, id, parentId);
            }
        }
        return result;
    }

    /**
     * 递归查询父节点
     *
     * @param dataAll  所有数据
     * @param json     当前对象
     * @param result   结果数据
     * @param id       id
     * @param parentId parentId
     * @param <T>
     * @return
     */
    private static <T> JSONArray result(List<T> dataAll, JSONObject json, JSONArray result, String id, String parentId) {
        JSONArray dataAlls = JSONArray.parseArray(JSON.toJSONString(dataAll));
        for (int i = 0; i < dataAlls.size(); i++) {
            JSONObject aVal = (JSONObject) dataAlls.get(i);
            String ids = aVal.getString(id);
            String parentIds = aVal.getString(parentId);
            if (json.getString(parentId).equals(ids)) {
                if (result.stream().filter(t -> t.equals(aVal)).count() == 0) {
                    result.add(aVal);
                }
                if ("-1".equals(parentIds)) {
                    break;
                }
                ListToTreeUtil.result(dataAll, aVal, result, id, parentId);
            }
        }
        return result;
    }

    /**
     * 递归查询子节点
     *
     * @param data     所有的数据
     * @param id       id
     * @param parentId parentId
     * @param fid      查询的父亲节点
     * @param <T>
     * @return
     */
    public static <T> JSONArray treeWhere(String fid, List<T> data, String id, String parentId) {
        JSONArray datas = JSONArray.parseArray(JSON.toJSONString(data));
        JSONArray result = new JSONArray();
        for (int i = 0; i < datas.size(); i++) {
            JSONObject json = (JSONObject) datas.get(i);
            String fId = json.getString(id);
            String fParentId = json.getString(parentId);
            if (fid.equals(fParentId)) {
                result.add(json);
                ListToTreeUtil.result(fId, data, result, id, parentId);
            }
        }
        return result;
    }

    /**
     * 递归查询子节点
     *
     * @param data     所有的数据
     * @param fid      查询的父亲节点
     * @param <T>
     * @return
     */
    public static <T> JSONArray treeWhere(String fid, List<T> data) {
        String id ="id";
        String parentId ="parentId";
        JSONArray datas = JSONArray.parseArray(JSON.toJSONString(data));
        JSONArray result = new JSONArray();
        for (int i = 0; i < datas.size(); i++) {
            JSONObject json = (JSONObject) datas.get(i);
            String fId = json.getString(id);
            String fParentId = json.getString(parentId);
            if (fid.equals(fParentId)) {
                result.add(json);
                ListToTreeUtil.result(fId, data, result, id, parentId);
            }
        }
        return result;
    }

    /**
     * 递归查询子节点
     *
     * @param data     所有的数据
     * @param id       F_Id
     * @param parentId F_ParentId
     * @param fid      查询的父亲节点
     * @param <T>
     * @return
     */
    public static <T> JSONArray result(String fid, List<T> data, JSONArray result, String id, String parentId) {
        JSONArray dataAll = JSONArray.parseArray(JSON.toJSONString(data));
        for (int i = 0; i < dataAll.size(); i++) {
            JSONObject aVal = (JSONObject) dataAll.get(i);
            String fId = aVal.getString(id);
            String fParentId = aVal.getString(parentId);
            if (fid.equals(fParentId)) {
                result.add(aVal);
                ListToTreeUtil.result(fId, data, result, id, parentId);
            }
        }
        return result;
    }
}
