use fastfood_db;

/*SET FOREIGN_KEY_CHECKS = 0;

drop table users;
drop table categories;
drop table products;
drop table orders;
drop table order_items;
drop table reviews;
drop table cart_items;
SET FOREIGN_KEY_CHECKS = 1;*/

INSERT INTO users(username, password, email, phone_number, full_name, address, role, created_at, status)
VALUES ('John', '123456', 'john@gmail.com', '0453453453', 'John Java', 'Bolo Bala Street', 'CUSTOMER', now(), 'ACTIVE'),
	('Jane', '123456', 'jane@gmail.com', '0546456464', 'Jane Cena', 'Bala Bolo Street', 'CUSTOMER', now(), 'ACTIVE'),
    ('Bolo', '123456', 'bolo@gmail.com', '0435353453', 'Bolo Balala', 'Groves Street', 'MANAGER', now(), 'ACTIVE'),
    ('Mike', '123456', 'mike@gmail.com', '0471234567', 'Mike Thomson', 'Riverdale Avenue', 'CUSTOMER', now(), 'ACTIVE'),
	('Sara', '123456', 'sara@gmail.com', '0467891234', 'Sara Mitchell', 'Greenwood Lane', 'CUSTOMER', now(), 'ACTIVE'),
	('Johnny', '123456', 'johnnyboy@gmail.com', '0498765432', 'Johnny Silverhand', 'Night Drive', 'CUSTOMER', now(), 'ACTIVE'),
	('Emily', '123456', 'emily@gmail.com', '0451122334', 'Emily Johnson', 'Pinecrest Street', 'CUSTOMER', now(), 'ACTIVE'),
	('Robert', '123456', 'robert@gmail.com', '0489988776', 'Robert Miles', 'Oakridge Boulevard', 'MANAGER', now(), 'ACTIVE'),
	('Lucy', '123456', 'lucy@gmail.com', '0423344556', 'Lucy Kushinada', 'Willow Creek Road', 'CUSTOMER', now(), 'ACTIVE'),
	('David', '123456', 'david@gmail.com', '0476655443', 'David Peterson', 'Cedarwood Avenue', 'CUSTOMER', now(), 'ACTIVE'),
    ('Buggy', '123456', 'buggy@gmail.com', '0423369876', 'Buggy Johnson', 'Willow Creek Road', 'CUSTOMER', now(), 'ACTIVE'),
	('Joseph', '123456', 'joseph@gmail.com', '0927192197', 'Joseph Joestar', 'Cedarwood Avenue', 'CUSTOMER', now(), 'ACTIVE');

INSERT INTO categories (name, description)
VALUES
('Beverages', 'Soft drinks, coffee, and tea'),
('Snacks', 'Quick bites and packaged snacks'),
('Fast Food', 'Burgers, fries, and sandwiches'),
('Dessert', 'light foods to eat after meal');

INSERT INTO products (name, description, price, image_url, stock_quantity, is_available, category_id, created_at)
VALUES
('Coca Cola', 'Refreshing soft drink', 1.50, 'images/cocacola.jpg', 100, true, 1, NOW()),
('French Fries', 'Crispy and salty snack', 2.00, 'images/fries.jpg', 50, true, 3, NOW()),
('Cheeseburger', 'Juicy beef burger with cheese', 5.99, 'images/cheeseburger.jpg', 30, true, 3, NOW()),
('Diet Coke', 'Cola for people do not want too much sugar intake', 1.50, 'images/diet.jpg', 80, true, 1, NOW()),
('Fanta Orange', 'Orange-flavored carbonated water', 1.50, 'images/fanta_o.jpg', 60, true, 1, NOW()),
('Chicken Nuggets', 'Crispy breaded chicken pieces', 4.50, 'images/chicken_nuggets.jpg', 40, true, 3, NOW()),
('Fried Chicken', 'Crispy and super flavorful chicken', 4.50, 'images/chicken.jpg', 100, true, 3, NOW()),
('Chocolate Milkshake', 'Thick and creamy chocolate shake', 3.20, 'images/chocolate_milkshake.jpg', 35, true, 2, NOW()),
('Vanilla Ice Cream', 'Smooth and creamy vanilla ice cream', 2.80, 'images/vanilla_ice_cream.jpg', 45, true, 2, NOW()),
('Grilled Chicken Sandwich', 'Grilled chicken with lettuce and mayo', 6.20, 'images/grilled_chicken_sandwich.jpg', 20, true, 3, NOW()),
('Caesar Salad', 'Fresh romaine lettuce with Caesar dressing', 4.00, 'images/caesar_salad.jpg', 30, true, 4, NOW()),
('Water Bottle', 'Pure spring bottled water', 1.00, 'images/water_bottle.jpg', 200, true, 1, NOW()),
('Honey Butter Fried Chicken', 'Super addictive, sweet-and-salty honey-butter sauce', 4.80, 'images/honey_chicken.jpg', 28, true, 3, NOW());

INSERT INTO orders (order_number, total_amount, status, delivery_address, notes, payment_method, created_at, user_id)
VALUES
('ORD-1001', 3.00, 'PENDING', '123 Main Street', 'Leave at the door', 'CASH', NOW(), 1),
('ORD-1002', 11.98, 'PENDING', 'Admin HQ', 'Deliver during office hours', 'CASH', NOW(), 2),
('ORD-1003', 5.99, 'PENDING', 'Staff Office', NULL, 'CASH', NOW(), 4),
('ORD-1004', 8.00, 'PENDING', '456 Elm Street', 'Ring the bell twice', 'ONLINE_BANKING', NOW(), 5),
('ORD-1005', 4.50, 'PENDING', '789 Oak Avenue', NULL, 'CASH', NOW(), 11),
('ORD-1006', 9.00, 'PENDING', '321 Pine Road', 'Handle with care', 'ONLINE_BANKING', NOW(), 6),
('ORD-1007', 4.50, 'PENDING', '654 Maple Drive', 'Leave at reception', 'CASH', NOW(), 7),
('ORD-1008', 12.40, 'PENDING', '987 Willow Lane', 'Call upon arrival', 'ONLINE_BANKING', NOW(), 12),
('ORD-1009', 12.80, 'PENDING', '246 Cedar Boulevard', NULL, 'CASH', NOW(), 9),
('ORD-1010', 4.00, 'PENDING', '135 Birch Court', 'Do not knock loudly', 'CASH', NOW(), 10),
('ORD-1011', 8.00, 'PENDING', '579 Spruce Terrace', NULL, 'ONLINE_BANKING', NOW(), 2),
('ORD-1012', 8.40, 'PENDING', '864 Ashwood Street', 'Gate code: 1234', 'ONLINE_BANKING', NOW(), 1),
('ORD-1013', 4.80, 'PENDING', '753 Poplar Avenue', 'Friendly dog in yard', 'CASH', NOW(), 4);

INSERT INTO order_items (quantity, unit_price, subtotal, special_instructions, order_id, product_id)
VALUES
(2, 1.50, 3.00, 'Chilled please', 1, 1),                
(2, 5.99, 11.98, 'Extra cheese', 2, 3),         
(1, 5.99, 5.99, 'No pickles', 3, 3),
(1, 2.00, 2.00, 'Extra crispy', 4, 2),
(1, 4.50, 4.50, 'Extra crispy', 4, 6),
(1, 1.50, 1.50, 'No straw', 4, 5),
(3, 1.50, 4.50, 'No straw', 5, 4),
(2, 4.50, 9.00, 'Extra crispy', 6, 7),
(1, 4.50, 4.50, 'Add extra sauce', 7, 6),
(2, 6.20, 12.40, Null, 8, 10),
(4, 3.20, 12.80, 'Less sweet', 9, 8),
(1, 4.00, 4.00, 'More dressing', 10, 11),
(8, 1.00, 8.00, Null, 11, 12),
(3, 2.80, 8.40, 'Add a bit more sugar', 12, 9),
(1, 4.80, 4.80, Null, 13, 13);

INSERT INTO reviews (rating, comment, created_at, user_id, product_id)
VALUES
(5, 'Absolutely loved it! Highly recommend.', NOW(), 1, 3), 
(2, 'Not crispy at all, fail advertisement', NOW(), 2, 2),         
(1, 'Terrible, Pepsi is better', NOW(), 4, 1),
(4, 'Tasty and fresh, will order again.', NOW(), 7, 6),
(3, 'Crispy but very salty.', NOW(), 5, 6),
(3, "It's ok, not bad but not exactly good either", NOW(), 5, 2),
(3, 'Average taste, a bit too salty.', NOW(), 6, 7),
(5, 'Perfectly chilled and refreshing.', NOW(), 11, 4),
(4, 'Loved the creamy texture, yum!', NOW(), 1, 9),
(2, 'Too sweet for my liking.', NOW(), 4, 13),
(5, 'Somehow cheaper than in store, i guess i will buy water from here from now on', NOW(), 2, 12),
(3, 'Good, but the chicken piece is too small', NOW(), 6, 7),
(5, 'Good price compare to supermarket', NOW(), 5, 5),
(5, 'Perfect taste!', NOW(), 12, 10);

INSERT INTO cart_items (quantity, created_at, updated_at, user_id, product_id)
VALUES
(2, NOW(), NOW(), 1, 1),  
(2, NOW(), NOW(), 2, 3),  
(1, NOW(), NOW(), 4, 3),   
(1, NOW(), NOW(), 5, 2),  
(1, NOW(), NOW(), 5, 6),  
(1, NOW(), NOW(), 5, 5),  
(3, NOW(), NOW(), 11, 4),  
(2, NOW(), NOW(), 6, 7), 
(1, NOW(), NOW(), 7, 6), 
(2, NOW(), NOW(), 12, 10), 
(4, NOW(), NOW(), 9, 8),  
(1, NOW(), NOW(), 10, 11), 
(8, NOW(), NOW(), 2, 12),
(3, NOW(), NOW(), 1, 9), 
(1, NOW(), NOW(), 4, 13);








