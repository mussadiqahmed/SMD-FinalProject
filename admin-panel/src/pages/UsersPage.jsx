import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../api/client';
import './UsersPage.css';

async function fetchUsers() {
  const { data } = await api.get('/users');
  return data;
}

export default function UsersPage() {
  const { data: users = [], refetch } = useQuery({
    queryKey: ['users'],
    queryFn: fetchUsers
  });
  const [form, setForm] = useState({ fullName: '', email: '', phone: '', notes: '' });
  const [selected, setSelected] = useState(null);

  const handleSubmit = async (event) => {
    event.preventDefault();
    if (selected) {
      await api.put(`/users/${selected.id}`, form);
    } else {
      await api.post('/users', form);
    }
    setForm({ fullName: '', email: '', phone: '', notes: '' });
    setSelected(null);
    refetch();
  };

  const handleEdit = (user) => {
    setSelected(user);
    setForm({
      fullName: user.fullName,
      email: user.email,
      phone: user.phone || '',
      notes: user.notes || ''
    });
  };

  const handleDelete = async (id) => {
    if (window.confirm('Delete this user?')) {
      await api.delete(`/users/${id}`);
      refetch();
    }
  };

  return (
    <div className="users-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Customers</p>
          <h1>Users</h1>
        </div>
      </header>
      <div className="users-layout">
        <section className="users-list">
          {users.map((user) => (
            <article key={user.id}>
              <div>
                <strong>{user.fullName}</strong>
                <p>{user.email}</p>
                {user.phone && <p>{user.phone}</p>}
              </div>
              <div className="actions">
                <button type="button" onClick={() => handleEdit(user)}>Edit</button>
                <button type="button" onClick={() => handleDelete(user.id)}>Delete</button>
              </div>
            </article>
          ))}
        </section>
        <aside className="user-form">
          <form onSubmit={handleSubmit}>
            <h3>{selected ? 'Edit User' : 'New User'}</h3>
            <label htmlFor="fullName">
              Full Name
              <input
                id="fullName"
                name="fullName"
                value={form.fullName}
                onChange={(e) => setForm((prev) => ({ ...prev, fullName: e.target.value }))}
                required
              />
            </label>
            <label htmlFor="email">
              Email
              <input
                id="email"
                name="email"
                type="email"
                value={form.email}
                onChange={(e) => setForm((prev) => ({ ...prev, email: e.target.value }))}
                required
              />
            </label>
            <label htmlFor="phone">
              Phone
              <input
                id="phone"
                name="phone"
                value={form.phone}
                onChange={(e) => setForm((prev) => ({ ...prev, phone: e.target.value }))}
              />
            </label>
            <label htmlFor="notes">
              Notes
              <textarea
                id="notes"
                name="notes"
                rows={3}
                value={form.notes}
                onChange={(e) => setForm((prev) => ({ ...prev, notes: e.target.value }))}
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

