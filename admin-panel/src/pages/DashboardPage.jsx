import { useQuery } from '@tanstack/react-query';
import api from '../api/client';
import './DashboardPage.css';

async function fetchStats() {
  const { data } = await api.get('/stats');
  return data;
}

export default function DashboardPage() {
  const { data, isLoading } = useQuery({
    queryKey: ['stats'],
    queryFn: fetchStats
  });

  const totals = data?.totals || { products: 0, users: 0, orders: 0 };
  const highlights = data?.highlights || { recentProducts: [], recentUsers: [], recentOrders: [] };

  return (
    <div className="dashboard-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Overview</p>
          <h1>Control center</h1>
        </div>
      </header>

      {isLoading ? (
        <p>Loading analytics...</p>
      ) : (
        <>
          <section className="stat-grid">
            <article>
              <p>Products</p>
              <strong>{totals.products}</strong>
              <span>Active SKUs</span>
            </article>
            <article>
              <p>Users</p>
              <strong>{totals.users}</strong>
              <span>Registered customers</span>
            </article>
            <article>
              <p>Orders</p>
              <strong>{totals.orders}</strong>
              <span>Total placed</span>
            </article>
          </section>

          <section className="card-grid">
         
            <div className="card">
              <h3>Latest users</h3>
              <ul>
                {highlights.recentUsers?.length ? (
                  highlights.recentUsers.map((user) => (
                    <li key={user.id}>
                      <div>
                        <strong>{user.fullName}</strong>
                        <span>{user.email}</span>
                      </div>
                      <span className="meta">{new Date(user.createdAt).toLocaleDateString()}</span>
                    </li>
                  ))
                ) : (
                  <li>No users yet.</li>
                )}
              </ul>
            </div>
          </section>
        </>
      )}
    </div>
  );
}
