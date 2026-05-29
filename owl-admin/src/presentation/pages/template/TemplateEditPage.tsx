import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import styled from 'styled-components';
import { PageContainer } from '../../widgets/common/PageContainer';
import { templateService } from '../../../core/services/templateService';
import { TemplateEntity, TemplateUpdateRequest } from '../../../data/models/templateModels';
import { AppColors } from '../../../core/theme/appColors';

const Form = styled.form`
  max-width: 600px;
`;

const FormGroup = styled.div`
  margin-bottom: 20px;
`;

const Label = styled.label`
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: ${AppColors.textPrimary};
  margin-bottom: 8px;
`;

const Input = styled.input`
  width: 100%;
  padding: 12px 16px;
  border: 1px solid ${AppColors.border};
  border-radius: 8px;
  font-size: 14px;

  &:focus {
    outline: none;
    border-color: ${AppColors.primary};
  }
`;

const TextArea = styled.textarea`
  width: 100%;
  padding: 12px 16px;
  border: 1px solid ${AppColors.border};
  border-radius: 8px;
  font-size: 14px;
  min-height: 120px;
  resize: vertical;

  &:focus {
    outline: none;
    border-color: ${AppColors.primary};
  }
`;

const CheckboxLabel = styled.label`
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: ${AppColors.textPrimary};
  cursor: pointer;
`;

const ButtonGroup = styled.div`
  display: flex;
  gap: 12px;
  margin-top: 24px;
`;

const Button = styled.button<{ variant?: 'primary' | 'secondary' }>`
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  background-color: ${({ variant }) => (variant === 'primary' ? AppColors.primary : 'transparent')};
  color: ${({ variant }) => (variant === 'primary' ? '#FFFFFF' : AppColors.textSecondary)};
  border: ${({ variant }) => (variant === 'primary' ? 'none' : `1px solid ${AppColors.border}`)};
  cursor: pointer;

  &:hover {
    opacity: 0.9;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
`;

const ErrorMessage = styled.div`
  background-color: ${AppColors.error}10;
  color: ${AppColors.error};
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  margin-bottom: 16px;
`;

export const TemplateEditPage: React.FC = () => {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();
  const isNewMode = id === 'new';
  const [template, setTemplate] = useState<TemplateEntity | null>(null);
  const [isLoading, setIsLoading] = useState(!isNewMode);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    content: '',
    remark: '',
    enabled: true,
  });

  useEffect(() => {
    if (!isNewMode && id) {
      const fetchTemplate = async () => {
        try {
          const data = await templateService.getTemplate(id);
          setTemplate(data);
          setFormData({
            name: data.name,
            content: data.content || '',
            remark: data.remark || '',
            enabled: data.enabled ?? true,
          });
        } catch (err) {
          setError(err instanceof Error ? err.message : '加载失败');
        } finally {
          setIsLoading(false);
        }
      };
      fetchTemplate();
    }
  }, [id, isNewMode]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaving(true);
    setError(null);
    try {
      const request: TemplateUpdateRequest = {
        name: formData.name,
        content: formData.content,
        remark: formData.remark || undefined,
        enabled: formData.enabled,
      };
      if (isNewMode) {
        await templateService.createTemplate(request);
      } else if (id) {
        await templateService.updateTemplate(id, request);
      }
      navigate('/templates');
    } catch (err) {
      setError(err instanceof Error ? err.message : '保存失败');
    } finally {
      setIsSaving(false);
    }
  };

  if (isLoading) {
    return (
      <PageContainer title={isNewMode ? '新建模板' : '编辑模板'}>
        <div style={{ textAlign: 'center', padding: '48px', color: AppColors.textSecondary }}>加载中...</div>
      </PageContainer>
    );
  }

  return (
    <PageContainer title={isNewMode ? '新建模板' : '编辑模板'}>
      {error && <ErrorMessage>{error}</ErrorMessage>}
      <Form onSubmit={handleSubmit}>
        <FormGroup>
          <Label htmlFor="name">模板名称 *</Label>
          <Input
            id="name"
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
            placeholder="请输入模板名称"
            required
          />
        </FormGroup>
        <FormGroup>
          <Label htmlFor="content">模板内容</Label>
          <TextArea
            id="content"
            value={formData.content}
            onChange={(e) => setFormData({ ...formData, content: e.target.value })}
            placeholder="请输入模板内容，支持 {{参数名}} 占位符"
          />
        </FormGroup>
        <FormGroup>
          <Label htmlFor="remark">备注</Label>
          <Input
            id="remark"
            value={formData.remark}
            onChange={(e) => setFormData({ ...formData, remark: e.target.value })}
            placeholder="请输入备注信息"
          />
        </FormGroup>
        <FormGroup>
          <CheckboxLabel>
            <input
              type="checkbox"
              checked={formData.enabled}
              onChange={(e) => setFormData({ ...formData, enabled: e.target.checked })}
            />
            启用此模板
          </CheckboxLabel>
        </FormGroup>
        <ButtonGroup>
          <Button type="submit" variant="primary" disabled={isSaving}>
            {isSaving ? '保存中...' : '保存'}
          </Button>
          <Button type="button" onClick={() => navigate('/templates')}>
            取消
          </Button>
        </ButtonGroup>
      </Form>
    </PageContainer>
  );
};