# 🚀 Deployment Guide for Ecommerce App

This project consists of:
- **Backend API** (Node.js/Express) - Located in `server/`
- **Admin Panel** (React/Vite) - Located in `admin-panel/`

## 📋 Files Created for Deployment

1. **`render.yaml`** - Render Blueprint configuration (optional)
2. **`.renderignore`** - Files to exclude from deployment
3. **`RENDER_DEPLOYMENT.md`** - Detailed deployment instructions
4. **`DEPLOYMENT_QUICK_START.md`** - Quick reference guide

## 🎯 Quick Start

**For fastest deployment, follow:** [`DEPLOYMENT_QUICK_START.md`](./DEPLOYMENT_QUICK_START.md)

**For detailed instructions, see:** [`RENDER_DEPLOYMENT.md`](./RENDER_DEPLOYMENT.md)

## 📝 Pre-Deployment Checklist

- [ ] Code is pushed to Git repository (GitHub/GitLab/Bitbucket)
- [ ] You have a Render account
- [ ] You have generated a secure JWT secret
- [ ] You have chosen secure admin credentials

## 🔑 Environment Variables Summary

### Backend Required Variables:
```
NODE_ENV=production
PORT=10000
DB_PATH=./data/ecommerce.db
ADMIN_USERNAME=your_admin_username
ADMIN_PASSWORD=your_secure_password
JWT_SECRET=your_generated_secret
CLIENT_ORIGIN=https://your-admin-panel-url.onrender.com
BASE_URL=https://your-backend-url.onrender.com
```

### Admin Panel Required Variables:
```
VITE_API_URL=https://your-backend-url.onrender.com/api
```

## ⚠️ Important Warnings

1. **Database Persistence**: On Render's free tier, SQLite database is **ephemeral** and will be lost on restart. Consider:
   - Upgrading to a paid plan
   - Using Render's PostgreSQL service (free tier available)
   - Migrating to a managed database

2. **File Uploads**: Uploaded files are also ephemeral. Consider using cloud storage (S3, Cloudinary, etc.)

3. **CORS**: Ensure `CLIENT_ORIGIN` exactly matches your admin panel URL (including `https://`)

## 🛠️ Changes Made for Production

1. **Server Config** (`server/src/config.js`):
   - Updated database path handling for production
   - Uses `server/data/` in production, `data/` in development

2. **Server Uploads** (`server/src/server.js` & `server/src/middleware/upload.js`):
   - Updated upload paths for production environment
   - Creates uploads directory within server folder in production

3. **Build Configuration**:
   - Admin panel builds to `admin-panel/dist/`
   - Backend uses standard Node.js start command

## 📞 Need Help?

- Check Render logs in the dashboard
- Review [Render Documentation](https://render.com/docs)
- See troubleshooting section in `RENDER_DEPLOYMENT.md`

## 🎉 After Deployment

1. Test admin panel login
2. Verify API endpoints are accessible
3. Test file uploads (remember: ephemeral on free tier)
4. Monitor logs for any issues

Good luck with your deployment! 🚀

