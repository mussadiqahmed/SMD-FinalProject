const express = require('express');
const db = require('../db/connection');
const authenticate = require('../middleware/auth');
const { toStoredList, fromStoredList } = require('../utils/serializers');

const router = express.Router();

function mapProduct(row) {
  return {
    ...row,
    sizes: fromStoredList(row.sizes),
    colors: fromStoredList(row.colors),
    featured: !!row.featured
  };
}

router.get('/', (req, res) => {
  const { categoryId, search, featured } = req.query;
  let query = 'SELECT * FROM products';
  const filters = [];
  const params = [];

  if (categoryId) {
    filters.push('categoryId = ?');
    params.push(categoryId);
  }
  if (featured === 'true') {
    filters.push('featured = 1');
  }
  if (filters.length) {
    query += ` WHERE ${filters.join(' AND ')}`;
  }
  query += ' ORDER BY createdAt DESC';

  const products = db.prepare(query).all(...params).map(mapProduct);
  if (search) {
    const keyword = search.toLowerCase();
    return res.json(
      products.filter(
        (item) =>
          item.name.toLowerCase().includes(keyword) ||
          (item.description || '').toLowerCase().includes(keyword)
      )
    );
  }
  res.json(products);
});

router.get('/:id', (req, res) => {
  const product = db.prepare('SELECT * FROM products WHERE id = ?').get(req.params.id);
  if (!product) {
    return res.status(404).json({ message: 'Product not found' });
  }
  res.json(mapProduct(product));
});

router.post('/', authenticate, (req, res) => {
  const {
    name,
    description,
    price,
    imageUrl,
    categoryId,
    sizes,
    colors,
    featured
  } = req.body;

  if (!name || price === undefined) {
    return res.status(400).json({ message: 'Name and price are required' });
  }

  const stmt = db.prepare(`INSERT INTO products
    (name, description, price, imageUrl, categoryId, sizes, colors, featured)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)`
  );

  const result = stmt.run(
    name,
    description || '',
    Number(price),
    imageUrl || '',
    categoryId || null,
    toStoredList(sizes),
    toStoredList(colors),
    featured ? 1 : 0
  );

  const product = db.prepare('SELECT * FROM products WHERE id = ?').get(result.lastInsertRowid);
  res.status(201).json(mapProduct(product));
});

router.put('/:id', authenticate, (req, res) => {
  const existing = db.prepare('SELECT * FROM products WHERE id = ?').get(req.params.id);
  if (!existing) {
    return res.status(404).json({ message: 'Product not found' });
  }
  const {
    name = existing.name,
    description = existing.description,
    price = existing.price,
    imageUrl = existing.imageUrl,
    categoryId = existing.categoryId,
    sizes,
    colors,
    featured = existing.featured
  } = req.body;

  const stmt = db.prepare(`UPDATE products SET
    name = ?, description = ?, price = ?, imageUrl = ?, categoryId = ?, sizes = ?, colors = ?, featured = ?
    WHERE id = ?`);

  stmt.run(
    name,
    description,
    Number(price),
    imageUrl,
    categoryId,
    sizes ? toStoredList(sizes) : existing.sizes,
    colors ? toStoredList(colors) : existing.colors,
    featured ? 1 : 0,
    req.params.id
  );

  const product = db.prepare('SELECT * FROM products WHERE id = ?').get(req.params.id);
  res.json(mapProduct(product));
});

router.delete('/:id', authenticate, (req, res) => {
  const stmt = db.prepare('DELETE FROM products WHERE id = ?');
  const result = stmt.run(req.params.id);
  if (result.changes === 0) {
    return res.status(404).json({ message: 'Product not found' });
  }
  res.status(204).send();
});

module.exports = router;

