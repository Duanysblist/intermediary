-- How far each routine has generated plan items, so generation resumes after that date and never
-- recreates items the user deleted or moved. Backfilled from the items that already exist.
alter table recurring_plans add column generated_through date;
update recurring_plans rp set generated_through = (select max(pi.target_date) from plan_items pi where pi.recurring_plan_id = rp.id);
