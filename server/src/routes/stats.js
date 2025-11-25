const express = require('express');
const db = require('../db/connection');

const router = express.Router();

router.get('/', (req, res) => {
  try {
    const productCount = db.prepare('SELECT COUNT(*) as count FROM products').get().count || 0;
    
    // Count users from both app_users and users tables
    const appUserCount = db.prepare('SELECT COUNT(*) as count FROM app_users').get().count || 0;
    const adminUserCount = db.prepare('SELECT COUNT(*) as count FROM users').get().count || 0;
    const userCount = appUserCount + adminUserCount;
    
    const orderCount = db.prepare('SELECT COUNT(*) as count FROM orders').get().count || 0;

    const recentProducts = db
      .prepare(
        `SELECT id, name, price, createdAt
         FROM products
         ORDER BY datetime(createdAt) DESC
         LIMIT 5`
      )
      .all();

    // Get recent users from both tables and combine
    const recentAppUsers = db
      .prepare(
        `SELECT id, fullName, email, createdAt
         FROM app_users
         ORDER BY datetime(createdAt) DESC
         LIMIT 5`
      )
      .all();
    
    const recentAdminUsers = db
      .prepare(
        `SELECT id, fullName, email, createdAt
         FROM users
         ORDER BY datetime(createdAt) DESC
         LIMIT 5`
      )
      .all();
    
    // Combine and sort by date, then take top 5
    const allRecentUsers = [...recentAppUsers, ...recentAdminUsers]
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      .slice(0, 5);
    
    const recentUsers = allRecentUsers;

    const recentOrders = db
      .prepare(
        `SELECT id, customerName, total, status, createdAt
         FROM orders
         ORDER BY createdAt DESC
         LIMIT 5`
      )
      .all();

    res.json({
      totals: {
        products: productCount,
        users: userCount,
        orders: orderCount
      },
      highlights: {
        recentProducts,
        recentUsers,
        recentOrders
      }
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: 'Unable to load stats' });
  }
});

module.exports = router;
