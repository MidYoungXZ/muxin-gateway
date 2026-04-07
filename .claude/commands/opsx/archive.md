---
name: "OPSX: Archive"
description: 在实验性工作流中归档已完成的变更
category: Workflow
tags: [workflow, archive, experimental]
---

在实验性工作流中归档已完成的变更。

**输入**：可选择在 `/opsx:archive` 后指定变更名称（例如 `/opsx:archive add-auth`）。如果省略，检查是否可以从对话上下文推断。如果模糊或有歧义，你必须提示可用的变更。

**步骤**

1. **如果没有提供变更名称，提示选择**

   运行 `openspec list --json` 获取可用变更。使用 **AskUserQuestion 工具** 让用户选择。

   只显示活跃变更（尚未归档的）。
   如果可用，显示每个变更使用的模式。

   **重要**：不要猜测或自动选择变更。始终让用户选择。

2. **检查工件完成状态**

   运行 `openspec status --change "<name>" --json` 检查工件完成情况。

   解析 JSON 以了解：
   - `schemaName`：正在使用的工作流
   - `artifacts`：工件列表及其状态（`done` 或其他）

   **如果有工件未完成（不是 `done`）：**
   - 显示警告，列出未完成的工件
   - 提示用户确认是否继续
   - 用户确认后继续

3. **检查任务完成状态**

   读取任务文件（通常是 `tasks.md`）检查未完成的任务。

   统计标记为 `- [ ]`（未完成）vs `- [x]`（已完成）的任务。

   **如果发现未完成任务：**
   - 显示警告，显示未完成任务数量
   - 提示用户确认是否继续
   - 用户确认后继续

   **如果不存在任务文件：** 直接继续，不显示任务相关警告。

4. **评估增量规格同步状态**

   检查 `openspec/changes/<name>/specs/` 中的增量规格。如果不存在，直接继续，不提示同步。

   **如果存在增量规格：**
   - 将每个增量规格与对应的主规格 `openspec/specs/<capability>/spec.md` 进行比较
   - 确定会应用哪些更改（添加、修改、移除、重命名）
   - 在提示前显示合并摘要

   **提示选项：**
   - 如果需要更改："立即同步（推荐）"，"不同步直接归档"
   - 如果已同步："立即归档"，"仍然同步"，"取消"

   如果用户选择同步，使用 Task 工具（subagent_type: "general-purpose"，prompt: "使用 Skill 工具调用 openspec-sync-specs 处理变更 '<name>'。增量规格分析：<包含分析的增量规格摘要>"）。无论选择什么，继续归档。

5. **执行归档**

   如果归档目录不存在则创建：
   ```bash
   mkdir -p openspec/changes/archive
   ```

   使用当前日期生成目标名称：`YYYY-MM-DD-<change-name>`

   **检查目标是否已存在：**
   - 如果是：失败并显示错误，建议重命名已有归档或使用不同的日期
   - 如果否：移动变更目录到归档

   ```bash
   mv openspec/changes/<name> openspec/changes/archive/YYYY-MM-DD-<name>
   ```

6. **显示总结**

   显示归档完成总结，包括：
   - 变更名称
   - 使用的模式
   - 归档位置
   - 规格同步状态（已同步 / 跳过同步 / 无增量规格）
   - 任何警告的说明（未完成的工件/任务）

**成功时的输出**

```
## 归档完成

**变更：** <change-name>
**模式：** <schema-name>
**归档到：** openspec/changes/archive/YYYY-MM-DD-<name>/
**规格：** ✓ 已同步到主规格

所有工件完成。所有任务完成。
```

**成功时的输出（无增量规格）**

```
## 归档完成

**变更：** <change-name>
**模式：** <schema-name>
**归档到：** openspec/changes/archive/YYYY-MM-DD-<name>/
**规格：** 无增量规格

所有工件完成。所有任务完成。
```

**成功但有警告时的输出**

```
## 归档完成（有警告）

**变更：** <change-name>
**模式：** <schema-name>
**归档到：** openspec/changes/archive/YYYY-MM-DD-<name>/
**规格：** 同步已跳过（用户选择跳过）

**警告：**
- 归档时有 2 个未完成的工件
- 归档时有 3 个未完成的任务
- 增量规格同步被跳过（用户选择跳过）

如果不是有意为之，请审查归档内容。
```

**归档失败时的输出（目标已存在）**

```
## 归档失败

**变更：** <change-name>
**目标：** openspec/changes/archive/YYYY-MM-DD-<name>/

目标归档目录已存在。

**选项：**
1. 重命名已有归档
2. 如果是重复的则删除已有归档
3. 等到不同日期再归档
```

**注意事项**
- 如果未提供变更，始终提示选择
- 使用工件图（openspec status --json）进行完成度检查
- 不要因警告阻止归档 - 只需通知并确认
- 移动到归档时保留 .openspec.yaml（它会随目录一起移动）
- 显示清晰的总结说明发生了什么
- 如果请求同步，使用 Skill 工具调用 `openspec-sync-specs`（代理驱动）
- 如果存在增量规格，始终在提示前运行同步评估并显示合并摘要
