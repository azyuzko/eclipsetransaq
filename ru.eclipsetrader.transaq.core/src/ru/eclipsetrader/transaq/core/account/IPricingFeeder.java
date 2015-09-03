package ru.eclipsetrader.transaq.core.account;




/**
 * Поставщик данных для сделок купли-продажи
 * @author Zyuzko-AA
 *
 */
public interface IPricingFeeder {

	double sellPrice();
	double buyPrice();
	
}
