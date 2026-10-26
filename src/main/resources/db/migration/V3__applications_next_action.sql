-- The resume variant enum becomes a free-text reference to the resume file that was sent.
-- Existing values keep their meaning under the new names.
alter table applications drop constraint if exists applications_resume_variant_check;

update applications set resume_variant = case resume_variant
    when 'VARIANT_A_DEFENSE' then 'Variant A - Defense'
    when 'VARIANT_B_COMMERCIAL' then 'Variant B - Commercial'
    when 'VARIANT_C_CACI_SPECIFIC' then 'Variant C - CACI'
    else resume_variant
end;

alter table applications rename column resume_variant to resume_file;
alter table applications alter column resume_file type varchar(200);

-- Follow-up and interview tracking: who to follow up with, when the next step is, and the interview time.
alter table applications add column contact_name varchar(200);
alter table applications add column next_action_date date;
alter table applications add column interview_at timestamp with time zone;
