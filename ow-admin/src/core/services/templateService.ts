import { apiClient } from '../utils/apiClient';
import { ApiConstants } from '../constants/apiConstants';
import { TemplateEntity, TemplateUpdateRequest } from '../../data/models/templateModels';
import { QueriesPageRequest } from '../../data/models/queryModels';

export class TemplateService {
  async queryTemplates(request: QueriesPageRequest): Promise<TemplateEntity[]> {
    return apiClient.post<TemplateEntity[]>(ApiConstants.templateQuery, request);
  }

  async getTemplate(id: string): Promise<TemplateEntity> {
    return apiClient.get<TemplateEntity>(`${ApiConstants.template}/${id}`);
  }

  async getTemplateByCode(code: string): Promise<TemplateEntity> {
    return apiClient.get<TemplateEntity>(`${ApiConstants.templateByCode}/${code}`);
  }

  async createTemplate(data: Record<string, unknown>): Promise<TemplateEntity> {
    return apiClient.post<TemplateEntity>(ApiConstants.template, data);
  }

  async updateTemplate(id: string, request: TemplateUpdateRequest): Promise<TemplateEntity> {
    return apiClient.put<TemplateEntity>(`${ApiConstants.template}/${id}`, request);
  }

  async deleteTemplate(id: string): Promise<void> {
    return apiClient.delete<void>(`${ApiConstants.template}/${id}`);
  }
}

export const templateService = new TemplateService();