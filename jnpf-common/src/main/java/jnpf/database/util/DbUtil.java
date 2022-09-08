package jnpf.database.util;

import jnpf.base.DbTableModel;
import jnpf.database.enums.DataTypeEnum;
import jnpf.database.enums.DbAliasEnum;
import jnpf.database.enums.DbSttEnum;
import jnpf.database.exception.DataException;
import jnpf.database.model.DataSourceUtil;
import jnpf.database.model.DbLinkEntity;
import jnpf.database.model.DbTableFieldModel;
import jnpf.database.model.dto.DbConnDTO;
import jnpf.database.source.DbBase;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;


/**
 * 数据库上下文切换
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/6/21
 */
public class DbUtil {

    public static DbTableModel getTableModelCommon(ResultSet result,Boolean tableFlag,Boolean tableCommentFlag,
                     Boolean descriptionFlag,Boolean sumFlag,Boolean sizeFlag,Boolean primaryFlag) throws SQLException{
        DbTableModel model = new DbTableModel();
        try{
            //表名
            String table = tableFlag ? result.getString(DbAliasEnum.TABLE_NAME.AS()):null;
            //表注释
            String tableComment = tableCommentFlag ? result.getString(DbAliasEnum.TABLE_COMMENT.AS()):null;
            //表说明
            String description = descriptionFlag ? table + "(" + tableComment + ")":null;
            //表总数
            Integer sum = sumFlag ? result.getInt(DbAliasEnum.TABLE_SUM.AS()):null;
            //表大小
            String size = sizeFlag ? result.getString(DbAliasEnum.TABLE_SIZE.AS()):null;
            //表主键
            String primary = primaryFlag ? "F_Id":null;
            return  model.
                    setTable(table).
                    setTableName(tableComment).
                    setDescription(description).
                    setSum(sum).
                    setSize(size).
                    setPrimaryKey(primary);
        }catch (Exception e){
            e.getMessage();
            throw e;
        }
    }

    public static String getReplaceSql(String sql, String table, DbConnDTO dbConnDTO,
                                       Boolean dbNameFlag, Boolean schemaFlag, Boolean tableSpaceFlag){
        if(table!=null && !"".equals(table)){
            sql = sql.replace(DbSttEnum.TABLE.getTarget(),table);
        }
        if(dbNameFlag){
            String dbName = dbConnDTO.getServiceName();
            sql = sql.replace(DbSttEnum.DB_NAME.getTarget(),dbName);
        }
        if(schemaFlag){
            String schema = dbConnDTO.getUserName();
            sql = sql.replace(DbSttEnum.DB_SCHEMA.getTarget(),schema);
        }
        if(tableSpaceFlag){
            String tableSpace = dbConnDTO.getTableSpace();
            sql = sql.replace(DbSttEnum.TABLE_SPACE.getTarget(),tableSpace);
        }
        return sql;
    }

    public static DbTableFieldModel getFieldModelCommon(ResultSet result,DbBase db,DbTableFieldModel model) throws SQLException,DataException{
        //字段类型
        String dataType = model.getDataType()!=null ? model.getDataType() :
                DataTypeEnum.getCommonFieldType(result.getString(DbAliasEnum.DATA_TYPE.asByDb(db)), db);
        //字段名
        String fieldName = model.getField()!=null ? model.getField() :
                result.getString(DbAliasEnum.FIELD_NAME.asByDb(db));
        //字段注释
        String fieldComment = model.getFieldComment()!=null ? model.getFieldComment() :
                result.getString(DbAliasEnum.FIELD_COMMENT.asByDb(db));
        //字段默认值
        /*String defaults = model.getDefaults()!=null ? model.getDefaults() :
                result.getString(DbAliasEnum.DEFAULTS.AS());*/

        //字段长度
        String dataLength = model.getDataLength()!= null ? model.getDataLength() :
                String.valueOf(result.getInt(DbAliasEnum.DATA_LENGTH.asByDb(db)));
        //text的长度默认无法设置
        if(dataType!=null){
            if("0".equals(dataLength) || dataType.equals(DataTypeEnum.TEXT.getCommonFieldType()) || dataLength == ""){
                dataLength = "默认";
            }
        }
        //字段主键
        Integer primaryKey = model.getPrimaryKey()!=null ?  model.getPrimaryKey() :
                result.getInt(DbAliasEnum.PRIMARY_KEY.asByDb(db));
        //字段允空
        Integer allowNull = model.getAllowNull()!=null ? model.getAllowNull() :
                result.getInt(DbAliasEnum.ALLOW_NULL.asByDb(db));
        return  model.
                setField(fieldName).
                //早期的前后协议命名comment为fieldName;
                setFieldName(fieldComment).
               /* setDefaults(defaults).*/
                setDataType(dataType).
                setDescription(fieldName + "(" + fieldComment + ")").
                setDataLength(dataLength).
                setAllowNull(allowNull).
                setPrimaryKey(primaryKey);
    }




    /**
     * 默认表（不可删）
     */
    public static final String BYO_TABLE =
            "base_authorize,base_comfields,base_billrule,base_dbbackup,base_dblink,base_dictionarydata," +
                    "base_dictionarytype,base_imcontent,base_languagemap,base_languagetype,base_menu," +
                    "base_message,base_messagereceive,base_module,base_modulebutton,base_modulecolumn," +
                    "base_moduledataauthorize,base_moduledataauthorizescheme,base_organize,base_position," +
                    "base_province,base_role,base_sysconfig,base_syslog,base_timetask,base_timetasklog," +
                    "base_user,base_userrelation,crm_busines,crm_businesproduct,crm_clue,crm_contract," +
                    "crm_contractinvoice,crm_contractmoney,crm_contractproduct,crm_customer,crm_customercontacts," +
                    "crm_followlog,crm_invoice,crm_product,crm_receivable,ext_bigdata,ext_document," +
                    "ext_documentshare,ext_emailconfig,ext_emailreceive,ext_emailsend,ext_employee,ext_order," +
                    "ext_orderentry,ext_orderreceivable,ext_projectgantt,ext_schedule,ext_tableexample," +
                    "ext_worklog,ext_worklogshare,flow_delegate,flow_engine,flow_engineform,flow_enginevisible," +
                    "flow_task,flow_taskcirculate,flow_tasknode,flow_taskoperator,flow_taskoperatorrecord," +
                    "wechat_mpeventcontent,wechat_mpmaterial,wechat_mpmessage,wechat_qydepartment,wechat_qymessage," +
                    "wechat_qyuser,wform_applybanquet,wform_applydelivergoods,wform_applydelivergoodsentry," +
                    "wform_applymeeting,wform_archivalborrow,wform_articleswarehous,wform_batchpack,wform_batchtable," +
                    "wform_conbilling,wform_contractapproval,wform_contractapprovalsheet,wform_debitbill," +
                    "wform_documentapproval,wform_documentsigning,wform_expenseexpenditure,wform_finishedproduct," +
                    "wform_finishedproductentry,wform_incomerecognition,wform_leaveapply,wform_letterservice," +
                    "wform_materialrequisition,wform_materialrequisitionentry,wform_monthlyreport,wform_officesupplies," +
                    "wform_outboundorder,wform_outboundorderentry,wform_outgoingapply,wform_paydistribution," +
                    "wform_paymentapply,wform_postbatchtab,wform_procurementmaterial,wform_procurementmaterialentry," +
                    "wform_purchaselist,wform_purchaselistentry,wform_quotationapproval,wform_receiptprocessing," +
                    "wform_receiptsign,wform_rewardpunishment,wform_salesorder,wform_salesorderentry,wform_salessupport," +
                    "wform_staffovertime,wform_supplementcard,wform_travelapply,wform_travelreimbursement,wform_vehicleapply," +
                    "wform_violationhandling,wform_warehousereceipt,wform_warehousereceiptentry,wform_workcontactsheet";
}

