
INSERT INTO offer (title, category, description, cost_points, image_url, active) VALUES
('Festive 15% Off','Lifestyle','Seasonal sale voucher for lifestyle category',900,'https://images.unsplash.com/photo-1542831371-29b0f74f9713',true),
('Travel Cab ₹150 Off','Travel','Comfortable rides at a discount',200,'https://images.unsplash.com/photo-1503376780353-7e6692767b70',true),
('Sportswear 12% Off','Sports','Gear up for workouts',450,'https://images.unsplash.com/photo-1517649763962-0c623066013b',true),
('₹300 Grocery Wallet','Groceries','Stock your pantry with essentials.',350,'https://images.unsplash.com/photo-1511690743698-d9d85f2fbf38',true);

INSERT INTO fraud_alert (severity, message, ref_txn_id) VALUES
('HIGH','Transaction #1102 - Fraud attempt detected','1102'),
('MEDIUM','Transaction #1056 - Unusual location','1056'),
('LOW','Transaction #1023 - Minor anomaly','1023');

INSERT INTO anomaly (title, detail) VALUES
('Suspicious Amount','Unusually large transaction compared to history'),
('Location Mismatch','Card used in a new country'),
('Multiple rapid attempts','Repeated failed transactions in seconds');

INSERT INTO audit_log (user_name, action, date) VALUES
('John Doe','Login', '2025-12-20'),
('Jane Smith','Viewed Alerts', '2025-12-20'),
('John Doe','Exported Report', '2025-12-19');
