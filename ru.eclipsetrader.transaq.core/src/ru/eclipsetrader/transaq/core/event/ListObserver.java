package ru.eclipsetrader.transaq.core.event;

import java.util.List;

@FunctionalInterface
public interface ListObserver<T> extends Observer<List<T>> {

}
