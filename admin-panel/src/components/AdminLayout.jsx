import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './AdminLayout.css';

const navLinks = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/products', label: 'Products' },
  { to: '/users', label: 'Users' }
];

export default function AdminLayout() {
  const { logout } = useAuth();

  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-logo">Nova Admin</div>
        <nav>
          {navLinks.map((link) => (
            <NavLink key={link.to} to={link.to}>
              {link.label}
            </NavLink>
          ))}
        </nav>
        <button type="button" onClick={logout} className="logout-button">
          Logout
        </button>
      </aside>

      <div className="admin-main">
        <main className="admin-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

