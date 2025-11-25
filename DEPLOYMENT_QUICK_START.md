# Quick Start: Deploy to Render

## 🚀 Quick Deployment Steps

### 1. Backend Deployment (5 minutes)

1. Go to [Render Dashboard](https://dashboard.render.com) → **New +** → **Web Service**
2. Connect your Git repository
3. Configure:
   - **Name**: `ecommerce-backend`
   - **Root Directory**: Leave empty (or set to `server` if you want)
   - **Environment**: `Node`
   - **Build Command**: `cd server && npm install`
   - **Start Command**: `cd server && npm start`
4. Add Environment Variables:
   ```
   NODE_ENV=production
   PORT=10000
   DB_PATH=./data/ecommerce.db
   ADMIN_USERNAME=admin
   ADMIN_PASSWORD=YourSecurePassword123!
   JWT_SECRET=GenerateWith: openssl rand -base64 32
   CLIENT_ORIGIN=https://your-admin-url.onrender.com
   BASE_URL=https://your-backend-url.onrender.com
   ```
5. Click **Create Web Service**
6. **Copy the URL** (e.g., `https://ecommerce-backend-xxxx.onrender.com`)

### 2. Admin Panel Deployment (5 minutes)

1. Go to **New +** → **Static Site**
2. Connect the same repository
3. Configure:
   - **Name**: `ecommerce-admin-panel`
   - **Build Command**: `cd admin-panel && npm install && npm run build`
   - **Publish Directory**: `admin-panel/dist`
4. Add Environment Variable:
   ```
   VITE_API_URL=https://your-backend-url.onrender.com/api
   ```
   (Use the backend URL from step 1)
5. Click **Create Static Site**
6. **Copy the URL** (e.g., `https://ecommerce-admin-panel-xxxx.onrender.com`)

### 3. Update Backend CORS (2 minutes)

1. Go back to your backend service
2. Update `CLIENT_ORIGIN` to your admin panel URL
3. Update `BASE_URL` to your backend URL
4. Save (auto-redeploys)

### 4. Test

1. Visit your admin panel URL
2. Login with the credentials you set in `ADMIN_USERNAME` and `ADMIN_PASSWORD`

## ⚠️ Important Notes

### Database Warning
- **Free tier**: Database is **ephemeral** (lost on restart)
- **Solution**: Upgrade to paid plan OR use Render PostgreSQL (free tier available)

### Generate JWT Secret
Run this command to generate a secure secret:
```bash
openssl rand -base64 32
```

### Environment Variables Checklist

**Backend:**
- ✅ `NODE_ENV=production`
- ✅ `PORT=10000`
- ✅ `DB_PATH=./data/ecommerce.db`
- ✅ `ADMIN_USERNAME` (your choice)
- ✅ `ADMIN_PASSWORD` (strong password)
- ✅ `JWT_SECRET` (generated secret)
- ✅ `CLIENT_ORIGIN` (admin panel URL)
- ✅ `BASE_URL` (backend URL)

**Admin Panel:**
- ✅ `VITE_API_URL` (backend URL + `/api`)

## 🔧 Troubleshooting

**Backend won't start?**
- Check logs in Render dashboard
- Verify PORT is `10000`
- Check all env vars are set

**Admin panel can't connect?**
- Verify `VITE_API_URL` is correct
- Check `CLIENT_ORIGIN` in backend matches admin URL exactly
- Ensure URLs use `https://`

**Database issues?**
- Free tier: Database resets on restart (expected)
- Consider upgrading or using PostgreSQL

## 📚 Full Documentation

See [RENDER_DEPLOYMENT.md](./RENDER_DEPLOYMENT.md) for detailed instructions.

