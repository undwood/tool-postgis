select sp.full_name as seis_name, sp.profile_name as profile_name, sp.picket as picket, sp.lat as lat, sp.lon as lon from seis_part sp where 1=1


SELECT
  T.SEISMICPARTY AS SEISMICPARTY,
  T.PICKET AS PICKET,
  T.XCOR AS XCOR,
  T.YCOR AS YCOR
FROM
(SELECT
       SP.NAME AS SEISMICPARTY,
       PI.VALUE AS PICKET,
       WELL.XTOB_ZONE(P_ZONE_ID => SP.ZONE_ID, P_X => PI.X, P_Y => PI.Y) AS XCOR,
       WELL.YTOL_ZONE(P_ZONE_ID => SP.ZONE_ID, P_X => PI.X, P_Y => PI.Y) AS YCOR
FROM WELL.SEIS_PART SP
JOIN WELL.SEIS_PROFILE P ON P.SP_ID=SP.ID
JOIN WELL.SEIS_PICKET PI ON PI.PROFILE_ID=P.ID
WHERE P.NAME = 'KONTUR') T
WHERE 1=1
