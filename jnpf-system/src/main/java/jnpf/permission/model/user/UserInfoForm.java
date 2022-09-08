package jnpf.permission.model.user;

import lombok.Data;

/**
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司
 * @date 2021/3/12 15:31
 */
@Data
public class UserInfoForm {
    private String signature;
    private int gender;
    private String nation;
    private String nativePlace;
    private String entryDate;
    private String certificatesType;
    private String certificatesNumber;
    private String education;
    private String birthday;
    private String telePhone;
    private String landline;
    private String mobilePhone;
    private String email;
    private String urgentContacts;
    private String urgentTelePhone;
    private String postalAddress;
    private String realName;
}
