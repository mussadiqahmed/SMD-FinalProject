const express = require('express');
const cors = require('cors');
const morgan = require('morgan');
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
app.use(express.json({ limit: '5mb' }));
app.use(morgan('dev'));

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

app.use(errorHandler);

app.listen(config.port, () => {
  console.log(`API running on http://localhost:${config.port}`);
});

