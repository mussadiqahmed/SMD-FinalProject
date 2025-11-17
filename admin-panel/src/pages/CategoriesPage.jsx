import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../api/client';
import './CategoriesPage.css';

async function fetchCategories() {
  const { data } = await api.get('/categories');
  return data;
}

export default function CategoriesPage() {
  const { data: categories = [], refetch } = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories
  });
  const [form, setForm] = useState({ title: '', imageUrl: '' });
  const [selected, setSelected] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (selected) {
      await api.put(`/categories/${selected.id}`, form);
    } else {
      await api.post('/categories', form);
    }
    setForm({ title: '', imageUrl: '' });
    setSelected(null);
    refetch();
  };

  const handleEdit = (category) => {
    setSelected(category);
    setForm({ title: category.title, imageUrl: category.imageUrl || '' });
  };

  const handleDelete = async (id) => {
    if (window.confirm('Delete this category?')) {
      await api.delete(`/categories/${id}`);
      refetch();
    }
  };

  return (
    <div className="categories-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Catalog</p>
          <h1>Categories</h1>
        </div>
      </header>
      <div className="categories-layout">
        <section className="categories-list">
          {categories.map((cat) => (
            <article key={cat.id}>
              <div>
                <strong>{cat.title}</strong>
                <p>{cat.imageUrl}</p>
              </div>
              <div className="actions">
                <button type="button" onClick={() => handleEdit(cat)}>Edit</button>
                <button type="button" onClick={() => handleDelete(cat.id)}>Delete</button>
              </div>
            </article>
          ))}
        </section>
        <aside className="category-form">
          <form onSubmit={handleSubmit}>
            <h3>{selected ? 'Edit Category' : 'New Category'}</h3>
            <label htmlFor="title">
              Title
              <input
                id="title"
                name="title"
                value={form.title}
                onChange={(e) => setForm((prev) => ({ ...prev, title: e.target.value }))}
                required
              />
            </label>
            <label htmlFor="imageUrl">
              Image URL
              <input
                id="imageUrl"
                name="imageUrl"
                value={form.imageUrl}
                onChange={(e) => setForm((prev) => ({ ...prev, imageUrl: e.target.value }))}
              />
            </label>
            <button type="submit" className="primary">
              {selected ? 'Update' : 'Create'}
            </button>
          </form>
        </aside>
      </div>
    </div>
  );
}

