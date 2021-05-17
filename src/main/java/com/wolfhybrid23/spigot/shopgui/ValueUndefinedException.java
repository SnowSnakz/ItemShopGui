package com.wolfhybrid23.spigot.shopgui;

public class ValueUndefinedException extends Exception {
	private static final long serialVersionUID = -1748091114922492003L;
	String message;
	
	ValueUndefinedException(String name, String path) {
		StringBuilder bld = new StringBuilder();
		bld.append("Property ")
			.append(name != null ? name : "<unknown>")
			.append(" does not exist inside the section ")
			.append(path != null ? path : "<unknown>")
			.append('.');
		message =  bld.toString();
	}

	@Override
	public String getMessage() {
		return message;
	}
}
