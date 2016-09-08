select 
  nedr as nedr,
  arear as arear,
  field as field,
  district as district,
  uwi as uwi,
  wellname as wellname,
  license as license,
  lic_code as lic_code,
  xco as xco,
  yco as yco
from well where 1=1


select
     T.NEDR AS NEDR,
     T.AREAR AS AREAR,
     T.FIELD AS FIELD,
     T.DISTRICT AS DISTRICT,
     T.UWI AS UWI,
     T.WELLNAME AS WELLNAME,
     T.LICENSE AS LICENSE,
     T.LIC_CODE as LIC_CODE,
     T.XCO AS XCO,
     T.YCO AS YCO
from (
select
     E.NAME_SHORT AS NEDR,
     A.NAME AS AREAR,
     f.name AS FIELD,
     F.DISTRICT_NAME AS DISTRICT,
     uwi AS UWI,
     wi.well_name AS WELLNAME,
     LI.NAME AS LICENSE,
     lse.name || lpad (to_char(li.license_num),5,'0') || lk.code as LIC_CODE,
     xy_deg AS XCO,
     yy_deg AS YCO
from well.well_info wi
left join WELLREF.FIELD f on f.id=wi.field_id
left join well.well_license wl on wl.wellid=wi.wellid and wl.actual_flag='Y'
left join wellref.enterprise e on e.id=wi.enterp_id
left join wellref.area a on a.id=wi.area_id
left join WELLREF.WELL_STATUS s on s.id=wi.assign_id
left join well.COORDINATES_WELLHEAD coord on coord.wellid=wi.wellid
left join WELLREF.LICENSE li on LI.Id=wl.license_id
left join wellref.license_series lse on lse.id=li.series_id
left join wellref.license_kind lk on lk.id=li.kind_id
where wi.assign_id in (1,3,5)
      and exists (select 1 from wellref.enterprise_group_link egl
                  join wellref.enterprise_group eg on eg.id=egl.enterprise_group_id
                  where eg.code='01' and egl.enterp_id=wi.enterp_id)
                  ) T where 1=1