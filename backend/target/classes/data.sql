-- Clear existing test data first to avoid duplicates
DELETE FROM audit_logs;
DELETE FROM transaction_anomalies;
DELETE FROM transactions;
DELETE FROM alerts;

INSERT INTO offer (title, category, description, cost_points, image_url, active, tier_level) VALUES
('Festive 15% Off','Lifestyle','Seasonal sale voucher for lifestyle category',900,'https://images.unsplash.com/photo-1542831371-29b0f74f9713',true, NULL),
('Travel Cab ₹150 Off','Travel','Comfortable rides at a discount',200,'https://images.unsplash.com/photo-1503376780353-7e6692767b70',true, 'Bronze'),
('Sportswear 12% Off','Sports','Gear up for workouts',450,'https://images.unsplash.com/photo-1517649763962-0c623066013b',true, 'Silver'),
('₹300 Grocery Wallet','Groceries','Stock your pantry with essentials.',350,'https://images.unsplash.com/photo-1511690743698-d9d85f2fbf38',true, 'Bronze'),
('Premium Dining ₹500 Off','Dining','Exclusive dining experience',800,'https://images.unsplash.com/photo-1414235077428-338989a2e8c0',true, 'Gold'),
('Luxury Spa Package','Wellness','Relaxation and rejuvenation',1200,'https://images.unsplash.com/photo-1540555700478-4be289fbecef',true, 'Platinum'),
('Electronics 20% Off','Electronics','Latest gadgets at discounted prices',600,'https://images.unsplash.com/photo-1498049794561-7780e7231661',true, 'Silver');

-- Sample Alerts
INSERT INTO alerts (severity, status, title, description, created_at) VALUES
('HIGH', 'OPEN', 'Fraud attempt detected', 'Transaction #1102 shows suspicious activity patterns', NOW()),
('MEDIUM', 'OPEN', 'Unusual location', 'Transaction #1056 originated from an unexpected geographical location', NOW()),
('LOW', 'ACKNOWLEDGED', 'Minor anomaly', 'Transaction #1023 has a small deviation from normal patterns', NOW());

-- Sample Transaction Anomalies
INSERT INTO transaction_anomalies (transaction_id, account_id, anomaly_type, score, severity, flagged_reason, detected_at) VALUES
('TXN-1102', 'ACC-001', 'AMOUNT_SPIKE', 0.85, 'HIGH', 'Transaction amount is 3.5x higher than average', NOW()),
('TXN-1056', 'ACC-002', 'GEO_MISMATCH', 0.72, 'MEDIUM', 'Card used in a different country within 2 hours', NOW()),
('TXN-1023', 'ACC-003', 'VELOCITY', 0.45, 'LOW', 'Multiple transactions in rapid succession', NOW()),
('TXN-1104', 'ACC-001', 'HIGH_VALUE', 0.90, 'CRITICAL', 'Single transaction exceeds $50,000 threshold', NOW());

-- Sample Audit Logs
INSERT INTO audit_logs (user_id, username, action, entity_type, entity_id, details, created_at) VALUES
('1', 'admin', 'LOGIN', 'USER', '1', 'Admin user logged in successfully', NOW()),
('1', 'admin', 'ALERT_VIEW', 'ALERT', NULL, 'Viewed fraud alerts dashboard', NOW()),
('1', 'admin', 'EXPORT', 'REPORT', 'audit-2025-12', 'Exported audit report for December 2025', NOW()),
('2', 'john.doe', 'TX_REVIEW', 'TRANSACTION', 'TXN-1102', 'Reviewed suspicious transaction', NOW());

-- Sample Transactions for Fraud Monitoring
INSERT INTO transactions (transaction_id, account_id, amount, currency, merchant_name, merchant_category, payment_method, location, risk_level, status, created_at) VALUES
('TXN-20001', 'ACC-001', 129.99, 'USD', 'Amazon', 'ELECTRONICS', 'CARD', 'New York, US', 'LOW', 'CLEARED', NOW()),
('TXN-20002', 'ACC-002', 7.25, 'USD', 'Starbucks', 'FOOD', 'CARD', 'Seattle, US', 'LOW', 'CLEARED', NOW()),
('TXN-20003', 'ACC-003', 545.00, 'EUR', 'eBay', 'ELECTRONICS', 'CARD', 'Berlin, DE', 'MEDIUM', 'CLEARED', NOW()),
('TXN-20004', 'ACC-004', 3200.00, 'USD', 'CryptoEx', 'FINANCIAL', 'CARD', 'Singapore, SG', 'HIGH', 'CLEARED', NOW()),
('TXN-1102', 'ACC-001', 8500.00, 'USD', 'Luxury Goods Inc', 'LUXURY', 'CARD', 'Dubai, AE', 'CRITICAL', 'REVIEW', NOW()),
('TXN-1056', 'ACC-002', 450.00, 'GBP', 'London Electronics', 'ELECTRONICS', 'CARD', 'London, UK', 'HIGH', 'REVIEW', NOW()),
('TXN-1023', 'ACC-003', 89.99, 'USD', 'Gas Station', 'FUEL', 'CARD', 'Los Angeles, US', 'MEDIUM', 'CLEARED', NOW()),
('TXN-1104', 'ACC-001', 52000.00, 'USD', 'Jewelry Store', 'LUXURY', 'CARD', 'Paris, FR', 'CRITICAL', 'BLOCKED', NOW());
