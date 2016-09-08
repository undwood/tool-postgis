select t.group_fld as group_fld, t.seis_name as seis_name, t.profile_name as profile_name, t.picket as picket, t.lat as lat, t.lon as lon from (
select sp.full_name || ' '|| sp.profile_name as group_fld, sp.full_name as seis_name, sp.profile_name as profile_name, sp.picket as picket, sp.lat as lat, sp.lon as lon from seis_part sp
) t where 1=1