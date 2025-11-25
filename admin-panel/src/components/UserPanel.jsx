import { useEffect, useState } from 'react';
import SidePanel from './SidePanel';
import api from '../api/client';
import './UserPanel.css';

const emptyUser = {
  fullName: '',
  email: '',
  phone: '',
  password: '',
  confirmPassword: '',
  notes: '',
  gender: ''
};

export default function UserPanel({ open, onClose, user, onSaved }) {
  const [form, setForm] = useState(emptyUser);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState({});

  // Check if this is an app user (registered through the app)
  const isAppUser = user?.source === 'app';

  useEffect(() => {
    if (user) {
      // Normalize gender to lowercase to match select options
      const normalizedGender = user.gender ? user.gender.toLowerCase() : '';
      setForm({
        fullName: user.fullName || '',
        email: user.email || '',
        phone: user.phone || '',
        password: '',
        confirmPassword: '',
        notes: user.notes || '',
        gender: normalizedGender
      });
    } else {
      setForm(emptyUser);
    }
  }, [user]);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const validatePassword = (password) => {
    if (!password) return { valid: false, message: 'Password is required' };
    if (password.length < 8) return { valid: false, message: 'Password must be at least 8 characters long' };
    if (!/[A-Z]/.test(password)) return { valid: false, message: 'Password must contain at least one uppercase letter' };
    if (!/[a-z]/.test(password)) return { valid: false, message: 'Password must contain at least one lowercase letter' };
    if (!/[0-9]/.test(password)) return { valid: false, message: 'Password must contain at least one digit' };
    if (!/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)) return { valid: false, message: 'Password must contain at least one special character' };
    return { valid: true };
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    setFieldErrors({});

    // Validate password for new users (required)
    if (!user?.id) {
      if (!form.password) {
        setError('Password is required');
        setSaving(false);
        return;
      }
      const passwordValidation = validatePassword(form.password);
      if (!passwordValidation.valid) {
        setError(passwordValidation.message);
        setSaving(false);
        return;
      }
      if (form.password !== form.confirmPassword) {
        setError('Passwords do not match');
        setSaving(false);
        return;
      }
    } else {
      // For existing users, validate password only if provided (optional)
      if (form.password || form.confirmPassword) {
        if (!form.password) {
          setFieldErrors({ password: 'Password is required if you want to change it' });
          setSaving(false);
          return;
        }
        if (!form.confirmPassword) {
          setFieldErrors({ confirmPassword: 'Please confirm the password' });
          setSaving(false);
          return;
        }
        const passwordValidation = validatePassword(form.password);
        if (!passwordValidation.valid) {
          setFieldErrors({ password: passwordValidation.message });
          setSaving(false);
          return;
        }
        if (form.password !== form.confirmPassword) {
          setFieldErrors({ confirmPassword: 'Passwords do not match' });
          setSaving(false);
          return;
        }
      }
    }

    try {
      if (user?.id) {
        // For existing users, send password only if provided
        const updateData = { ...form };
        if (!updateData.password) {
          // Remove password fields if not provided
          delete updateData.password;
          delete updateData.confirmPassword;
        } else {
          // Remove confirmPassword before sending
          delete updateData.confirmPassword;
        }
        await api.put(`/users/${user.id}`, updateData);
      } else {
        // For new users, send password but not confirmPassword
        const { confirmPassword, ...createData } = form;
        await api.post('/users', createData);
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
        {!isAppUser && (
          <label>
            Phone
            <input name="phone" value={form.phone} onChange={handleChange} />
          </label>
        )}
        {isAppUser && (
          <label>
            Gender
            <select name="gender" value={form.gender} onChange={handleChange}>
              <option value="">Select gender</option>
              <option value="male">Male</option>
              <option value="female">Female</option>
              <option value="other">Other</option>
            </select>
          </label>
        )}
        <label>
          {user?.id ? 'New Password (leave blank to keep current)' : 'Password'}
          <input 
            name="password" 
            type="password" 
            value={form.password} 
            onChange={handleChange} 
            required={!user?.id}
            placeholder={user?.id ? 'Enter new password to change' : ''}
          />
          {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
          <small style={{ color: '#9a9aae', fontSize: '12px', marginTop: '4px', display: 'block' }}>
            {user?.id 
              ? 'Leave blank to keep current password. Must be 8+ characters with uppercase, lowercase, digit, and special character if changing.'
              : 'Must be 8+ characters with uppercase, lowercase, digit, and special character'
            }
          </small>
        </label>
        <label>
          {user?.id ? 'Confirm New Password' : 'Confirm Password'}
          <input 
            name="confirmPassword" 
            type="password" 
            value={form.confirmPassword} 
            onChange={handleChange} 
            required={!user?.id}
            placeholder={user?.id ? 'Confirm new password' : ''}
          />
          {fieldErrors.confirmPassword && <span className="field-error">{fieldErrors.confirmPassword}</span>}
        </label>
        {!isAppUser && (
          <label>
            Notes
            <textarea name="notes" rows={3} value={form.notes} onChange={handleChange} />
          </label>
        )}
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
