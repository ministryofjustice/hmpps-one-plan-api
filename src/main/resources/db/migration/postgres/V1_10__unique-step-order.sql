-- Assign existing steps in dev to have ascending order from creation date
with so as (
    select id,
           rank() OVER (PARTITION BY objective_id ORDER BY created_at) as new_order
    from step
)
update step s
set step_order = so.new_order
from so
where s.id = so.id;

create unique index step_order ON step(objective_id,step_order);
