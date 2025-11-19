import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../api/client';
import UserPanel from '../components/UserPanel';
import './UsersPage.css';

async function fetchUsers() {
  const { data } = await api.get('/users');
  return data;
}

export default function UsersPage() {
  const [panelOpen, setPanelOpen] = useState(false);
  const [editingUser, setEditingUser] = useState(null);
  const { data: users = [], refetch, isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: fetchUsers
  });

  const openPanel = (user = null) => {
    setEditingUser(user);
    setPanelOpen(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Remove this user?')) {
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
        <button type="button" className="primary" onClick={() => openPanel(null)}>
          + Add user
        </button>
      </header>

      <section className="users-table">
        {isLoading ? (
          <p>Loading users...</p>
        ) : users.length ? (
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>
                    <strong>{user.fullName}</strong>
                  </td>
                  <td>{user.email}</td>
                  <td>{user.phone || '—'}</td>
                  <td>
                    <div className="row-actions">
                      <button type="button" onClick={() => openPanel(user)}>
                        Edit
                      </button>
                      <button type="button" className="danger" onClick={() => handleDelete(user.id)}>
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="empty-state">
            <p>No users found.</p>
          </div>
        )}
      </section>

      <UserPanel
        open={panelOpen}
        user={editingUser}
        onClose={() => setPanelOpen(false)}
        onSaved={refetch}
      />
    </div>
  );
}
