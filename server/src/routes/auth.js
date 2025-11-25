const express = require('express');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcrypt');
const db = require('../db/connection');
const config = require('../config');

const router = express.Router();

// Admin login
router.post('/login', (req, res) => {
  const { username, password } = req.body;
  if (
    username === config.adminUsername &&
    password === config.adminPassword
  ) {
    const token = jwt.sign(
      { username, role: 'admin' },
      config.jwtSecret,
      { expiresIn: '24h' }
    );
    return res.json({ token });
  }
  return res.status(401).json({ message: 'Invalid credentials' });
});

// User registration
router.post('/register', async (req, res) => {
  try {
    const { firstName, lastName, gender, email, password, confirmPassword } = req.body;

    if (!firstName || !lastName || !email || !password || !confirmPassword) {
      return res.status(400).json({ message: 'All fields are required' });
    }

    if (password !== confirmPassword) {
      return res.status(400).json({ message: 'Passwords do not match' });
    }

    if (password.length < 6) {
      return res.status(400).json({ message: 'Password must be at least 6 characters' });
    }

    // Check if user already exists
    const existing = db.prepare('SELECT id FROM app_users WHERE email = ?').get(email);
    if (existing) {
      return res.status(400).json({ message: 'Email already registered' });
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(password, 10);
    const fullName = `${firstName} ${lastName}`;

    // Insert user
    const stmt = db.prepare(
      'INSERT INTO app_users (firstName, lastName, fullName, gender, email, password) VALUES (?, ?, ?, ?, ?, ?)'
    );
    const result = stmt.run(firstName, lastName, fullName, gender || '', email, hashedPassword);

    // Generate token
    const token = jwt.sign(
      { userId: result.lastInsertRowid, email, role: 'user' },
      config.jwtSecret,
      { expiresIn: '30d' }
    );

    res.status(201).json({
      token,
      user: {
        id: result.lastInsertRowid,
        firstName,
        lastName,
        fullName,
        email,
        gender
      }
    });
  } catch (error) {
    console.error('Registration error:', error);
    res.status(500).json({ message: 'Registration failed' });
  }
});

// User login
router.post('/user-login', async (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ message: 'Email and password are required' });
    }

    const user = db.prepare('SELECT * FROM app_users WHERE email = ?').get(email);
    if (!user) {
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    const isValid = await bcrypt.compare(password, user.password);
    if (!isValid) {
      return res.status(401).json({ message: 'Invalid email or password' });
    }

    const token = jwt.sign(
      { userId: user.id, email: user.email, role: 'user' },
      config.jwtSecret,
      { expiresIn: '30d' }
    );

    res.json({
      token,
      user: {
        id: user.id,
        firstName: user.firstName,
        lastName: user.lastName,
        fullName: user.fullName,
        email: user.email,
        gender: user.gender
      }
    });
  } catch (error) {
    console.error('Login error:', error);
    res.status(500).json({ message: 'Login failed' });
  }
});

module.exports = router;

