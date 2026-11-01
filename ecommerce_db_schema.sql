-- ==============================================================================
-- E-COMMERCE MANAGEMENT SYSTEM (ShopEase)
-- MySQL Database Creation & Table Schema Script
-- Fulfills Section 4 of PDF Requirements (database: ecommerce_db)
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS ecommerce_db;
USE ecommerce_db;

-- 1. Categories Table
CREATE TABLE IF NOT EXISTS categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

-- 2. Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- 3. Products Table (PDF requirement: stores product ID, name, price, stock quantity)
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    price DOUBLE NOT NULL,
    stock INT NOT NULL,
    image_url VARCHAR(1000),
    category_id BIGINT,
    CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- 4. Orders Table (PDF requirement: stores order ID, order date, total amount)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    customer_name VARCHAR(255),
    customer_email VARCHAR(255),
    address VARCHAR(500),
    phone VARCHAR(50),
    payment_method VARCHAR(50) DEFAULT 'CASH_ON_DELIVERY',
    total_amount DOUBLE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PLACED',
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- 5. Order Items Table (PDF requirement: stores order ID, product ID, quantity, purchase price)
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255),
    image_url VARCHAR(1000),
    purchase_price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- 6. Cart Items Table (for persistent shopping carts)
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ==============================================================================
-- 5. Seed Data: categories and products (images: Open Library, Wikimedia, Unsplash)
-- Safe to re-run: rows are only inserted if the product name does not exist yet.
-- ==============================================================================
INSERT IGNORE INTO categories (name) VALUES ('Electronics');
INSERT IGNORE INTO categories (name) VALUES ('Fashion');
INSERT IGNORE INTO categories (name) VALUES ('Home & Kitchen');
INSERT IGNORE INTO categories (name) VALUES ('Sports & Fitness');
INSERT IGNORE INTO categories (name) VALUES ('Books');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Clean Code: A Handbook of Agile Software Craftsmanship - Robert C. Martin', 'Robert C. Martin''s world-renowned classic on agile software craftsmanship. Teaches software developers how to write clean, readable, maintainable, and refactorable code through practical code-cleaning case studies, best naming conventions, error handling techniques, and unit test discipline.', 749.00, 60, 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg?default=false', (SELECT id FROM categories WHERE name = 'Books')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Clean Code: A Handbook of Agile Software Craftsmanship - Robert C. Martin');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Effective Java (3rd Edition) - Joshua Bloch', 'The definitive industry standard guide for Java developers written by Joshua Bloch, former Chief Java Architect at Google. Covers 90 indispensable rules of thumb, design patterns, generics, enums, annotations, lambdas, and modern streams.', 999.00, 50, 'https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg?default=false', (SELECT id FROM categories WHERE name = 'Books')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Effective Java (3rd Edition) - Joshua Bloch');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'The Pragmatic Programmer: Your Journey to Mastery (20th Anniversary Edition)', 'Written by David Thomas and Andrew Hunt, this timeless software engineering masterpiece cuts through the jargon to teach core programming philosophies: avoiding duplicate knowledge (DRY), writing flexible code, tracer bullets, debugging psychology, and continuous professional growth.', 1199.00, 45, 'https://covers.openlibrary.org/b/isbn/9780135957059-L.jpg?default=false', (SELECT id FROM categories WHERE name = 'Books')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'The Pragmatic Programmer: Your Journey to Mastery (20th Anniversary Edition)');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Designing Data-Intensive Applications - Martin Kleppmann', 'The gold standard architectural reference for modern backend and cloud engineers. Details the principles and trade-offs of relational vs NoSQL storage engines, replication strategies, partitioning, transactions, consensus algorithms, and distributed stream processing.', 1550.00, 40, 'https://covers.openlibrary.org/b/isbn/9781449373320-L.jpg?default=false', (SELECT id FROM categories WHERE name = 'Books')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Designing Data-Intensive Applications - Martin Kleppmann');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Introduction to Algorithms (CLRS 3rd Edition)', 'The most widely used algorithms textbook globally by Cormen, Leiserson, Rivest, and Stein. Complete with rigorous mathematical analysis, dynamic programming, graph algorithms, NP-completeness, and fundamental data structures.', 1850.00, 35, 'https://covers.openlibrary.org/b/isbn/9780262033848-L.jpg?default=false', (SELECT id FROM categories WHERE name = 'Books')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Introduction to Algorithms (CLRS 3rd Edition)');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Head First Design Patterns: A Brain-Friendly Guide', 'A highly visual, cognitive science-driven guide to software design patterns. Teaches Observer, Strategy, Decorator, Factory, Singleton, and Command patterns in Java with practical, real-world object-oriented architectures.', 899.00, 50, 'https://covers.openlibrary.org/b/isbn/9780596007126-L.jpg?default=false', (SELECT id FROM categories WHERE name = 'Books')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Head First Design Patterns: A Brain-Friendly Guide');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Python Crash Course: A Hands-On Project-Based Introduction - Eric Matthes', 'The world''s best-selling guide to Python programming. Walks beginners from syntax fundamentals to building practical real-world projects including 2D arcade games with Pygame, data visualizations with Matplotlib, and web applications with Django.', 849.00, 55, 'https://covers.openlibrary.org/b/isbn/9781593279288-L.jpg?default=false', (SELECT id FROM categories WHERE name = 'Books')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Python Crash Course: A Hands-On Project-Based Introduction - Eric Matthes');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Levi''s Men''s Original Trucker Denim Jacket', 'The iconic original Levi''s denim trucker jacket crafted from 100% non-stretch rugged cotton denim in dark rinse blue. Features point collar, signature copper shank button placket, button-flap chest pockets, and adjustable waist tabs.', 3999.00, 40, 'https://commons.wikimedia.org/wiki/Special:FilePath/Denim_Jacket_%2851079649933%29.jpg?width=800', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Levi''s Men''s Original Trucker Denim Jacket');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Hanes Heavyweight 100% Cotton Crewneck T-Shirt', 'Premium pre-shrunk combed organic cotton regular fit t-shirt with bio-wash softness finish. Built with lay-flat rib-knit collar, double-needle stitched sleeves and hem, and shoulder-to-shoulder neck taping for long-lasting durability.', 499.00, 100, 'https://commons.wikimedia.org/wiki/Special:FilePath/Crew_neck_T-shirt.jpg', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hanes Heavyweight 100% Cotton Crewneck T-Shirt');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Ray-Ban Classic Gold Aviator Sunglasses (G-15 Polarized)', 'Legendary authentic Ray-Ban pilot sunglasses with polished gold-tone metal alloy frame, crystal G-15 green polarized lenses providing 100% UV400 protection, adjustable clear silicone nose pads, and acetate temple tips.', 7590.00, 45, 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ray-Ban Classic Gold Aviator Sunglasses (G-15 Polarized)');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Titan Men''s Genuine Full-Grain Leather Bi-Fold Wallet', 'Handcrafted 100% genuine top-grain cowhide leather wallet with reinforced nylon perimeter stitching. Features 8 dedicated credit card slots, dual full-length currency compartments, transparent ID window, and built-in RFID blocking shield.', 1199.00, 70, 'https://commons.wikimedia.org/wiki/Special:FilePath/Leather_bi-fold_wallet_by_Eugene_Pik_%282021%29_04.jpg?width=800', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Titan Men''s Genuine Full-Grain Leather Bi-Fold Wallet');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Champion Reverse Weave Heavyweight Fleece Pullover Hoodie', 'Authentic athletic heavyweight fleece pullover hoodie with brushed interior warmth. Features cross-grain cut to resist vertical shrinkage, 2-ply double-layer hood with knotted drawstrings, ribbed side gussets, and front kangaroo pouch.', 2899.00, 50, 'https://commons.wikimedia.org/wiki/Special:FilePath/White_hoodie_made_by_COQ_manufacture.jpg?width=800', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Champion Reverse Weave Heavyweight Fleece Pullover Hoodie');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Adidas Originals Samba Classic White Leather Sneakers', 'Timeless football-inspired low-top street sneaker crafted with full-grain leather upper, suede T-toe overlay, die-cut EVA insole for lightweight cushioning, and non-marking gum rubber pivot-point outsole.', 7999.00, 35, 'https://commons.wikimedia.org/wiki/Special:FilePath/Adidas_Samba_In_White_March_2024.jpg?width=800', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Adidas Originals Samba Classic White Leather Sneakers');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Tommy Hilfiger Men''s Reversible Leather Dress Belt', 'Versatile 100% genuine split leather 35mm dress belt with twist-and-turn polished brushed silver buckle allowing instant switching between formal black and rich chestnut brown leather.', 1799.00, 60, 'https://commons.wikimedia.org/wiki/Special:FilePath/Germany_Belt-and-Buckle-02.jpg?width=800', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Tommy Hilfiger Men''s Reversible Leather Dress Belt');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Dockers Men''s Signature Straight-Fit Stretch Chino Trousers', 'Straight-fit stretch-cotton khaki chinos engineered with No Wrinkle technology, flex waistband, and reinforced welt back pockets. Breathable and comfortable for all-day office wear and casual weekends.', 2199.00, 55, 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Dockers Men''s Signature Straight-Fit Stretch Chino Trousers');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Hamilton Beach Automatic Drip Coffee Maker (12-Cup)', 'Programmable 12-cup electric drip coffee maker featuring an intuitive LED digital display, keep-warm non-stick hot plate, automatic pause-and-serve valve, reusable cone mesh filter basket, and high-temp borosilicate glass carafe with measuring markers.', 2499.00, 35, 'https://commons.wikimedia.org/wiki/Special:FilePath/Hamilton_Beach_home_coffee_maker.jpg?width=800', (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Hamilton Beach Automatic Drip Coffee Maker (12-Cup)');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Morphy Richards 15-Bar High Pressure Pump Espresso Machine', 'Authentic semi-automatic Italian espresso machine equipped with a 15-bar high-pressure pump, high-grade die-cast aluminum portafilter, dual stainless steel filter sieves, and swivel steam wand for rich crema and velvety cappuccino milk frothing.', 6499.00, 25, 'https://commons.wikimedia.org/wiki/Special:FilePath/Semi_Automatic_Espresso_Machine.jpg?width=800', (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Morphy Richards 15-Bar High Pressure Pump Espresso Machine');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Prestige Hard-Anodized Aluminium Non-Stick Frying Pan (26cm)', 'Heavy-gauge hard-anodized aluminum 26cm skillet with PFOA-free 3-layer granite non-stick coating. Induction and gas stove compatible with stainless steel stay-cool riveted handle for even heat distribution.', 1350.00, 45, 'https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Prestige Hard-Anodized Aluminium Non-Stick Frying Pan (26cm)');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Philips Daily Collection 1.5L Stainless Steel Electric Kettle', '1800-watt rapid boil electric kettle crafted from food-grade stainless steel with 360-degree cordless pirouette base, one-touch spring lid, automatic shut-off dry-boil safety protection, and internal water level indicator.', 1699.00, 40, 'https://commons.wikimedia.org/wiki/Special:FilePath/Philips_Kettle.jpg?width=800', (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Philips Daily Collection 1.5L Stainless Steel Electric Kettle');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Victorinox Swiss Classic 8-Inch Chef''s Knife', 'Swiss-crafted high-carbon stainless steel 20cm chef''s knife with laser-tested razor edge. Features an ergonomic slip-resistant Fibrox handle balanced for precision slicing, dicing, and chopping vegetables and meats.', 2799.00, 40, 'https://commons.wikimedia.org/wiki/Special:FilePath/Victorinox_Fibrox_5.2063.20_chef%27s_knife.jpg?width=800', (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Victorinox Swiss Classic 8-Inch Chef''s Knife');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Clay Craft Artisan Stoneware Ceramic Coffee Mug (Set of 2)', 'Handmade 350ml high-fired stoneware ceramic coffee mugs with generous C-shaped comfort handle. Features a durable glazed matte finish that is 100% microwave, oven, and dishwasher safe.', 599.00, 60, 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Clay Craft Artisan Stoneware Ceramic Coffee Mug (Set of 2)');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Sony MDR-7506 Professional Studio Monitor Headphones', 'Industry-standard closed-back studio reference headphones featuring 40mm neodymium driver units, gold-plated unimatch 1/4-inch adapter, folding rugged design, and high-purity copper voice coil for crystal clear acoustic monitoring.', 8490.00, 30, 'https://commons.wikimedia.org/wiki/Special:FilePath/Sony_MDR-7506_7615.jpg?width=600', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sony MDR-7506 Professional Studio Monitor Headphones');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Apple Watch Series 8 GPS 45mm (Midnight Aluminium)', 'Advanced fitness and health companion with edge-to-edge Always-On Retina display, temperature sensing, ECG app, blood oxygen monitoring, Crash Detection, swim-proof WR50 water resistance, and fast magnetic USB-C charging.', 38900.00, 25, 'https://commons.wikimedia.org/wiki/Special:FilePath/Apple_Watch_Series_8_Midnight_Aluminium_Case.jpg?width=600', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Apple Watch Series 8 GPS 45mm (Midnight Aluminium)');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Keychron K4 Wireless RGB Backlit Mechanical Keyboard', 'Compact 96% 100-key wireless mechanical keyboard with hot-swappable Gateron G Pro switches, aluminum frame, 15+ RGB backlight modes, Bluetooth 5.1 multi-device pairing, and massive 4000mAh rechargeable battery.', 8499.00, 35, 'https://cdn.shopify.com/s/files/1/0059/0630/1017/files/K4V3-QMK-3.jpg?v=1736907431', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Keychron K4 Wireless RGB Backlit Mechanical Keyboard');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Logitech G502 HERO Precision Optical Gaming Mouse', 'Legendary precision optical gaming mouse with HERO 25K sensor up to 25,600 DPI, 11 customizable buttons, onboard memory profiles, adjustable weight tuning system (5x 3.6g weights), and mechanical switch button tensioning.', 4195.00, 45, 'https://images.icecat.biz/img/gallery/21741975_9291503382.jpg', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Logitech G502 HERO Precision Optical Gaming Mouse');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Milton Stainless Steel Insulated Sports Water Bottle (750ml)', 'Double-walled vacuum insulated 18/8 food-grade stainless steel bottle. Keeps drinks freezing cold for 24 hours or steaming hot for 12 hours. Features wide-mouth ice filling, condensation-free powder coat, and leak-proof spout lid.', 899.00, 75, 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Sports & Fitness')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Milton Stainless Steel Insulated Sports Water Bottle (750ml)');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'The North Face Recon 30L Durable Travel Backpack', 'Iconic commuter and trail backpack engineered with water-repellent ripstop nylon, FlexVent injection-molded suspension straps, padded breathable lumbar mesh panel, dedicated 15.6-inch laptop compartment, and external stretch stash pocket.', 4299.00, 35, 'https://assets.thenorthface.com/images/t_img/c_fill,ar_4:5,f_auto,h_625,e_sharpen:60,w_500/dpr_2.0/v1792174970/NF0A52SHBEI-HERO/Recon-TNF-Duck-GreenNew-Taupe-Green-HERO.png', (SELECT id FROM categories WHERE name = 'Sports & Fitness')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'The North Face Recon 30L Durable Travel Backpack');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Levi''s Denim Jeans', 'Classic straight-fit denim jeans in mid-wash blue cotton denim with five-pocket styling, zip fly and button closure. Durable everyday wear.', 3299.00, 50, 'https://images.unsplash.com/photo-1542272604-787c3835535d?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Levi''s Denim Jeans');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Titan Minimalist Watch', 'Minimalist analog wrist watch with a clean dial, slim case and leather strap. Everyday style for work and casual wear.', 4495.00, 40, 'https://images.unsplash.com/photo-1524805444758-089113d48a6d?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Titan Minimalist Watch');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Tommy Hilfiger Oxford Shirt', 'Regular-fit button-down Oxford shirt in soft cotton with a chest logo detail. Smart-casual staple.', 2999.00, 45, 'https://images.unsplash.com/photo-1596755094514-f87e34085b2c?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Tommy Hilfiger Oxford Shirt');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Morphy Richards Toaster', 'Compact pop-up bread toaster with adjustable browning control, wide slots and removable crumb tray.', 1899.00, 40, 'https://commons.wikimedia.org/wiki/Special:FilePath/OK._Toaster.jpg?width=800', (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Morphy Richards Toaster');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Philips Air Fryer', 'Digital air fryer that cooks with little or no oil using rapid air circulation. Non-stick basket for easy cleaning.', 8995.00, 30, 'https://commons.wikimedia.org/wiki/Special:FilePath/Air_Fryer_5458.jpg?width=800', (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Philips Air Fryer');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Victorinox Swiss Knife', 'Swiss-made multi-tool pocket knife in stainless steel with blade, scissors and screwdriver tools.', 2499.00, 50, 'https://commons.wikimedia.org/wiki/Special:FilePath/Victorinox_Swiss_Army_Knife.jpg?width=800', (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Victorinox Swiss Knife');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'The North Face Weatherproof Jacket', 'Weatherproof outdoor jacket with a protective shell, adjustable hood and zip pockets for trail and travel use.', 9999.00, 25, 'https://images.unsplash.com/photo-1544441893-675973e31985?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'The North Face Weatherproof Jacket');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Sony WH-1000XM Wireless Headphones', 'Over-ear wireless noise-cancelling headphones with Bluetooth connectivity, long battery life and a comfortable padded headband.', 24990.00, 30, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sony WH-1000XM Wireless Headphones');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Apple AirPods Pro', 'True wireless earbuds with active noise cancellation, transparency mode and a wireless charging case.', 24900.00, 35, 'https://images.unsplash.com/photo-1600294037681-c80b4cb5b434?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Apple AirPods Pro');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Logitech MX Master 3S Mouse', 'Ergonomic wireless performance mouse with precision scroll wheel, quiet clicks and multi-device Bluetooth pairing.', 9995.00, 40, 'https://images.unsplash.com/photo-1615663245857-ac93bb7c39e7?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Logitech MX Master 3S Mouse');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Keychron K2 Mechanical Keyboard', 'Compact 75% wireless mechanical keyboard with Bluetooth multi-device support and backlit keys.', 8999.00, 35, 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Keychron K2 Mechanical Keyboard');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Fossil Leather Chronograph Watch', 'Men''s chronograph wrist watch with stainless steel case and genuine leather strap.', 11995.00, 25, 'https://commons.wikimedia.org/wiki/Special:FilePath/Jacques_Lemans_quartz_chronograph_watch.jpg?width=800', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Fossil Leather Chronograph Watch');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Puma Suede Classic Sneakers', 'Iconic low-top suede sneakers with a cushioned insole and durable rubber outsole for everyday wear.', 5999.00, 40, 'https://images.unsplash.com/photo-1525966222134-fcfa99b8ae77?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Puma Suede Classic Sneakers');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Nike Sport Backpack', 'Durable everyday sports backpack with padded shoulder straps, a main compartment and front zip pocket.', 2995.00, 45, 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Sports & Fitness')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Nike Sport Backpack');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'JBL Flip 6 Bluetooth Speaker', 'Portable waterproof Bluetooth speaker with powerful sound and long playtime for outdoor use.', 9999.00, 35, 'https://images.unsplash.com/photo-1545454675-3531b543be5d?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'JBL Flip 6 Bluetooth Speaker');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Bialetti Moka Express', 'Classic stovetop aluminium moka pot that brews rich Italian-style coffee on gas or electric hobs.', 3200.00, 40, 'https://commons.wikimedia.org/wiki/Special:FilePath/Moka_Express_Bialetti.jpg?width=800', (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bialetti Moka Express');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Zojirushi Travel Mug', 'Vacuum-insulated stainless steel travel mug with a leak-resistant lid that keeps drinks hot or cold on the go.', 3499.00, 50, NULL, (SELECT id FROM categories WHERE name = 'Home & Kitchen')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Zojirushi Travel Mug');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Ray-Ban Classic Wayfarer', 'Iconic Wayfarer-style sunglasses with a bold acetate frame and UV-protective lenses.', 8590.00, 35, 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ray-Ban Classic Wayfarer');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Smart Fitness Tracker Watch', 'Smart fitness tracker watch with heart-rate monitoring, step counting and sleep tracking.', 3999.00, 50, 'https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Smart Fitness Tracker Watch');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Minimalist Analog Dial Watch', 'Minimalist analog dial wrist watch with a slim case and clean face design.', 2499.00, 45, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Minimalist Analog Dial Watch');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Studio Over-Ear Headphones', 'Wired over-ear studio headphones with padded ear cups for detailed monitoring and comfortable long sessions.', 5499.00, 40, 'https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Studio Over-Ear Headphones');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Classic Brown Leather Loafers', 'Classic brown leather slip-on loafers with a cushioned insole, ideal for formal and smart-casual outfits.', 3799.00, 40, 'https://commons.wikimedia.org/wiki/Special:FilePath/A_brown_Loafer_Shoes_from_TODS.jpg?width=800', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Classic Brown Leather Loafers');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Casual Knit Crewneck Sweater', 'Soft knit crewneck sweater with ribbed cuffs and hem for cool-weather layering.', 1999.00, 50, 'https://commons.wikimedia.org/wiki/Special:FilePath/Grey_knitted_sweater.jpg?width=800', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Casual Knit Crewneck Sweater');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Polaroid Instant Camera', 'Retro-style instant film camera that prints photos within minutes of the shot.', 9499.00, 25, 'https://images.unsplash.com/photo-1526170375885-4d8ecf77b99f?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Electronics')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Polaroid Instant Camera');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Leather Crossbody Messenger Satchel', 'Genuine leather crossbody messenger satchel with an adjustable strap and flap closure for daily carry.', 3299.00, 35, 'https://commons.wikimedia.org/wiki/Special:FilePath/Brown_leather_messenger_bag_filled_with_notebooks_on_a_coffee_table_with_couch_and_pillows_in_the_background_%2826212677253%29.jpg?width=800', (SELECT id FROM categories WHERE name = 'Fashion')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Leather Crossbody Messenger Satchel');

INSERT INTO products (name, description, price, stock, image_url, category_id)
SELECT 'Men''s Sport Running Shoes', 'Lightweight men''s running shoes with a breathable mesh upper and cushioned sole for training and daily runs.', 4499.00, 45, 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', (SELECT id FROM categories WHERE name = 'Sports & Fitness')
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Men''s Sport Running Shoes');

