const express = require('express');
const bcrypt = require('bcrypt');
const db = require('../db/connection');
const authenticate = require('../middleware/auth');
const { validatePassword } = require('../utils/passwordValidator');

const router = express.Router();

router.get('/', authenticate, (req, res) => {
  // Get app users (customers who registered through the app)
  const appUsers = db.prepare('SELECT id, firstName, lastName, fullName, email, gender, createdAt FROM app_users ORDER BY createdAt DESC').all();
  // Get admin-created users
  const adminUsers = db.prepare('SELECT * FROM users ORDER BY createdAt DESC').all();
  // Combine both, marking app users
  const allUsers = [
    ...appUsers.map(u => ({ ...u, source: 'app' })),
    ...adminUsers.map(u => ({ ...u, source: 'admin' }))
  ];
  res.json(allUsers);
});

router.post('/', authenticate, async (req, res) => {
  const { fullName, email, phone, password, avatarUrl, notes } = req.body;
  if (!fullName || !email) {
    return res.status(400).json({ message: 'Full name and email are required' });
  }
  
  if (!password) {
    return res.status(400).json({ message: 'Password is required' });
  }

  // Validate password
  const passwordValidation = validatePassword(password);
  if (!passwordValidation.valid) {
    return res.status(400).json({ message: passwordValidation.message });
  }

  try {
    // Check if email already exists in app_users
    const existingAppUser = db.prepare('SELECT id FROM app_users WHERE email = ?').get(email);
    if (existingAppUser) {
      return res.status(400).json({ message: 'Email already exists' });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);

    // Insert into app_users table (since we need password)
    const stmt = db.prepare(`INSERT INTO app_users (firstName, lastName, fullName, email, password)
      VALUES (?, ?, ?, ?, ?)`);
    
    // Split fullName into firstName and lastName
    const nameParts = fullName.trim().split(/\s+/);
    const firstName = nameParts[0] || '';
    const lastName = nameParts.slice(1).join(' ') || '';
    
    const result = stmt.run(firstName, lastName, fullName, email, hashedPassword);
    const user = db.prepare('SELECT id, firstName, lastName, fullName, email, gender, createdAt FROM app_users WHERE id = ?').get(result.lastInsertRowid);
    res.status(201).json({ ...user, source: 'admin' });
  } catch (error) {
    if (error.code === 'SQLITE_CONSTRAINT_UNIQUE') {
      return res.status(400).json({ message: 'Email already exists' });
    }
    console.error('Error creating user:', error);
    res.status(500).json({ message: 'Unable to create user' });
  }
});

router.put('/:id', authenticate, async (req, res) => {
  const userId = req.params.id;
  const { password } = req.body;
  
  // Validate password if provided
  if (password) {
    const passwordValidation = validatePassword(password);
    if (!passwordValidation.valid) {
      return res.status(400).json({ message: passwordValidation.message });
    }
  }
  
  // First check if user exists in app_users table
  const appUser = db.prepare('SELECT * FROM app_users WHERE id = ?').get(userId);
  if (appUser) {
    const {
      fullName = appUser.fullName,
      email = appUser.email,
      gender = appUser.gender || ''
    } = req.body;

    // Split fullName into firstName and lastName for app_users table
    const nameParts = fullName.trim().split(/\s+/);
    const firstName = nameParts[0] || '';
    const lastName = nameParts.slice(1).join(' ') || '';

    // Update password if provided
    if (password) {
      const hashedPassword = await bcrypt.hash(password, 10);
      const stmt = db.prepare(`UPDATE app_users SET
        firstName = ?, lastName = ?, fullName = ?, email = ?, gender = ?, password = ?
        WHERE id = ?`);
      stmt.run(firstName, lastName, fullName, email, gender, hashedPassword, userId);
    } else {
      const stmt = db.prepare(`UPDATE app_users SET
        firstName = ?, lastName = ?, fullName = ?, email = ?, gender = ?
        WHERE id = ?`);
      stmt.run(firstName, lastName, fullName, email, gender, userId);
    }
    
    const updated = db.prepare('SELECT id, firstName, lastName, fullName, email, gender, createdAt FROM app_users WHERE id = ?').get(userId);
    return res.json({ ...updated, source: 'app' });
  }
  
  // If not in app_users, check users table
  const adminUser = db.prepare('SELECT * FROM users WHERE id = ?').get(userId);
  if (adminUser) {
    const {
      fullName = adminUser.fullName,
      email = adminUser.email,
      phone = adminUser.phone,
      avatarUrl = adminUser.avatarUrl,
      notes = adminUser.notes
    } = req.body;

    // Note: users table doesn't have password field, so we skip password update for admin-created users
    const stmt = db.prepare(`UPDATE users SET
      fullName = ?, email = ?, phone = ?, avatarUrl = ?, notes = ?
      WHERE id = ?`);
    stmt.run(fullName, email, phone, avatarUrl, notes, userId);
    const updated = db.prepare('SELECT * FROM users WHERE id = ?').get(userId);
    return res.json({ ...updated, source: 'admin' });
  }
  
  // User not found in either table
  return res.status(404).json({ message: 'User not found' });
});

router.delete('/:id', authenticate, (req, res) => {
  const userId = req.params.id;
  
  // First check if user exists in app_users table
  const appUser = db.prepare('SELECT id, fullName, email FROM app_users WHERE id = ?').get(userId);
  if (appUser) {
    // Delete all orders associated with this user (by customerName and email/phone)
    // Note: Orders use customerName and phone, so we'll match by name and email
    // Since we don't have phone in app_users, we'll match by customerName
    db.prepare('DELETE FROM orders WHERE customerName = ?').run(appUser.fullName);
    
    // Delete the user
    const stmt = db.prepare('DELETE FROM app_users WHERE id = ?');
    const result = stmt.run(userId);
    if (result.changes === 0) {
      return res.status(404).json({ message: 'User not found' });
    }
    return res.status(204).send();
  }
  
  // If not in app_users, check users table
  const adminUser = db.prepare('SELECT id, fullName, email, phone FROM users WHERE id = ?').get(userId);
  if (adminUser) {
    // Delete all orders associated with this user
    // Try matching by customerName first
    db.prepare('DELETE FROM orders WHERE customerName = ?').run(adminUser.fullName);
    // Also try matching by phone if available
    if (adminUser.phone) {
      db.prepare('DELETE FROM orders WHERE phone = ?').run(adminUser.phone);
    }
    
    // Delete the user
    const stmt = db.prepare('DELETE FROM users WHERE id = ?');
    const result = stmt.run(userId);
    if (result.changes === 0) {
      return res.status(404).json({ message: 'User not found' });
    }
    return res.status(204).send();
  }
  
  // User not found in either table
  return res.status(404).json({ message: 'User not found' });
});

module.exports = router;

