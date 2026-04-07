---
name: openspec-verify-change
description: Verify implementation matches change artifacts. Use when the user wants to validate that implementation is complete, correct, and coherent before archiving.
license: MIT
compatibility: Requires openspec CLI.
metadata:
  author: openspec
  version: "1.0"
  generatedBy: "1.2.0"
---

验证实现是否与变更产物（规范、任务、设计）匹配。

**输入**：可选变更名称。省略时推断，模糊时提示。

**步骤**

1. **选择变更** - 始终让用户选择，不自动选取
2. **检查状态**: `openspec status --change "<name>" --json`
3. **加载产物**: `openspec instructions apply --change "<name>" --json` → 读取 contextFiles
4. **三维度验证**:
   - **完整性**: 任务复选框完成度 + 增量规格覆盖（搜索代码库确认需求已实现）
   - **正确性**: 需求实施映射 + 场景覆盖检查
   - **一致性**: 设计决策遵循度 + 代码模式一致性
5. **生成报告** - 总结评分卡 + 按优先级列出问题 (CRITICAL/WARNING/SUGGESTION)

**验证启发式**

- 完整性：专注客观检查项
- 正确性：关键词搜索+合理推断，不要求完美确定性
- 一致性：找明显矛盾，不挑剔风格
- 不确定时优先 SUGGESTION > WARNING > CRITICAL
- 每个问题必须有具体建议

**优雅降级**

- 仅 tasks.md → 只验证任务完成度
- tasks + specs → 验证完整性和正确性
- 完整产物 → 验证三个维度
