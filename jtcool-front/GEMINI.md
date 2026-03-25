# JtCool Front-End Project

## Project Overview

JtCool (v3.9.1) is the front-end application for a Java rapid development framework based on SpringBoot and Vue 3 with a separated front-end and back-end architecture. This front-end is built with Vue 3, Vite, and Element Plus, designed for a management system interface.

Key technologies used:
- **Vue 3** (Composition API)
- **Vite** (Build tool and dev server)
- **Element Plus** (UI Component library)
- **Vue Router** (Routing)
- **Pinia** (State management)
- **ECharts** (Data visualization)

The project includes various built-in features for a complete management system:
- User, Department, Role, and Menu Management
- Dictionary and Parameter Configuration
- Operation and Login Logs
- Online User Monitoring
- Scheduled Tasks
- Code Generation
- Server and Cache Monitoring
- Online Form Builder

## Building and Running

This project uses `npm` (or `yarn`/`pnpm` depending on your environment, but `package.json` suggests standard Node.js package managers) for dependency management.

**Install Dependencies:**
```bash
npm install
```

**Development Server:**
Start the Vite development server. It runs on port 80 by default and proxies `/dev-api` requests to a backend server at `http://localhost:8080`.
```bash
npm run dev
```

**Production Build:**
Build the project for production environments. The output will be placed in the `dist` directory.
```bash
npm run build:prod
```

**Staging Build:**
Build the project for staging environments.
```bash
npm run build:stage
```

**Preview Production Build:**
Locally preview the production build output.
```bash
npm run preview
```

## Directory Structure & Architecture

The project follows a standard Vue/Vite application structure:

- `src/api/`: Contains Axios request modules categorized by business domains (e.g., system, monitor, wms, oms, product).
- `src/assets/`: Static assets like images, SVG icons, and global SCSS styles.
- `src/components/`: Reusable, globally available Vue components (e.g., FileUpload, DictTag, Pagination).
- `src/layout/`: The main layout components defining the overall application structure (Sidebar, Navbar, Main Content area).
- `src/router/`: Vue Router configuration, defining page routes.
- `src/store/`: Pinia state management modules.
- `src/utils/`: Utility functions and helpers (e.g., request handling, authentication logic, dynamic title).
- `src/views/`: Page-level Vue components representing different routes/screens in the application.

## Development Conventions

- **Component Structure:** The project makes heavy use of Vue 3 Single File Components (`.vue`).
- **Styling:** SCSS is used for styling (`sass-embedded` is a dependency). Global variables and mixins are located in `src/assets/styles/`.
- **API Requests:** API calls are modularized under `src/api/` and likely utilize an Axios instance configured in `src/utils/request.js`.
- **Environment Variables:** The project uses standard Vite environment variables (`.env.development`, `.env.production`, `.env.staging`).

## Backend API Dependencies

Based on the README, the dashboard (Personal Workbench) relies on specific backend APIs under the `/workbench/` path:
- `/workbench/overview`: Fetches overview statistics.
- `/workbench/pendingOrders`: Fetches pending orders list.
- `/workbench/pendingApprovals`: Fetches pending approvals.
- `/workbench/pendingInventory`: Fetches pending inventory tasks.
