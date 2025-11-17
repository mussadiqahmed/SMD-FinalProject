import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AdminLayout.css';

export default function AdminLayout() {
  const { logout } = useAuth();
  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-logo">Nova Admin</div>
        <nav>
          <NavLink to="/products">Products</NavLink>
          <NavLink to="/categories">Categories</NavLink>
          <NavLink to="/users">Users</NavLink>
          <button type="button" onClick={logout} className="logout-button">
            Logout
          </button>
        </nav>
      </aside>
      <main className="admin-content">
        <Outlet />
      </main>
    </div>
  );
}

