select sp.full_name as seis_name, sp.profile_name as profile_name, sp.picket as picket, sp.lat as lat, sp.lon as lon from seis_part sp where 1=1


select
       sp.name as SEISMICPARTY,
       pi.value as PICKET,
       well.xtob_zone(p_zone_id => sp.zone_id, P_X => pi.x, P_Y => pi.y) as XCOR,
       well.ytol_zone(p_zone_id => sp.zone_id, P_X => pi.x, P_Y => pi.y) as YCOR
from well.seis_part sp
join well.seis_profile p on p.sp_id=sp.id
join well.seis_picket pi on pi.profile_id=p.id
where p.name = 'KONTUR'


CREATE TABLE wellinfo_stratum_points AS
select nextval('sq_sq') as id,
    (ST_DumpPoints(ST_ConcaveHull(ST_Collect(geo_point), 1))).geom As geom_point,
    field as field,
    stratum as stratum
from wellinfo_stratum group by field, stratum having count(field)=2
union all
select nextval('sq_sq') as id,
    ST_ConcaveHull(ST_Collect(geo_point), 1) As geom_point,
    field as field,
    stratum as stratum
from wellinfo_stratum group by field, stratum having count(field)=1;
ALTER TABLE wellinfo_stratum_points ALTER COLUMN geom_point TYPE geometry(Point,4326);

CREATE TABLE wellinfo_stratum_polygon AS
select nextval('sq_sq') as id,
    ST_ConcaveHull(ST_Collect(geo_point), 1) As geom_polygon,
    field as field,
    stratum as stratum
from wellinfo_stratum group by field, stratum having count(field)>2;
ALTER TABLE wellinfo_stratum_polygon ALTER COLUMN geom_polygon TYPE geometry(Polygon,4326);