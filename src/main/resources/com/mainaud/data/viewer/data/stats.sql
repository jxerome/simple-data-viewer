with fullstats (variable, count, sum, average) as (
    select "€VARIABLE_NAME€", count(*), sum("€VALUE_NAME€"), avg("€VALUE_NAME€")
      from "€TABLE_NAME€"
     where "€VARIABLE_NAME€" is not null
       and "€VALUE_NAME€" is not null
  group by "€VARIABLE_NAME€"
  order by count(*) desc
)
  select 1, variable, count, average
    from (select variable, count, average
            from fullstats
           limit 100)
  union all
  select 2, null, sum(count), 1.0 * sum(sum) / sum(count)
    from (select count, sum
          from fullstats
         limit -1
        offset 100)
order by 1, 3 desc;
