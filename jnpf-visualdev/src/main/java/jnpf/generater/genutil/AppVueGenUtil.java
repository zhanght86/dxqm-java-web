package jnpf.generater.genutil;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import jnpf.base.UserInfo;
import jnpf.config.ConfigValueUtil;
import jnpf.base.entity.VisualdevEntity;
import jnpf.database.source.impl.DbDm;
import jnpf.database.source.impl.DbMysql;
import jnpf.database.source.impl.DbOracle;
import jnpf.database.source.impl.DbSqlserver;
import jnpf.database.util.DbTypeUtil;
import jnpf.generater.genutil.custom.CustomGenerator;
import jnpf.generater.model.GenFileNameSuffix;
import jnpf.model.visiual.DownloadCodeForm;
import jnpf.model.visiual.TableModel;
import jnpf.base.model.template6.Template6Model;
import jnpf.model.visiual.FormDataModel;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.model.visiual.fields.config.ConfigModel;
import jnpf.base.util.*;
import jnpf.database.model.DataSourceUtil;
import jnpf.util.DateUtil;
import jnpf.util.JsonUtil;
import jnpf.util.StringUtil;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @author JNPF开发平台组
 * @date 2021/3/16
 */
public class AppVueGenUtil {

        //------------------------------------界面----------------------------------

        /**
         * 界面模板
         *
         * @param fileName        文件夹名称
         * @param entity          实体
         * @param model           模型
         * @param templatePath    模板路径
         * @param userInfo        用户
         * @param configValueUtil 下载路径
         */
        public static void htmlTemplates(String fileName, VisualdevEntity entity, FormDataModel model, String templatePath, UserInfo userInfo, ConfigValueUtil configValueUtil) {
            Map<String, Object> map = new HashMap<>(16);
            //界面
            map.put("module", model.getAreasName());
            map.put("className", model.getClassName());
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

            //取出列表数据中的查询列表和数据列表
            map.put("columnData", JsonUtil.stringToMap(entity.getColumnData()));
            //添加判断默认值类型的字段
            List<FieLdsModel> list = JsonUtil.getJsonToList(model.getFields(), FieLdsModel.class);
//        List<FieLdsModel> dataForm = new ArrayList<>();
            for (FieLdsModel model1 : list) {
                ConfigModel configModel = model1.getConfig();
                if (configModel.getDefaultValue() instanceof String) {
                    configModel.setValueType("String");
                }
                if (configModel.getDefaultValue() == null) {
                    configModel.setValueType("undefined");
                }
                //前台界面的属性去掉前2个
                if (StringUtil.isNotEmpty(model1.getVModel())) {
                    String vmodel = model1.getVModel().substring(2);
                    model1.setVModel(vmodel.substring(0, 1).toLowerCase() + vmodel.substring(1));
                }
            }
            map.put("fields", list);

            //model
            Template6Model temModel = new Template6Model();
            temModel.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
            temModel.setCreateDate(DateUtil.getNow());
            temModel.setCreateUser(userInfo.getUserName()+"/"+userInfo.getUserAccount());
            temModel.setCopyright("引迈信息技术有限公司");
            temModel.setDescription("");
            map.put("genInfo",temModel);
            map.put("modelName", model.getClassName());
            map.put("package","jnpf");

            //app
            map.put("form",list);
            htmlTemplates(model.getServiceDirectory() + fileName, map, templatePath);
        }

        /**
         * 获取文件名
         *
         * @param path      路径
         * @param template  模板名称
         * @param className 文件名称
         * @return
         */
        private static String getFileName(String path, String template, String className) {
            String modelPath = path + File.separator +"java"+ File.separator +"model";
            String htmlPath = path + File.separator + "html";
            File htmlfile = new File(htmlPath);
            File modelfile = new File(modelPath);
            if (!htmlfile.exists()) {
                htmlfile.mkdirs();
            }
            if (!modelfile.exists()) {
                modelfile.mkdirs();
            }
            if (template.contains("form.vue.vm")) {
                className = "index";
                return htmlPath + File.separator + className + ".vue";
            }
            if (template.contains("app.vue.vm")) {
                return htmlPath + File.separator + className + ".vue";
            }
            if (template.contains("Form.java.vm")) {
                return modelPath + File.separator + className + "Form.java";
            }
            if (template.contains("InfoVO.java.vm")) {
                return modelPath + File.separator + className + "InfoVO.java";
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
            templates.add(template + File.separator + "html" + File.separator + "Field.vue.vm");
            templates.add(template + File.separator + "html" + File.separator + "Form.vue.vm");
            templates.add(template + File.separator + "html" + File.separator + "Index.vue.vm");
            return templates;
        }

        /**
         * 渲染html模板
         *
         * @param path         路径
         * @param object       模板数据
         * @param templatePath 模板路径
         */
        private static void htmlTemplates(String path, Object object, String templatePath) {
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
        private static void setCode(DataSourceUtil dataSourceUtil, String path, String fileName, DownloadCodeForm downloadCodeForm, VisualdevEntity entity, UserInfo userInfo, ConfigValueUtil configValueUtil) {
            List<Map<String, Object>> tablesList = JsonUtil.getJsonToListMap(entity.getTables());
            Map<String, Object> columndata = new HashMap<>(16);

            Template6Model model = new Template6Model();
            model.setClassName(downloadCodeForm.getClassName().substring(0, 1).toUpperCase() + downloadCodeForm.getClassName().substring(1));
            model.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
            model.setCreateDate(DateUtil.getNow());
            model.setCreateUser(userInfo.getUserName() + "/" + userInfo.getUserAccount());
            model.setCopyright("引迈信息技术有限公司");
            model.setDescription(downloadCodeForm.getDescription());

            model.setDbTableRelation(JsonUtil.getJsonToList(tablesList, TableModel.class));
            String tableName = "";
            List<Map<String, Object>> childList = new ArrayList<>();
            int i = 0;
            for (TableModel tableModel : model.getDbTableRelation()) {
                if ("1".equals(tableModel.getTypeId())) {
                    tableName = tableModel.getTable();
                } else if ("0".equals(tableModel.getTypeId())) {
                    Map<String, Object> map = JsonUtil.entityToMap(tableModel);
                    map.put("className", downloadCodeForm.getSubClassName().split(",")[i]);
                    childList.add(map);
                    i++;
                }
            }

            columndata.put("genInfo", model);
            columndata.put("areasName", downloadCodeForm.getModule());
            columndata.put("modelName", model.getClassName());
            columndata.put("typeId", 1);
            columndata.put("dbtable", childList);

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
            DataSourceConfig dsc = sourceUtil.dbConfig(userInfo.getTenantDbConnectionString());
            mpg.setDataSource(dsc);

            // 策略配置
            StrategyConfig strategy = new StrategyConfig();
            strategy.setEntityLombokModel(true);
            // 表名生成策略
            strategy.setNaming(NamingStrategy.underline_to_camel);
            if (DbTypeUtil.checkDb(dataSourceUtil, DbMysql.DB_ENCODE)) {
                // 需要生成的表
                strategy.setInclude(tableName);
            } else if (DbTypeUtil.checkDb(dataSourceUtil, DbSqlserver.DB_ENCODE)) {
                // 需要生成的表
                strategy.setInclude(tableName);
            }else if(DbTypeUtil.checkDb(dataSourceUtil, DbOracle.DB_ENCODE)){
                // 需要生成的表
                strategy.setInclude(tableName);
            }else if(DbTypeUtil.checkDb(dataSourceUtil, DbDm.DB_ENCODE)){
                // 需要生成的表
                strategy.setInclude(tableName);
            }
            strategy.setRestControllerStyle(true);
            mpg.setStrategy(strategy);

            // 包配置
            PackageConfig pc = new PackageConfig();
            pc.setParent("jnpf." + downloadCodeForm.getModule());
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


            //自带
            focList.add(new FileOutConfig("TemplateCode7" + File.separator + "" + File.separator + "java" + File.separator + "" + File.separator + "Controller.java.vm") {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return javaPath + fileName + File.separator + "java" + File.separator + "jnpf.controller" + File.separator + tableInfo.getControllerName() + StringPool.DOT_JAVA;
                }
            });
            focList.add(new FileOutConfig("TemplateCode7" + File.separator + "" + File.separator + "java" + File.separator + "" + File.separator + "Entity.java.vm") {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return javaPath + fileName + File.separator + "java" + File.separator + "entity" + File.separator + tableInfo.getEntityName() + StringPool.DOT_JAVA;
                }
            });
            focList.add(new FileOutConfig("TemplateCode7" + File.separator + "java" + File.separator + "jnpf.mapper.java.vm") {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return javaPath + fileName + File.separator + "java" + File.separator + "jnpf.mapper" + File.separator + tableInfo.getMapperName() + StringPool.DOT_JAVA;
                }
            });
            focList.add(new FileOutConfig("TemplateCode7" + File.separator + "java" + File.separator + "jnpf.mapper.xml.vm") {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return javaPath + fileName + File.separator + "resources" + File.separator + "jnpf.mapper" + File.separator + tableInfo.getMapperName() + StringPool.DOT_XML;
                }
            });
            focList.add(new FileOutConfig("TemplateCode7" + File.separator + "" + File.separator + "java" + File.separator + "" + File.separator + "jnpf.service.java.vm") {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return javaPath + fileName + File.separator + "java" + File.separator + "jnpf.service" + File.separator + tableInfo.getServiceName() + StringPool.DOT_JAVA;
                }
            });
            focList.add(new FileOutConfig("TemplateCode7" + File.separator + "" + File.separator + "java" + File.separator + "" + File.separator + "serviceImpl.java.vm") {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return javaPath + fileName + File.separator + "java" + File.separator + "jnpf.service" + File.separator + "" + File.separator + "impl" + File.separator + tableInfo.getServiceImplName() + StringPool.DOT_JAVA;
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
         * @param dataSourceUtil  数据源
         * @param path            路径
         * @param fileName        文件夹名称
         * @param entity          实体
         * @param className       文件名称
         * @param table           子表
         * @param userInfo        用户
         * @param configValueUtil 下载路径
         * @return
         */
        private static String childTable(DataSourceUtil dataSourceUtil, String path, String fileName, VisualdevEntity entity, String className, String table, UserInfo userInfo, ConfigValueUtil configValueUtil) {
            Map<String, Object> columndata = new HashMap<>(16);

            Template6Model model = new Template6Model();

            model.setClassName(table);
            model.setServiceDirectory(configValueUtil.getServiceDirectoryPath());
            model.setCreateDate(DateUtil.getNow());
            model.setCreateUser(userInfo.getUserName() + "/" + userInfo.getUserAccount());
            model.setCopyright("引迈信息技术有限公司");
            model.setDescription(table);

            columndata.put("genInfo", model);

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
            mpg.setGlobalConfig(gc);

            // 数据源配置
            SourceUtil sourceUtil = new SourceUtil();
            DataSourceConfig dsc = sourceUtil.dbConfig(userInfo.getTenantDbConnectionString());
            mpg.setDataSource(dsc);

            // 策略配置
            StrategyConfig strategy = new StrategyConfig();
            strategy.setEntityLombokModel(true);
            // 表名生成策略
            strategy.setNaming(NamingStrategy.underline_to_camel);
            if (DbTypeUtil.checkDb(dataSourceUtil,DbMysql.DB_ENCODE)) {
                // 需要生成的表
                strategy.setInclude(table);
            } else if (DbTypeUtil.checkDb(dataSourceUtil,DbSqlserver.DB_ENCODE)) {
                // 需要生成的表
                strategy.setInclude(table);
            }else if(DbTypeUtil.checkDb(dataSourceUtil,DbOracle.DB_ENCODE)){
                // 需要生成的表
                strategy.setInclude(table);
            }else if(DbTypeUtil.checkDb(dataSourceUtil,DbDm.DB_ENCODE)){
                // 需要生成的表
                strategy.setInclude(table);
            }
            strategy.setRestControllerStyle(true);
            mpg.setStrategy(strategy);

            // 包配置
            PackageConfig pc = new PackageConfig();
            pc.setParent("jnpf");
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
            focList.add(new FileOutConfig("TemplateCode7" + File.separator + "java" + File.separator + "Entity.java.vm") {
                @Override
                public String outputFile(TableInfo tableInfo) {
                    return javaPath + fileName + File.separator + "java" + File.separator + "entity" + File.separator + tableInfo.getEntityName() + StringPool.DOT_JAVA;
                }
            });
            cfg.setFileOutConfigList(focList);
            mpg.setTemplate(new TemplateConfig().setXml(null).setMapper(null).setController(null).setEntity(null).setService(null).setServiceImpl(null));
            mpg.setCfg(cfg);
            // 执行生成
            mpg.execute(path);
            return fileName;
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
        public static void generate(VisualdevEntity entity, DataSourceUtil dataSourceUtil, String fileName, DownloadCodeForm downloadCodeForm, UserInfo userInfo, ConfigValueUtil configValueUtil) {
            List<TableModel> list = JsonUtil.getJsonToList(entity.getTables(), TableModel.class);
            //生成代码
            int i = 0;
            for (TableModel model : list) {
                if ("1".equals(model.getTypeId())) {
                    setCode(dataSourceUtil, configValueUtil.getTemplateCodePath(), fileName, downloadCodeForm, entity, userInfo, configValueUtil);
                } else if ("0".equals(model.getTypeId())) {
                    childTable(dataSourceUtil, configValueUtil.getTemplateCodePath(), fileName, entity, downloadCodeForm.getSubClassName().split(",")[i], model.getTable(), userInfo, configValueUtil);
                    i++;
                }
            }
        }
}
