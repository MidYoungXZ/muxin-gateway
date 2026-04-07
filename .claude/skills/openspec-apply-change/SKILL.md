---
name: openspec-apply-change
description: Implement tasks from an OpenSpec change. Use when the user wants to start implementing, continue implementation, or work through tasks.
license: MIT
compatibility: Requires openspec CLI.
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.2.0"
---

实施 OpenSpec 变更中的任务。

**输入**：可选变更名称。省略时推断，模糊时提示。

**步骤**

1. **选择变更** - 提供名称/推断/只有一个自动选/模糊时让用户选
2. **检查状态**: `openspec status --change "<name>" --json`
3. **获取指令**: `openspec instructions apply --change "<name>" --json`
   - `state: "blocked"` → 建议先完成产物
   - `state: "all_done"` → 建议归档
4. **读取上下文文件** - 从指令输出的 `contextFiles`
5. **循环实施任务**:
   - 显示当前任务
   - 代码更改保持最小和专注
   - 完成后标记 `- [ ]` → `- [x]`
   - 暂停条件：不明确/设计问题/错误/用户中断

**保护措施**

- 持续处理直到完成或受阻
- 开始前阅读上下文文件
- 任务模糊时暂停询问
- 揭示设计问题时建议更新产物
- 每个任务完成后立即更新复选框
- 使用 CLI 输出的 contextFiles，不假设文件名
