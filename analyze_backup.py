#!/usr/bin/env python3
import re

with open(r'backend\scripts\relay-backup-pre-v20-repair.sql', 'r', encoding='utf-8', errors='ignore') as f:
    content = f.read()

# Find all CREATE INDEX statements 
pattern = r'(?:KEY|CREATE INDEX)\s+(?:IF NOT EXISTS\s+)?`?(\w+)`?\s*\(([^)]+)\)'
matches = re.finditer(pattern, content, re.IGNORECASE)

print("=== Indexes in backup ===\n")
for match in matches:
    index_name = match.group(1)
    if any(x in index_name.lower() for x in ['idx_messages_channel', 'idx_messages_parent', 'idx_tasks_workspace', 'idx_notifications_recipient', 'idx_task_activity', 'idx_file']):
        columns = match.group(2).strip()
        print(f"{index_name}: ({columns})")
        
# Also try to find the actual CREATE INDEX SQL statements used in V20 migration (if in backup)
print("\n=== Looking for migration V20 content ===\n")
v20_pattern = r'add_performance_indexes|idx_messages_channel_created_desc|idx_tasks_workspace_status'
if re.search(v20_pattern, content, re.IGNORECASE):
    print("Found potential V20 markers in backup")
    # Try to extract context
    idx = content.find('idx_messages_channel_created_desc')
    if idx > 0:
        snippet = content[max(0, idx-200):idx+200]
        print(f"\nContext around idx_messages_channel_created_desc:")
        print(snippet)
else:
    print("No direct V20 migration markers found in backup schema")
    print("(Backup may contain schema snapshot, not original SQL)")
