import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../api/client';
import ProductForm from '../sections/ProductForm';
import './ProductsPage.css';

async function fetchProducts() {
  const { data } = await api.get('/products');
  return data;
}

export default function ProductsPage() {
  const [selectedProduct, setSelectedProduct] = useState(null);
  const { data: products = [], isLoading, refetch } = useQuery({
    queryKey: ['products'],
    queryFn: fetchProducts
  });

  const handleEdit = (product) => {
    setSelectedProduct(product);
  };

  const resetForm = () => {
    setSelectedProduct(null);
    refetch();
  };

  return (
    <div className="products-page">
      <header className="page-header">
        <div>
          <p className="eyebrow">Inventory</p>
          <h1>Products</h1>
        </div>
        <button type="button" className="primary" onClick={() => setSelectedProduct({})}>
          + New Product
        </button>
      </header>

      <div className="products-layout">
        <section className="products-list">
          {isLoading ? (
            <p>Loading products...</p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Category</th>
                  <th>Price</th>
                  <th>Featured</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.id}>
                    <td>
                      <div className="product-name">
                        <img src={product.imageUrl} alt={product.name} />
                        <div>
                          <strong>{product.name}</strong>
                          <p>{product.description}</p>
                        </div>
                      </div>
                    </td>
                    <td>{product.categoryId || '—'}</td>
                    <td>${product.price.toFixed(2)}</td>
                    <td>{product.featured ? 'Yes' : 'No'}</td>
                    <td>
                      <button type="button" onClick={() => handleEdit(product)}>
                        Edit
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>

        <aside className="product-form-panel">
          <ProductForm product={selectedProduct} onSuccess={resetForm} />
        </aside>
      </div>
    </div>
  );
}

