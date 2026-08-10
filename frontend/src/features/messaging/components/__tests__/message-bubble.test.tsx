import { render, screen } from '@testing-library/react'
import { MessageBubble } from '../message-bubble'
import { describe, it, expect, vi } from 'vitest'
import { Message } from '../../api/message-api'
import '@testing-library/jest-dom'

// Mock the hooks
vi.mock('@/features/auth/hooks/use-current-user', () => ({
  useCurrentUser: () => ({ data: { publicId: 'user-1' } })
}))

vi.mock('@/features/workspaces/store/workspace-store', () => ({
  useWorkspaceStore: () => ({ activeWorkspace: { role: 'MEMBER' } })
}))

vi.mock('@tanstack/react-query', () => ({
  useMutation: () => ({ mutate: vi.fn(), isPending: false }),
  useQueryClient: () => ({})
}))

describe('MessageBubble', () => {
  const mockMessage: Message = {
    publicId: 'msg-1',
    channelPublicId: 'chan-1',
    content: 'Hello World',
    messageType: 'TEXT',
    sender: {
      publicId: 'user-2',
      displayName: 'Jane Doe'
    },
    createdAt: new Date().toISOString(),
    updatedAt: new Date().toISOString(),
    replyCount: 0
  }

  it('renders message content correctly', () => {
    render(<MessageBubble message={mockMessage} />)
    
    expect(screen.getByText('Hello World')).toBeInTheDocument()
    expect(screen.getByText('Jane Doe')).toBeInTheDocument()
  })

  it('renders deleted state correctly', () => {
    const deletedMessage = { ...mockMessage, deletedAt: new Date().toISOString() }
    render(<MessageBubble message={deletedMessage} />)
    
    expect(screen.getByText('This message was deleted.')).toBeInTheDocument()
    expect(screen.queryByText('Hello World')).not.toBeInTheDocument()
  })
})
