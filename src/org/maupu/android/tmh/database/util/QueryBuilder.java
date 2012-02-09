package org.maupu.android.tmh.database.util;

import java.io.Serializable;

public class QueryBuilder implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String currentTableAlias;
	private StringBuilder sb = null;
	
	public QueryBuilder(StringBuilder builder) {
		setStringBuilder(builder);
	}
	
	public void setCurrentTableAlias(String currentTableAlias) {
		this.currentTableAlias = currentTableAlias;
	}
	
	public void setStringBuilder(StringBuilder builder) {
		sb = builder;
		if(sb == null)
			sb = new StringBuilder();
	}
	
	public QueryBuilder addSelectToQuery(String fieldName, String fieldAlias) {
		if(currentTableAlias != null)
			sb.append(currentTableAlias).append(".");
		
		sb.append(fieldName);
		
		if(fieldAlias != null)
			sb.append(" ").append(fieldAlias);
		
		return this;
	}
	
	public QueryBuilder addSelectToQuery(String fieldName) {
		return addSelectToQuery(fieldName, null);
	}
	
	public QueryBuilder append(String value) {
		sb.append(value);
		return this;
	}
	
	public StringBuilder getStringBuilder() {
		return sb;
	}
}
