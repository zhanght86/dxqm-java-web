package jnpf.util.enums;

/**
 * 导入导出模板类型
 *
 * @author JNPF开发平台组
 * @version V3.1.0
 * @copyright 引迈信息技术有限公司（https://www.jnpfsoft.com）
 * @date  2021/7/15
 */

public enum ExportModelTypeEnum {
	/**
	 * 功能设计
	 */
	Design(1,"design"),

	/**
	 * APP
	 */
	App(2,"app"),

	/**
	 *门户
	 */
	Portal(5,"portal");
	private final int code;
	private final String message;

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	ExportModelTypeEnum(int code, String message) {
		this.code = code;
		this.message = message;
	}
}
