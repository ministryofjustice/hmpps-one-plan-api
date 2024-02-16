BEGIN TRANSACTION;
insert into roles(role_id, role_code, role_name, admin_type) values (gen_random_uuid(), 'ONE_PLAN_EDIT', 'One Plan Edit', 'DPS_ADM');

INSERT INTO users (user_id, username, password, password_expiry, email, first_name, last_name, verified, enabled, locked, source)
VALUES (gen_random_uuid(), 'ONE_PLAN_USER', '{bcrypt}$2a$10$Fmcp2KUKRW53US3EJfsxkOh.ekZhqz5.Baheb9E98QLwEFLb9csxy', '3013-01-28 13:23:19.0000000', 'one_plan_user@digital.justice.gov.uk', 'Auth', 'Only', true, true, false, 'auth');

INSERT INTO user_role (role_id, user_id)
SELECT role_id, user_id from roles, users where username = 'ONE_PLAN_USER' and role_code = 'ONE_PLAN_EDIT';
commit;
