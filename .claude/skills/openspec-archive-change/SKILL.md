---
name: openspec-archive-change
description: Archive a completed change in the experimental workflow. Use when the user wants to finalize and archive a change after implementation is complete.
license: MIT
compatibility: Requires openspec CLI.
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.2.0"
---

归档已完成的变更。

**输入**：可选变更名称。省略时推断，模糊时提示。

**步骤**

1. **选择变更** - 仅显示活跃变更，让用户选择
2. **检查产物完成**: `openspec status --change "<name>" --json` → 未完成时警告并确认
3. **检查任务完成**: 读取 tasks.md → 未完成时警告并确认
4. **评估增量规范同步**:
   - 有增量规范时：比较增量和主规范，显示合并摘要，提示同步选项
   - 用户选同步时调用 openspec-sync-specs
5. **执行归档**: `mv openspec/changes/<name> openspec/changes/archive/YYYY-MM-DD-<name>`
6. **显示摘要**

**保护措施**

- 始终提示选择变更
- 警告不阻止归档，只通知并确认
- 保留 .openspec.yaml（随目录移动）
- 有增量规范时始终先评估同步状态
