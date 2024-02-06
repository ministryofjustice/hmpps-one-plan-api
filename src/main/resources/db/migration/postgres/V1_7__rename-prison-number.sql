alter table plan rename column prison_number to crn;
alter index plan_pnumber_ref rename to plan_crn_ref;
alter index plan_pnumber rename to plan_crn;
