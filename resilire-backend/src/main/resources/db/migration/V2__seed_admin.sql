-- Default admin account for local/dev use. Change this password after first login.
-- email: admin@resilire.com / password: Admin@123
INSERT INTO users (email, password, role, enabled)
VALUES ('admin@resilire.com', '$2b$10$b2L3aqAO6iTdUmWN/6qfoONbOEBVJysIcTKd2.v7qkLB5erxn5b5W', 'ADMIN', TRUE);
