const express = require('express');
const db = require('../db/connection');
const authenticate = require('../middleware/auth');
const upload = require('../middleware/upload');
const config = require('../config');
const { toStoredList, fromStoredList } = require('../utils/serializers');

const router = express.Router();

const parseImages = (value) => {
  if (!value) return [];
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed.filter(Boolean) : [];
  } catch (error) {
    return value ? [value] : [];
  }
};

function mapProduct(row) {
  return {
    id: row.id,
    name: row.name,
    description: row.description,
    price: row.price,
    discountPercent: row.discountPercent || 0,
    imageUrl: row.imageUrl,
    images: parseImages(row.images),
    categoryId: row.categoryId,
    category: row.categoryTitle
      ? { id: row.categoryId, title: row.categoryTitle, slug: row.categorySlug }
      : null,
    sizes: fromStoredList(row.sizes),
    colors: fromStoredList(row.colors),
    stock: row.stock || 0,
    featured: !!row.featured,
    createdAt: row.createdAt,
    updatedAt: row.updatedAt
  };
}

function buildList(value) {
  if (!value) return [];
  if (Array.isArray(value)) return value;
  if (typeof value === 'string') {
    return value
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  }
  return [];
}

router.get('/', (req, res) => {
  const { categoryId, categorySlug, search, featured } = req.query;
  let query = `
    SELECT p.*, c.title as categoryTitle, c.slug as categorySlug
    FROM products p
    LEFT JOIN categories c ON c.id = p.categoryId
  `;
  const filters = [];
  const params = [];

  if (categoryId) {
    filters.push('p.categoryId = ?');
    params.push(Number(categoryId));
  }
  if (categorySlug) {
    filters.push('c.slug = ?');
    params.push(categorySlug);
  }
  if (featured === 'true') {
    filters.push('p.featured = 1');
  }
  if (filters.length) {
    query += ` WHERE ${filters.join(' AND ')}`;
  }
  query += ' ORDER BY datetime(p.createdAt) DESC';

  const records = db.prepare(query).all(...params).map(mapProduct);
  if (search) {
    const keyword = search.toLowerCase();
    return res.json(
      records.filter(
        (item) =>
          item.name.toLowerCase().includes(keyword) ||
          (item.description || '').toLowerCase().includes(keyword)
      )
    );
  }
  res.json(records);
});

router.get('/:id', (req, res) => {
  const product = db
    .prepare(
      `SELECT p.*, c.title as categoryTitle, c.slug as categorySlug
       FROM products p
       LEFT JOIN categories c ON c.id = p.categoryId
       WHERE p.id = ?`
    )
    .get(req.params.id);
  if (!product) {
    return res.status(404).json({ message: 'Product not found' });
  }
  res.json(mapProduct(product));
});

router.post('/', authenticate, upload.array('imageFiles', 3), (req, res) => {
  try {
    let imageUrls = [];
    try {
      if (req.body.imageUrls) {
        imageUrls = typeof req.body.imageUrls === 'string' 
          ? JSON.parse(req.body.imageUrls) 
          : req.body.imageUrls;
      }
    } catch (e) {
      // If parsing fails, treat as empty array
      imageUrls = [];
    }

    const {
      name,
      description,
      price,
      categoryId,
      sizes,
      colors,
      featured,
      discountPercent,
      stock
    } = req.body;

    if (!name || price === undefined || !categoryId) {
      return res.status(400).json({ message: 'Name, price, and category are required' });
    }

    // Combine uploaded files with URL inputs
    const uploadedUrls = (req.files || []).map(
      (file) => `${config.baseUrl}/uploads/products/${file.filename}`
    );
    const urlList = buildList(imageUrls);
    const normalizedImages = [...uploadedUrls, ...urlList].slice(0, 3);

    if (!normalizedImages.length) {
      return res.status(400).json({ message: 'At least one product image is required' });
    }

    const stmt = db.prepare(`INSERT INTO products
      (name, description, price, discountPercent, imageUrl, images, categoryId, sizes, colors, stock, featured)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`
    );

    const result = stmt.run(
      name,
      description || '',
      Number(price),
      Number(discountPercent) || 0,
      normalizedImages[0],
      JSON.stringify(normalizedImages),
      Number(categoryId),
      toStoredList(buildList(sizes)),
      toStoredList(buildList(colors)),
      Number.isFinite(Number(stock)) ? Number(stock) : 0,
      featured ? 1 : 0
    );

    const product = db
      .prepare(
        `SELECT p.*, c.title as categoryTitle, c.slug as categorySlug
         FROM products p
         LEFT JOIN categories c ON c.id = p.categoryId
         WHERE p.id = ?`
      )
      .get(result.lastInsertRowid);
    res.status(201).json(mapProduct(product));
  } catch (error) {
    console.error('Error creating product:', error);
    res.status(500).json({ message: error.message || 'Failed to create product' });
  }
});

router.put('/:id', authenticate, upload.array('imageFiles', 3), (req, res) => {
  try {
    const existing = db.prepare('SELECT * FROM products WHERE id = ?').get(req.params.id);
    if (!existing) {
      return res.status(404).json({ message: 'Product not found' });
    }

    let imageUrls = parseImages(existing.images);
    try {
      if (req.body.imageUrls) {
        imageUrls = typeof req.body.imageUrls === 'string' 
          ? JSON.parse(req.body.imageUrls) 
          : req.body.imageUrls;
      }
    } catch (e) {
      // Keep existing images if parsing fails
    }

    const {
      name = existing.name,
      description = existing.description,
      price = existing.price,
      categoryId = existing.categoryId,
      sizes,
      colors,
      featured = existing.featured,
      discountPercent = existing.discountPercent,
      stock = existing.stock
    } = req.body;

    // Combine uploaded files with URL inputs
    const uploadedUrls = (req.files || []).map(
      (file) => `${config.baseUrl}/uploads/products/${file.filename}`
    );
    const urlList = buildList(imageUrls);
    const normalizedImages = [...uploadedUrls, ...urlList].slice(0, 3);
    
    if (!normalizedImages.length) {
      normalizedImages.push(existing.imageUrl || '');
    }

    const stmt = db.prepare(`UPDATE products SET
      name = ?, description = ?, price = ?, discountPercent = ?, imageUrl = ?, images = ?, categoryId = ?, sizes = ?, colors = ?, stock = ?, featured = ?
      WHERE id = ?`);

    stmt.run(
      name,
      description,
      Number(price),
      Number(discountPercent) || 0,
      normalizedImages[0],
      JSON.stringify(normalizedImages),
      Number(categoryId),
      sizes ? toStoredList(buildList(sizes)) : existing.sizes,
      colors ? toStoredList(buildList(colors)) : existing.colors,
      Number.isFinite(Number(stock)) ? Number(stock) : existing.stock,
      featured ? 1 : 0,
      req.params.id
    );

    const product = db
      .prepare(
        `SELECT p.*, c.title as categoryTitle, c.slug as categorySlug
         FROM products p
         LEFT JOIN categories c ON c.id = p.categoryId
         WHERE p.id = ?`
      )
      .get(req.params.id);
    res.json(mapProduct(product));
  } catch (error) {
    console.error('Error updating product:', error);
    res.status(500).json({ message: error.message || 'Failed to update product' });
  }
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

