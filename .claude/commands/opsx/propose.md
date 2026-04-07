---
name: "OPSX: Propose"
description: 提议新变更 - 一步创建变更并生成所有工件
category: Workflow
tags: [workflow, artifacts, experimental]
---

提议新变更 - 一步创建变更并生成所有工件。

我将创建包含以下工件的变更：
- proposal.md（做什么 & 为什么）
- design.md（怎么做）
- tasks.md（实施步骤）

准备好实施时，运行 /opsx:apply

---

**输入**：`/opsx:propose` 后面的参数是变更名称（kebab-case），或者用户想要构建的功能描述。

**步骤**

1. **如果没有提供输入，询问用户想要构建什么**

   使用 **AskUserQuestion 工具**（开放式的，不预设选项）来询问：
   > "你想要做什么变更？请描述你想要构建或修复的内容。"

   根据描述推导一个 kebab-case 名称（例如 "添加用户认证" → `add-user-auth`）。

   **重要**：在理解用户想要构建什么之前，不要继续。

2. **创建变更目录**
   ```bash
   openspec new change "<name>"
   ```
   这会在 `openspec/changes/<name>/` 创建带有 `.openspec.yaml` 的变更脚手架。

3. **获取工件构建顺序**
   ```bash
   openspec status --change "<name>" --json
   ```
   解析 JSON 获取：
   - `applyRequires`：实施前需要的工件 ID 数组（例如 `["tasks"]`）
   - `artifacts`：所有工件及其状态和依赖关系的列表

4. **按顺序创建工件直到可以实施**

   使用 **TodoWrite 工具** 跟踪工件创建进度。

   按依赖顺序循环处理工件（先处理没有待处理依赖的工件）：

   a. **对于每个 `ready`（依赖已满足）的工件**：
      - 获取指令：
        ```bash
        openspec instructions <artifact-id> --change "<name>" --json
        ```
      - 指令 JSON 包含：
        - `context`：项目背景（对你的约束 - 不要包含在输出中）
        - `rules`：工件特定规则（对你的约束 - 不要包含在输出中）
        - `template`：输出文件的结构模板
        - `instruction`：此工件类型的模式特定指导
        - `outputPath`：工件写入路径
        - `dependencies`：需要读取的已完成工件
      - 读取已完成的依赖文件获取上下文
      - 使用 `template` 作为结构创建工件文件
      - 将 `context` 和 `rules` 作为约束应用 - 但不要将它们复制到文件中
      - 简要显示进度："Created <artifact-id>"

   b. **继续直到所有 `applyRequires` 工件完成**
      - 创建每个工件后，重新运行 `openspec status --change "<name>" --json`
      - 检查 `applyRequires` 中的每个工件 ID 在 artifacts 数组中是否 `status: "done"`
      - 当所有 `applyRequires` 工件完成时停止

   c. **如果工件需要用户输入**（上下文不明确）：
      - 使用 **AskUserQuestion 工具** 来澄清
      - 然后继续创建

5. **显示最终状态**
   ```bash
   openspec status --change "<name>"
   ```

**输出**

完成所有工件后，总结：
- 变更名称和位置
- 创建的工件列表及简要说明
- 就绪状态："所有工件已创建！可以开始实施。"
- 提示："运行 `/opsx:apply` 开始实施。"

**工件创建指南**

- 遵循 `openspec instructions` 中每种工件类型的 `instruction` 字段
- 模式定义了每个工件应包含的内容 - 遵循它
- 在创建新工件之前读取依赖工件获取上下文
- 使用 `template` 作为输出文件的结构 - 填充其各个部分
- **重要**：`context` 和 `rules` 是对你的约束，不是文件内容
  - 不要将 `<context>`、`<rules>`、`<project_context>` 块复制到工件中
  - 它们指导你写什么，但不应出现在输出中

**注意事项**
- 创建实施所需的所有工件（由模式的 `apply.requires` 定义）
- 在创建新工件之前始终读取依赖工件
- 如果上下文严重不清晰，询问用户 - 但优先做出合理决定以保持进度
- 如果同名变更已存在，询问用户是想继续还是创建新的
- 写入后验证每个工件文件是否存在，再继续下一个
