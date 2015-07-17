package ru.eclipsetrader.transaq.core.model;

public enum SecurityType {
	
	// Торгуемые инструменты:
	SHARE, // - акции
	BOND, // - облигации корпоративные
	FUT, // - фьючерсы FORTS
	OPT, // - опционы
	GKO, // - гос. бумаги
	FOB, // - фьючерсы ММВБ
	
	MCT,  
	ETS_CURRENCY,
	ETS_SWAP,

	// Неторгуемые (все кроме IDX приходят только с зарубежных площадок):
	IDX, // - индексы
	QUOTES, // - котировки (прочие)
	CURRENCY, // - валютные пары
	ADR, // - АДР
	NYSE, // - данные с NYSE
	METAL, // - металлы
	OIL, // - нефтянка
	
	/*SHA, // это гавно приходит с продуктива!
	OP,
	SH,
	BO,
	RE,
	T,
	BON,
	ND,
	ARE,
	D,
	MC,
	FU,
	SHAR,
	E,
	ETS_CURRENC,
	Y,
	ID,
	X,
	ETS_SW,
	ETS_CU,
	AP,
	ETS_CURR,*/
}
