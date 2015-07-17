package ru.eclipsetrader.transaq.core.exception;

import ru.eclipsetrader.transaq.core.model.internal.CommandResult;

public class CommandException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5787150274561130957L;

	CommandResult commandResult;
	
	public CommandException(CommandResult commandResult) {
		super(commandResult.getMessage());
		this.commandResult = commandResult;
	}
	
	public CommandException(Exception e) {
		super(e);
	}
	
	@Override
	public String getMessage() {
		return commandResult.getMessage();
	}
	
} 
