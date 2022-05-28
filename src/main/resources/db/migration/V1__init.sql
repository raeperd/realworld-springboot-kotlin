create table if not exists users
(
    id       bigint primary key auto_increment,
    name     varchar(100) not null,
    bio      varchar(1024),
    image    varchar(1024),
    email    varchar(255) not null,
    password varchar(255) not null
);