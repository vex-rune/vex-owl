import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { AppColors } from '../../../core/theme/appColors';
import { useLoginStore } from '../../../store/loginStore';

const LoginContainer = styled.div`
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, ${AppColors.primary} 0%, ${AppColors.primaryDark} 100%);
`;

const LoginCard = styled.div`
  background: ${AppColors.bgPrimary};
  border-radius: 16px;
  padding: 48px;
  width: 100%;
  max-width: 420px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.15);
`;

const Title = styled.h1`
  font-size: 28px;
  font-weight: 600;
  color: ${AppColors.textPrimary};
  text-align: center;
  margin-bottom: 8px;
`;

const Subtitle = styled.p`
  font-size: 14px;
  color: ${AppColors.textSecondary};
  text-align: center;
  margin-bottom: 32px;
`;

const Form = styled.form`
  display: flex;
  flex-direction: column;
  gap: 20px;
`;

const FormGroup = styled.div`
  display: flex;
  flex-direction: column;
`;

const Label = styled.label`
  font-size: 14px;
  font-weight: 500;
  color: ${AppColors.textPrimary};
  margin-bottom: 8px;
`;

const Input = styled.input`
  padding: 14px 16px;
  border: 1px solid ${AppColors.border};
  border-radius: 8px;
  font-size: 16px;
  transition: all 0.2s;

  &:focus {
    outline: none;
    border-color: ${AppColors.primary};
    box-shadow: 0 0 0 3px ${AppColors.primaryLight}30;
  }
`;

const ErrorMessage = styled.div`
  background-color: ${AppColors.error}10;
  color: ${AppColors.error};
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
`;

const SubmitButton = styled.button`
  padding: 14px 24px;
  background-color: ${AppColors.primary};
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  margin-top: 8px;

  &:hover {
    background-color: ${AppColors.primaryDark};
  }

  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
  }
`;

export const LoginPage: React.FC = () => {
  const navigate = useNavigate();
  const { login, isLoading, error, clearError } = useLoginStore();
  const [principal, setPrincipal] = useState('');
  const [credentials, setCredentials] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();
    await login(principal, credentials);
    if (!error) {
      navigate('/');
    }
  };

  return (
    <LoginContainer>
      <LoginCard>
        <Title>Vex-Owl 管理后台</Title>
        <Subtitle>请登录以继续</Subtitle>
        <Form onSubmit={handleSubmit}>
          {error && <ErrorMessage>{error}</ErrorMessage>}
          <FormGroup>
            <Label htmlFor="principal">账号</Label>
            <Input
              id="principal"
              type="text"
              value={principal}
              onChange={(e) => setPrincipal(e.target.value)}
              placeholder="请输入账号"
              required
            />
          </FormGroup>
          <FormGroup>
            <Label htmlFor="credentials">密码</Label>
            <Input
              id="credentials"
              type="password"
              value={credentials}
              onChange={(e) => setCredentials(e.target.value)}
              placeholder="请输入密码"
              required
            />
          </FormGroup>
          <SubmitButton type="submit" disabled={isLoading}>
            {isLoading ? '登录中...' : '登录'}
          </SubmitButton>
        </Form>
      </LoginCard>
    </LoginContainer>
  );
};