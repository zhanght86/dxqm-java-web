package jnpf.model.visiual;

import jnpf.model.visiual.fields.FieLdsModel;
import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/16 8:49
 */
@Data
public class FormDataModel {
    /**
     * 模块
     */
    private String areasName;
    /**
     * 功能名称
     */
    private String className;
    /**
     * 后端目录
     */
    private String serviceDirectory;
    /**
     * 所属模块
     */
    private String module;
    /**
     * 子表名称集合
     */
    private String subClassName;


    private String formRef;
    private String formModel;
    private String size;
    private String labelPosition;
    private Integer labelWidth;
    private String formRules;
    private Integer gutter;
    private Boolean disabled;
    private String span;
    private Boolean formBtns;
    private Integer idGlobal;
    private String fields;
    private String popupType;
    private String fullScreenWidth;
    private String formStyle;
    private String generalWidth;
    private String cancelButtonText;
    private String confirmButtonText;


    private FieLdsModel children;
}
