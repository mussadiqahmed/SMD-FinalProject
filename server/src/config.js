const path = require('path');
const dotenv = require('dotenv');

dotenv.config();

const port = process.env.PORT || 8003;

// For production, ensure database is in a writable location
const getDbPath = () => {
  if (process.env.DB_PATH) {
    return process.env.DB_PATH;
  }
  // In production (Render), use a local path that will be created
  if (process.env.NODE_ENV === 'production') {
    return path.join(__dirname, '..', 'data', 'ecommerce.db');
  }
  // Development: use the shared data directory
  return path.join(__dirname, '..', '..', 'data', 'ecommerce.db');
};

const config = {
  port: port,
  dbPath: getDbPath(),
  adminUsername: process.env.ADMIN_USERNAME || 'admin',
  adminPassword: process.env.ADMIN_PASSWORD || 'changeme',
  jwtSecret: process.env.JWT_SECRET || 'supersecretkey',
  clientOrigin: process.env.CLIENT_ORIGIN || 'http://localhost:8002',
  baseUrl: process.env.BASE_URL || `http://localhost:${port}`
};

module.exports = config;

