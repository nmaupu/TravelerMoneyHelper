package org.maupu.android.tmh.database.filter;

import org.maupu.android.tmh.util.QueryBuilder;

public abstract class AFilter {
	public static final int FUNCTION_EQUAL=0;
	public static final int FUNCTION_NOTEQUAL=1;
	public static final int FUNCTION_IN=2;
	public static final int FUNCTION_NOTIN=3;
	
	protected abstract QueryBuilder getBaseQuery();
}
