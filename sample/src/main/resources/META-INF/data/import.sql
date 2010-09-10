insert into License (value, name) values ('apache-2.0', 'Apache License, Version 2.0');
insert into License (value, name) values ('gpl-2.0', 'GNU General Public License, version 2');
insert into License (value, name) values ('cddl', 'Common Development and Distribution License');
insert into License (value, name) values ('mit-license', 'MIT License');

insert into Status (value, name) values ('unstable', 'Unstable');
insert into Status (value, name) values ('testing', 'Testing');
insert into Status (value, name) values ('stable', 'Stable');

insert into Technology (value, name, required) values ('summer', 'Summer', true);
insert into Technology (value, name, required) values ('spring-framework', 'Spring Framework', true);
insert into Technology (value, name, required) values ('jsf', 'Java Server Faces', true);
insert into Technology (value, name, required) values ('hibernate', 'Hibernate', false);
insert into Technology (value, name, required) values ('jquery', 'jQuery', false);
insert into Technology (value, name, required) values ('atmosphere', 'Atmosphere', false);
