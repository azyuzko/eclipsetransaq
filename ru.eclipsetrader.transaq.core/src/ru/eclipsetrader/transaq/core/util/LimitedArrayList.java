package ru.eclipsetrader.transaq.core.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * ArrayList � ������������ ���-�� ��������
 * ��� ���������� limitSize ��������� ������ ��������
 * @author Zyuzko-AA
 *
 * @param <T>
 */
public class LimitedArrayList<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;

	int limitSize = 100;
	
	public LimitedArrayList(int limitSize) {
		super();
		this.limitSize = limitSize;
	}
	
	/**
	 * ���������� ��������� ������� ������
	 * last(0) - ��������� �������
	 * last(-1) - ������������� �������
	 * � �.�.
	 * @param fromLast ������ �� ����������
	 * @return
	 */
	public T last(int fromLast) {
		if (size() > 0) {
			int lastIndex = size()-1;
			if (lastIndex+fromLast >= 0) {
				return get(lastIndex+fromLast);
			}
		}
		return null;
	}
	
	/**
	 * ���������� ��������� ������� ������
	 * @return
	 */
	public T last() {
		return last(0);
	}
	
	@Override
	public boolean add(T e) {
		boolean result = super.add(e);
		removeToLimit();
		return result;
	}
	
	@Override
	public void add(int index, T element) {
		super.add(index, element);
		removeToLimit();
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean result = super.addAll(c);
		removeToLimit();
		return result;
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		boolean result = super.addAll(index, c);
		removeToLimit();
		return result;
	}

	private void removeToLimit() {
		while (size() > limitSize) {
			remove(0);
		}
	}

	public static void main(String[] args) {
		LimitedArrayList<Integer> a = new LimitedArrayList<>(3);
		a.add(1);
		a.add(2);
		a.add(3);
		a.add(4);
		System.out.println(a.last(-2));
	}
}
