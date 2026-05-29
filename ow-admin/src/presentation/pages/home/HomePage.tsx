import React from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import styled from 'styled-components';
import { AppColors } from '../../core/theme/appColors';
import { useLoginStore } from '../../store/loginStore';

const LayoutContainer = styled.div`
  min-height: 100vh;
  display: flex;
  flex-direction: column;
`;

const Header = styled.header`
  height: 64px;
  background-color: ${AppColors.bgPrimary};
  border-bottom: 1px solid ${AppColors.border};
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
`;

const Logo = styled.h1`
  font-size: 18px;
  font-weight: 600;
  color: ${AppColors.textPrimary};
  margin: 0;
`;

const Nav = styled.nav`
  display: flex;
  gap: 8px;
`;

const NavItem = styled.button<{ active?: boolean }>`
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  background-color: ${({ active }) => (active ? AppColors.primary : 'transparent')};
  color: ${({ active }) => (active ? '#FFFFFF' : AppColors.textSecondary)};
  border: none;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    background-color: ${({ active }) => (active ? AppColors.primaryDark : AppColors.bgTertiary)};
  }
`;

const LogoutButton = styled.button`
  padding: 8px 16px;
  border: 1px solid ${AppColors.border};
  border-radius: 8px;
  font-size: 14px;
  background-color: transparent;
  color: ${AppColors.textSecondary};
  cursor: pointer;

  &:hover {
    background-color: ${AppColors.bgTertiary};
  }
`;

const Content = styled.main`
  flex: 1;
  background-color: ${AppColors.bgSecondary};
`;

const navItems = [
  { label: '用户管理', path: '/users' },
  { label: '通知模板', path: '/templates' },
];

export const HomePage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { logout } = useLoginStore();

  const handleNavClick = (path: string) => {
    navigate(path);
  };

  const handleLogout = async () => {
    if (window.confirm('确定要退出登录吗？')) {
      await logout();
      navigate('/login');
    }
  };

  return (
    <LayoutContainer>
      <Header>
        <Logo>Vex-Owl 管理后台</Logo>
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <Nav>
            {navItems.map((item) => (
              <NavItem
                key={item.path}
                active={location.pathname.startsWith(item.path)}
                onClick={() => handleNavClick(item.path)}
              >
                {item.label}
              </NavItem>
            ))}
          </Nav>
          <LogoutButton onClick={handleLogout}>退出</LogoutButton>
        </div>
      </Header>
      <Content>
        <Outlet />
      </Content>
    </LayoutContainer>
  );
};