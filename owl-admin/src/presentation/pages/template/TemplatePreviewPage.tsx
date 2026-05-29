import React, { useEffect, useState, useRef } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import styled from 'styled-components';
import { PageContainer } from '../../widgets/common/PageContainer';
import { templateService } from '../../../core/services/templateService';
import { TemplateEntity } from '../../../data/models/templateModels';
import { AppColors } from '../../../core/theme/appColors';

const PreviewContainer = styled.div`
  max-width: 800px;
`;

const Card = styled.div`
  background-color: ${AppColors.cardBg};
  border: 1px solid ${AppColors.border};
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
`;

const CardTitle = styled.h3`
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

const ContentBox = styled.div`
  background-color: ${AppColors.bgSecondary};
  padding: 16px;
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  min-height: 100px;
`;

const ParamSection = styled.div`
  margin-top: 24px;
`;

const ParamLabel = styled.label`
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: ${AppColors.textPrimary};
  margin-bottom: 8px;
`;

const ParamInput = styled.input`
  width: 100%;
  max-width: 300px;
  padding: 8px 12px;
  border: 1px solid ${AppColors.border};
  border-radius: 6px;
  font-size: 14px;
  margin-bottom: 12px;

  &:focus {
    outline: none;
    border-color: ${AppColors.primary};
  }
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 24px;
`;

const Button = styled.button`
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  background-color: ${AppColors.primary};
  color: white;
  border: none;
  cursor: pointer;

  &:hover {
    background-color: ${AppColors.primaryDark};
  }
`;

const SecondaryButton = styled.button`
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  background-color: transparent;
  color: ${AppColors.textSecondary};
  border: 1px solid ${AppColors.border};
  cursor: pointer;

  &:hover {
    background-color: ${AppColors.bgTertiary};
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

const extractParams = (content: string): string[] => {
  const regex = /\{\{(\w+)\}\}/g;
  const matches = content.matchAll(regex);
  return [...new Set([...matches].map(m => m[1]))];
};

export const TemplatePreviewPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const [template, setTemplate] = useState<TemplateEntity | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [params, setParams] = useState<Record<string, string>>({});
  const [previewContent, setPreviewContent] = useState('');
  const paramInputRefs = useRef<Record<string, HTMLInputElement | null>>({});

  useEffect(() => {
    const fetchTemplate = async () => {
      try {
        const data = await templateService.getTemplate(id!);
        setTemplate(data);
        updatePreview(data.content || '', {});
      } catch (err) {
        setError(err instanceof Error ? err.message : '加载失败');
      } finally {
        setIsLoading(false);
      }
    };
    fetchTemplate();
  }, [id]);

  const updatePreview = (content: string, paramValues: Record<string, string>) => {
    let result = content;
    for (const [key, value] of Object.entries(paramValues)) {
      result = result.replace(new RegExp(`\\{\\{${key}\\}\\}`, 'g'), value || `[${key}]`);
    }
    setPreviewContent(result);
  };

  const handleParamChange = (paramName: string, value: string) => {
    const newParams = { ...params, [paramName]: value };
    setParams(newParams);
    if (template) {
      updatePreview(template.content || '', newParams);
    }
  };

  if (isLoading) {
    return (
      <PageContainer title="模板预览">
        <div style={{ textAlign: 'center', padding: '48px', color: AppColors.textSecondary }}>加载中...</div>
      </PageContainer>
    );
  }

  if (error || !template) {
    return (
      <PageContainer title="模板预览">
        <div style={{ textAlign: 'center', padding: '48px', color: AppColors.error }}>加载失败: {error || '模板不存在'}</div>
      </PageContainer>
    );
  }

  const paramNames = extractParams(template.content || '');
  for (const param of paramNames) {
    if (!params[param]) {
      params[param] = '';
    }
  }

  return (
    <PageContainer title="模板预览">
      <PreviewContainer>
        <Card>
          <CardTitle>模板信息</CardTitle>
          <InfoGrid>
            <InfoItem>
              <InfoLabel>模板名称</InfoLabel>
              <InfoValue>{template.name}</InfoValue>
            </InfoItem>
            <InfoItem>
              <InfoLabel>编码</InfoLabel>
              <InfoValue>{template.code}</InfoValue>
            </InfoItem>
            <InfoItem>
              <InfoLabel>状态</InfoLabel>
              <StatusBadge active={template.enabled}>{template.enabled ? '启用' : '禁用'}</StatusBadge>
            </InfoItem>
            <InfoItem>
              <InfoLabel>创建时间</InfoLabel>
              <InfoValue>{template.createdAt ? new Date(template.createdAt).toLocaleString('zh-CN') : '-'}</InfoValue>
            </InfoItem>
          </InfoGrid>
        </Card>

        <Card>
          <CardTitle>原内容</CardTitle>
          <ContentBox>{template.content || '暂无内容'}</ContentBox>
        </Card>

        {paramNames.length > 0 && (
          <Card>
            <CardTitle>参数设置</CardTitle>
            <ParamSection>
              {paramNames.map((param) => (
                <div key={param}>
                  <ParamLabel>{param}</ParamLabel>
                  <ParamInput
                    ref={(el) => { paramInputRefs.current[param] = el; }}
                    value={params[param] || ''}
                    onChange={(e) => handleParamChange(param, e.target.value)}
                    placeholder={`请输入 ${param} 的值`}
                  />
                </div>
              ))}
            </ParamSection>
          </Card>
        )}

        <Card>
          <CardTitle>预览效果</CardTitle>
          <ContentBox>{previewContent || template.content || '暂无内容'}</ContentBox>
        </Card>

        <ButtonGroup>
          <Button onClick={() => navigate(`/templates/edit/${id}`)}>编辑</Button>
          <SecondaryButton onClick={() => navigate('/templates')}>返回</SecondaryButton>
        </ButtonGroup>
      </PreviewContainer>
    </PageContainer>
  );
};