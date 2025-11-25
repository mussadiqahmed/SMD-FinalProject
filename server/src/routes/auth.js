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

    // Validate password strength
    const { validatePassword } = require('../utils/passwordValidator');
    const passwordValidation = validatePassword(password);
    if (!passwordValidation.valid) {
      return res.status(400).json({ message: passwordValidation.message });
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

// Get current user info (requires authentication)
router.get('/me', (req, res) => {
  try {
    // Get token from Authorization header
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ message: 'No token provided' });
    }

    const token = authHeader.substring(7);
    const decoded = jwt.verify(token, config.jwtSecret);

    // Fetch user from database
    const user = db.prepare('SELECT id, firstName, lastName, fullName, email, gender, createdAt FROM app_users WHERE id = ?').get(decoded.userId);
    
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    res.json({
      id: user.id,
      firstName: user.firstName,
      lastName: user.lastName,
      fullName: user.fullName,
      email: user.email,
      gender: user.gender,
      createdAt: user.createdAt
    });
  } catch (error) {
    if (error.name === 'JsonWebTokenError' || error.name === 'TokenExpiredError') {
      return res.status(401).json({ message: 'Invalid or expired token' });
    }
    console.error('Get user error:', error);
    res.status(500).json({ message: 'Unable to fetch user info' });
  }
});

// Change password (requires authentication and current password)
router.post('/change-password', async (req, res) => {
  try {
    // Get token from Authorization header
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ message: 'No token provided' });
    }

    const token = authHeader.substring(7);
    const decoded = jwt.verify(token, config.jwtSecret);

    const { currentPassword, newPassword, confirmPassword } = req.body;

    if (!currentPassword || !newPassword || !confirmPassword) {
      return res.status(400).json({ message: 'All password fields are required' });
    }

    if (newPassword !== confirmPassword) {
      return res.status(400).json({ message: 'New passwords do not match' });
    }

    // Validate new password strength
    const { validatePassword } = require('../utils/passwordValidator');
    const passwordValidation = validatePassword(newPassword);
    if (!passwordValidation.valid) {
      return res.status(400).json({ message: passwordValidation.message });
    }

    // Fetch user from database
    const user = db.prepare('SELECT * FROM app_users WHERE id = ?').get(decoded.userId);
    
    if (!user) {
      return res.status(404).json({ message: 'User not found' });
    }

    // Verify current password
    const isValid = await bcrypt.compare(currentPassword, user.password);
    if (!isValid) {
      return res.status(401).json({ message: 'Current password is incorrect' });
    }

    // Hash and update new password
    const hashedPassword = await bcrypt.hash(newPassword, 10);
    db.prepare('UPDATE app_users SET password = ? WHERE id = ?').run(hashedPassword, decoded.userId);

    res.json({ message: 'Password changed successfully' });
  } catch (error) {
    if (error.name === 'JsonWebTokenError' || error.name === 'TokenExpiredError') {
      return res.status(401).json({ message: 'Invalid or expired token' });
    }
    console.error('Change password error:', error);
    res.status(500).json({ message: 'Unable to change password' });
  }
});

// Update user profile (name and email)
router.put('/profile', async (req, res) => {
  try {
    // Get token from Authorization header
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      return res.status(401).json({ message: 'No token provided' });
    }

    const token = authHeader.substring(7);
    const decoded = jwt.verify(token, config.jwtSecret);

    const { fullName, email } = req.body;

    if (!fullName || !email) {
      return res.status(400).json({ message: 'Full name and email are required' });
    }

    // Split fullName into firstName and lastName
    const nameParts = fullName.trim().split(/\s+/);
    const firstName = nameParts[0] || '';
    const lastName = nameParts.slice(1).join(' ') || '';

    // Update user
    db.prepare('UPDATE app_users SET firstName = ?, lastName = ?, fullName = ?, email = ? WHERE id = ?')
      .run(firstName, lastName, fullName, email, decoded.userId);

    // Fetch updated user
    const updated = db.prepare('SELECT id, firstName, lastName, fullName, email, gender, createdAt FROM app_users WHERE id = ?').get(decoded.userId);
    
    res.json({
      id: updated.id,
      firstName: updated.firstName,
      lastName: updated.lastName,
      fullName: updated.fullName,
      email: updated.email,
      gender: updated.gender,
      createdAt: updated.createdAt
    });
  } catch (error) {
    if (error.name === 'JsonWebTokenError' || error.name === 'TokenExpiredError') {
      return res.status(401).json({ message: 'Invalid or expired token' });
    }
    console.error('Update profile error:', error);
    res.status(500).json({ message: 'Unable to update profile' });
  }
});

module.exports = router;

