import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { PageContainer } from '../../widgets/common/PageContainer';
import { DataTableView } from '../../widgets/common/DataTableView';
import { useUserStore } from '../../../store/userStore';
import { AppColors } from '../../../core/theme/appColors';

const LinkText = styled.span`
  color: ${AppColors.primary};
  cursor: pointer;
  &:hover {
    text-decoration: underline;
  }
`;

const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleDateString('zh-CN');
};

const StatusBadge = styled.span<{ active?: boolean }>`
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  background-color: ${({ active }) => (active ? AppColors.success + '20' : AppColors.textTertiary + '20')};
  color: ${({ active }) => (active ? AppColors.success : AppColors.textSecondary)};
`;

export const AccountListPage: React.FC = () => {
  const navigate = useNavigate();
  const { accounts, isLoading, error, fetchAccounts } = useUserStore();

  useEffect(() => {
    fetchAccounts();
  }, [fetchAccounts]);

  const columns = ['账号ID', '主体ID', '账号', '类型', '状态', '创建时间'];
  const rows = accounts.map((account) => [
    <span style={{ fontSize: '12px' }}>{account.id}</span>,
    <LinkText onClick={() => navigate(`/users/detail/${account.subjectId}`)}>{account.subjectId}</LinkText>,
    account.account || '-',
    account.type || '-',
    <StatusBadge active={account.enabled}>{account.enabled ? '启用' : '禁用'}</StatusBadge>,
    formatDate(account.createdAt),
  ]);

  return (
    <PageContainer
      title="账号列表"
      actions={
        <button onClick={fetchAccounts} style={{ padding: '8px', background: 'none', border: 'none', cursor: 'pointer' }}>
          🔄
        </button>
      }
    >
      <DataTableView
        columns={columns}
        rows={rows}
        isLoading={isLoading}
        error={error || undefined}
        onRetry={fetchAccounts}
      />
    </PageContainer>
  );
};