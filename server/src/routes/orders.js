const express = require('express');
const db = require('../db/connection');
const authenticate = require('../middleware/auth');

const router = express.Router();

// Create order (no auth required - app users can create orders)
router.post('/', (req, res) => {
  try {
    const { customerName, phone, addressLine, city, total } = req.body;
    
    if (!customerName || !phone || !addressLine || !city || total === undefined) {
      return res.status(400).json({ message: 'All fields are required' });
    }

    // Default status is 'processing'
    const status = 'processing';
    const createdAt = Math.floor(Date.now() / 1000); // Unix timestamp in seconds

    const stmt = db.prepare(`
      INSERT INTO orders (customerName, phone, addressLine, city, total, status, createdAt)
      VALUES (?, ?, ?, ?, ?, ?, ?)
    `);
    
    const result = stmt.run(customerName, phone, addressLine, city, total, status, createdAt);
    const order = db.prepare('SELECT * FROM orders WHERE id = ?').get(result.lastInsertRowid);
    res.status(201).json(order);
  } catch (error) {
    console.error('Error creating order:', error);
    res.status(500).json({ message: 'Unable to create order' });
  }
});

// Get orders for app users (by customer name and phone, no auth required)
router.get('/customer', (req, res) => {
  try {
    const { customerName, phone } = req.query;
    
    if (!customerName || !phone) {
      return res.status(400).json({ message: 'Customer name and phone are required' });
    }

    const orders = db.prepare(`
      SELECT id, customerName, phone, addressLine, city, total, status, createdAt
      FROM orders
      WHERE customerName = ? AND phone = ?
      ORDER BY createdAt DESC
    `).all(customerName, phone);
    res.json(orders);
  } catch (error) {
    console.error('Error fetching customer orders:', error);
    res.status(500).json({ message: 'Unable to load orders' });
  }
});

// Get all orders (admin only, requires auth)
router.get('/', authenticate, (req, res) => {
  try {
    const orders = db.prepare(`
      SELECT id, customerName, phone, addressLine, city, total, status, createdAt
      FROM orders
      ORDER BY createdAt DESC
    `).all();
    res.json(orders);
  } catch (error) {
    console.error('Error fetching orders:', error);
    res.status(500).json({ message: 'Unable to load orders' });
  }
});

router.get('/:id', authenticate, (req, res) => {
  try {
    const order = db.prepare('SELECT * FROM orders WHERE id = ?').get(req.params.id);
    if (!order) {
      return res.status(404).json({ message: 'Order not found' });
    }
    res.json(order);
  } catch (error) {
    console.error('Error fetching order:', error);
    res.status(500).json({ message: 'Unable to load order' });
  }
});

router.put('/:id/status', authenticate, (req, res) => {
  try {
    const { status } = req.body;
    if (!status) {
      return res.status(400).json({ message: 'Status is required' });
    }
    const existing = db.prepare('SELECT * FROM orders WHERE id = ?').get(req.params.id);
    if (!existing) {
      return res.status(404).json({ message: 'Order not found' });
    }
    db.prepare('UPDATE orders SET status = ? WHERE id = ?').run(status, req.params.id);
    const updated = db.prepare('SELECT * FROM orders WHERE id = ?').get(req.params.id);
    res.json(updated);
  } catch (error) {
    console.error('Error updating order status:', error);
    res.status(500).json({ message: 'Unable to update order status' });
  }
});

router.delete('/:id', authenticate, (req, res) => {
  try {
    const orderId = req.params.id;
    const existing = db.prepare('SELECT * FROM orders WHERE id = ?').get(orderId);
    if (!existing) {
      return res.status(404).json({ message: 'Order not found' });
    }
    const result = db.prepare('DELETE FROM orders WHERE id = ?').run(orderId);
    if (result.changes === 0) {
      return res.status(404).json({ message: 'Order not found' });
    }
    res.status(204).send();
  } catch (error) {
    console.error('Error deleting order:', error);
    res.status(500).json({ message: 'Unable to delete order' });
  }
});

// Clear all orders (admin only)
router.delete('/', authenticate, (req, res) => {
  try {
    const result = db.prepare('DELETE FROM orders').run();
    res.json({ message: `Deleted ${result.changes} orders` });
  } catch (error) {
    console.error('Error clearing all orders:', error);
    res.status(500).json({ message: 'Unable to clear orders' });
  }
});

module.exports = router;

