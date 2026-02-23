create table user_info (
  id bigserial primary key,
  user_id bigint not null unique references app_user(id) on delete cascade,
  first_name varchar(100),
  last_name varchar(100),
  phone varchar(50),
  avatar_path varchar(512),
  avatar_content_type varchar(100),
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  created_by bigint,
  updated_by bigint,
  version bigint not null default 0
);

create index idx_user_info_user_id on user_info(user_id);