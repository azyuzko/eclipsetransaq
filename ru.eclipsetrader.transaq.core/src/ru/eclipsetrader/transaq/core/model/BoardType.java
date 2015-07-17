package ru.eclipsetrader.transaq.core.model;

public enum BoardType {
	
	// ������������
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
		case AUCT: return MarketType.����;
		case CETS: return MarketType.ETS;
		case CNGD: return MarketType.ETS;
		case CUR: return MarketType.INF;
		case EQDB: return MarketType.����;
		case EQDP: return MarketType.����;
		case EQEU: return MarketType.����;
		case EQNE: return MarketType.����;
		case EQOB: return MarketType.����;
		case EQQI: return MarketType.����;
		case EQRP: return MarketType.����;
		case FBCB: return MarketType.����;
		case FBFX: return MarketType.����;
		case FRNF: return MarketType.����;
		case FUT: return MarketType.FORTS;
		case ICE: return MarketType.INF;
		case INDE: return MarketType.INF;
		case INDEXE: return MarketType.ETS;
		case INDEXM: return MarketType.����;
		case INDEXR: return MarketType.FORTS;
		case LME: return MarketType.INF;
		case MCT: return MarketType.MMA;
		case NASD: return MarketType.AMERICA;
		case OPT: return MarketType.FORTS;
		case PSAU: return MarketType.����;
		case PSDB: return MarketType.����;
		case PSDE: return MarketType.����;
		case PSEQ: return MarketType.����;
		case PSEU: return MarketType.����;
		case PSIF: return MarketType.����;
		case PSOB: return MarketType.����;
		case PSQI: return MarketType.����;
		case PSRP: return MarketType.����;
		case PSTF: return MarketType.����;
		case PTDE: return MarketType.����;
		case PTEQ: return MarketType.����;
		case PTIF: return MarketType.����;
		case PTOB: return MarketType.����;
		case PTQI: return MarketType.����;
		case PTTF: return MarketType.����;
		case RPEU: return MarketType.����;
		case RPMA: return MarketType.����;
		case RPMO: return MarketType.����;
		case RPQI: return MarketType.����;
		case RSKC: return MarketType.ETS;
		case SHFT: return MarketType.ETS;
		case SMAL: return MarketType.����;
		case TQBR: return MarketType.����;
		case TQDE: return MarketType.����;
		case TQIF: return MarketType.����;
		case TQOB: return MarketType.����;
		case TQQI: return MarketType.����;
		case TQTF: return MarketType.����;
		default:
			break;
		}
		return null;
	}
}
