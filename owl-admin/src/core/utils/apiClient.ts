import axios, { AxiosInstance, AxiosError } from 'axios';
import { AppConstants } from '../constants/appConstants';
import { storage } from './storage';

export class ApiException extends Error {
  constructor(message: string, public statusCode?: number) {
    super(message);
    this.name = 'ApiException';
  }
}

interface ApiResponse<T> {
  code: number;
  data: T;
  message?: string;
  success?: boolean;
}

class ApiClient {
  private client: AxiosInstance;

  constructor() {
    this.client = axios.create({
      baseURL: AppConstants.apiBaseUrl,
      timeout: AppConstants.httpTimeout,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.client.interceptors.request.use(async (config) => {
      const token = storage.getToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
      return config;
    });

    this.client.interceptors.response.use(
      (response) => response,
      (error: AxiosError) => {
        if (error.response?.status === 401) {
          storage.clearAuth();
          window.location.href = '/login';
        }
        return Promise.reject(error);
      }
    );
  }

  async get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
    try {
      const response = await this.client.get<ApiResponse<T>>(url, { params });
      const data = response.data;
      if (data.code === 0 || data.success) {
        return data.data;
      }
      throw new ApiException(data.message || '请求失败');
    } catch (error) {
      if (error instanceof AxiosError) {
        const message = error.response?.data?.message || error.message || '网络错误';
        throw new ApiException(message, error.response?.status);
      }
      throw error;
    }
  }

  async post<T>(url: string, data?: unknown): Promise<T> {
    try {
      const response = await this.client.post<ApiResponse<T>>(url, data);
      const responseData = response.data;
      if (responseData.code === 0 || responseData.success) {
        return responseData.data;
      }
      throw new ApiException(responseData.message || '请求失败');
    } catch (error) {
      if (error instanceof AxiosError) {
        const message = error.response?.data?.message || error.message || '网络错误';
        throw new ApiException(message, error.response?.status);
      }
      throw error;
    }
  }

  async put<T>(url: string, data?: unknown): Promise<T> {
    try {
      const response = await this.client.put<ApiResponse<T>>(url, data);
      const responseData = response.data;
      if (responseData.code === 0 || responseData.success) {
        return responseData.data;
      }
      throw new ApiException(responseData.message || '请求失败');
    } catch (error) {
      if (error instanceof AxiosError) {
        const message = error.response?.data?.message || error.message || '网络错误';
        throw new ApiException(message, error.response?.status);
      }
      throw error;
    }
  }

  async delete<T>(url: string): Promise<T> {
    try {
      const response = await this.client.delete<ApiResponse<T>>(url);
      const responseData = response.data;
      if (responseData.code === 0 || responseData.success) {
        return responseData.data;
      }
      throw new ApiException(responseData.message || '请求失败');
    } catch (error) {
      if (error instanceof AxiosError) {
        const message = error.response?.data?.message || error.message || '网络错误';
        throw new ApiException(message, error.response?.status);
      }
      throw error;
    }
  }
}

export const apiClient = new ApiClient();