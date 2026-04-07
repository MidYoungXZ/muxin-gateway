---
name: "OPSX: Sync"
description: 将变更中的增量规格同步到主规格
category: Workflow
tags: [workflow, specs, experimental]
---

将变更中的增量规格同步到主规格。

这是一个**代理驱动的操作** - 你将读取增量规格并直接编辑主规格来应用更改。这允许智能合并（例如，添加一个场景而不需要复制整个需求）。

**输入**：可选择在 `/opsx:sync` 后指定变更名称（例如 `/opsx:sync add-auth`）。如果省略，检查是否可以从对话上下文推断。如果模糊或有歧义，你必须提示可用的变更。

**步骤**

1. **如果没有提供变更名称，提示选择**

   运行 `openspec list --json` 获取可用变更。使用 **AskUserQuestion 工具** 让用户选择。

   显示有增量规格（在 `specs/` 目录下）的变更。

   **重要**：不要猜测或自动选择变更。始终让用户选择。

2. **查找增量规格**

   在 `openspec/changes/<name>/specs/*/spec.md` 中查找增量规格文件。

   每个增量规格文件包含以下部分：
   - `## ADDED Requirements` - 要添加的新需求
   - `## MODIFIED Requirements` - 对现有需求的修改
   - `## REMOVED Requirements` - 要移除的需求
   - `## RENAMED Requirements` - 要重命名的需求（FROM:/TO: 格式）

   如果没有找到增量规格，通知用户并停止。

3. **对于每个增量规格，将更改应用到主规格**

   对于 `openspec/changes/<name>/specs/<capability>/spec.md` 中的每个增量规格：

   a. **读取增量规格**以了解预期的更改

   b. **读取主规格** `openspec/specs/<capability>/spec.md`（可能尚不存在）

   c. **智能应用更改**：

      **ADDED Requirements：**
      - 如果需求在主规格中不存在 → 添加它
      - 如果需求已存在 → 更新它以匹配（视为隐式的 MODIFIED）

      **MODIFIED Requirements：**
      - 在主规格中找到该需求
      - 应用更改 - 可以是：
        - 添加新场景（不需要复制已有的场景）
        - 修改已有场景
        - 更改需求描述
      - 保留增量中未提及的场景/内容

      **REMOVED Requirements：**
      - 从主规格中移除整个需求块

      **RENAMED Requirements：**
      - 找到 FROM 需求，重命名为 TO

   d. **如果能力不存在则创建新的主规格**：
      - 创建 `openspec/specs/<capability>/spec.md`
      - 添加 Purpose 部分（可以简短，标记为 TBD）
      - 添加 Requirements 部分和 ADDED 需求

4. **显示总结**

   应用所有更改后，总结：
   - 更新了哪些能力
   - 做了什么更改（需求添加/修改/移除/重命名）

**增量规格格式参考**

```markdown
## ADDED Requirements

### Requirement: New Feature
The system SHALL do something new.

#### Scenario: Basic case
- **WHEN** user does X
- **THEN** system does Y

## MODIFIED Requirements

### Requirement: Existing Feature
#### Scenario: New scenario to add
- **WHEN** user does A
- **THEN** system does B

## REMOVED Requirements

### Requirement: Deprecated Feature

## RENAMED Requirements

- FROM: `### Requirement: Old Name`
- TO: `### Requirement: New Name`
```

**核心原则：智能合并**

与程序化合并不同，你可以应用**部分更新**：
- 要添加一个场景，只需在 MODIFIED 下包含该场景 - 不需要复制已有场景
- 增量代表*意图*，不是整体替换
- 使用你的判断合理地合并更改

**成功时的输出**

```
## 规格已同步：<change-name>

已更新的主规格：

**<capability-1>**：
- 添加需求："New Feature"
- 修改需求："Existing Feature"（添加了 1 个场景）

**<capability-2>**：
- 创建了新的规格文件
- 添加需求："Another Feature"

主规格已更新。变更仍保持活跃 - 实施完成后归档。
```

**注意事项**
- 在更改之前先读取增量和主规格
- 保留增量中未提及的已有内容
- 如果有不明确的，要求澄清
- 在操作时展示你正在更改的内容
- 操作应该是幂等的 - 运行两次应该得到相同结果
