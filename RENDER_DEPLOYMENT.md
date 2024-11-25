# Render Deployment Guide

This guide will help you deploy both the backend API and admin panel to Render.

## Prerequisites

1. A [Render](https://render.com) account (free tier available)
2. Your code pushed to a Git repository (GitHub, GitLab, or Bitbucket)

## Deployment Steps

### Step 1: Deploy the Backend API

1. **Go to Render Dashboard** → Click "New +" → Select "Web Service"

2. **Connect your repository**:
   - Connect your Git repository
   - Select the repository containing this project

3. **Configure the Backend Service**:
   - **Name**: `ecommerce-backend` (or any name you prefer)
   - **Environment**: `Node`
   - **Build Command**: `cd server && npm install`
   - **Start Command**: `cd server && npm start`
   - **Plan**: Free (or choose a paid plan)

4. **Set Environment Variables** (in the Environment tab):
   ```
   NODE_ENV=production
   PORT=10000
   DB_PATH=./data/ecommerce.db
   ADMIN_USERNAME=your_admin_username
   ADMIN_PASSWORD=your_secure_password
   JWT_SECRET=your_very_secure_random_secret_key_here
   CLIENT_ORIGIN=https://your-admin-panel-url.onrender.com
   BASE_URL=https://your-backend-url.onrender.com
   ```

   **Important Notes**:
   - Generate a strong `JWT_SECRET` (you can use: `openssl rand -base64 32`)
   - Set a secure `ADMIN_PASSWORD`
   - For `CLIENT_ORIGIN` and `BASE_URL`, you'll need to update these after both services are deployed with the actual Render URLs

5. **Click "Create Web Service"**

6. **Wait for deployment** and note the URL (e.g., `https://ecommerce-backend-xxxx.onrender.com`)

### Step 2: Deploy the Admin Panel

1. **Go to Render Dashboard** → Click "New +" → Select "Static Site"

2. **Configure the Static Site**:
   - **Name**: `ecommerce-admin-panel` (or any name you prefer)
   - **Build Command**: `cd admin-panel && npm install && npm run build`
   - **Publish Directory**: `admin-panel/dist`
   - **Environment**: `Node`

3. **Set Environment Variables**:
   ```
   VITE_API_URL=https://your-backend-url.onrender.com/api
   ```
   Replace `your-backend-url.onrender.com` with your actual backend URL from Step 1.

4. **Click "Create Static Site"**

5. **Wait for deployment** and note the URL (e.g., `https://ecommerce-admin-panel-xxxx.onrender.com`)

### Step 3: Update Environment Variables

After both services are deployed, you need to update the environment variables:

1. **Update Backend Service**:
   - Go to your backend service settings
   - Update `CLIENT_ORIGIN` to: `https://your-admin-panel-url.onrender.com`
   - Update `BASE_URL` to: `https://your-backend-url.onrender.com`
   - Save and redeploy

2. **Update Admin Panel** (if needed):
   - The `VITE_API_URL` should already be set correctly
   - If you need to change it, update the environment variable and redeploy

### Step 4: Database Initialization

The backend will automatically create the database on first run. However, if you need to run migrations:

1. Go to your backend service in Render
2. Open the "Shell" tab
3. Run: `cd server && npm run migrate`

## Important Notes

### Database Persistence

⚠️ **Warning**: On Render's free tier, the filesystem is **ephemeral**. This means:
- Your SQLite database will be **lost** when the service restarts or redeploys
- For production, consider:
  - Upgrading to a paid plan with persistent disk
  - Using a managed database (PostgreSQL, MySQL) instead of SQLite
  - Using Render's PostgreSQL service (free tier available)

### CORS Configuration

Make sure `CLIENT_ORIGIN` in the backend matches your admin panel URL exactly (including `https://`).

### File Uploads

The `uploads` directory is also ephemeral on the free tier. Consider:
- Using cloud storage (AWS S3, Cloudinary, etc.) for file uploads
- Storing file paths in the database and serving from external storage

### Auto-Deploy

By default, Render will auto-deploy on every push to your main branch. You can configure this in the service settings.

## Troubleshooting

### Backend won't start
- Check the logs in Render dashboard
- Verify all environment variables are set
- Ensure `PORT` is set to `10000` (Render's default)

### Admin panel can't connect to backend
- Verify `VITE_API_URL` is set correctly
- Check CORS settings in backend (`CLIENT_ORIGIN`)
- Ensure backend URL includes `https://` protocol

### Database issues
- Check if the `data` directory exists
- Verify database file permissions
- Check logs for SQLite errors

## Alternative: Using render.yaml (Blueprint)

If you prefer using Render Blueprints:

1. Push `render.yaml` to your repository
2. In Render Dashboard → "New +" → "Blueprint"
3. Connect your repository
4. Render will detect `render.yaml` and create both services
5. You'll still need to set the environment variables manually in the dashboard

## Support

For Render-specific issues, check:
- [Render Documentation](https://render.com/docs)
- [Render Community](https://community.render.com)

