const db = require('./db/connection');

const migrationStatements = [
  `CREATE TABLE IF NOT EXISTS categories (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      title TEXT NOT NULL,
      imageUrl TEXT,
      createdAt TEXT DEFAULT CURRENT_TIMESTAMP
  );`,
  `CREATE TABLE IF NOT EXISTS products (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      description TEXT,
      price REAL NOT NULL,
      imageUrl TEXT,
      categoryId INTEGER,
      sizes TEXT,
      colors TEXT,
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
  );`
];

function runMigrations() {
  const transaction = db.transaction(() => {
    migrationStatements.forEach((statement) => db.prepare(statement).run());
  });

  transaction();
  console.log('Database migrated successfully');
}

runMigrations();

