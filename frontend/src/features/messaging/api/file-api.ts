import { apiClient } from '@/shared/api/client'
import type { ApiResponse } from '@/shared/types/api'

export interface FileAttachment {
  publicId: string
  originalName: string
  mimeType: string
  extension: string
  fileSize: number
  url: string
  thumbnailUrl?: string
}

export const fileApi = {
  uploadFile: async (file: File, onUploadProgress?: (progressEvent: any) => void): Promise<FileAttachment> => {
    const formData = new FormData()
    formData.append('file', file)
    const { data } = await apiClient.post<ApiResponse<FileAttachment>>('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress
    })
    return data.data
  }
}
