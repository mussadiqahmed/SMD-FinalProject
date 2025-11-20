const express = require('express');
const db = require('../db/connection');

const router = express.Router();

router.get('/', (req, res) => {
  try {
    const productCount = db.prepare('SELECT COUNT(*) as count FROM products').get().count || 0;
    const userCount = db.prepare('SELECT COUNT(*) as count FROM users').get().count || 0;
    const orderCount = db.prepare('SELECT COUNT(*) as count FROM orders').get().count || 0;

    const recentProducts = db
      .prepare(
        `SELECT id, name, price, createdAt
         FROM products
         ORDER BY datetime(createdAt) DESC
         LIMIT 5`
      )
      .all();

    const recentUsers = db
      .prepare(
        `SELECT id, fullName, email, createdAt
         FROM users
         ORDER BY datetime(createdAt) DESC
         LIMIT 5`
      )
      .all();

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
