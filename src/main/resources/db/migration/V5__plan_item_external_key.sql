-- A key set by another system (TII sends "tii:2026-11-07:block") so it can update the same item
-- every day instead of adding a copy. Null for items made in the app.
alter table plan_items add column external_key varchar(120);
alter table plan_items add constraint uk_plan_items_external_key unique (external_key);
