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
import jnpf.database.model.DataSourceUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.base.util.*;
import jnpf.config.ConfigValueUtil;
import jnpf.base.entity.VisualdevEntity;
import jnpf.generater.genutil.custom.CustomGenerator;
import jnpf.generater.model.GenBaseInfo;
import jnpf.generater.model.GenFileNameSuffix;
import jnpf.model.visiual.DownloadCodeForm;
import jnpf.model.visiual.JnpfKeyConsts;
import jnpf.model.visiual.TableModel;
import jnpf.base.model.template7.*;
import jnpf.model.visiual.FormDataModel;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.model.visiual.fields.config.ConfigModel;
import jnpf.model.FormAllModel;
import jnpf.model.FormColumnModel;
import jnpf.model.FormColumnTableModel;
import jnpf.model.FormEnum;
import jnpf.model.visiual.fields.slot.SlotModel;
import jnpf.util.*;
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
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date 2021/3/16
 */
public class WorkVueGenUtil {

    //------------------------------------界面----------------------------------
    /**
     * 判断包名是否要加form
     */
    private static boolean isForm = true;

    /**
     * 界面模板
     *
     * @param fileName         文件夹名称
     * @param downloadCodeForm 文件名称
     * @param model            模型
     * @param templatePath     模板路径
     * @param userInfo         用户
     * @param configValueUtil  下载路径
     */
    public static void htmlTemplates(String fileName, VisualdevEntity entity, DownloadCodeForm downloadCodeForm, FormDataModel model, String templatePath, UserInfo userInfo, ConfigValueUtil configValueUtil, String pKeyName) {
        Map<String, Object> map = new HashMap<>(16);
        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<FormAllModel> formAllModel = new ArrayList<>();
        FormCloumnUtil.recursionForm(list, formAllModel);

        //form的属性
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //子表赋值
        List<Map<String, Object>> child = new ArrayList<>();
        for (int i = 0; i < table.size(); i++) {
            FormColumnTableModel childList = table.get(i).getChildList();
            List<FormColumnModel> tableList = childList.getChildList();
            for (FormColumnModel columnModel : tableList) {
                FieLdsModel fieLdsModel = columnModel.getFieLdsModel();
                model(fieLdsModel);
            }
            childList.setChildList(tableList);
            String name = downloadCodeForm.getSubClassName().split(",")[i];
            Map<String, Object> childs = JsonUtil.entityToMap(childList);
            String className = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            childs.put("className", className);
            child.add(childs);
        }
        //主表赋值
        for (int i = 0; i < mast.size(); i++) {
            FieLdsModel fieLdsModel = mast.get(i).getFormColumnModel().getFieLdsModel();
            model(fieLdsModel);
            String vmodel = fieLdsModel.getVModel();
            if (StringUtil.isEmpty(vmodel)) {
                mast.remove(i);
            }
        }
        if (isForm) {
            map.put("isForm", downloadCodeForm.getModule());
        }
        //界面
        map.put("module", downloadCodeForm.getModule());
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

        //form和model
        Template7Model temModel = new Template7Model();
        temModel.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
        temModel.setCreateDate(DateUtil.daFormat(new Date()));
        temModel.setCreateUser(GenBaseInfo.AUTHOR);
        temModel.setCopyright("引迈信息技术有限公司(https://www.jnpfsoft.com)");
        temModel.setClassName(downloadCodeForm.getClassName().substring(0, 1).toLowerCase() + downloadCodeForm.getClassName().substring(1).toLowerCase());
        temModel.setDescription("");
        map.put("genInfo", temModel);
        map.put("modelName", model.getClassName());
        map.put("package", "jnpf");
        //共用
        map.put("children", child);
        map.put("fields", mast);
        pKeyName = pKeyName.toLowerCase().trim().replaceAll("f_", "");
        map.put("pKeyName", pKeyName);

        htmlTemplates(model.getServiceDirectory() + fileName, map, templatePath);

        //子表model
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
            if (map.get("isForm") != null) {
                objectMap.put("isForm", map.get("isForm"));
            }
            childrenTemplates(model.getServiceDirectory() + fileName, objectMap, templatePath, className, downloadCodeForm.getClassName());
        }
    }

    private static void model(FieLdsModel fieLdsModel){
        ConfigModel configModel = fieLdsModel.getConfig();
        String jnpfKey = configModel.getJnpfKey();
        if (configModel.getDefaultValue() instanceof String) {
            configModel.setValueType("String");
        }
        if (configModel.getDefaultValue() == null) {
            configModel.setValueType("undefined");
            if(JnpfKeyConsts.NUM_INPUT.equals(jnpfKey)){
                configModel.setDefaultValue(0);
                configModel.setValueType(null);
            }
        }
        fieLdsModel.setConfig(configModel);
    }

    /**
     * 获取文件名
     *
     * @param path      路径
     * @param template  模板名称
     * @param className 文件名称
     * @return
     */
    private static String getFileName(String path, String template, String className, String isForm) {
        String modelName = className.substring(0, 1).toLowerCase() + className.substring(1).toLowerCase();
        String modelPath = path + File.separator + "java" + File.separator + "model";
        if (StringUtil.isNotEmpty(isForm)) {
            modelPath = path + File.separator + "java" + File.separator + "model" + File.separator + modelName;
        }
        String pcHtmlPath = path + File.separator + "html" +File.separator +"pc";
        File pcHtmlfile = new File(pcHtmlPath);
        if (!pcHtmlfile.exists()) {
            pcHtmlfile.mkdirs();
        }
        String appHtmlPath = path + File.separator + "html" +File.separator +"app";
        File appHtmlfile = new File(appHtmlPath);
        if (!appHtmlfile.exists()) {
            appHtmlfile.mkdirs();
        }
        File modelfile = new File(modelPath);
        if (!modelfile.exists()) {
            modelfile.mkdirs();
        }
        if (template.contains("form.vue.vm")) {
            className = "index";
            return pcHtmlfile + File.separator + className + ".vue";
        }
        if (template.contains("app.vue.vm")) {
            className = "index";
            return appHtmlfile + File.separator + className + ".vue";
        }
        if (template.contains("Form.java.vm")) {
            return modelPath + File.separator + className + "Form.java";
        }
        if (template.contains("InfoVO.java.vm")) {
            return modelPath + File.separator + className + "InfoVO.java";
        }
        if (template.contains("Model.java.vm")) {
            return modelPath + File.separator + className + "Model.java";
        }
        return null;
    }

    /**
     * 界面的模板
     *
     * @param template 模板集合
     * @return
     */
    private static List<String> getTemplates(String template) {
        List<String> templates = new ArrayList<>();
        templates.add(template + File.separator + "html" + File.separator + "app.vue.vm");
        templates.add(template + File.separator + "html" + File.separator + "form.vue.vm");
        templates.add(template + File.separator + "java" + File.separator + "Form.java.vm");
        templates.add(template + File.separator + "java" + File.separator + "InfoVO.java.vm");
        return templates;
    }

    /**
     * 渲染html模板
     *
     * @param path         路径
     * @param object       模板数据
     * @param templatePath 模板路径
     */
    private static void htmlTemplates(String path, Map<String, Object> object, String templatePath) {
        List<String> templates = getTemplates(templatePath);
        //界面模板
        VelocityContext context = new VelocityContext();
        context.put("context", object);
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF_8);
            tpl.merge(context, sw);
            try {
                String className = object.get("className").toString();
                String isForm = object.get("isForm") != null ? object.get("isForm").toString() : null;
                String fileNames = getFileName(path, template, className, isForm);
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
                System.out.println("渲染模板失败，表名：" + e);
            }
        }
    }

    /**
     * 渲染html模板
     *
     * @param path         路径
     * @param object       模板数据
     * @param templatePath 模板路径
     */
    private static void childrenTemplates(String path, Map<String, Object> object, String templatePath, String className, String model) {
        List<String> templates = new ArrayList<>();
        templates.add(templatePath + File.separator + "java" + File.separator + "Model.java.vm");
        //界面模板
        VelocityContext context = new VelocityContext();
        context.put("context", object);
        for (String template : templates) {
            // 渲染模板
            StringWriter sw = new StringWriter();
            Template tpl = Velocity.getTemplate(template, Constants.UTF_8);
            tpl.merge(context, sw);
            try {
                String fileNames = path + File.separator + "java" + File.separator + "model" + File.separator + className + "Model.java";
                if (object.get("isForm") != null) {
                    fileNames = path + File.separator + "java" + File.separator + "model" + File.separator + model + File.separator + className + "Model.java";
                }
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
                System.out.println("渲染模板失败，表名：" + e);
            }
        }
    }
    //-------------------------代码----------------------------------

    /**
     * 生成主表
     *
     * @param dataSourceUtil   数据源
     * @param path             路径
     * @param fileName         文件夹名称
     * @param downloadCodeForm 文件名称
     * @param entity           实体
     * @param userInfo         用户
     * @param configValueUtil  下载路径
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
        model.setCopyright("引迈信息技术有限公司(https://www.jnpfsoft.com)");
        model.setDescription(downloadCodeForm.getDescription());

        //formTempJson
        FormDataModel formData = JsonUtil.getJsonToBean(entity.getFormData(), FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<FormAllModel> formAllModel = new ArrayList<>();
        FormCloumnUtil.recursionForm(list, formAllModel);
        //主表数据
        List<FormAllModel> mast = formAllModel.stream().filter(t -> FormEnum.mast.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        List<FormAllModel> table = formAllModel.stream().filter(t -> FormEnum.table.getMessage().equals(t.getJnpfKey())).collect(Collectors.toList());
        //主表的字段
        String tableName = tableModelList.stream().filter(t -> "1".equals(t.getTypeId())).findFirst().get().getTable();
        String billNo = "";
        List<FieLdsModel> system = new ArrayList<>();
        for (int i = 0; i < mast.size(); i++) {
            FormAllModel mastModel = mast.get(i);
            FieLdsModel fieLdsModel = mastModel.getFormColumnModel().getFieLdsModel();
            ConfigModel configModel = fieLdsModel.getConfig();
            String vmodel = fieLdsModel.getVModel();
            String jnpfkey = configModel.getJnpfKey();
            if (JnpfKeyConsts.BILLRULE.equals(jnpfkey) && StringUtil.isEmpty(billNo)) {
                billNo = configModel.getRule();
            }
            if (StringUtil.isNotEmpty(vmodel)) {
                system.add(fieLdsModel);
            }
        }

        @Cleanup Connection conn = VisualUtils.getDataConn(linkEntity);
        //获取主表主键
        String pKeyName = VisualUtils.getpKey(conn, tableName).toLowerCase().trim().replaceAll("f_", "");
        //子表的属性
        List<Map<String, Object>> child = new ArrayList<>();
        for (int i = 0; i < table.size(); i++) {
            FormColumnTableModel childList = table.get(i).getChildList();
            String name = downloadCodeForm.getSubClassName().split(",")[i];
            Map<String, Object> childs = JsonUtil.entityToMap(childList);
            String className = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
            TableModel tableModel = tableModelList.stream().filter(t -> t.getTable().equals(childList.getTableName())).findFirst().get();
            //获取主表主键
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
        //判断是否要加包位置
        if (isForm) {
            columndata.put("isForm", downloadCodeForm.getModule());
        }
        //后台
        columndata.put("module", downloadCodeForm.getModule());
        columndata.put("genInfo", model);
        columndata.put("modelName", model.getClassName());
        columndata.put("typeId", 1);
        columndata.put("system", system);
        columndata.put("child", child);
        columndata.put("billNo", billNo);
        columndata.put("pKeyName", pKeyName);

        CustomGenerator mpg = new CustomGenerator(columndata);
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setFileOverride(true);
        // 不需要ActiveRecord特性的请改为false
        gc.setActiveRecord(false);
        // XML 二级缓存
        gc.setEnableCache(false);
        // XML ResultMap
        gc.setBaseResultMap(false);
        // XML columList
        gc.setBaseColumnList(false);
        gc.setAuthor(userInfo.getUserName() + "/" + userInfo.getUserAccount());
        gc.setOpen(false);

        // 自定义文件命名，注意 %s 会自动填充表实体属性！
        gc.setEntityName(model.getClassName() + GenFileNameSuffix.ENTITY);
        gc.setMapperName(model.getClassName() + GenFileNameSuffix.MAPPER);
        gc.setXmlName(model.getClassName() + GenFileNameSuffix.MAPPER_XML);
        gc.setServiceName(model.getClassName() + GenFileNameSuffix.SERVICE);
        gc.setServiceImplName(model.getClassName() + GenFileNameSuffix.SERVICEIMPL);
        gc.setControllerName(model.getClassName() + GenFileNameSuffix.CONTROLLER);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        SourceUtil sourceUtil = new SourceUtil();
        DataSourceConfig dsc = sourceUtil.dbConfig(userInfo.getTenantDbConnectionString(), linkEntity);
        if (dsc.getUrl().contains(DbType.ORACLE.getDb())) {
            String schema = linkEntity != null ? linkEntity.getUserName() : dataSourceUtil.getUserName();
            //oracle 默认 schema=username
            dsc.setSchemaName(schema);
        }
        mpg.setDataSource(dsc);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setEntityLombokModel(true);
        // 表名生成策略
        strategy.setNaming(NamingStrategy.underline_to_camel);
        // 需要生成的表
        strategy.setInclude(tableName);
        strategy.setRestControllerStyle(true);
        mpg.setStrategy(strategy);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("jnpf");
        if (columndata.get("isForm") != null) {
            pc.setParent("jnpf." + columndata.get("isForm"));
        }
        mpg.setPackageInfo(pc);

        // 自定义配置
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
        // 执行生成
        mpg.execute(path);
    }

    /**
     * 生成子表
     *
     * @param modelName       模块
     * @param dataSourceUtil  数据源
     * @param path            路径
     * @param fileName        文件夹名称
     * @param className       文件名称
     * @param table           子表
     * @param userInfo        用户
     * @param configValueUtil 下载路径
     */
    private static void childTable(DataSourceUtil dataSourceUtil, String path, String fileName, String templatePath, String modelName, String className, String table, UserInfo userInfo, ConfigValueUtil configValueUtil, DbLinkEntity linkEntity) {
        Map<String, Object> columndata = new HashMap<>(16);

        Template7Model model = new Template7Model();
        model.setClassName(table);
        model.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
        model.setCreateDate(DateUtil.daFormat(new Date()));
        model.setCreateUser(GenBaseInfo.AUTHOR);
        model.setCopyright("引迈信息技术有限公司(https://www.jnpfsoft.com)");
        model.setDescription(table);

        columndata.put("genInfo", model);
        if (isForm) {
            columndata.put("isForm", modelName);
        }
        CustomGenerator mpg = new CustomGenerator(columndata);
        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        gc.setFileOverride(true);
        // 不需要ActiveRecord特性的请改为false
        gc.setActiveRecord(false);
        // XML 二级缓存
        gc.setEnableCache(false);
        // XML ResultMap
        gc.setBaseResultMap(false);
        // XML columList
        gc.setBaseColumnList(false);
        gc.setAuthor(model.getCreateUser());
        gc.setOpen(false);

        // 自定义文件命名，注意 %s 会自动填充表实体属性！
        gc.setEntityName(className + GenFileNameSuffix.ENTITY);
        gc.setMapperName(className + GenFileNameSuffix.MAPPER);
        gc.setXmlName(className + GenFileNameSuffix.MAPPER_XML);
        gc.setServiceName(className + GenFileNameSuffix.SERVICE);
        gc.setServiceImplName(className + GenFileNameSuffix.SERVICEIMPL);
        mpg.setGlobalConfig(gc);

        // 数据源配置
        SourceUtil sourceUtil = new SourceUtil();
        DataSourceConfig dsc = sourceUtil.dbConfig(userInfo.getTenantDbConnectionString(), linkEntity);
        if (dsc.getUrl().contains(DbType.ORACLE.getDb())) {
            String schema = linkEntity != null ? linkEntity.getUserName() : dataSourceUtil.getUserName();
            //oracle 默认 schema=username
            dsc.setSchemaName(schema);
        }
        mpg.setDataSource(dsc);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        strategy.setEntityLombokModel(true);
        // 表名生成策略
        strategy.setNaming(NamingStrategy.underline_to_camel);
        // 需要生成的表
        strategy.setInclude(table);
        strategy.setRestControllerStyle(true);
        mpg.setStrategy(strategy);

        // 包配置
        PackageConfig pc = new PackageConfig();
        pc.setParent("jnpf");
        if (columndata.get("isForm") != null) {
            pc.setParent("jnpf." + columndata.get("isForm"));
        }
        mpg.setPackageInfo(pc);

        // 自定义配置
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
        // 执行生成
        mpg.execute(path);
    }

    /**
     * 生成表集合
     *
     * @param entity           实体
     * @param dataSourceUtil   数据源
     * @param fileName         文件夹名称
     * @param downloadCodeForm 文件名称
     * @param userInfo         用户
     * @param configValueUtil  下载路径
     */
    public static void generate(VisualdevEntity entity, DataSourceUtil dataSourceUtil, String fileName, String templatePath, DownloadCodeForm downloadCodeForm, UserInfo userInfo, ConfigValueUtil configValueUtil, DbLinkEntity linkEntity) throws SQLException {
        List<TableModel> list = JsonUtil.getJsonToList(entity.getTables(), TableModel.class);
        //生成代码
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
