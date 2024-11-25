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
  const [fieldErrors, setFieldErrors] = useState({});

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
      setImageFiles([null, null, null]);
    } else {
      setForm(emptyProduct);
      setImageFiles([null, null, null]);
    }
  }, [product, resolvedCategories]);

  // Reset form when panel opens for a new product
  useEffect(() => {
    if (open && !product) {
      setForm(emptyProduct);
      setImageFiles([null, null, null]);
      setError('');
      setFieldErrors({});
    }
  }, [open, product]);

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

  const [imageFiles, setImageFiles] = useState([null, null, null]);

  const handleImageChange = (index, value) => {
    setForm((prev) => {
      const next = [...prev.images];
      next[index] = value;
      return { ...prev, images: next };
    });
  };

  const handleFileChange = (index, file) => {
    setImageFiles((prev) => {
      const next = [...prev];
      next[index] = file;
      return next;
    });
    // Clear URL when file is selected
    setForm((prev) => {
      const next = [...prev.images];
      next[index] = '';
      return { ...prev, images: next };
    });
  };

  const validateForm = () => {
    const errors = {};
    
    if (!form.name || !form.name.trim()) {
      errors.name = 'Product name is required';
    }
    
    if (!form.price || form.price === '' || Number(form.price) <= 0) {
      errors.price = 'Price must be greater than 0';
    }
    
    if (!form.categoryId) {
      errors.categoryId = 'Category is required';
    }
    
    const urlImages = form.images.filter((img) => img && img.trim());
    const hasImages = urlImages.length > 0 || imageFiles.some((f) => f !== null);
    if (!hasImages) {
      errors.images = 'At least one product image is required (upload file or enter URL)';
    }
    
    if (form.discountPercent && (Number(form.discountPercent) < 0 || Number(form.discountPercent) > 95)) {
      errors.discountPercent = 'Discount must be between 0 and 95%';
    }
    
    if (form.stock && Number(form.stock) < 0) {
      errors.stock = 'Stock cannot be negative';
    }
    
    return errors;
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});

    const errors = validateForm();
    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      setSaving(false);
      return;
    }

    const formData = new FormData();
    formData.append('name', form.name.trim());
    formData.append('description', form.description || '');
    formData.append('categoryId', Number(form.categoryId));
    formData.append('price', Number(form.price));
    formData.append('discountPercent', form.discountPercent ? Number(form.discountPercent) : 0);
    formData.append('stock', form.stock ? Number(form.stock) : 0);
    formData.append('sizes', JSON.stringify(form.sizes));
    formData.append('colors', JSON.stringify(form.colors));
    formData.append('featured', form.featured ? '1' : '0');

    // Add image URLs (non-empty ones) - use separate field name
    const urlImages = form.images.filter((img) => img && img.trim());
    if (urlImages.length) {
      formData.append('imageUrls', JSON.stringify(urlImages));
    }

    // Add file uploads - use separate field name
    imageFiles.forEach((file) => {
      if (file) {
        formData.append('imageFiles', file);
      }
    });

    try {
      const config = {
        headers: { 'Content-Type': 'multipart/form-data' }
      };
      if (product?.id) {
        await api.put(`/products/${product.id}`, formData, config);
      } else {
        await api.post('/products', formData, config);
      }
      setImageFiles([null, null, null]);
      setFieldErrors({});
      setForm(emptyProduct);
      setError('');
      onSaved();
      onClose();
    } catch (err) {
      const errorMessage = err.response?.data?.message || 'Unable to save product';
      setError(errorMessage);
      
      // Try to parse field-specific errors from response
      if (err.response?.data?.errors) {
        setFieldErrors(err.response.data.errors);
      }
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
          <input 
            name="name" 
            value={form.name} 
            onChange={handleFieldChange} 
            className={fieldErrors.name ? 'error' : ''}
          />
          {fieldErrors.name && <span className="field-error">{fieldErrors.name}</span>}
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
            <select 
              name="categoryId" 
              value={form.categoryId} 
              onChange={handleFieldChange}
              className={fieldErrors.categoryId ? 'error' : ''}
            >
              {resolvedCategories.map((category) => (
                <option key={category.id} value={category.id}>{category.title}</option>
              ))}
            </select>
            {fieldErrors.categoryId && <span className="field-error">{fieldErrors.categoryId}</span>}
          </label>
          <label>
            Price (Rs)
            <input
              name="price"
              type="number"
              step="0.01"
              min="0"
              value={form.price}
              onChange={handleFieldChange}
              className={fieldErrors.price ? 'error' : ''}
            />
            {fieldErrors.price && <span className="field-error">{fieldErrors.price}</span>}
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
              className={fieldErrors.discountPercent ? 'error' : ''}
            />
            {fieldErrors.discountPercent && <span className="field-error">{fieldErrors.discountPercent}</span>}
          </label>
          <label>
            Stock
            <input
              name="stock"
              type="number"
              min="0"
              value={form.stock}
              onChange={handleFieldChange}
              className={fieldErrors.stock ? 'error' : ''}
            />
            {fieldErrors.stock && <span className="field-error">{fieldErrors.stock}</span>}
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
          <p>Images (up to 3) <span className="required">*</span></p>
          {form.images.map((image, index) => (
            <div key={index} className="image-input-group">
              <label>
                Upload Image {index + 1} {index === 0 && <span className="required">*</span>}
                <input
                  type="file"
                  accept="image/*"
                  onChange={(event) => handleFileChange(index, event.target.files[0] || null)}
                />
                {imageFiles[index] && (
                  <span className="file-name">{imageFiles[index].name}</span>
                )}
              </label>
              <span className="or-divider">OR</span>
              <label>
                Image URL {index + 1}
                <input
                  type="url"
                  value={image}
                  onChange={(event) => handleImageChange(index, event.target.value)}
                  placeholder="https://example.com/image.jpg"
                />
              </label>
            </div>
          ))}
          {fieldErrors.images && <span className="field-error">{fieldErrors.images}</span>}
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
