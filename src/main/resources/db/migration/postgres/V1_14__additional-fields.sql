alter table plan add column created_by_display_name varchar(250);
alter table plan add column updated_by_display_name varchar(250);
alter table plan add column created_at_prison varchar(250);
alter table plan add column updated_at_prison varchar(250);

alter table objective add column created_by_display_name varchar(250);
alter table objective add column updated_by_display_name varchar(250);
alter table objective add column created_at_prison varchar(250);
alter table objective add column updated_at_prison varchar(250);

alter table step add column created_by_display_name varchar(250);
alter table step add column updated_by_display_name varchar(250);
alter table step add column created_at_prison varchar(250);
alter table step add column updated_at_prison varchar(250);
