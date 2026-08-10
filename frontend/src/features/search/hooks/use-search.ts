import { useQuery } from '@tanstack/react-query';
import { searchApi } from '../api/search-api';

export const useGlobalSearch = (query: string, workspaceId?: string, type?: string) => {
  return useQuery({
    queryKey: ['search', query, workspaceId, type],
    queryFn: () => searchApi.search(query, workspaceId, type),
    enabled: query.trim().length >= 2,
    staleTime: 60 * 1000,
  });
};
