create table users (
                       id uuid primary key,
                       created_at timestamp not null,
                       updated_at timestamp not null,
                       email varchar(255) not null unique,
                       phone varchar(30) unique,
                       password_hash varchar(255) not null,
                       status varchar(50) not null,
                       last_login_at timestamp
);

create table worker_profiles (
                                 id uuid primary key,
                                 created_at timestamp not null,
                                 updated_at timestamp not null,
                                 user_id uuid not null unique,
                                 display_name varchar(150) not null,
                                 about varchar(2000),
                                 city varchar(255),
                                 experience_years integer,
                                 rating_avg numeric(3,2) not null default 0.00,
                                 reviews_count integer not null default 0,
                                 verified boolean not null default false,
                                 active boolean not null default true,
                                 constraint fk_worker_profiles_user
                                     foreign key (user_id) references users(id)
);

create table customer_profiles (
                                   id uuid primary key,
                                   created_at timestamp not null,
                                   updated_at timestamp not null,
                                   user_id uuid not null unique,
                                   display_name varchar(150) not null,
                                   city varchar(255),
                                   constraint fk_customer_profiles_user
                                       foreign key (user_id) references users(id)
);

create table categories (
                            id uuid primary key,
                            created_at timestamp not null,
                            updated_at timestamp not null,
                            name varchar(150) not null unique,
                            slug varchar(150) not null unique,
                            parent_id uuid,
                            active boolean not null default true,
                            constraint fk_categories_parent
                                foreign key (parent_id) references categories(id)
);

create table worker_profile_categories (
                                           worker_profile_id uuid not null,
                                           category_id uuid not null,
                                           primary key (worker_profile_id, category_id),
                                           constraint fk_wpc_worker
                                               foreign key (worker_profile_id) references worker_profiles(id),
                                           constraint fk_wpc_category
                                               foreign key (category_id) references categories(id)
);

create table job_requests (
                              id uuid primary key,
                              created_at timestamp not null,
                              updated_at timestamp not null,
                              customer_id uuid not null,
                              category_id uuid not null,
                              title varchar(255) not null,
                              description varchar(4000) not null,
                              address_text varchar(500),
                              city varchar(255),
                              scheduled_at timestamp,
                              budget_from numeric(12,2),
                              budget_to numeric(12,2),
                              status varchar(50) not null,
                              selected_offer_id uuid,
                              constraint fk_job_requests_customer
                                  foreign key (customer_id) references customer_profiles(id),
                              constraint fk_job_requests_category
                                  foreign key (category_id) references categories(id)
);

create table job_offers (
                            id uuid primary key,
                            created_at timestamp not null,
                            updated_at timestamp not null,
                            job_request_id uuid not null,
                            worker_id uuid not null,
                            message varchar(2000) not null,
                            price numeric(12,2) not null,
                            estimated_days integer,
                            status varchar(50) not null,
                            expires_at timestamp,
                            constraint fk_job_offers_job_request
                                foreign key (job_request_id) references job_requests(id),
                            constraint fk_job_offers_worker
                                foreign key (worker_id) references worker_profiles(id)
);

alter table job_requests
    add constraint fk_job_requests_selected_offer
        foreign key (selected_offer_id) references job_offers(id);

create table deals (
                       id uuid primary key,
                       created_at timestamp not null,
                       updated_at timestamp not null,
                       job_request_id uuid not null,
                       job_offer_id uuid not null unique,
                       customer_id uuid not null,
                       worker_id uuid not null,
                       agreed_price numeric(12,2) not null,
                       platform_fee numeric(12,2),
                       status varchar(50) not null,
                       started_at timestamp,
                       completed_at timestamp,
                       cancelled_at timestamp,
                       cancellation_reason varchar(1000),
                       constraint fk_deals_job_request
                           foreign key (job_request_id) references job_requests(id),
                       constraint fk_deals_job_offer
                           foreign key (job_offer_id) references job_offers(id),
                       constraint fk_deals_customer
                           foreign key (customer_id) references customer_profiles(id),
                       constraint fk_deals_worker
                           foreign key (worker_id) references worker_profiles(id)
);

create table payments (
                          id uuid primary key,
                          created_at timestamp not null,
                          updated_at timestamp not null,
                          deal_id uuid not null,
                          amount numeric(12,2) not null,
                          currency varchar(10) not null default 'RUB',
                          provider varchar(50) not null,
                          provider_payment_id varchar(255),
                          status varchar(50) not null,
                          type varchar(50) not null,
                          paid_at timestamp,
                          refunded_at timestamp,
                          constraint fk_payments_deal
                              foreign key (deal_id) references deals(id)
);

create table reviews (
                         id uuid primary key,
                         created_at timestamp not null,
                         updated_at timestamp not null,
                         deal_id uuid not null,
                         author_user_id uuid not null,
                         target_worker_id uuid not null,
                         rating integer not null,
                         text varchar(3000),
                         status varchar(50) not null,
                         constraint fk_reviews_deal
                             foreign key (deal_id) references deals(id),
                         constraint fk_reviews_author_user
                             foreign key (author_user_id) references users(id),
                         constraint fk_reviews_target_worker
                             foreign key (target_worker_id) references worker_profiles(id)
);

create unique index ux_reviews_deal_author
    on reviews(deal_id, author_user_id);

create index ix_worker_profiles_user_id
    on worker_profiles(user_id);

create index ix_customer_profiles_user_id
    on customer_profiles(user_id);

create index ix_categories_parent_id
    on categories(parent_id);

create index ix_job_requests_customer_id
    on job_requests(customer_id);

create index ix_job_requests_category_id
    on job_requests(category_id);

create index ix_job_requests_selected_offer_id
    on job_requests(selected_offer_id);

create index ix_job_offers_job_request_id
    on job_offers(job_request_id);

create index ix_job_offers_worker_id
    on job_offers(worker_id);

create index ix_deals_job_request_id
    on deals(job_request_id);

create index ix_deals_customer_id
    on deals(customer_id);

create index ix_deals_worker_id
    on deals(worker_id);

create index ix_payments_deal_id
    on payments(deal_id);

create index ix_reviews_target_worker_id
    on reviews(target_worker_id);
