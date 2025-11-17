const express = require('express');
const jwt = require('jsonwebtoken');
const config = require('../config');

const router = express.Router();

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

module.exports = router;

