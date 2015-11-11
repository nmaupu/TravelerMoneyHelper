package org.maupu.android.tmh.ui;

public interface ICallback<T extends Object> {
	public T callback(Object item);
}
