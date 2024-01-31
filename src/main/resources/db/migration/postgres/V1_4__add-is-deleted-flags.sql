alter table plan add column is_deleted boolean not null default false;
alter table objective add column is_deleted boolean not null default false;
alter table step add column is_deleted boolean not null default false;
