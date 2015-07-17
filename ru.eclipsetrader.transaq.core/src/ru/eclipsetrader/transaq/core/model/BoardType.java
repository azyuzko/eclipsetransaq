package ru.eclipsetrader.transaq.core.model;

public enum BoardType {
	
	// оПНДСЙРХБМШЕ
	ADR,
	AETS,
	AUCB,
	AUCT,
	CETS,
	CNGD,
	CUR,
	EQDB,
	EQDP,
	EQEU,
	EQNE,
	EQOB,
	EQQI,
	EQRP,
	FBCB,
	FBFX,
	FRNF,
	FUT,
	ICE,
	INDE,
	INDEXE,
	INDEXM,
	INDEXR,
	INDX,
	LME,
	MCT,
	NASD,
	NYSE,
	OPT,
	PSAU,
	PSDB,
	PSDE,
	PSEQ,
	PSEU,
	PSIF,
	PSOB,
	PSQI,
	PSRP,
	PSTF,
	PTDE,
	PTEQ,
	PTIF,
	PTOB,
	PTQI,
	PTTF,
	RPEU,
	RPMA,
	RPMO,
	RPQI,
	RSKC,
	SCOH,
	SHFT,
	SMAL,
	TQBR,
	TQDE,
	TQIF,
	TQOB,
	TQQI,
	TQTF;
	
	public MarketType getBoardMarket() {
		switch (this) {
		case ADR: return MarketType.INF;
		case AETS: return MarketType.ETS;
		case AUCB: return MarketType.ETS;
		case AUCT: return MarketType.ллба;
		case CETS: return MarketType.ETS;
		case CNGD: return MarketType.ETS;
		case CUR: return MarketType.INF;
		case EQDB: return MarketType.ллба;
		case EQDP: return MarketType.ллба;
		case EQEU: return MarketType.ллба;
		case EQNE: return MarketType.ллба;
		case EQOB: return MarketType.ллба;
		case EQQI: return MarketType.ллба;
		case EQRP: return MarketType.ллба;
		case FBCB: return MarketType.ллба;
		case FBFX: return MarketType.ллба;
		case FRNF: return MarketType.ллба;
		case FUT: return MarketType.FORTS;
		case ICE: return MarketType.INF;
		case INDE: return MarketType.INF;
		case INDEXE: return MarketType.ETS;
		case INDEXM: return MarketType.ллба;
		case INDEXR: return MarketType.FORTS;
		case LME: return MarketType.INF;
		case MCT: return MarketType.MMA;
		case NASD: return MarketType.AMERICA;
		case OPT: return MarketType.FORTS;
		case PSAU: return MarketType.ллба;
		case PSDB: return MarketType.ллба;
		case PSDE: return MarketType.ллба;
		case PSEQ: return MarketType.ллба;
		case PSEU: return MarketType.ллба;
		case PSIF: return MarketType.ллба;
		case PSOB: return MarketType.ллба;
		case PSQI: return MarketType.ллба;
		case PSRP: return MarketType.ллба;
		case PSTF: return MarketType.ллба;
		case PTDE: return MarketType.ллба;
		case PTEQ: return MarketType.ллба;
		case PTIF: return MarketType.ллба;
		case PTOB: return MarketType.ллба;
		case PTQI: return MarketType.ллба;
		case PTTF: return MarketType.ллба;
		case RPEU: return MarketType.ллба;
		case RPMA: return MarketType.ллба;
		case RPMO: return MarketType.ллба;
		case RPQI: return MarketType.ллба;
		case RSKC: return MarketType.ETS;
		case SHFT: return MarketType.ETS;
		case SMAL: return MarketType.ллба;
		case TQBR: return MarketType.ллба;
		case TQDE: return MarketType.ллба;
		case TQIF: return MarketType.ллба;
		case TQOB: return MarketType.ллба;
		case TQQI: return MarketType.ллба;
		case TQTF: return MarketType.ллба;
		default:
			break;
		}
		return null;
	}
}
