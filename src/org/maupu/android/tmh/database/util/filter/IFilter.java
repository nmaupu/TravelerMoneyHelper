package org.maupu.android.tmh.database.util.filter;

import java.io.Serializable;

import org.maupu.android.tmh.database.util.QueryBuilder;

public interface IFilter extends Serializable {
	
	/**
	 * Add a filter to a given query builder.
	 * @param qb
	 * @param function
	 * @param operand
	 * @param value
	 * @return An updated query builder
	 */
	public QueryBuilder addFilter(int function, String operand, String value);
	
	/**
	 * Reset a filter to its original state
	 * @return Reset query builder
	 */
	public QueryBuilder resetFilter();
}
