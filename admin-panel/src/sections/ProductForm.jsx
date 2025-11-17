import { useEffect, useState } from 'react';
import api from '../api/client';
import './ProductForm.css';

const initialState = {
  name: '',
  description: '',
  price: '',
  imageUrl: '',
  categoryId: '',
  sizes: '',
  colors: '',
  featured: false
};

export default function ProductForm({ product, onSuccess }) {
  const [form, setForm] = useState(initialState);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (product && product.id) {
      setForm({
        ...product,
        price: product.price.toString(),
        sizes: (product.sizes || []).join(', '),
        colors: (product.colors || []).join(', ')
      });
    } else if (product) {
      setForm(initialState);
    }
  }, [product]);

  const handleChange = (event) => {
    const { name, value, type, checked } = event.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');
    const payload = {
      ...form,
      price: Number(form.price),
      sizes: form.sizes.split(',').map((item) => item.trim()).filter(Boolean),
      colors: form.colors.split(',').map((item) => item.trim()).filter(Boolean)
    };
    try {
      if (product?.id) {
        await api.put(`/products/${product.id}`, payload);
      } else {
        await api.post('/products', payload);
      }
      setForm(initialState);
      onSuccess?.();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to save product');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="product-form" onSubmit={handleSubmit}>
      <h3>{product?.id ? 'Edit Product' : 'Create Product'}</h3>
      {error && <div className="form-error">{error}</div>}
      <label htmlFor="name">
        Name
        <input id="name" name="name" value={form.name} onChange={handleChange} required />
      </label>
      <label htmlFor="description">
        Description
        <textarea
          id="description"
          name="description"
          rows={3}
          value={form.description}
          onChange={handleChange}
        />
      </label>
      <div className="form-row">
        <label htmlFor="price">
          Price
          <input
            id="price"
            name="price"
            type="number"
            step="0.01"
            value={form.price}
            onChange={handleChange}
            required
          />
        </label>
        <label htmlFor="categoryId">
          Category ID
          <input
            id="categoryId"
            name="categoryId"
            type="number"
            value={form.categoryId}
            onChange={handleChange}
          />
        </label>
      </div>
      <label htmlFor="imageUrl">
        Image URL
        <input id="imageUrl" name="imageUrl" value={form.imageUrl} onChange={handleChange} />
      </label>
      <label htmlFor="sizes">
        Sizes (comma separated)
        <input id="sizes" name="sizes" value={form.sizes} onChange={handleChange} />
      </label>
      <label htmlFor="colors">
        Colors (comma separated)
        <input id="colors" name="colors" value={form.colors} onChange={handleChange} />
      </label>
      <label className="checkbox">
        <input
          type="checkbox"
          name="featured"
          checked={form.featured}
          onChange={handleChange}
        />
        Featured
      </label>
      <button type="submit" className="primary" disabled={loading}>
        {loading ? 'Saving...' : product?.id ? 'Update Product' : 'Create Product'}
      </button>
    </form>
  );
}

