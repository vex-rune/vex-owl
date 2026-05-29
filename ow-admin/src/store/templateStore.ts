import { create } from 'zustand';
import { templateService } from '../core/services/templateService';
import { TemplateEntity, TemplateUpdateRequest } from '../data/models/templateModels';
import { QueriesPageRequest } from '../data/models/queryModels';

interface TemplateState {
  templates: TemplateEntity[];
  currentTemplate: TemplateEntity | null;
  isLoading: boolean;
  error: string | null;
  fetchTemplates: () => Promise<void>;
  fetchTemplate: (id: string) => Promise<TemplateEntity | null>;
  updateTemplate: (id: string, request: TemplateUpdateRequest) => Promise<void>;
  deleteTemplate: (id: string) => Promise<void>;
}

export const useTemplateStore = create<TemplateState>((set, get) => ({
  templates: [],
  currentTemplate: null,
  isLoading: false,
  error: null,
  fetchTemplates: async () => {
    set({ isLoading: true, error: null });
    try {
      const request: QueriesPageRequest = { page: { page: 0, size: 100 } };
      const templates = await templateService.queryTemplates(request);
      set({ templates, isLoading: false });
    } catch (error) {
      set({ isLoading: false, error: error instanceof Error ? error.message : '加载失败' });
    }
  },
  fetchTemplate: async (id: string) => {
    set({ isLoading: true, error: null });
    try {
      const template = await templateService.getTemplate(id);
      set({ currentTemplate: template, isLoading: false });
      return template;
    } catch (error) {
      set({ isLoading: false, error: error instanceof Error ? error.message : '加载失败' });
      return null;
    }
  },
  updateTemplate: async (id: string, request: TemplateUpdateRequest) => {
    set({ isLoading: true, error: null });
    try {
      await templateService.updateTemplate(id, request);
      set({ isLoading: false });
    } catch (error) {
      set({ isLoading: false, error: error instanceof Error ? error.message : '保存失败' });
    }
  },
  deleteTemplate: async (id: string) => {
    set({ isLoading: true, error: null });
    try {
      await templateService.deleteTemplate(id);
      const templates = get().templates.filter(t => t.id !== id);
      set({ templates, isLoading: false });
    } catch (error) {
      set({ isLoading: false, error: error instanceof Error ? error.message : '删除失败' });
    }
  },
}));