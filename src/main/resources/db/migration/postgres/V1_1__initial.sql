create table plan
(
    id            uuid primary key,
    reference     uuid                        not null,
    created_at    timestamp(3) with time zone not null,
    created_by    varchar(50)                 not null,
    updated_at    timestamp(3) with time zone not null,
    updated_by    varchar(50)                 not null,

    prison_number varchar(10)                 not null,
    type          smallint                    not null
);

create table objective
(
    id                     uuid primary key,
    reference              uuid                        not null,
    created_at             timestamp(3) with time zone not null,
    created_by             varchar(50)                 not null,
    updated_at             timestamp(3) with time zone not null,
    updated_by             varchar(50)                 not null,

    title                  varchar(512)                not null,
    target_completion_date date                        not null,
    status                 varchar(50)                 not null,
    note                   text                        not null,
    outcome                text                        not null
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

    previous_value    text                        not null,
    new_value         text                        not null,

    updated_at        timestamp(3) with time zone not null,
    updated_by        varchar(50)                 not null,
    reason_for_change varchar(250)                not null,

    constraint objective_id_fk foreign key (objective_id) references objective (id)
);

create table step
(
    id           uuid primary key,
    objective_id uuid                        not null,
    reference    uuid                        not null,
    description  varchar(512)                not null,
    step_order   smallint                    not null,
    status       varchar(50)                 not null,

    created_at   timestamp(3) with time zone not null,
    created_by   varchar(50)                 not null,
    updated_at   timestamp(3) with time zone not null,
    updated_by   varchar(50)                 not null,

    constraint objective_id_fk foreign key (objective_id) references objective (id)
);

create table step_history
(
    id                uuid primary key,
    step_id           uuid                        not null,
    previous_value    text                        not null,
    new_value         text                        not null,
    updated_at        timestamp(3) with time zone not null,
    updated_by        varchar(50)                 not null,
    reason_for_change varchar(250)                not null,


    constraint objective_id_fk foreign key (step_id) references step (id)
);
