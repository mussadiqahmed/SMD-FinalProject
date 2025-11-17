const express = require('express');
const db = require('../db/connection');
const authenticate = require('../middleware/auth');

const router = express.Router();

router.get('/', (req, res) => {
  const categories = db.prepare('SELECT * FROM categories ORDER BY title ASC').all();
  res.json(categories);
});

router.get('/:id', (req, res) => {
  const category = db.prepare('SELECT * FROM categories WHERE id = ?').get(req.params.id);
  if (!category) {
    return res.status(404).json({ message: 'Category not found' });
  }
  res.json(category);
});

router.post('/', authenticate, (req, res) => {
  const { title, imageUrl } = req.body;
  if (!title) {
    return res.status(400).json({ message: 'Title is required' });
  }
  const stmt = db.prepare('INSERT INTO categories (title, imageUrl) VALUES (?, ?)');
  const result = stmt.run(title, imageUrl || null);
  const category = db.prepare('SELECT * FROM categories WHERE id = ?').get(result.lastInsertRowid);
  res.status(201).json(category);
});

router.put('/:id', authenticate, (req, res) => {
  const { title, imageUrl } = req.body;
  const existing = db.prepare('SELECT * FROM categories WHERE id = ?').get(req.params.id);
  if (!existing) {
    return res.status(404).json({ message: 'Category not found' });
  }
  const stmt = db.prepare('UPDATE categories SET title = ?, imageUrl = ? WHERE id = ?');
  stmt.run(title || existing.title, imageUrl ?? existing.imageUrl, req.params.id);
  const updated = db.prepare('SELECT * FROM categories WHERE id = ?').get(req.params.id);
  res.json(updated);
});

router.delete('/:id', authenticate, (req, res) => {
  const stmt = db.prepare('DELETE FROM categories WHERE id = ?');
  const result = stmt.run(req.params.id);
  if (result.changes === 0) {
    return res.status(404).json({ message: 'Category not found' });
  }
  res.status(204).send();
});

module.exports = router;

