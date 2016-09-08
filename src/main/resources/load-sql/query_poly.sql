select
  l.lic_name as lic_name,
  l.issue_date as issue_date,
  l.end_date as end_date,
  l.activity as activity,
  l.reason as reason,
  l.squ as squ,
  l.status as status,
  l.lic_ser as lic_ser,
  l.lic_num as lic_num,
  l.lik_kind as lik_kind,
  l.nedr as nedr,
  l.xco as  xco,
  l.yco as yco,
  l.pt as pt
from  (

with t as
(select  l.id, l.name license_name, l.date_begin, l.date_end, l.type_activity, l.reason_license, l.area_license,
       ls.name license_state, lse.name license_series, l.license_num, lk.name license_kind,
       (select e.name_short from wellref.enterprise e where e.id=l.owner_id) owner_name,
       mlp.point_num,
       well.xtob_zone(p_zone_id => l.zone_id, P_X => mlp.x, P_Y => mlp.y) x_deg,
       well.ytol_zone(p_zone_id => l.zone_id, P_X => mlp.x, P_Y => mlp.y) y_deg,
       mlp.id point_id
from wellref.license l
left join wellref.license_state ls on ls.id=l.state_id
left join wellref.license_status lst on lst.id=l.status_id
left join wellref.license_series lse on lse.id=l.series_id
left join wellref.license_kind lk on lk.id=l.kind_id
left join well.map_license_point mlp on l.id=mlp.license_id
where exists (select 1 from wellref.enterprise_group_link egl
                  join wellref.enterprise_group eg on eg.id=egl.enterprise_group_id
                  where eg.code='01' and egl.enterp_id=l.owner_id)
      or l.owner_id=228
)
select distinct case when t.x_deg is null then t.id else t.point_id end as "#id",
       t.license_name as lic_name,
       t.date_begin as issue_date,
       t.date_end as end_date,
       t.type_activity as activity,
       t.reason_license as reason,
       t.area_license as squ,
       t.license_state as status,
       t.license_series as lic_ser,
       t.license_num as lic_num,
       t.license_kind as lik_kind,
       t.owner_name as nedr,
       case when t.x_deg is null then null else t.point_num end as pt,
       x_deg as xco,
       y_deg as yco
from t where x_deg is not null) l where 1=1



