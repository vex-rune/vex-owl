import { createBrowserRouter, Navigate } from 'react-router-dom';
import { LoginPage } from '../presentation/pages/login/LoginPage';
import { HomePage } from '../presentation/pages/home/HomePage';
import { UserListPage } from '../presentation/pages/user/UserListPage';
import { AccountListPage } from '../presentation/pages/user/AccountListPage';
import { UserDetailPage } from '../presentation/pages/user/UserDetailPage';
import { LoginLogPage } from '../presentation/pages/user/LoginLogPage';
import { TemplateListPage } from '../presentation/pages/template/TemplateListPage';
import { TemplateEditPage } from '../presentation/pages/template/TemplateEditPage';
import { TemplatePreviewPage } from '../presentation/pages/template/TemplatePreviewPage';
import { storage } from '../core/utils/storage';

const RequireAuth = ({ children }: { children: React.ReactNode }) => {
  if (!storage.isLoggedIn()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
};

const RequireGuest = ({ children }: { children: React.ReactNode }) => {
  if (storage.isLoggedIn()) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
};

export const router = createBrowserRouter([
  {
    path: '/login',
    element: (
      <RequireGuest>
        <LoginPage />
      </RequireGuest>
    ),
  },
  {
    path: '/',
    element: (
      <RequireAuth>
        <HomePage />
      </RequireAuth>
    ),
    children: [
      {
        index: true,
        element: <Navigate to="/users" replace />,
      },
      {
        path: 'users',
        children: [
          {
            index: true,
            element: <UserListPage />,
          },
          {
            path: 'accounts',
            element: <AccountListPage />,
          },
          {
            path: 'detail/:subjectId',
            element: <UserDetailPage />,
          },
          {
            path: 'login-logs',
            element: <LoginLogPage />,
          },
        ],
      },
      {
        path: 'templates',
        children: [
          {
            index: true,
            element: <TemplateListPage />,
          },
          {
            path: 'edit/:id',
            element: <TemplateEditPage />,
          },
          {
            path: 'preview/:id',
            element: <TemplatePreviewPage />,
          },
        ],
      },
    ],
  },
]);