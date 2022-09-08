package jnpf.onlinedev.model.OnlineDevEnum;

/**
 *
 * 在线开发缓存的key
 * @author JNPF开发平台组
 * @version V3.2.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/31
 */
public enum CacheKeyEnum {
	/**
	 * 修改用户，创建用户，用户组件
	 */
	USER("_user","用户"),

	POS("_position","岗位"),

	ORG("_organization","公司"),

	PRO("_province","省份");
	private final String name;
	private final String message;

	CacheKeyEnum(String name, String message) {
		this.name = name;
		this.message = message;
	}

	public String getName() {
		return name;
	}

	public String getMessage() {
		return message;
	}


}
