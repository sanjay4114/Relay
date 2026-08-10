import { axiosInstance } from '@/shared/api/axios-instance';

export interface SearchResultDto {
  id: string;
  type: 'MESSAGE' | 'CHANNEL' | 'USER' | 'WORKSPACE' | 'FILE' | 'TASK';
  title: string;
  subtitle: string;
  url?: string;
  imageUrl?: string;
  createdAt: string;
  metadata: Record<string, any>;
}

export interface UnifiedSearchResponse {
  results: SearchResultDto[];
  page: number;
  size: number;
  hasNext: boolean;
}

export const searchApi = {
  search: async (
    query: string,
    workspaceId?: string,
    type?: string,
    page = 0,
    size = 20
  ): Promise<UnifiedSearchResponse> => {
    const params = new URLSearchParams({
      query,
      page: page.toString(),
      size: size.toString(),
    });
    if (workspaceId) params.append('workspaceId', workspaceId);
    if (type) params.append('type', type);

    const response = await axiosInstance.get(`/search?${params.toString()}`);
    return response.data.data;
  },
};
