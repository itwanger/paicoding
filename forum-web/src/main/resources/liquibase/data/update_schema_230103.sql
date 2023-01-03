alter table `config` add `extra` varchar(1024) not null default '{}' comment '扩展信息' after `tags`;

insert into config(`type`, `name`, `jump_url`, `banner_url`, `content`, `rank`, `status`, `tags`, `extra`)
values (6, '轻松学会设计模式', 'http://cdn.hhui.top/forum/pdf/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F.pdf',
        'http://cdn.hhui.top/forum/pdf/%E8%AE%BE%E8%AE%A1%E6%A8%A1%E5%BC%8F.jpg', '程序开发者的指导圣经', 2, 1, '1',
        '{"rate": 9.1, "visit": 110235, "download": 12121}');
insert into config(`type`, `name`, `jump_url`, `banner_url`, `content`, `rank`, `status`, `tags`, `extra`)
values (6, 'Java开发手册(黄山版)',
        'http://cdn.hhui.top/forum/pdf/Java%E5%BC%80%E5%8F%91%E6%89%8B%E5%86%8C%28%E9%BB%84%E5%B1%B1%E7%89%88%29.pdf',
        'http://cdn.hhui.top/forum/pdf/javaGuide.png', 'Javaer开发者规范手册', 2, 1, '1',
        '{"rate": 9.3, "visit": 120231, "download": 212103}');
