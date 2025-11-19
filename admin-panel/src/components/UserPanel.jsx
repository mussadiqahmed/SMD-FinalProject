import { useEffect, useState } from 'react';
import SidePanel from './SidePanel';
import api from '../api/client';
import './UserPanel.css';

const emptyUser = {
  fullName: '',
  email: '',
  phone: '',
  notes: ''
};

export default function UserPanel({ open, onClose, user, onSaved }) {
  const [form, setForm] = useState(emptyUser);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (user) {
      setForm({
        fullName: user.fullName || '',
        email: user.email || '',
        phone: user.phone || '',
        notes: user.notes || ''
      });
    } else {
      setForm(emptyUser);
    }
  }, [user]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (user?.id) {
        await api.put(`/users/${user.id}`, form);
      } else {
        await api.post('/users', form);
      }
      onSaved();
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to save user');
    } finally {
      setSaving(false);
    }
  };

  return (
    <SidePanel
      open={open}
      title={user ? 'Edit user' : 'Add user'}
      onClose={onClose}
      width={440}
    >
      <form className="user-panel" onSubmit={handleSubmit}>
        {error && <div className="panel-error">{error}</div>}
        <label>
          Full name
          <input name="fullName" value={form.fullName} onChange={handleChange} required />
        </label>
        <label>
          Email address
          <input name="email" type="email" value={form.email} onChange={handleChange} required />
        </label>
        <label>
          Phone
          <input name="phone" value={form.phone} onChange={handleChange} />
        </label>
        <label>
          Notes
          <textarea name="notes" rows={3} value={form.notes} onChange={handleChange} />
        </label>
        <div className="panel-actions">
          <button type="button" className="ghost" onClick={onClose} disabled={saving}>
            Cancel
          </button>
          <button type="submit" className="primary" disabled={saving}>
            {saving ? 'Saving...' : user ? 'Update user' : 'Create user'}
          </button>
        </div>
      </form>
    </SidePanel>
  );
}
