create table candles
(
  board varchar2(20),
  seccode varchar2(20),
  candletype varchar2(20),
  startDate date,
  open number(20,6),
  high number(20,6),
  low number(20,6),
  close number(20,6),
  volume number(10),
  oi number(10)
);

create unique index candles_IX1 on candles(board, seccode, candletype, startDate);
