const express = require('express');
const db = require('../db/connection');
const authenticate = require('../middleware/auth');

const router = express.Router();

router.get('/', authenticate, (req, res) => {
  const users = db.prepare('SELECT * FROM users ORDER BY createdAt DESC').all();
  res.json(users);
});

router.post('/', authenticate, (req, res) => {
  const { fullName, email, phone, avatarUrl, notes } = req.body;
  if (!fullName || !email) {
    return res.status(400).json({ message: 'Full name and email are required' });
  }
  try {
    const stmt = db.prepare(`INSERT INTO users (fullName, email, phone, avatarUrl, notes)
      VALUES (?, ?, ?, ?, ?)`); 
    const result = stmt.run(fullName, email, phone || '', avatarUrl || '', notes || '');
    const user = db.prepare('SELECT * FROM users WHERE id = ?').get(result.lastInsertRowid);
    res.status(201).json(user);
  } catch (error) {
    if (error.code === 'SQLITE_CONSTRAINT_UNIQUE') {
      return res.status(400).json({ message: 'Email already exists' });
    }
    throw error;
  }
});

router.put('/:id', authenticate, (req, res) => {
  const existing = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
  if (!existing) {
    return res.status(404).json({ message: 'User not found' });
  }
  const {
    fullName = existing.fullName,
    email = existing.email,
    phone = existing.phone,
    avatarUrl = existing.avatarUrl,
    notes = existing.notes
  } = req.body;

  const stmt = db.prepare(`UPDATE users SET
    fullName = ?, email = ?, phone = ?, avatarUrl = ?, notes = ?
    WHERE id = ?`);
  stmt.run(fullName, email, phone, avatarUrl, notes, req.params.id);
  const updated = db.prepare('SELECT * FROM users WHERE id = ?').get(req.params.id);
  res.json(updated);
});

router.delete('/:id', authenticate, (req, res) => {
  const stmt = db.prepare('DELETE FROM users WHERE id = ?');
  const result = stmt.run(req.params.id);
  if (result.changes === 0) {
    return res.status(404).json({ message: 'User not found' });
  }
  res.status(204).send();
});

module.exports = router;

