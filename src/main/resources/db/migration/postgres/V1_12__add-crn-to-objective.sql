alter table objective
    add column crn varchar(10);

create unique index objective_crn_reference ON objective (reference, crn);
create index objective_crn ON objective (crn);

update objective o
set crn = (select crn
           from plan p
                    join plan_objective_link l on p.id = l.plan_id
           where l.objective_id = o.id
           limit 1)
