

begin;
delete from objective_history where objective_id in (select id from objective where crn like 'perf-_');
delete from objective where crn like 'perf-_';
commit;
