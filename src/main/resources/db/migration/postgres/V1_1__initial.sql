create table plan
(
    id            uuid primary key,
    reference     uuid,
    created_at    timestamp(3) with time zone,
    created_by    varchar(50),
    updated_at    timestamp(3) with time zone,
    updated_by    varchar(50),

    prison_number varchar(10),
    type          smallint
);

create table objective
(
    id                     uuid primary key,
    reference              uuid,
    created_at             timestamp(3) with time zone,
    created_by             varchar(50),
    updated_at             timestamp(3) with time zone,
    updated_by             varchar(50),

    title                  varchar(512),
    target_completion_date date,
    status                 varchar(50),
    note                   text,
    outcome                text
);

create table plan_objective_link
(
    plan_id      uuid,
    objective_id uuid,

    constraint plan_id_fk foreign key (plan_id) references plan (id),
    constraint objective_id_fk foreign key (objective_id) references objective (id)
);


create table objective_history
(
    id                uuid primary key,
    objective_id      uuid,

    previous_value    text,
    new_value         text,

    updated_at        timestamp(3) with time zone,
    updated_by        varchar(50),
    reason_for_change varchar(250),

    constraint objective_id_fk foreign key (objective_id) references objective (id)
);

create table step
(
    id           uuid primary key,
    objective_id uuid,
    reference    uuid,
    description  varchar(512),
    step_order   smallint,
    status       varchar(50),

    created_at   timestamp(3) with time zone,
    created_by   varchar(50),
    updated_at   timestamp(3) with time zone,
    updated_by   varchar(50),


    constraint objective_id_fk foreign key (objective_id) references objective (id)
);

create table step_history
(
    id                uuid primary key,
    step_id           uuid,
    previous_value    text,
    new_value         text,
    updated_at        timestamp(3) with time zone,
    updated_by        varchar(50),
    reason_for_change varchar(250),


    constraint objective_id_fk foreign key (step_id) references step (id)
)