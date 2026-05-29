import React, { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import { PageContainer } from '../../widgets/common/PageContainer';
import { DataTableView } from '../../widgets/common/DataTableView';
import { useTemplateStore } from '../../../store/templateStore';
import { AppColors } from '../../../core/theme/appColors';

const ButtonGroup = styled.div`
  display: flex;
  gap: 8px;
`;

const LinkText = styled.span`
  color: ${AppColors.primary};
  cursor: pointer;
  &:hover {
    text-decoration: underline;
  }
`;

const DangerText = styled.span`
  color: ${AppColors.error};
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

export const TemplateListPage: React.FC = () => {
  const navigate = useNavigate();
  const { templates, isLoading, error, fetchTemplates, deleteTemplate } = useTemplateStore();

  useEffect(() => {
    fetchTemplates();
  }, [fetchTemplates]);

  const handleDelete = async (id: string) => {
    if (window.confirm('确定要删除这个模板吗？')) {
      await deleteTemplate(id);
    }
  };

  const columns = ['模板名称', '编码', '备注', '状态', '创建时间', '操作'];
  const rows = templates.map((template) => [
    template.name,
    <span style={{ fontSize: '12px' }}>{template.code}</span>,
    <span style={{ fontSize: '12px' }}>{template.remark || '-'}</span>,
    <StatusBadge active={template.enabled}>{template.enabled ? '启用' : '禁用'}</StatusBadge>,
    formatDate(template.createdAt),
    <ButtonGroup>
      <LinkText onClick={() => navigate(`/templates/preview/${template.id}`)}>预览</LinkText>
      <LinkText onClick={() => navigate(`/templates/edit/${template.id}`)}>编辑</LinkText>
      <DangerText onClick={() => handleDelete(template.id)}>删除</DangerText>
    </ButtonGroup>,
  ]);

  return (
    <PageContainer
      title="通知模板"
      actions={
        <button onClick={fetchTemplates} style={{ padding: '8px', background: 'none', border: 'none', cursor: 'pointer' }}>
          🔄
        </button>
      }
    >
      <DataTableView
        columns={columns}
        rows={rows}
        isLoading={isLoading}
        error={error || undefined}
        onRetry={fetchTemplates}
      />
    </PageContainer>
  );
};