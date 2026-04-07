---
name: openspec-propose
description: Propose a new change with all artifacts generated in one step. Use when the user wants to quickly describe what they want to build and get a complete proposal with design, specs, and tasks ready for implementation.
license: MIT
compatibility: Requires openspec CLI.
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.2.0"
---

提议新变更 - 创建变更并一次性生成所有产物（proposal、design、tasks）。

准备好实施时，运行 /opsx:apply

---

**输入**：变更名称（kebab-case）或功能描述。无输入时询问用户。

**步骤**

1. **创建变更目录**: `openspec new change "<name>"`
2. **获取构建顺序**: `openspec status --change "<name>" --json` → 解析 `applyRequires` 和 `artifacts`
3. **按依赖顺序创建产物**:
   - 对每个 `ready` 状态的产物：`openspec instructions <artifact-id> --change "<name>" --json`
   - 读取已完成的依赖文件获取上下文
   - 使用 `template` 作为结构创建产物文件
   - `context` 和 `rules` 是对 AI 的约束，**不要**复制到产物中
4. **循环直到所有 `applyRequires` 产物完成**
5. **显示最终状态**: `openspec status --change "<name>"`

**产物创建指南**

- 遵循 `openspec instructions` 的 `instruction` 字段
- 创建前读取依赖产物
- 上下文不明确时询问用户，但优先合理推断保持进度
- 同名变更已存在时询问继续还是新建
- 每个产物写入后验证文件存在
