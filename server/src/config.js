const path = require('path');
const dotenv = require('dotenv');

dotenv.config();

const config = {
  port: process.env.PORT || 8001,
  dbPath: process.env.DB_PATH || path.join(__dirname, '..', '..', 'data', 'ecommerce.db'),
  adminUsername: process.env.ADMIN_USERNAME || 'admin',
  adminPassword: process.env.ADMIN_PASSWORD || 'changeme',
  jwtSecret: process.env.JWT_SECRET || 'supersecretkey',
  clientOrigin: process.env.CLIENT_ORIGIN || 'http://localhost:8000',
  baseUrl: process.env.BASE_URL || 'http://localhost:8001'
};

module.exports = config;

