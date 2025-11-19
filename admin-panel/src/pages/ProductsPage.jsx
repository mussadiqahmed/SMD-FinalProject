import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../api/client';
import ProductPanel from '../components/ProductPanel';
import { CATEGORY_OPTIONS } from '../constants/catalog';
import './ProductsPage.css';

async function fetchProducts() {
  const { data } = await api.get('/products');
  return data;
}

async function fetchCategories() {
  const { data } = await api.get('/categories');
  return data;
}

export default function ProductsPage() {
  const [panelOpen, setPanelOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [filters, setFilters] = useState({ search: '', category: 'all' });

  const {
    data: products = [],
    isLoading,
    refetch
  } = useQuery({
    queryKey: ['products'],
    queryFn: fetchProducts
  });

  const { data: categories = CATEGORY_OPTIONS } = useQuery({
    queryKey: ['categories'],
    queryFn: fetchCategories,
    staleTime: 1000 * 60 * 5,
    retry: 1
  });

  const filteredProducts = useMemo(() => {
    return products.filter((product) => {
      const matchesCategory =
        filters.category === 'all' || product?.category?.slug === filters.category;
      const searchTerm = filters.search.trim().toLowerCase();
      const matchesSearch =
        !searchTerm ||
        product.name.toLowerCase().includes(searchTerm) ||
        (product.description || '').toLowerCase().includes(searchTerm);
      return matchesCategory && matchesSearch;
    });
  }, [products, filters]);

  const handleDelete = async (id) => {
    if (window.confirm('Remove this product?')) {
      await api.delete(`/products/${id}`);
      refetch();
    }
  };

  const openCreate = () => {
    setEditingProduct(null);
    setPanelOpen(true);
  };

  const openEdit = (product) => {
    setEditingProduct(product);
    setPanelOpen(true);
  };

  return (
    <div className="products-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Catalog</p>
          <h1>Products</h1>
        </div>
        <button type="button" className="primary" onClick={openCreate}>
          + Add product
        </button>
      </header>

      <div className="filter-bar">
        <input
          placeholder="Search products"
          value={filters.search}
          onChange={(event) => setFilters((prev) => ({ ...prev, search: event.target.value }))}
        />
        <select
          value={filters.category}
          onChange={(event) => setFilters((prev) => ({ ...prev, category: event.target.value }))}
        >
          <option value="all">All categories</option>
          {categories.map((category) => (
            <option key={category.id} value={category.slug}>
              {category.title}
            </option>
          ))}
        </select>
      </div>

      <section className="products-table">
        {isLoading ? (
          <p>Loading products...</p>
        ) : filteredProducts.length ? (
          <table>
            <thead>
              <tr>
                <th>Product</th>
                <th>Category</th>
                <th>Price</th>
                <th>Discount</th>
                <th>Stock</th>
                <th>Featured</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {filteredProducts.map((product) => (
                <tr key={product.id}>
                  <td>
                    <div className="product-cell">
                      <img src={product.imageUrl || product.images?.[0]} alt={product.name} />
                      <div>
                        <strong>{product.name}</strong>
                        <p>{product.description || '—'}</p>
                      </div>
                    </div>
                  </td>
                  <td>{product.category?.title || '—'}</td>
                  <td>${Number(product.price).toFixed(2)}</td>
                  <td>{product.discountPercent ? `${product.discountPercent}%` : '—'}</td>
                  <td>{product.stock ?? 0}</td>
                  <td>
                    <span className={product.featured ? 'tag tag-success' : 'tag'}>
                      {product.featured ? 'Yes' : 'No'}
                    </span>
                  </td>
                  <td>
                    <div className="row-actions">
                      <button type="button" onClick={() => openEdit(product)}>
                        Edit
                      </button>
                      <button type="button" className="danger" onClick={() => handleDelete(product.id)}>
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
            <p>No products found. Try adjusting filters or add a new product.</p>
          </div>
        )}
      </section>

      <ProductPanel
        open={panelOpen}
        onClose={() => setPanelOpen(false)}
        onSaved={refetch}
        categories={categories}
        product={editingProduct}
      />
    </div>
  );
}
