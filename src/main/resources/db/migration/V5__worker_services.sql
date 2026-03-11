-- V5: Worker services (portfolio / price list)

create table worker_services (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    worker_profile_id uuid not null,
    title varchar(200) not null,
    description varchar(2000),
    price_from numeric(12,2),
    price_to numeric(12,2),
    active boolean not null default true,
    constraint fk_worker_services_worker_profile foreign key (worker_profile_id) references worker_profiles(id)
);

create index ix_worker_services_worker_profile_id on worker_services(worker_profile_id);
create index ix_worker_services_active on worker_services(active);

