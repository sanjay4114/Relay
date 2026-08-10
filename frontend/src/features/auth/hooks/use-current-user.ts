import { useQuery } from '@tanstack/react-query'
import { getMe } from '../api/auth-api'

export function useCurrentUser() {
  return useQuery({
    queryKey: ['currentUser'],
    queryFn: getMe,
    staleTime: 1000 * 60 * 60, // 1 hour
  })
}
