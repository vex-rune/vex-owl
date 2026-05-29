export interface TemplateEntity {
  id: string;
  name: string;
  code: string;
  content?: string;
  remark?: string;
  enabled?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface TemplateUpdateRequest {
  name?: string;
  content?: string;
  remark?: string;
  enabled?: boolean;
}