package com.wolfhybrid23.spigot.shopgui;

public class InvalidValueException extends Exception {
	private static final long serialVersionUID = 5661984273218736281L;
	String message;
	
	InvalidValueException(String property, String path, String propertyValue, String expectedType, String helpLink)
	{
		StringBuilder bld = new StringBuilder();
		bld.append("The property ")
			.append(property != null ? property : "<unknown>")
			.append(" in ")
			.append(path)
			.append(" contains an invalid value for the type(s) ")
			.append(expectedType)
			.append('.');
		
		if(helpLink != null) 
		{
			bld.append(" See ")
				.append(helpLink)
				.append(" for help.");
		}
		
		message = bld.toString();
	}
	
	InvalidValueException(String prop, String path, String message) {
		StringBuilder bld = new StringBuilder();
		bld.append("Issue with property ")
			.append(prop != null ? prop : "<unknown>")
			.append(" in ")
			.append(path)
			.append(" contains an invalid value. ")
			.append(message);
			
		this.message = bld.toString();
	}
	
	@Override
	public String getMessage() {
		return message;
	}
}
