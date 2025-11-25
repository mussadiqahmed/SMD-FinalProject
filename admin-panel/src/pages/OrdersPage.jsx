import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api/client';
import './OrdersPage.css';

async function fetchOrders() {
  const { data } = await api.get('/orders');
  return data;
}

const statusOptions = ['processing', 'confirmed', 'packed', 'delivered'];

export default function OrdersPage() {
  const queryClient = useQueryClient();
  const { data: orders = [], refetch, isLoading } = useQuery({
    queryKey: ['orders'],
    queryFn: fetchOrders
  });

  const deleteOrderMutation = useMutation({
    mutationFn: (orderId) => api.delete(`/orders/${orderId}`),
    onSuccess: () => {
      queryClient.invalidateQueries(['orders']);
    },
    onError: (error) => {
      alert(error.response?.data?.message || 'Failed to delete order');
    }
  });

  const handleStatusChange = async (orderId, newStatus) => {
    try {
      await api.put(`/orders/${orderId}/status`, { status: newStatus });
      refetch();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update order status');
    }
  };

  const handleDelete = (orderId) => {
    if (window.confirm(`Are you sure you want to delete Order #${orderId}? This action cannot be undone.`)) {
      deleteOrderMutation.mutate(orderId);
    }
  };

  const formatDate = (timestamp) => {
    if (!timestamp) return '—';
    const date = new Date(timestamp * 1000);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString();
  };

  const getStatusColor = (status) => {
    const colors = {
      processing: '#3b82f6',
      confirmed: '#10b981',
      packed: '#8b5cf6',
      delivered: '#10b981'
    };
    return colors[status] || '#6b7280';
  };

  return (
    <div className="orders-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Sales</p>
          <h1>Orders</h1>
        </div>
      </header>

      <section className="orders-table">
        {isLoading ? (
          <p>Loading orders...</p>
        ) : orders.length ? (
          <table>
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Customer</th>
                <th>Phone</th>
                <th>Address</th>
                <th>Total</th>
                <th>Status</th>
                <th>Date</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>#{order.id}</td>
                  <td>
                    <strong>{order.customerName}</strong>
                  </td>
                  <td>{order.phone || '—'}</td>
                  <td>
                    {order.addressLine}, {order.city}
                  </td>
                  <td>Rs {Number(order.total).toFixed(2)}</td>
                  <td>
                    <select
                      value={order.status}
                      onChange={(e) => handleStatusChange(order.id, e.target.value)}
                      style={{
                        backgroundColor: getStatusColor(order.status),
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        padding: '6px 12px',
                        cursor: 'pointer',
                        fontWeight: '600',
                        textTransform: 'capitalize'
                      }}
                    >
                      {statusOptions.map((status) => (
                        <option key={status} value={status} style={{ backgroundColor: '#fff', color: '#000' }}>
                          {status}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td>{formatDate(order.createdAt)}</td>
                  <td>
                    <button
                      onClick={() => handleDelete(order.id)}
                      disabled={deleteOrderMutation.isLoading}
                      style={{
                        backgroundColor: '#ef4444',
                        color: 'white',
                        border: 'none',
                        borderRadius: '6px',
                        padding: '6px 12px',
                        cursor: deleteOrderMutation.isLoading ? 'not-allowed' : 'pointer',
                        fontWeight: '600',
                        fontSize: '14px',
                        opacity: deleteOrderMutation.isLoading ? 0.6 : 1
                      }}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="empty-state">
            <p>No orders found.</p>
          </div>
        )}
      </section>
    </div>
  );
}

