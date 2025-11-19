import { useEffect, useMemo, useState } from 'react';
import SidePanel from './SidePanel';
import api from '../api/client';
import { SIZE_PRESETS, CATEGORY_OPTIONS } from '../constants/catalog';
import './ProductPanel.css';

const emptyProduct = {
  name: '',
  description: '',
  categoryId: CATEGORY_OPTIONS[0].id,
  price: '',
  discountPercent: '',
  stock: '',
  sizes: [],
  colors: [],
  featured: false,
  images: ['', '', '']
};

export default function ProductPanel({ open, onClose, onSaved, categories = [], product }) {
  const [form, setForm] = useState(emptyProduct);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const resolvedCategories = categories.length ? categories : CATEGORY_OPTIONS;

  useEffect(() => {
    if (product) {
      setForm({
        name: product.name || '',
        description: product.description || '',
        categoryId: product.categoryId || resolvedCategories[0].id,
        price: product.price ?? '',
        discountPercent: product.discountPercent ?? '',
        stock: product.stock ?? '',
        sizes: product.sizes || [],
        colors: product.colors || [],
        featured: !!product.featured,
        images: (product.images && product.images.length
          ? [...product.images, '', '', '']
          : [product.imageUrl || '', '', '']
        ).slice(0, 3)
      });
    } else {
      setForm(emptyProduct);
    }
  }, [product, resolvedCategories]);

  const activeCategory = useMemo(() => {
    return resolvedCategories.find((item) => item.id === Number(form.categoryId));
  }, [resolvedCategories, form.categoryId]);

  const sizeOptions = SIZE_PRESETS[activeCategory?.slug] || SIZE_PRESETS.mens;

  const handleFieldChange = (event) => {
    const { name, value, type, checked } = event.target;
    setForm((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const toggleSize = (value) => {
    setForm((prev) => {
      const exists = prev.sizes.includes(value);
      const next = exists ? prev.sizes.filter((item) => item !== value) : [...prev.sizes, value];
      return { ...prev, sizes: next };
    });
  };

  const handleImageChange = (index, value) => {
    setForm((prev) => {
      const next = [...prev.images];
      next[index] = value;
      return { ...prev, images: next };
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');

    const payload = {
      name: form.name.trim(),
      description: form.description,
      categoryId: Number(form.categoryId),
      price: Number(form.price),
      discountPercent: form.discountPercent ? Number(form.discountPercent) : 0,
      stock: form.stock ? Number(form.stock) : 0,
      sizes: form.sizes,
      colors: form.colors,
      featured: form.featured,
      images: form.images.filter((img) => img && img.trim())
    };

    if (!payload.images.length) {
      setError('Please provide at least one product image URL.');
      setSaving(false);
      return;
    }

    try {
      if (product?.id) {
        await api.put(`/products/${product.id}`, payload);
      } else {
        await api.post('/products', payload);
      }
      onSaved();
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to save product');
    } finally {
      setSaving(false);
    }
  };

  return (
    <SidePanel
      open={open}
      title={product ? 'Edit product' : 'Add product'}
      onClose={onClose}
    >
      <form className="product-panel" onSubmit={handleSubmit}>
        {error && <div className="panel-error">{error}</div>}
        <label>
          Product name
          <input name="name" value={form.name} onChange={handleFieldChange} required />
        </label>
        <label>
          Description
          <textarea
            name="description"
            rows={3}
            value={form.description}
            onChange={handleFieldChange}
          />
        </label>
        <div className="form-row">
          <label>
            Category
            <select name="categoryId" value={form.categoryId} onChange={handleFieldChange}>
              {resolvedCategories.map((category) => (
                <option key={category.id} value={category.id}>{category.title}</option>
              ))}
            </select>
          </label>
          <label>
            Price ($)
            <input
              name="price"
              type="number"
              step="0.01"
              min="0"
              value={form.price}
              onChange={handleFieldChange}
              required
            />
          </label>
        </div>
        <div className="form-row">
          <label>
            Discount (%)
            <input
              name="discountPercent"
              type="number"
              min="0"
              max="95"
              value={form.discountPercent}
              onChange={handleFieldChange}
            />
          </label>
          <label>
            Stock
            <input
              name="stock"
              type="number"
              min="0"
              value={form.stock}
              onChange={handleFieldChange}
            />
          </label>
        </div>

        <div className="size-grid">
          <p>Sizes</p>
          <div className="size-options">
            {sizeOptions.map((size) => (
              <label key={size}>
                <input
                  type="checkbox"
                  checked={form.sizes.includes(size)}
                  onChange={() => toggleSize(size)}
                />
                <span>{size}</span>
              </label>
            ))}
          </div>
        </div>

        <div className="image-grid">
          <p>Images (up to 3)</p>
          {form.images.map((image, index) => (
            <label key={index}>
              Image {index + 1} {index === 0 && <span className="required">*</span>}
              <input
                value={image}
                onChange={(event) => handleImageChange(index, event.target.value)}
                required={index === 0}
              />
            </label>
          ))}
        </div>

        <label className="checkbox-field">
          <input
            type="checkbox"
            name="featured"
            checked={form.featured}
            onChange={handleFieldChange}
          />
          Feature on storefront
        </label>

        <div className="panel-actions">
          <button type="button" className="ghost" onClick={onClose} disabled={saving}>
            Cancel
          </button>
          <button type="submit" className="primary" disabled={saving}>
            {saving ? 'Saving...' : product ? 'Update product' : 'Create product'}
          </button>
        </div>
      </form>
    </SidePanel>
  );
}
