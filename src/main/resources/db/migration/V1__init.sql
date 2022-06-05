create table if not exists users
(
    id       BIGINT primary key auto_increment,
    username varchar(100) not null unique,
    bio      varchar(1024),
    image    varchar(1024),
    email    varchar(255) not null,
    password varchar(255) not null
);

create table if not exists user_followings
(
    follower_id BIGINT not null,
    followee_id BIGINT not null,
    primary key (follower_id, followee_id),
    constraint fk_follower foreign key (follower_id) references users (id) on delete cascade,
    constraint fk_followee foreign key (followee_id) references users (id) on delete cascade
);


create table if not exists articles
(
    id          BIGINT primary key,
    author_id   BIGINT       not null,
    title       VARCHAR(255) not null,
    slug        VARCHAR(255) not null unique,
    description VARCHAR(255) not null,
    body        VARCHAR(255) not null,
    created_at  TIMESTAMP    not null default current_timestamp,
    updated_at  TIMESTAMP    not null default current_timestamp,
    constraint fk_author foreign key (author_id) references users (id) on delete cascade
);

create table if not exists tags
(
    id   BIGINT primary key,
    name VARCHAR(255) unique not null
);

create table if not exists articles_tags
(
    article_id BIGINT not null,
    tag_id     BIGINT not null,
    primary key (article_id, tag_id),
    constraint fk_article foreign key (article_id) references articles (id) on delete cascade,
    constraint fk_tag foreign key (tag_id) references tags (id) on delete cascade
);