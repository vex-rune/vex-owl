import styled from 'styled-components';
import { AppColors } from '../../../core/theme/appColors';

export const Container = styled.div`
  padding: 16px 24px;
  border-bottom: 1px solid ${AppColors.border};
  background-color: ${AppColors.bgPrimary};
  display: flex;
  align-items: center;
  justify-content: space-between;
`;

export const Title = styled.h2`
  font-size: 20px;
  font-weight: 600;
  color: ${AppColors.textPrimary};
  margin: 0;
`;

export const Actions = styled.div`
  display: flex;
  gap: 8px;
`;

export const Content = styled.div`
  flex: 1;
  background-color: ${AppColors.bgSecondary};
  padding: 24px;
  overflow: auto;
`;

export const Card = styled.div`
  background-color: ${AppColors.cardBg};
  border: 1px solid ${AppColors.border};
  border-radius: 12px;
  padding: 24px;
`;

export const Table = styled.table`
  width: 100%;
  border-collapse: collapse;

  th {
    background-color: ${AppColors.bgTertiary};
    padding: 12px 16px;
    text-align: left;
    font-weight: 600;
    color: ${AppColors.textPrimary};
  }

  td {
    padding: 12px 16px;
    border-bottom: 1px solid ${AppColors.border};
    color: ${AppColors.textSecondary};
  }

  tr:hover td {
    background-color: ${AppColors.bgSecondary};
  }
`;

export const Button = styled.button<{ variant?: 'primary' | 'secondary' | 'danger' }>`
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  background-color: ${({ variant }) => {
    switch (variant) {
      case 'primary':
        return AppColors.primary;
      case 'danger':
        return AppColors.error;
      default:
        return 'transparent';
    }
  }};
  color: ${({ variant }) => {
    switch (variant) {
      case 'primary':
      case 'danger':
        return '#FFFFFF';
      default:
        return AppColors.primary;
    }
  }};
  border: ${({ variant }) => (variant ? 'none' : `1px solid ${AppColors.primary}`)};

  &:hover {
    opacity: 0.9;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

export const TextButton = styled.button`
  padding: 4px 8px;
  background: none;
  color: ${AppColors.primary};
  font-size: 14px;

  &:hover {
    text-decoration: underline;
  }
`;

export const Input = styled.input`
  padding: 12px 16px;
  border: 1px solid ${AppColors.border};
  border-radius: 8px;
  font-size: 14px;
  width: 100%;
  background-color: ${AppColors.bgPrimary};

  &:focus {
    outline: none;
    border-color: ${AppColors.primary};
    box-shadow: 0 0 0 2px ${AppColors.primaryLight}40;
  }
`;

export const FormGroup = styled.div`
  margin-bottom: 16px;
`;

export const Label = styled.label`
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: ${AppColors.textPrimary};
`;

export const ErrorText = styled.span`
  color: ${AppColors.error};
  font-size: 12px;
  margin-top: 4px;
`;

export const LoadingContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  color: ${AppColors.textSecondary};
`;

export const ErrorContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px;
  color: ${AppColors.error};
`;

export const StatusBadge = styled.span<{ active?: boolean }>`
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  background-color: ${({ active }) => active ? AppColors.success + '20' : AppColors.textTertiary + '20'};
  color: ${({ active }) => active ? AppColors.success : AppColors.textSecondary};
`;