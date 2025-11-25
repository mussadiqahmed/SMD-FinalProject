const db = require('./db/connection');

const baseCategories = [
  {
    id: 1,
    title: `Men's Shirts`,
    slug: 'mens',
    imageUrl: 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab'
  },
  {
    id: 2,
    title: `Women's Shirts`,
    slug: 'womens',
    imageUrl: 'https://images.unsplash.com/photo-1441986300917-64674bd600d8'
  },
  {
    id: 3,
    title: 'Kids Shirts',
    slug: 'kids',
    imageUrl: 'https://images.unsplash.com/photo-1503919545889-aef636e10ad4'
  }
];

const migrationStatements = [
  `CREATE TABLE IF NOT EXISTS categories (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      title TEXT NOT NULL,
      slug TEXT UNIQUE,
      imageUrl TEXT,
      createdAt TEXT DEFAULT CURRENT_TIMESTAMP
  );`,
  `CREATE TABLE IF NOT EXISTS products (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      description TEXT,
      price REAL NOT NULL,
      discountPercent REAL DEFAULT 0,
      imageUrl TEXT,
      images TEXT,
      categoryId INTEGER NOT NULL,
      sizes TEXT,
      colors TEXT,
      stock INTEGER DEFAULT 0,
      featured INTEGER DEFAULT 0,
      createdAt TEXT DEFAULT CURRENT_TIMESTAMP,
      updatedAt TEXT DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (categoryId) REFERENCES categories(id) ON DELETE SET NULL
  );`,
  `CREATE TRIGGER IF NOT EXISTS trigger_products_updated_at
      AFTER UPDATE ON products
      BEGIN
          UPDATE products SET updatedAt = CURRENT_TIMESTAMP WHERE id = NEW.id;
      END;`,
  `CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      fullName TEXT NOT NULL,
      email TEXT UNIQUE NOT NULL,
      phone TEXT,
      avatarUrl TEXT,
      notes TEXT,
      createdAt TEXT DEFAULT CURRENT_TIMESTAMP
  );`,
  `CREATE TABLE IF NOT EXISTS app_users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      firstName TEXT NOT NULL,
      lastName TEXT NOT NULL,
      fullName TEXT NOT NULL,
      gender TEXT,
      email TEXT UNIQUE NOT NULL,
      password TEXT NOT NULL,
      createdAt TEXT DEFAULT CURRENT_TIMESTAMP
  );`,
  `CREATE TABLE IF NOT EXISTS orders (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      customerName TEXT NOT NULL,
      phone TEXT NOT NULL,
      addressLine TEXT NOT NULL,
      city TEXT NOT NULL,
      total REAL NOT NULL,
      status TEXT NOT NULL,
      createdAt INTEGER NOT NULL DEFAULT (strftime('%s','now'))
  );`
];

function ensureColumn(table, column, definition) {
  const columns = db.prepare(`PRAGMA table_info(${table})`).all();
  if (!columns.some((col) => col.name === column)) {
    db.prepare(`ALTER TABLE ${table} ADD COLUMN ${column} ${definition};`).run();
  }
}

function ensureSchema() {
  ensureColumn('categories', 'slug', 'TEXT');
  ensureColumn('products', 'discountPercent', 'REAL DEFAULT 0');
  ensureColumn('products', 'images', 'TEXT');
  ensureColumn('products', 'stock', 'INTEGER DEFAULT 0');
}

function seedCategories() {
  const placeholders = baseCategories.map(() => '?').join(',');
  db.prepare(`DELETE FROM categories WHERE slug NOT IN (${placeholders})`).run(...baseCategories.map((c) => c.slug));
  const stmt = db.prepare(`
    INSERT INTO categories (id, title, slug, imageUrl)
    VALUES (@id, @title, @slug, @imageUrl)
    ON CONFLICT(id) DO UPDATE SET title = excluded.title,
                                   slug = excluded.slug,
                                   imageUrl = excluded.imageUrl;
  `);
  baseCategories.forEach((category) => stmt.run(category));
}

function runMigrations() {
  const transaction = db.transaction(() => {
    migrationStatements.forEach((statement) => db.prepare(statement).run());
    ensureSchema();
    seedCategories();
  });

  transaction();
  console.log('Database migrated successfully');
}

runMigrations();

