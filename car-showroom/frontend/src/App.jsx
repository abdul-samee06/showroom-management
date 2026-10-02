import { NavLink, Navigate, Outlet, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import LoginPage from './pages/LoginPage.jsx';
import ForbiddenPage from './pages/ForbiddenPage.jsx';
import VehiclesPage from './pages/VehiclesPage.jsx';
import VehicleFormPage from './pages/VehicleFormPage.jsx';
import UsersPage from './pages/UsersPage.jsx';
import UserFormPage from './pages/UserFormPage.jsx';
import RolesPage from './pages/RolesPage.jsx';
import RoleFormPage from './pages/RoleFormPage.jsx';

/** Header with the links the user may open, plus Logout. */
function Layout() {
  const { user, hasPermission, logout } = useAuth();
  return (
    <>
      <header className="topbar">
        <div className="brand">Car Showroom</div>
        <nav>
          {hasPermission('VEHICLE_VIEW') && <NavLink to="/vehicles">Vehicles</NavLink>}
          {hasPermission('USER_VIEW') && <NavLink to="/users">Users</NavLink>}
          {hasPermission('ROLE_VIEW') && <NavLink to="/roles">Roles</NavLink>}
        </nav>
        <div className="switcher">
          <span className="who">{user.name} · {user.showroomName}</span>
          <button className="btn ghost" onClick={logout}>Log out</button>
        </div>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </>
  );
}

const guard = (permission, element) => <ProtectedRoute permission={permission}>{element}</ProtectedRoute>;

export default function App() {
  const { user, firstAllowedPath } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forbidden" element={<ForbiddenPage />} />

      <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route path="/vehicles" element={guard('VEHICLE_VIEW', <VehiclesPage />)} />
        <Route path="/vehicles/new" element={guard('VEHICLE_CREATE', <VehicleFormPage />)} />
        <Route path="/vehicles/:id/edit" element={guard('VEHICLE_EDIT', <VehicleFormPage />)} />
        <Route path="/users" element={guard('USER_VIEW', <UsersPage />)} />
        <Route path="/users/new" element={guard('USER_CREATE', <UserFormPage />)} />
        <Route path="/users/:id/edit" element={guard('USER_EDIT', <UserFormPage />)} />
        <Route path="/roles" element={guard('ROLE_VIEW', <RolesPage />)} />
        <Route path="/roles/new" element={guard('ROLE_MANAGE', <RoleFormPage />)} />
        <Route path="/roles/:id/edit" element={guard('ROLE_MANAGE', <RoleFormPage />)} />
      </Route>

      <Route path="*" element={<Navigate to={user ? firstAllowedPath() : '/login'} replace />} />
    </Routes>
  );
}
