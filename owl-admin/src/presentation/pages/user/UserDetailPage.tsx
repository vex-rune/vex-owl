import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import styled from 'styled-components';
import { PageContainer } from '../../widgets/common/PageContainer';
import { DataTableView } from '../../widgets/common/DataTableView';
import { userService } from '../../../core/services/userService';
import { SubjectEntity, AccountEntity, LoginRecordEntity } from '../../../data/models/userModels';
import { AppColors } from '../../../core/theme/appColors';

const InfoCard = styled.div`
  background-color: ${AppColors.cardBg};
  border: 1px solid ${AppColors.border};
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
`;

const InfoTitle = styled.h3`
  font-size: 16px;
  font-weight: 600;
  color: ${AppColors.textPrimary};
  margin-bottom: 16px;
`;

const InfoGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
`;

const InfoItem = styled.div`
  display: flex;
  flex-direction: column;
`;

const InfoLabel = styled.span`
  font-size: 12px;
  color: ${AppColors.textSecondary};
  margin-bottom: 4px;
`;

const InfoValue = styled.span`
  font-size: 14px;
  color: ${AppColors.textPrimary};
`;

const TabsContainer = styled.div`
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
`;

const Tab = styled.button<{ active?: boolean }>`
  padding: 8px 16px;
  border: 1px solid ${({ active }) => (active ? AppColors.primary : AppColors.border)};
  border-radius: 8px;
  background-color: ${({ active }) => (active ? AppColors.primary : 'transparent')};
  color: ${({ active }) => (active ? '#FFFFFF' : AppColors.textSecondary)};
  cursor: pointer;
  font-size: 14px;

  &:hover {
    background-color: ${({ active }) => (active ? AppColors.primaryDark : AppColors.bgTertiary)};
  }
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

const formatDate = (timestamp?: number) => {
  if (!timestamp) return '-';
  return new Date(timestamp).toLocaleString('zh-CN');
};

const formatDateStr = (dateStr?: string) => {
  if (!dateStr) return '-';
  return new Date(dateStr).toLocaleDateString('zh-CN');
};

export const UserDetailPage: React.FC = () => {
  const { subjectId } = useParams<{ subjectId: string }>();
  const [user, setUser] = useState<any>(null);
  const [subjects, setSubjects] = useState<SubjectEntity[]>([]);
  const [accounts, setAccounts] = useState<AccountEntity[]>([]);
  const [logs, setLogs] = useState<LoginRecordEntity[]>([]);
  const [activeTab, setActiveTab] = useState<'info' | 'accounts' | 'logs'>('info');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      if (!subjectId) return;
      setIsLoading(true);
      setError(null);
      try {
        const [userData, subjectsData, accountsData, logsData] = await Promise.all([
          userService.getUser(subjectId).catch(() => null),
          userService.querySubjects({ predicate: [{ field: 'id', op: 'eq', value: subjectId }], page: { page: 0, size: 1 } }).catch(() => []),
          userService.queryAccounts({ predicate: [{ field: 'subjectId', op: 'eq', value: subjectId }], page: { page: 0, size: 100 } }).catch(() => []),
          userService.queryLoginLogs({ predicate: [{ field: 'subjectId', op: 'eq', value: subjectId }], page: { page: 0, size: 100 }, order: [{ field: 'loginTime', direction: 'desc' }] }).catch(() => []),
        ]);
        setUser(userData);
        setSubjects(subjectsData);
        setAccounts(accountsData);
        setLogs(logsData);
      } catch (err) {
        setError(err instanceof Error ? err.message : '加载失败');
      } finally {
        setIsLoading(false);
      }
    };
    fetchData();
  }, [subjectId]);

  if (isLoading) {
    return (
      <PageContainer title="用户详情">
        <div style={{ textAlign: 'center', padding: '48px', color: AppColors.textSecondary }}>加载中...</div>
      </PageContainer>
    );
  }

  if (error) {
    return (
      <PageContainer title="用户详情">
        <div style={{ textAlign: 'center', padding: '48px', color: AppColors.error }}>加载失败: {error}</div>
      </PageContainer>
    );
  }

  const subject = subjects[0];

  return (
    <PageContainer title="用户详情">
      <TabsContainer>
        <Tab active={activeTab === 'info'} onClick={() => setActiveTab('info')}>基本信息</Tab>
        <Tab active={activeTab === 'accounts'} onClick={() => setActiveTab('accounts')}>账号列表</Tab>
        <Tab active={activeTab === 'logs'} onClick={() => setActiveTab('logs')}>登录日志</Tab>
      </TabsContainer>

      {activeTab === 'info' && (
        <InfoCard>
          <InfoTitle>基本信息</InfoTitle>
          <InfoGrid>
            <InfoItem>
              <InfoLabel>用户ID</InfoLabel>
              <InfoValue>{subjectId}</InfoValue>
            </InfoItem>
            <InfoItem>
              <InfoLabel>昵称</InfoLabel>
              <InfoValue>{user?.nickname || '-'}</InfoValue>
            </InfoItem>
            <InfoItem>
              <InfoLabel>邮箱</InfoLabel>
              <InfoValue>{user?.email || '-'}</InfoValue>
            </InfoItem>
            <InfoItem>
              <InfoLabel>手机</InfoLabel>
              <InfoValue>{user?.phone || '-'}</InfoValue>
            </InfoItem>
            <InfoItem>
              <InfoLabel>状态</InfoLabel>
              <StatusBadge active={user?.status === 'active'}>{user?.status || '-'}</StatusBadge>
            </InfoItem>
            <InfoItem>
              <InfoLabel>主体名称</InfoLabel>
              <InfoValue>{subject?.name || '-'}</InfoValue>
            </InfoItem>
            <InfoItem>
              <InfoLabel>主体类型</InfoLabel>
              <InfoValue>{subject?.type || '-'}</InfoValue>
            </InfoItem>
          </InfoGrid>
        </InfoCard>
      )}

      {activeTab === 'accounts' && (
        <DataTableView
          columns={['账号ID', '账号', '类型', '状态', '创建时间']}
          rows={accounts.map((account) => [
            <span style={{ fontSize: '12px' }}>{account.id}</span>,
            account.account || '-',
            account.type || '-',
            <StatusBadge active={account.enabled}>{account.enabled ? '启用' : '禁用'}</StatusBadge>,
            formatDateStr(account.createdAt),
          ])}
        />
      )}

      {activeTab === 'logs' && (
        <DataTableView
          columns={['日志ID', '账号ID', '登录方式', '登录时间']}
          rows={logs.map((log) => [
            <span style={{ fontSize: '12px' }}>{log.id}</span>,
            <span style={{ fontSize: '12px' }}>{log.accountId || '-'}</span>,
            log.loginType || '-',
            formatDate(log.loginTime),
          ])}
        />
      )}
    </PageContainer>
  );
};