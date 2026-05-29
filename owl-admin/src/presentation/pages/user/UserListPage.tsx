import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { PageContainer } from '../../widgets/common/PageContainer';
import { DataTableView } from '../../widgets/common/DataTableView';
import { useUserStore } from '../../../store/userStore';
import { AppColors } from '../../../core/theme/appColors';

const CardList = styled.div`
  display: flex;
  flex-direction: column;
  gap: 12px;
`;

const Card = styled.div`
  background-color: ${AppColors.cardBg};
  border: 1px solid ${AppColors.border};
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
`;

const CardTitle = styled.div`
  font-size: 16px;
  font-weight: 600;
  color: ${AppColors.textPrimary};
  margin-bottom: 8px;
`;

const CardMeta = styled.div`
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: ${AppColors.textSecondary};
`;

const StatusBadge = styled.span<{ active?: boolean }>`
  display: inline-block;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  background-color: ${({ active }) => (active ? AppColors.success + '20' : AppColors.textTertiary + '20')};
  color: ${({ active }) => (active ? AppColors.success : AppColors.textSecondary)};
`;

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

const getStatusActive = (status?: string) => status?.toLowerCase() === 'active';

export const UserListPage: React.FC = () => {
  const navigate = useNavigate();
  const { subjects, isLoading, error, fetchSubjects } = useUserStore();

  useEffect(() => {
    fetchSubjects();
  }, [fetchSubjects]);

  const columns = ['ID', '名称', '类型', '状态', '创建时间', '操作'];
  const rows = subjects.map((subject) => [
    <span style={{ fontSize: '12px' }}>{subject.id}</span>,
    subject.name || '-',
    subject.type || '-',
    <StatusBadge active={getStatusActive(subject.status)}>{subject.status || '-'}</StatusBadge>,
    formatDate(subject.createdAt),
    <LinkText onClick={() => navigate(`/users/detail/${subject.id}`)}>查看详情</LinkText>,
  ]);

  return (
    <PageContainer
      title="主体列表"
      actions={
        <button onClick={fetchSubjects} style={{ padding: '8px', background: 'none', border: 'none', cursor: 'pointer' }}>
          🔄
        </button>
      }
    >
      <DataTableView
        columns={columns}
        rows={rows}
        isLoading={isLoading}
        error={error || undefined}
        onRetry={fetchSubjects}
      />
    </PageContainer>
  );
};