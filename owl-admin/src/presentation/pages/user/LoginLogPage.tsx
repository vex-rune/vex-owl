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

const formatDateTime = (timestamp?: number) => {
  if (!timestamp) return '-';
  return new Date(timestamp).toLocaleString('zh-CN');
};

const LoginTypeBadge = styled.span`
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  background-color: ${AppColors.info}20;
  color: ${AppColors.info};
`;

export const LoginLogPage: React.FC = () => {
  const navigate = useNavigate();
  const { loginLogs, isLoading, error, fetchLoginLogs } = useUserStore();

  useEffect(() => {
    fetchLoginLogs();
  }, [fetchLoginLogs]);

  const columns = ['日志ID', '主体ID', '账号ID', '登录方式', '登录时间'];
  const rows = loginLogs.map((log) => [
    <span style={{ fontSize: '12px' }}>{log.id}</span>,
    <LinkText onClick={() => navigate(`/users/detail/${log.subjectId}`)}>{log.subjectId}</LinkText>,
    <span style={{ fontSize: '12px' }}>{log.accountId || '-'}</span>,
    <LoginTypeBadge>{log.loginType || '-'}</LoginTypeBadge>,
    formatDateTime(log.loginTime),
  ]);

  return (
    <PageContainer
      title="登录日志"
      actions={
        <button onClick={fetchLoginLogs} style={{ padding: '8px', background: 'none', border: 'none', cursor: 'pointer' }}>
          🔄
        </button>
      }
    >
      <DataTableView
        columns={columns}
        rows={rows}
        isLoading={isLoading}
        error={error || undefined}
        onRetry={fetchLoginLogs}
      />
    </PageContainer>
  );
};