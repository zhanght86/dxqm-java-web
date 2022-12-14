package jnpf.generater.genutil;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableField;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.google.common.base.CaseFormat;
import jnpf.base.UserInfo;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.entity.VisualdevEntity;
import jnpf.base.model.template7.Template7Model;
import jnpf.base.util.FormCloumnUtil;
import jnpf.base.util.SourceUtil;
import jnpf.base.util.VisualUtils;
import jnpf.config.ConfigValueUtil;
import jnpf.database.source.impl.DbDm;
import jnpf.database.source.impl.DbMysql;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.source.impl.DbSqlserver;
import jnpf.database.util.DbTypeUtil;
import jnpf.generater.genutil.custom.CustomGenerator;
import jnpf.generater.model.GenBaseInfo;
import jnpf.generater.model.GenFileNameSuffix;
import jnpf.model.FormAllModel;
import jnpf.model.FormColumnModel;
import jnpf.model.FormColumnTableModel;
import jnpf.model.FormEnum;
import jnpf.model.visiual.ColumnDataModel;
import jnpf.model.visiual.DownloadCodeForm;
import jnpf.model.visiual.FormDataModel;
import jnpf.model.visiual.TableModel;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.model.visiual.fields.config.ConfigModel;
import jnpf.database.model.DataSourceUtil;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author JNPF???????????????
 * @version V3.1.0
 * @copyright ?????????????????????????????????https://www.jnpfsoft.com???
 * @date 2021/3/16
 */
public class AppIndexGenUtil {

    /**
     * ????????????
     *
     * @param fileName         ???????????????
     * @param downloadCodeForm ????????????
     * @param model            ??????
     * @param templatePath     ????????????
     * @param userInfo         ??????
     * @param configValueUtil  ????????????
     */
    public static void htmlTemplates(String fileName, VisualdevEntity entity, DownloadCodeForm downloadCodeForm, FormDataModel model, String templatePath, UserInfo userInfo, ConfigValueUtil configValueUtil, String pKeyName) {
        Map<String, Object> map = new HashMap<>(16);
        //formTempJson
        FormDataModel formDataModel = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formDataModel.getFields(), FieLdsModel.class);
        List<FormAllModel> formAllModel = new ArrayList<>();
        FormCloumnUtil.recursionForm(list, formAllModel);

        //form?????????
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //????????????
        List<Map<String, Object>> child = new ArrayList<>();
        for (int i = 0; i < table.size(); i++) {
            FormColumnTableModel childList = table.get(i).getChildList();
            List<FormColumnModel> tableList = childList.getChildList();
            childList.setChildList(tableList);
            String name = downloadCodeForm.getSubClassName().split(",")[i];
            Map<String, Object> childs = JsonUtil.entityToMap(childList);
            String className = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            childs.put("className", className);
            child.add(childs);
        }
        //????????????
        for (int i = 0; i < mast.size(); i++) {
            FieLdsModel fieLdsModel = mast.get(i).getFormColumnModel().getFieLdsModel();
            ConfigModel configModel = fieLdsModel.getConfig();
            String vmodel = fieLdsModel.getVModel();
            if (configModel.getDefaultValue() instanceof String) {
                configModel.setValueType("String");
            }
            if (configModel.getDefaultValue() == null) {
                configModel.setValueType("undefined");
            }
            fieLdsModel.setConfig(configModel);
            if (StringUtil.isEmpty(vmodel)) {
                mast.remove(i);
            }
        }
        //??????
        map.put("module", model.getAreasName());
        map.put("className", model.getClassName().substring(0, 1).toUpperCase() + model.getClassName().substring(1).toLowerCase());
        map.put("formRef", model.getFormRef());
        map.put("formModel", model.getFormModel());
        map.put("size", model.getSize());
        map.put("labelPosition", model.getLabelPosition());
        map.put("labelWidth", model.getLabelWidth());
        map.put("formRules", model.getFormRules());
        map.put("gutter", model.getGutter());
        map.put("disabled", model.getDisabled());
        map.put("span", model.getSpan());
        map.put("formBtns", model.getFormBtns());
        map.put("idGlobal", model.getIdGlobal());
        map.put("popupType", model.getPopupType());
        map.put("form", formAllModel);

        //form???model
        Template7Model temModel = new Template7Model();
        temModel.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
        temModel.setCreateDate(DateUtil.daFormat(new Date()));
        temModel.setCreateUser(GenBaseInfo.AUTHOR);
        temModel.setCopyright("??????????????????????????????(https://www.jnpfsoft.com)");
        temModel.setClassName(downloadCodeForm.getClassName().substring(0, 1).toLowerCase() + downloadCodeForm.getClassName().substring(1));
        temModel.setDescription("");
        map.put("genInfo", temModel);
        map.put("modelName", model.getClassName());
        map.put("package", "jnpf");

        //??????
        String columnData = entity.getColumnData().trim().replaceAll(":\"f_", ":\"");
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnData, ColumnDataModel.class);
        map.put("page", Boolean.valueOf(columnDataModel.getHasPage()) ? "0" : "1");
        map.put("defaultSidx", columnDataModel.getDefaultSidx());
        map.put("sort", columnDataModel.getSort());
        map.put("columnList", JsonUtil.getJsonToListMap(columnDataModel.getColumnList()));
        map.put("sortList", JsonUtil.getJsonToListMap(columnDataModel.getSortList()));
        map.put("pageSize", columnDataModel.getPageSize());

        //??????
        map.put("children", child);
        map.put("fields", mast);
        pKeyName = pKeyName.toLowerCase().trim().replaceAll("f_", "");
        map.put("pKeyName", pKeyName);
        map.put("searchList", JsonUtil.getJsonToListMap(columnDataModel.getSearchList()));

        htmlTemplates(model.getServiceDirectory() + fileName, map, templatePath);

        //??????model
        for (int i = 0; i < table.size(); i++) {
            FormColumnTableModel childList = table.get(i).getChildList();
            String name = downloadCodeForm.getSubClassName().split(",")[i];
            Map<String, Object> objectMap = JsonUtil.entityToMap(childList);
            String className = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            objectMap.put("children", childList);
            objectMap.put("genInfo", temModel);
            objectMap.put("package", "jnpf");
            objectMap.put("module", model.getAreasName());
            objectMap.put("className", className);
            childrenTemplates(model.getServiceDirectory() + fileName, objectMap, templatePath, className, downloadCodeForm.getClassName());
        }
    }

    /**
     * ???????????????
     *
     * @param path      ??????
     * @param template  ????????????
     * @param className ????????????
     * @return
     */
    private static String getFileName(String path, String template, String className) {
        String modelName = className.substring(0, 1).toLowerCase() + className.substring(1).toLowerCase();
        String modelPath = path + File.separator + "java" + File.separator + "model";
        String htmlPath = path + File.separator + "html";
        File htmlfile = new File(htmlPath);
        File modelfile = new File(modelPath);
        if (!htmlfile.exists()) {
            htmlfile.mkdirs();
        }
        if (!modelfile.exists()) {
            modelfile.mkdirs();
        }
        if (template.contains("index.vue.vm")) {
            className = "index";
            return htmlPath + File.separator + className + ".vue";
        }
        if (template.contains("form.vue.vm")) {
            className = "form";
            return htmlPath + File.separator + className + ".vue";
        }
        if (template.contains("Form.java.vm")) {
            return modelPath + File.separator + className + "Form.java";
        }
        if (template.contains("InfoVO.java.vm")) {
            return modelPath + File.separator + className + "InfoVO.java";
        }
        if (template.contains("ListVO.java.vm")) {
            return modelPath + File.separator + className + "ListVO.java";
        }
        if (template.contains("Model.java.vm")) {
            return modelPath + File.separator + className + "Model.java";
        }
        if (template.contains("Pagination.java.vm")) {
            return modelPath + File.separator + className + "Pagination.java";
        }
        return null;
    }

    /**
     * ???????????????
     *
     * @param template ????????????
     * @return
     */
    private static List<String> getTemplates(String template) {
        List<String> templates = new ArrayList<>();
        templates.add(template + File.separator + "html" + File.separator + "form.vue.vm");
        templates.add(template + File.separator + "html" + File.separator + "index.vue.vm");
        templates.add(template + File.separator + "java" + File.separator + "Form.java.vm");
        templates.add(template + File.separator + "java" + File.separator + "InfoVO.java.vm");
        templates.add(template + File.separator + "java" + File.separator + "ListVO.java.vm");
        templates.add(template + File.separator + "java" + File.separator + "Pagination.java.vm");
        return templates;
    }

    /**
     * ??????html??????
     *
     * @param path         ??????
     * @param object       ????????????
     * @param templatePath ????????????
     */
    private static void htmlTemplates(String path, Object object, String templatePath) {
        List<String> templates = getTemplates(templatePath);
        //????????????
        VelocityContext context = new VelocityContext();
        context.put("context", object);
        for (String template : templates) {
            // ????????????
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF_8);
            tpl.merge(context, sw);
            try {
                Map<String, Object> map = JsonUtil.stringToMap(JsonUtil.getObjectToString(object));
                String fileNames = getFileName(path, template, map.get("className").toString());
                if (fileNames != null) {
                    File file = new File(fileNames);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    IOUtils.write(sw.toString(), fos, Constants.UTF_8);
                    IOUtils.closeQuietly(sw);
                    IOUtils.closeQuietly(fos);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("??????????????????????????????" + e);
            }
        }
    }

    /**
     * ??????html??????
     *
     * @param path         ??????
     * @param object       ????????????
     * @param templatePath ????????????
     */
    private static void childrenTemplates(String path, Object object, String templatePath, String className, String model) {
        List<String> templates = new ArrayList<>();
        templates.add(templatePath + File.separator + "java" + File.separator + "Model.java.vm");
        //????????????
        VelocityContext context = new VelocityContext();
        context.put("context", object);
        for (String template : templates) {
            // ????????????
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF_8);
            tpl.merge(context, sw);
            try {
                String fileNames = path + File.separator + "java" + File.separator + "model" + File.separator + className + "Model.java";
                if (fileNames != null) {
                    File file = new File(fileNames);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    FileOutputStream fos = new FileOutputStream(file);
                    IOUtils.write(sw.toString(), fos, Constants.UTF_8);
                    IOUtils.closeQuietly(sw);
                    IOUtils.closeQuietly(fos);
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("??????????????????????????????" + e);
            }
        }
    }

    //----------------------------??????-------------------------------------------------------

    /**
     * ????????????
     *
     * @param dataSourceUtil   ?????????
     * @param path             ??????
     * @param fileName         ???????????????
     * @param downloadCodeForm ????????????
     * @param entity           ??????
     * @param userInfo         ??????
     * @param configValueUtil  ????????????
     */
    private static void setCode(DataSourceUtil dataSourceUtil, String path, String fileName, String templatePath, DownloadCodeForm downloadCodeForm, VisualdevEntity entity, UserInfo userInfo, ConfigValueUtil configValueUtil, DbLinkEntity linkEntity) throws SQLException {
        //tableJson
        List<TableModel> tableModelList = JsonUtil.getJsonToList(entity.getTables(), TableModel.class);
        Map<String, Object> columndata = new HashMap<>(16);
        Template7Model model = new Template7Model();
        model.setClassName(downloadCodeForm.getClassName().substring(0, 1).toUpperCase() + downloadCodeForm.getClassName().substring(1).toLowerCase());
        model.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
        model.setCreateDate(DateUtil.daFormat(new Date()));
        model.setCreateUser(GenBaseInfo.AUTHOR);
        model.setCopyright("??????????????????????????????(https://www.jnpfsoft.com)");
        model.setDescription(downloadCodeForm.getDescription());

        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<FormAllModel> formAllModel = new ArrayList<>();
        FormCloumnUtil.recursionForm(list, formAllModel);
        //????????????
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //???????????????
        String tableName = tableModelList.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
        List<FieLdsModel> system = new ArrayList<>();
        for (int i = 0; i < mast.size(); i++) {
            FormAllModel mastModel = mast.get(i);
            FieLdsModel fieLdsModel = mastModel.getFormColumnModel().getFieLdsModel();
            ConfigModel configModel = fieLdsModel.getConfig();
            String jnpfkey = configModel.getJnpfKey();
            if (!"JNPFText".equals(jnpfkey) && !"divider".equals(jnpfkey)) {
                system.add(fieLdsModel);
            }
        }

        @Cleanup Connection conn = VisualUtils.getDataConn(linkEntity);
        //??????????????????
        String pKeyName = VisualUtils.getpKey(conn, tableName).toLowerCase().trim().replaceAll("f_", "");
        //???????????????
        List<Map<String, Object>> child = new ArrayList<>();
        for (int i = 0; i < table.size(); i++) {
            FormColumnTableModel childList = table.get(i).getChildList();
            String name = downloadCodeForm.getSubClassName().split(",")[i];
            Map<String, Object> childs = JsonUtil.entityToMap(childList);
            String className = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            TableModel tableModel = tableModelList.stream().filter(t -> t.getTable().equals(childList.getTableName())).findFirst().get();
            //??????????????????
            String chidKeyName = VisualUtils.getpKey(conn, tableModel.getTable());
            String tableField = tableModel.getTableField().trim().replaceAll(":\"f_", ":\"");
            childs.put("tableField", tableField);
            String relationField = tableModel.getRelationField().trim().replaceAll(":\"f_", ":\"");
            childs.put("relationField", relationField);
            childs.put("className", className);
            String keyName = chidKeyName.trim().toLowerCase().replaceAll("f_", "");
            childs.put("chidKeyName", keyName);
            child.add(childs);
        }
        //???????????????
        String columnData = entity.getColumnData().trim().replaceAll(":\"f_", ":\"");
        ColumnDataModel columnDataModel = JsonUtil.getJsonToBean(columnData, ColumnDataModel.class);
        columndata.put("page", Boolean.valueOf(columnDataModel.getHasPage()) ? "0" : "1");
        columndata.put("defaultSidx", columnDataModel.getDefaultSidx());
        columndata.put("sort", columnDataModel.getSort());
        columndata.put("searchList", JsonUtil.getJsonToListMap(columnDataModel.getSearchList()));

        columndata.put("genInfo", model);
        columndata.put("areasName", downloadCodeForm.getModule());
        columndata.put("modelName", model.getClassName());
        columndata.put("typeId", 1);
        columndata.put("system", system);
        columndata.put("child", child);
        pKeyName = pKeyName.toLowerCase().trim().replaceAll("f_", "");
        columndata.put("pKeyName", pKeyName);

        CustomGenerator mpg = new CustomGenerator(columndata);
        // ????????????
        GlobalConfig gc = new GlobalConfig();
        gc.setFileOverride(true);
        // ?????????ActiveRecord??????????????????false
        gc.setActiveRecord(false);
        // XML ????????????
        gc.setEnableCache(false);
        // XML ResultMap
        gc.setBaseResultMap(false);
        // XML columList
        gc.setBaseColumnList(false);
        gc.setAuthor(userInfo.getUserName() + "/" + userInfo.getUserAccount());
        gc.setOpen(false);

        // ?????????????????????????????? %s ?????????????????????????????????
        gc.setEntityName(model.getClassName() + GenFileNameSuffix.ENTITY);
        gc.setMapperName(model.getClassName() + GenFileNameSuffix.MAPPER);
        gc.setXmlName(model.getClassName() + GenFileNameSuffix.MAPPER_XML);
        gc.setServiceName(model.getClassName() + GenFileNameSuffix.SERVICE);
        gc.setServiceImplName(model.getClassName() + GenFileNameSuffix.SERVICEIMPL);
        gc.setControllerName(model.getClassName() + GenFileNameSuffix.CONTROLLER);
        mpg.setGlobalConfig(gc);

        // ???????????????
        SourceUtil sourceUtil = new SourceUtil();
        DataSourceConfig dsc = sourceUtil.dbConfig(userInfo.getTenantDbConnectionString(), linkEntity);
        if (dsc.getUrl().contains(DbType.ORACLE.getDb())) {
            String schema = linkEntity != null ? linkEntity.getUserName() : dataSourceUtil.getUserName();
            //oracle ?????? schema=username
            dsc.setSchemaName(schema);
        }
        mpg.setDataSource(dsc);

        // ????????????
        StrategyConfig strategy = new StrategyConfig();
        strategy.setEntityLombokModel(true);
        // ??????????????????
        strategy.setNaming(NamingStrategy.underline_to_camel);
        // ??????????????????
        strategy.setInclude(tableName);
        strategy.setRestControllerStyle(true);
        mpg.setStrategy(strategy);

        // ?????????
        PackageConfig pc = new PackageConfig();
        pc.setParent("jnpf");
        mpg.setPackageInfo(pc);

        // ???????????????
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        List<FileOutConfig> focList = new ArrayList<>();
        String javaPath = model.getServiceDirectory();

        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "Controller.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "java" + File.separator + "controller" + File.separator + tableInfo.getControllerName() + StringPool.DOT_JAVA;
            }
        });
        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "Entity.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                List<TableField> fieldAll = tableInfo.getFields();
                for (TableField field : fieldAll) {
                    String name = field.getName().toLowerCase().replaceAll("f_", "");
                    field.setPropertyName(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name));
                }
                tableInfo.setFields(fieldAll);
                return javaPath + fileName + File.separator + "java" + File.separator + "entity" + File.separator + tableInfo.getEntityName() + StringPool.DOT_JAVA;
            }
        });
        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "Mapper.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "java" + File.separator + "mapper" + File.separator + tableInfo.getMapperName() + StringPool.DOT_JAVA;
            }
        });
        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "Mapper.xml.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "resources" + File.separator + "mapper" + File.separator + tableInfo.getMapperName() + StringPool.DOT_XML;
            }
        });
        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "Service.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "java" + File.separator + "service" + File.separator + tableInfo.getServiceName() + StringPool.DOT_JAVA;
            }
        });
        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "ServiceImpl.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "java" + File.separator + "service" + File.separator + "impl" + File.separator + tableInfo.getServiceImplName() + StringPool.DOT_JAVA;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setTemplate(new TemplateConfig().setXml(null).setMapper(null).setController(null).setEntity(null).setService(null).setServiceImpl(null));
        mpg.setCfg(cfg);
        // ????????????
        mpg.execute(path);
    }

    /**
     * ????????????
     *
     * @param modelName       ??????
     * @param dataSourceUtil  ?????????
     * @param path            ??????
     * @param fileName        ???????????????
     * @param className       ????????????
     * @param table           ??????
     * @param userInfo        ??????
     * @param configValueUtil ????????????
     */
    private static void childTable(DataSourceUtil dataSourceUtil, String path, String fileName, String templatePath, String modelName, String className, String table, UserInfo userInfo, ConfigValueUtil configValueUtil, DbLinkEntity linkEntity) {
        Map<String, Object> columndata = new HashMap<>(16);

        Template7Model model = new Template7Model();
        model.setClassName(table);
        model.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
        model.setCreateDate(DateUtil.daFormat(new Date()));
        model.setCreateUser(GenBaseInfo.AUTHOR);
        model.setCopyright("??????????????????????????????(https://www.jnpfsoft.com)");
        model.setDescription(table);

        columndata.put("genInfo", model);

        CustomGenerator mpg = new CustomGenerator(columndata);
        // ????????????
        GlobalConfig gc = new GlobalConfig();
        gc.setFileOverride(true);
        // ?????????ActiveRecord??????????????????false
        gc.setActiveRecord(false);
        // XML ????????????
        gc.setEnableCache(false);
        // XML ResultMap
        gc.setBaseResultMap(false);
        // XML columList
        gc.setBaseColumnList(false);
        gc.setAuthor(model.getCreateUser());
        gc.setOpen(false);

        // ?????????????????????????????? %s ?????????????????????????????????
        gc.setEntityName(className + GenFileNameSuffix.ENTITY);
        gc.setMapperName(className + GenFileNameSuffix.MAPPER);
        gc.setXmlName(className + GenFileNameSuffix.MAPPER_XML);
        gc.setServiceName(className + GenFileNameSuffix.SERVICE);
        gc.setServiceImplName(className + GenFileNameSuffix.SERVICEIMPL);
        mpg.setGlobalConfig(gc);

        // ???????????????
        SourceUtil sourceUtil = new SourceUtil();
        DataSourceConfig dsc = sourceUtil.dbConfig(userInfo.getTenantDbConnectionString(), linkEntity);
        if (dsc.getUrl().contains(DbType.ORACLE.getDb())) {
            String schema = linkEntity != null ? linkEntity.getUserName() : dataSourceUtil.getUserName();
            //oracle ?????? schema=username
            dsc.setSchemaName(schema);
        }
        mpg.setDataSource(dsc);

        // ????????????
        StrategyConfig strategy = new StrategyConfig();
        strategy.setEntityLombokModel(true);
        // ??????????????????
        strategy.setNaming(NamingStrategy.underline_to_camel);
        if (DbTypeUtil.checkDb(dataSourceUtil, DbMysql.DB_ENCODE)) {
            // ??????????????????
            strategy.setInclude(table);
        } else if (DbTypeUtil.checkDb(dataSourceUtil, DbSqlserver.DB_ENCODE)){
            // ??????????????????
            strategy.setInclude(table);
        } else if (DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE)) {
            // ??????????????????
            strategy.setInclude(table);
        } else if (DbTypeUtil.checkDb(dataSourceUtil, DbDm.DB_ENCODE)) {
            // ??????????????????
            strategy.setInclude(table);
        }
        strategy.setRestControllerStyle(true);
        mpg.setStrategy(strategy);

        // ?????????
        PackageConfig pc = new PackageConfig();
        pc.setParent("jnpf");
        mpg.setPackageInfo(pc);

        // ???????????????
        InjectionConfig cfg = new InjectionConfig() {
            @Override
            public void initMap() {
                // to do nothing
            }
        };
        List<FileOutConfig> focList = new ArrayList<>();
        String javaPath = model.getServiceDirectory();
        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "Entity.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                List<TableField> fieldAll = tableInfo.getFields();
                for (TableField field : fieldAll) {
                    String name = field.getName().toLowerCase().replaceAll("f_", "");
                    field.setPropertyName(CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name));
                }
                tableInfo.setFields(fieldAll);
                return javaPath + fileName + File.separator + "java" + File.separator + "entity" + File.separator + tableInfo.getEntityName() + StringPool.DOT_JAVA;
            }
        });
        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "Mapper.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "java" + File.separator + "mapper" + File.separator + tableInfo.getMapperName() + StringPool.DOT_JAVA;
            }
        });
        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "Mapper.xml.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "resources" + File.separator + "mapper" + File.separator + tableInfo.getMapperName() + StringPool.DOT_XML;
            }
        });
        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "Service.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "java" + File.separator + "service" + File.separator + tableInfo.getServiceName() + StringPool.DOT_JAVA;
            }
        });
        focList.add(new FileOutConfig(templatePath + File.separator + "java" + File.separator + "ServiceImpl.java.vm") {
            @Override
            public String outputFile(TableInfo tableInfo) {
                return javaPath + fileName + File.separator + "java" + File.separator + "service" + File.separator + "impl" + File.separator + tableInfo.getServiceImplName() + StringPool.DOT_JAVA;
            }
        });
        cfg.setFileOutConfigList(focList);
        mpg.setTemplate(new TemplateConfig().setXml(null).setMapper(null).setController(null).setEntity(null).setService(null).setServiceImpl(null));
        mpg.setCfg(cfg);
        // ????????????
        mpg.execute(path);
    }

    /**
     * ???????????????
     *
     * @param entity           ??????
     * @param dataSourceUtil   ?????????
     * @param fileName         ???????????????
     * @param downloadCodeForm ????????????
     * @param userInfo         ??????
     * @param configValueUtil  ????????????
     */
    public static void generate(VisualdevEntity entity, DataSourceUtil dataSourceUtil, String fileName, String templatePath, DownloadCodeForm downloadCodeForm, UserInfo userInfo, ConfigValueUtil configValueUtil, DbLinkEntity linkEntity) throws SQLException {
        List<TableModel> list = JsonUtil.getJsonToList(entity.getTables(), TableModel.class);
        //????????????
        int i = 0;
        for (TableModel model : list) {
            if ("1".equals(model.getTypeId())) {
                setCode(dataSourceUtil, configValueUtil.getTemplateCodePath(), fileName, templatePath, downloadCodeForm, entity, userInfo, configValueUtil, linkEntity);
            } else if ("0".equals(model.getTypeId())) {
                String name = downloadCodeForm.getSubClassName().split(",")[i];
                String className = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
                childTable(dataSourceUtil, configValueUtil.getTemplateCodePath(), fileName, templatePath, downloadCodeForm.getModule(), className, model.getTable(), userInfo, configValueUtil, linkEntity);
                i++;
            }
        }
    }

}
