const multer = require('multer');
const path = require('path');
const fs = require('fs');

// In production, use server/uploads; in development, use root uploads
const uploadDir = process.env.NODE_ENV === 'production'
  ? path.join(__dirname, '..', 'uploads', 'products')
  : path.join(__dirname, '..', '..', '..', 'uploads', 'products');

if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, uploadDir);
  },
  filename: (req, file, cb) => {
    const uniqueSuffix = Date.now() + '-' + Math.round(Math.random() * 1e9);
    const ext = path.extname(file.originalname);
    cb(null, `product-${uniqueSuffix}${ext}`);
  }
});

const fileFilter = (req, file, cb) => {
  // Only process actual files, skip if file is undefined or doesn't have expected properties
  if (!file || !file.originalname || !file.mimetype) {
    return cb(null, false);
  }

  const allowedTypes = /jpeg|jpg|png|gif|webp/;
  const extname = allowedTypes.test(path.extname(file.originalname).toLowerCase());
  const mimetype = allowedTypes.test(file.mimetype);

  if (mimetype && extname) {
    return cb(null, true);
  } else {
    cb(new Error('Only image files are allowed (jpeg, jpg, png, gif, webp)'));
  }
};

const upload = multer({
  storage,
  fileFilter
  // No file size limit - allow any size
});

module.exports = upload;

