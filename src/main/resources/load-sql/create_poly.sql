CREATE TABLE public.poly_lic
(
  id bigint CONSTRAINT poly_lic_pk PRIMARY KEY,
  lic_name text,
  issue_date date,
  end_date date,
  activity text,
  reason text,
  squ double precision,
  status text,
  lic_ser text,
  lic_num text,
  lik_kind text,
  nedr text
)
