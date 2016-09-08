CREATE TABLE public.p_well
(
  id bigint CONSTRAINT p_well_pk PRIMARY KEY,
  nedr_g text,
  arear_g text,
  field_g text,
  district_g text,
  uwi_g text,
  wellname_g text,
  license_g text,
  lic_code_g text
)