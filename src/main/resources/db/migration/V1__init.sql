create table if not exists users
(
    id       bigint primary key auto_increment,
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
