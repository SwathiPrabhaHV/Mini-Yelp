Create table business(
business_id varchar(50) primary key,
full_address varchar(500),
open_boolean varchar(50) check(open_boolean in('true','false')),
city varchar(50),
State varchar(50),
latitude real,
longitude real,
review_count number,
name varchar(100),
stars real,
business_type varchar(100));

CREATE TABLE YELP_USERS
(
user_id varchar2(100),
name varchar2(200) not null,
yelping_since varchar2(30),
review_count number(5,0),
average_stars number,
friend_count number(5,0),
votes_count number(5,0),
fans number(5,0),
compliments varchar2(4000),
elite varchar2(1000),
PRIMARY KEY(user_id));
	
create table Review(
user_ID varchar(30) not null,
Review_ID varchar(30) primary key,
stars number check(stars >=1 and stars <=5),
publish_date varchar(50) not null,
text long,
Business_ID varchar(30) not null,
type_review varchar(30),
number_of_votes number,
FOREIGN KEY (user_id) REFERENCES Yelp_Users(user_id) ON DELETE SET NULL,
FOREIGN KEY (business_id) REFERENCES Business(business_id) ON DELETE SET NULL);

create table main_category(
name varchar(50),
business_id varchar(50)
FOREIGN KEY (business_id) REFERENCES Business(business_id) ON DELETE SET NULL));


create table sub_category(
name varchar(50),
business_id varchar(50),
FOREIGN KEY (business_id) REFERENCES Business(business_id) ON DELETE SET NULL);

create table attributes(
attribute_name varchar(400),
business_id varchar(50),
FOREIGN KEY (business_id) REFERENCES Business(business_id) ON DELETE SET NULL);


CREATE INDEX INDEX_MAINCATEGORY_NAME ON Main_Category (name);
create index index_maincategory_id on main_category(business_id);
-- create index for Sub_Category table
CREATE INDEX INDEX_SUBCATEGORY_NAME ON Sub_Category(name);
CREATE INDEX index_business_id ON Sub_Category(business_id);

-- create index for Attribute table
CREATE INDEX INDEX_ATTRIBUTE ON Attributes (attribute_name);
CREATE INDEX index_attribute_business_id ON attributes(business_id);

-- create index for YelpUser table
CREATE INDEX INDEX_USER_MEMBERSINCE ON Yelp_Users (yelping_since);
CREATE INDEX INDEX_USER_REVIEWCOUNT ON Yelp_Users (review_count);
CREATE INDEX INDEX_USER_STAR ON Yelp_Users (average_stars);
CREATE INDEX INDEX_USER_FRIENDS ON Yelp_Users (friend_count);
CREATE INDEX INDEX_USER_STAR ON Yelp_Users (votes_count);

-- create index for review table
CREATE INDEX INDEX_REVIEW_DATE ON REVIEW (publish_date);
CREATE INDEX INDEX_REVIEW_STAR ON REVIEW (stars);
CREATE INDEX INDEX_REVIEW_VOTE ON REVIEW (number_of_votes);
create index review_business_id on review(business_id);
create index review_user_id on review(user_id);

-- create index on business
create index business_name on business(name);

