const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
const path = require('path');
const config = require('./config');
const authRoutes = require('./routes/auth');
const productRoutes = require('./routes/products');
const categoryRoutes = require('./routes/categories');
const userRoutes = require('./routes/users');
const statsRoutes = require('./routes/stats');
const errorHandler = require('./middleware/errorHandler');

const app = express();

app.use(cors({
  origin: config.clientOrigin,
  credentials: true
}));
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ extended: true, limit: '50mb' }));
app.use(morgan('dev'));

// Serve uploaded files
// In production, use server/uploads; in development, use root uploads
const uploadsPath = process.env.NODE_ENV === 'production' 
  ? path.join(__dirname, '..', 'uploads')
  : path.join(__dirname, '..', '..', 'uploads');
app.use('/uploads', express.static(uploadsPath));

app.get('/', (req, res) => {
  res.json({
    message: 'Nova Commerce Admin API',
    docs: '/api',
    version: '1.0.0'
  });
});

app.use('/api/auth', authRoutes);
app.use('/api/products', productRoutes);
app.use('/api/categories', categoryRoutes);
app.use('/api/users', userRoutes);
app.use('/api/stats', statsRoutes);
app.use('/api/orders', require('./routes/orders'));

app.use(errorHandler);

app.listen(config.port, () => {
  console.log(`API running on http://localhost:${config.port}`);
});

