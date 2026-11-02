-- The user's order within a board column (PUT /plan-items/order). Null sorts after the ordered items;
-- a status change clears it, so an item moved by an edit or a sync lands at the bottom of its new column.
alter table plan_items add column position integer;
