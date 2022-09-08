package jnpf.base.util;

import jnpf.model.*;
import jnpf.model.visiual.FormDataModel;
import jnpf.model.visiual.JnpfKeyConsts;
import jnpf.model.visiual.fields.FieLdsModel;
import jnpf.model.visiual.fields.config.ConfigModel;
import jnpf.util.JsonUtil;
import jnpf.util.RandomUtil;
import jnpf.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 在线工作流开发
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2019年9月27日 上午9:18
 */
public class FormCloumnUtil {

    public static void main(String[] args) {
//        String a = "{\"formRef\":\"elForm\",\"formModel\":\"dataForm\",\"size\":\"small\",\"labelPosition\":\"right\",\"labelWidth\":100,\"formRules\":\"rules\",\"popupType\":\"general\",\"generalWidth\":\"600px\",\"fullScreenWidth\":\"100%\",\"gutter\":15,\"disabled\":false,\"span\":24,\"formBtns\":false,\"cancelButtonText\":\"取 消\",\"confirmButtonText\":\"确 定\",\"formStyle\":\"\",\"idGlobal\":117,\"fields\":[{\"__config__\":{\"jnpfKey\":\"tab\",\"label\":\"标签页\",\"showLabel\":false,\"tag\":\"el-tab\",\"tagIcon\":\"icon-ym icon-ym-generator-label\",\"layout\":\"rowFormItem\",\"span\":24,\"dragDisabled\":false,\"children\":[{\"title\":\"Tab 1\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入1\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":107,\"renderKey\":1628151828662},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"aaa\"}],\"formId\":102,\"renderKey\":1628151825851}},{\"title\":\"Tab 2\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"collapse\",\"label\":\"折叠面板\",\"showLabel\":false,\"tag\":\"el-collapse\",\"tagIcon\":\"icon-ym icon-ym-generator-fold\",\"layout\":\"rowFormItem\",\"span\":24,\"dragDisabled\":false,\"children\":[{\"title\":\"面板1\",\"name\":\"1\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入1122\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":116,\"renderKey\":1628152597434},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"ccc\"}],\"formId\":113,\"renderKey\":1628152567322}},{\"title\":\"面板2\",\"name\":\"2\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入333\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":117,\"renderKey\":1628152600603},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"ddd\"}],\"formId\":114,\"renderKey\":1628152567322}}],\"active\":[\"1\",\"2\"],\"formId\":112,\"renderKey\":1628152567322,\"componentName\":\"row112\"},\"accordion\":false},{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入2\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":108,\"renderKey\":1628151832460},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"aa\"}],\"formId\":103,\"renderKey\":1628151825851}}],\"active\":\"1\",\"formId\":101,\"renderKey\":1628151825851,\"componentName\":\"row101\"},\"type\":\"\",\"tab-position\":\"top\"},{\"__config__\":{\"jnpfKey\":\"collapse\",\"label\":\"折叠面板\",\"showLabel\":false,\"tag\":\"el-collapse\",\"tagIcon\":\"icon-ym icon-ym-generator-fold\",\"layout\":\"rowFormItem\",\"span\":24,\"dragDisabled\":false,\"children\":[{\"title\":\"面板1\",\"name\":\"1\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":109,\"renderKey\":1628151836551},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"bb\"}],\"formId\":105,\"renderKey\":1628151826435}},{\"title\":\"面板2\",\"name\":\"2\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":110,\"renderKey\":1628151838644},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"bbb\"}],\"formId\":106,\"renderKey\":1628151826435}},{\"title\":\"新面板\",\"name\":\"7SffpY\",\"__config__\":{\"children\":[]}}],\"active\":[\"2\"],\"formId\":104,\"renderKey\":1628151826435,\"componentName\":\"row104\"},\"accordion\":false}]}";
//        String a = "{\"formRef\":\"elForm\",\"formModel\":\"dataForm\",\"size\":\"small\",\"labelPosition\":\"right\",\"labelWidth\":100,\"formRules\":\"rules\",\"popupType\":\"general\",\"generalWidth\":\"600px\",\"fullScreenWidth\":\"100%\",\"gutter\":15,\"disabled\":false,\"span\":24,\"formBtns\":false,\"cancelButtonText\":\"取 消\",\"confirmButtonText\":\"确 定\",\"formStyle\":\"\",\"idGlobal\":117,\"fields\":[{\"__config__\":{\"jnpfKey\":\"tab\",\"label\":\"标签页\",\"showLabel\":false,\"tag\":\"el-tab\",\"tagIcon\":\"icon-ym icon-ym-generator-label\",\"layout\":\"rowFormItem\",\"span\":24,\"dragDisabled\":false,\"children\":[{\"title\":\"1\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入1\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":107,\"renderKey\":1628151828662},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"aaa\"}],\"formId\":102,\"renderKey\":1628151825851}},{\"title\":\"2\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"collapse\",\"label\":\"折叠面板\",\"showLabel\":false,\"tag\":\"el-collapse\",\"tagIcon\":\"icon-ym icon-ym-generator-fold\",\"layout\":\"rowFormItem\",\"span\":24,\"dragDisabled\":false,\"children\":[{\"title\":\"面板1\",\"name\":\"1\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入1122\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":116,\"renderKey\":1628152597434},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"ccc\"}],\"formId\":113,\"renderKey\":1628152567322}},{\"title\":\"面板2\",\"name\":\"2\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入333\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":117,\"renderKey\":1628152600603},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"ddd\"}],\"formId\":114,\"renderKey\":1628152567322}}],\"active\":[\"1\",\"2\"],\"formId\":112,\"renderKey\":1628152567322,\"componentName\":\"row112\"},\"accordion\":false},{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入2\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":108,\"renderKey\":1628151832460},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"aa\"}],\"formId\":103,\"renderKey\":1628151825851}}],\"active\":\"1\",\"formId\":101,\"renderKey\":1628151825851,\"componentName\":\"row101\"},\"type\":\"\",\"tab-position\":\"top\"},{\"__config__\":{\"jnpfKey\":\"collapse\",\"label\":\"折叠面板\",\"showLabel\":false,\"tag\":\"el-collapse\",\"tagIcon\":\"icon-ym icon-ym-generator-fold\",\"layout\":\"rowFormItem\",\"span\":24,\"dragDisabled\":false,\"children\":[{\"title\":\"面板1\",\"name\":\"1\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":109,\"renderKey\":1628151836551},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"bb\"}],\"formId\":105,\"renderKey\":1628151826435}},{\"title\":\"面板2\",\"name\":\"2\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单行输入\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":110,\"renderKey\":1628151838644},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"bbb\"}],\"formId\":106,\"renderKey\":1628151826435}},{\"title\":\"新面板\",\"name\":\"7SffpY\",\"__config__\":{\"children\":[]}}],\"active\":[\"1\",\"2\"],\"formId\":104,\"renderKey\":1628151826435,\"componentName\":\"row104\"},\"accordion\":false}]}";
        String a = "{\"formRef\":\"elForm\",\"formModel\":\"dataForm\",\"size\":\"medium\",\"labelPosition\":\"right\",\"labelWidth\":100,\"formRules\":\"rules\",\"popupType\":\"general\",\"generalWidth\":\"600px\",\"fullScreenWidth\":\"100%\",\"gutter\":15,\"disabled\":false,\"span\":24,\"formBtns\":false,\"cancelButtonText\":\"取 消\",\"confirmButtonText\":\"保 存\",\"formStyle\":\"\",\"idGlobal\":132,\"fields\":[{\"__config__\":{\"jnpfKey\":\"collapse\",\"label\":\"折叠面板\",\"showLabel\":false,\"tag\":\"el-collapse\",\"tagIcon\":\"icon-ym icon-ym-generator-fold\",\"layout\":\"rowFormItem\",\"span\":24,\"dragDisabled\":false,\"children\":[{\"title\":\"面板1\",\"name\":\"1\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"collapse\",\"label\":\"折叠面板\",\"showLabel\":false,\"tag\":\"el-collapse\",\"tagIcon\":\"icon-ym icon-ym-generator-fold\",\"layout\":\"rowFormItem\",\"span\":24,\"dragDisabled\":false,\"children\":[{\"title\":\"面板1\",\"name\":\"1\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"tab\",\"label\":\"标签页\",\"showLabel\":false,\"tag\":\"el-tab\",\"tagIcon\":\"icon-ym icon-ym-generator-label\",\"layout\":\"rowFormItem\",\"span\":24,\"dragDisabled\":false,\"children\":[{\"title\":\"Tab 1\",\"__config__\":{\"children\":[],\"formId\":130,\"renderKey\":1628473895386}},{\"title\":\"Tab 2\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"主键\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":132,\"renderKey\":1628473899541},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"id\"}],\"formId\":131,\"renderKey\":1628473895386}}],\"active\":\"0\",\"formId\":129,\"renderKey\":1628473895386,\"componentName\":\"row129\"},\"type\":\"border-card\",\"tab-position\":\"bottom\"},{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"创建用户\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":128,\"renderKey\":1628473888271},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"creatoruserid\"}],\"formId\":126,\"renderKey\":1628473885853}},{\"title\":\"面板2\",\"name\":\"2\",\"__config__\":{\"children\":[],\"formId\":127,\"renderKey\":1628473885853}}],\"active\":[\"2\",\"1\"],\"formId\":125,\"renderKey\":1628473885853,\"componentName\":\"row125\"},\"accordion\":false},{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"项目类型\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":116,\"renderKey\":1628470414757},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"projecttype\"}],\"formId\":114,\"renderKey\":1628470410156}},{\"title\":\"面板2\",\"name\":\"2\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"项目名称\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":117,\"renderKey\":1628470416562},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"projectname\"}],\"formId\":115,\"renderKey\":1628470410156}},{\"title\":\"ee\",\"name\":\"M2iDBZ\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"项目编号\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":118,\"renderKey\":1628470462019},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"projectcode\"}]}}],\"active\":[\"1\",\"M2iDBZ\"],\"formId\":113,\"renderKey\":1628470410156,\"componentName\":\"row113\"},\"accordion\":false},{\"__config__\":{\"jnpfKey\":\"tab\",\"label\":\"标签页\",\"showLabel\":false,\"tag\":\"el-tab\",\"tagIcon\":\"icon-ym icon-ym-generator-label\",\"layout\":\"rowFormItem\",\"span\":24,\"dragDisabled\":false,\"children\":[{\"title\":\"Tab 1\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"单据规则\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":123,\"renderKey\":1628471125702},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"billno\"}],\"formId\":120,\"renderKey\":1628471114388}},{\"title\":\"Tab 2\",\"__config__\":{\"children\":[{\"__config__\":{\"jnpfKey\":\"comInput\",\"label\":\"项目经费\",\"showLabel\":true,\"tag\":\"el-input\",\"tagIcon\":\"icon-ym icon-ym-generator-input\",\"required\":false,\"layout\":\"colFormItem\",\"span\":24,\"dragDisabled\":false,\"regList\":[],\"trigger\":\"blur\",\"formId\":122,\"renderKey\":1628471123596},\"__slot__\":{\"prepend\":\"\",\"append\":\"\"},\"placeholder\":\"请输入\",\"style\":{\"width\":\"100%\"},\"clearable\":true,\"prefix-icon\":\"\",\"suffix-icon\":\"\",\"maxlength\":null,\"show-word-limit\":false,\"show-password\":false,\"readonly\":false,\"disabled\":false,\"__vModel__\":\"projectfunding\"}],\"formId\":121,\"renderKey\":1628471114388}}],\"active\":\"0\",\"formId\":119,\"renderKey\":1628471114388,\"componentName\":\"row119\"},\"type\":\"\",\"tab-position\":\"top\"}]}";
        FormDataModel formData = JsonUtil.getJsonToBean(a, FormDataModel.class);
        List<FieLdsModel> list = JsonUtil.getJsonToList(formData.getFields(), FieLdsModel.class);
        List<FormAllModel> formAllModel = new ArrayList<>();
        recursionForm(list, formAllModel);
        System.out.println("s");
    }

    /**
     * 引擎递归
     **/
    public static void recursionForm(List<FieLdsModel> list, List<FormAllModel> formAllModel) {
        for (FieLdsModel fieLdsModel : list) {
            FormAllModel start = new FormAllModel();
            FormAllModel end = new FormAllModel();
            ConfigModel config = fieLdsModel.getConfig();
            String jnpfkey = config.getJnpfKey();
            List<FieLdsModel> childrenList = config.getChildren();
            boolean isJnpfKey = StringUtil.isEmpty(jnpfkey);
            boolean isName = StringUtil.isNotEmpty(fieLdsModel.getName());
            if (FormEnum.row.getMessage().equals(jnpfkey) || FormEnum.card.getMessage().equals(jnpfkey) || FormEnum.tab.getMessage().equals(jnpfkey) || FormEnum.collapse.getMessage().equals(jnpfkey) || isJnpfKey) {
                String key = isJnpfKey ? isName ? FormEnum.collapse.getMessage() : FormEnum.tab.getMessage() : jnpfkey;
                //布局属性
                FormModel formModel = new FormModel();
                formModel.setShadow(fieLdsModel.getShadow());
                formModel.setHeader(fieLdsModel.getHeader());
                formModel.setName(fieLdsModel.getName());
                formModel.setTitle(fieLdsModel.getTitle());
                formModel.setSpan(config.getSpan());
                formModel.setActive(config.getActive());
                formModel.setAccordion(fieLdsModel.getAccordion());
                formModel.setTabPosition(fieLdsModel.getTabPosition());
                formModel.setType(fieLdsModel.getType());
                String outermost = "1";
                if (FormEnum.tab.getMessage().equals(key) || FormEnum.collapse.getMessage().equals(key)) {
                    if (!isJnpfKey) {
                        outermost = "0";
                        formModel.setModel("active" + RandomUtil.enUuid());
                    }
                    formModel.setOutermost(outermost);
                }
                start.setJnpfKey(key);
                start.setFormModel(formModel);
                formAllModel.add(start);
                recursionForm(childrenList, formAllModel);
                end.setIsEnd("1");
                end.setJnpfKey(key);
                //折叠、标签的判断里层还是外层
                FormModel endFormModel = new FormModel();
                endFormModel.setOutermost(outermost);
                end.setFormModel(endFormModel);
                formAllModel.add(end);
            } else if (FormEnum.table.getMessage().equals(jnpfkey)) {
                tableModel(fieLdsModel, formAllModel);
            } else {
                model(fieLdsModel, formAllModel);
            }
        }
    }

    /**
     * 主表属性添加
     **/
    private static void model(FieLdsModel model, List<FormAllModel> formAllModel) {
        FormColumnModel mastModel = formModel(model);
        FormAllModel formModel = new FormAllModel();
        formModel.setJnpfKey(FormEnum.mast.getMessage());
        formModel.setFormColumnModel(mastModel);
        formAllModel.add(formModel);
    }

    /**
     * 子表表属性添加
     **/
    private static void tableModel(FieLdsModel model, List<FormAllModel> formAllModel) {
        FormColumnTableModel tableModel = new FormColumnTableModel();
        List<FormColumnModel> childList = new ArrayList<>();
        ConfigModel config = model.getConfig();
        List<FieLdsModel> childModelList = config.getChildren();
        String table = model.getVModel();
        for (FieLdsModel childmodel : childModelList) {
            FormColumnModel childModel = formModel(childmodel);
            childList.add(childModel);
        }
        tableModel.setLabel(config.getLabel());
        tableModel.setShowTitle(config.getShowTitle());
        tableModel.setActionText(StringUtil.isNotEmpty(model.getActionText()) ? model.getActionText() : "新增");
        tableModel.setSpan(config.getSpan());
        tableModel.setTableModel(table);
        tableModel.setChildList(childList);
        tableModel.setTableName(config.getTableName());
        FormAllModel formModel = new FormAllModel();
        formModel.setJnpfKey(FormEnum.table.getMessage());
        formModel.setChildList(tableModel);
        formAllModel.add(formModel);
    }

    /**
     * 属性赋值
     **/
    private static FormColumnModel formModel(FieLdsModel model) {
        ConfigModel configModel = model.getConfig();
        if (configModel.getDefaultValue() instanceof String) {
            configModel.setValueType("String");
        }
        if (configModel.getDefaultValue() == null) {
            configModel.setValueType("undefined");
        }
        FormColumnModel formColumnModel = new FormColumnModel();
        //级联判断多选还是单选
        if (JnpfKeyConsts.CASCADER.equals(configModel.getJnpfKey())) {
            Map<String, Object> propsMap = JsonUtil.stringToMap(model.getProps().getProps());
            model.setMultiple(String.valueOf(propsMap.get("multiple")));
        }
        formColumnModel.setFieLdsModel(model);
        return formColumnModel;
    }

}
