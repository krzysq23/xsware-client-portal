create table app_user (
  id bigserial primary key,
  email varchar(255) not null unique,
  password_hash varchar(255) not null,
  role varchar(50) not null,
  enabled boolean not null default true,
  created_at timestamptz not null default now()
);

create table refresh_token (
  id bigserial primary key,
  user_id bigint not null references app_user(id) on delete cascade,
  token varchar(512) not null unique,
  expires_at timestamptz not null,
  revoked boolean not null default false,
  created_at timestamptz not null default now()
);

create index idx_refresh_token_user_id on refresh_token(user_id);
create index idx_refresh_token_token on refresh_token(token);