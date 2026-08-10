import { useState, useRef, KeyboardEvent } from 'react'
import { SendHorizonal, Paperclip, X, FileIcon, Loader2 } from 'lucide-react'
import { Button } from '@/shared/ui/button'
import { fileApi, FileAttachment } from '../api/file-api'
import { toast } from 'sonner'

interface MessageComposerProps {
  channelName: string
  onSend: (content: string, attachmentIds?: string[]) => void
  disabled?: boolean
}

export function MessageComposer({ channelName, onSend, disabled }: MessageComposerProps) {
  const [content, setContent] = useState('')
  const [attachments, setAttachments] = useState<FileAttachment[]>([])
  const [isUploading, setIsUploading] = useState(false)
  
  const textareaRef = useRef<HTMLTextAreaElement>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)

  const handleSend = () => {
    const trimmed = content.trim()
    if ((!trimmed && attachments.length === 0) || disabled || isUploading) return
    onSend(trimmed, attachments.map(a => a.publicId))
    setContent('')
    setAttachments([])
    
    // Reset height
    if (textareaRef.current) {
      textareaRef.current.style.height = 'auto'
    }
  }

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = Array.from(e.target.files || [])
    if (files.length === 0) return

    setIsUploading(true)

    try {
      for (const file of files) {
        if (file.size > 50 * 1024 * 1024) {
          toast.error(`File ${file.name} is too large. Max 50MB.`)
          continue
        }
        const uploaded = await fileApi.uploadFile(file)
        setAttachments(prev => [...prev, uploaded])
      }
    } catch (err: any) {
      toast.error('Failed to upload file')
    } finally {
      setIsUploading(false)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  const removeAttachment = (publicId: string) => {
    setAttachments(prev => prev.filter(a => a.publicId !== publicId))
  }

  const handleKeyDown = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend()
    }
  }

  const handleInput = (e: React.FormEvent<HTMLTextAreaElement>) => {
    const target = e.currentTarget
    target.style.height = 'auto'
    target.style.height = `${Math.min(target.scrollHeight, 250)}px`
    setContent(target.value)
  }

  return (
    <div className="p-4 bg-background">
      <div className="max-w-4xl mx-auto rounded-xl border bg-muted/20 shadow-sm focus-within:ring-1 focus-within:ring-primary focus-within:border-primary transition-all p-1 flex flex-col relative">
        
        {/* Attachments Preview Area */}
        {attachments.length > 0 && (
          <div className="flex gap-2 p-2 flex-wrap border-b mb-1">
            {attachments.map(att => (
              <div key={att.publicId} className="relative group border rounded-md p-2 flex items-center gap-2 bg-background shadow-sm w-48">
                {att.mimeType.startsWith('image/') && att.thumbnailUrl ? (
                  <img src={att.thumbnailUrl} alt="preview" className="h-10 w-10 object-cover rounded" />
                ) : (
                  <div className="h-10 w-10 bg-primary/10 text-primary flex items-center justify-center rounded">
                    <FileIcon className="h-5 w-5" />
                  </div>
                )}
                <div className="flex-1 min-w-0">
                  <p className="text-xs font-medium truncate" title={att.originalName}>{att.originalName}</p>
                  <p className="text-[10px] text-muted-foreground">{(att.fileSize / 1024 / 1024).toFixed(2)} MB</p>
                </div>
                <button 
                  onClick={() => removeAttachment(att.publicId)}
                  className="absolute -top-2 -right-2 bg-destructive text-destructive-foreground rounded-full p-1 opacity-0 group-hover:opacity-100 transition-opacity hover:scale-110"
                >
                  <X className="h-3 w-3" />
                </button>
              </div>
            ))}
          </div>
        )}
        <textarea
          ref={textareaRef}
          value={content}
          onChange={(e) => setContent(e.target.value)}
          onKeyDown={handleKeyDown}
          onInput={handleInput}
          disabled={disabled}
          placeholder={`Message #${channelName}`}
          className="w-full min-h-[44px] max-h-[250px] resize-none bg-transparent px-3 py-3 text-[15px] outline-none placeholder:text-muted-foreground disabled:cursor-not-allowed disabled:opacity-50"
          rows={1}
        />
        <div className="flex items-center justify-between p-2">
          <div className="flex items-center gap-1">
            <input 
              type="file" 
              multiple 
              className="hidden" 
              ref={fileInputRef} 
              onChange={handleFileSelect}
            />
            <Button 
              variant="ghost" 
              size="icon" 
              className="h-8 w-8 text-muted-foreground hover:text-foreground"
              onClick={() => fileInputRef.current?.click()}
              disabled={disabled || isUploading}
              title="Attach files"
            >
              {isUploading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Paperclip className="h-4 w-4" />}
            </Button>
          </div>
          
          <Button 
            size="icon" 
            variant={(content.trim() || attachments.length > 0) ? "default" : "ghost"}
            className={`h-8 w-8 rounded-md transition-all ${(content.trim() || attachments.length > 0) ? '' : 'opacity-50'}`}
            onClick={handleSend}
            disabled={(!content.trim() && attachments.length === 0) || disabled || isUploading}
          >
            <SendHorizonal className="h-4 w-4" />
          </Button>
        </div>
      </div>
      <div className="max-w-4xl mx-auto mt-2 text-center">
        <p className="text-[11px] text-muted-foreground/70">
          <strong>Return</strong> to send <span className="mx-1">•</span> <strong>Shift + Return</strong> to add a new line
        </p>
      </div>
    </div>
  )
}
