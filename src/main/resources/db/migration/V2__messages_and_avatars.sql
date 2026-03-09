-- V2: Chat messages + avatar URL field

-- Messages table for chat between users
create table messages (
                          id uuid primary key,
                          created_at timestamp not null,
                          updated_at timestamp not null,
                          sender_id uuid not null,
                          receiver_id uuid not null,
                          job_request_id uuid,
                          text varchar(4000) not null,
                          is_read boolean not null default false,
                          constraint fk_messages_sender foreign key (sender_id) references users(id),
                          constraint fk_messages_receiver foreign key (receiver_id) references users(id),
                          constraint fk_messages_job_request foreign key (job_request_id) references job_requests(id)
);

create index ix_messages_sender_id on messages(sender_id);
create index ix_messages_receiver_id on messages(receiver_id);
create index ix_messages_job_request_id on messages(job_request_id);

-- Avatar URL column on users
alter table users add column avatar_url varchar(500);

-- Add payout_amount column to deals if missing
alter table deals add column if not exists payout_amount numeric(12,2);