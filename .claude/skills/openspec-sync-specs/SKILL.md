---
name: openspec-sync-specs
description: Sync delta specs from a change to main specs. Use when the user wants to update main specs with changes from a delta spec, without archiving the change.
license: MIT
compatibility: Requires openspec CLI.
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.2.0"
---

将增量规范从变更同步到主规范（代理驱动智能合并）。

**输入**：可选变更名称。省略时推断，模糊时提示。

**步骤**

1. **选择变更** - 显示有增量规格的变更，让用户选择
2. **查找增量规范**: `openspec/changes/<name>/specs/*/spec.md`
   - 无增量规范时通知并停止
3. **对每个增量规范智能合并到主规范**:
   - 读取增量和主规范
   - **ADDED**: 不存在则添加，已存在则更新
   - **MODIFIED**: 找到需求，应用部分更新（添加场景、修改描述），保留未提及内容
   - **REMOVED**: 移除整个需求块
   - **RENAMED**: FROM → TO
   - 能力不存在时创建新主规范
4. **显示摘要** - 更新了哪些能力，做了什么更改

**关键原则：智能合并** - 增量代表意图而非整体替换，支持部分更新。

**保护措施**: 读写后再改、保留未提及内容、不明确时询问、操作幂等。
