package ru.eclipsetrader.transaq.core.server.command;

public class ChangePasswordCommand extends BaseCommand {
	
	/*<command id="change_pass"
			oldpass="текущий пароль"
			newpass="новый пароль"
			/>*/
	
	String oldPass;
	String newPass;
	
	public ChangePasswordCommand(String oldPass, String newPass) {
		this.oldPass = oldPass;
		this.newPass = newPass;
	}

	@Override
	public String createSubscribeCommand() {
		StringBuilder sb = new StringBuilder();
		sb.append("<command id=\"change_pass\"");
		sb.append("oldpass=\"" + oldPass + "\"");
		sb.append("newpass=\"" + newPass + "\"");
		sb.append("/>");
		return sb.toString();
	}

}
