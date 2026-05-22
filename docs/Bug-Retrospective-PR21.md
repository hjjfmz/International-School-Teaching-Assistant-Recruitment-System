# Bug 复盘：PR #21 合并导致 main 编译失败

## 事件概述

2026年4月6日晚，PR #21 合并后引入了破坏性提交，导致 `main` 分支编译失败，所有人无法运行项目。4月7日早发现并通过 hotfix 修复。

---

## 时间线

| 时间 | 事件 | 影响 |
|------|------|------|
| 4/4 | PR #18 merged（yiying-zhang 修 MO 按钮重叠） | ✅ main 稳定，SHA `f33183a` |
| 4/6 下午 | tdxb423 在 `feat/ui-i18n-enhancement` 分支开发 I18n，改了 27 个文件 | 开发中，未影响 main |
| 4/6 18:58 | **Stephen-QwQ** 提交 `6a8cc31` 到 `zhuolinLi/add_namelist_to_readme` 分支 | ❌ 破坏了 AdminWorkloadPage 和 I18n |
| 4/6 同期 | **whitebird11111** 提交 `5a665b3`、`4771e40` 到同一分支 | ❌ 进一步破坏 AdminWorkloadPage 和 AdminUserManagementPage |
| 4/6 18:49 UTC | **PR #20 merged**（tdxb423 的 I18n + UI 美化） | ✅ main 正常 |
| 4/6 19:04 UTC | **PR #21 merged**（zhuolinLi 分支，包含上述破坏性提交） | ❌ **main 编译失败** |
| 4/7 08:11 | 同学反映"admin 进不去" | 问题暴露 |
| 4/7 08:30 | 诊断确认：main 编译失败（133 个错误），非登录/权限问题 | 定位根因 |
| 4/7 08:38 | 推送 hotfix `bb24663`，恢复 3 个被损坏文件 | ✅ **main 编译恢复正常** |

---

## 根因分析

### 直接原因

PR #21 合并时带入了 3 个破坏性 commit，导致 `main` 上 3 个文件编译不过：

| 文件 | 破坏者 | commit | 具体破坏 |
|------|--------|--------|----------|
| `AdminWorkloadPage.java` | Stephen-QwQ → whitebird11111 | `6a8cc31`, `4771e40` | 删了 import、破坏文件结构、删了 `category` 字段但引用未删 |
| `I18n.java` | Stephen-QwQ | `6a8cc31` | 替换了 30 行翻译，打乱了 i18n 键值结构 |
| `AdminUserManagementPage.java` | whitebird11111 | `5a665b3` | 478 行差异，移除了部分功能 |

### 深层原因

1. **提交前未拉取最新 main** — 基于旧版本修改，与 PR #20 的大量改动冲突
2. **提交前未本地编译验证** — `AdminWorkloadPage.java` 编译都过不了就推了上去
3. **PR 合并时未做编译检查** — 仓库 owner 直接点了 merge，没有 CI 保护
4. **不相关代码塞进无关分支** — `zhuolinLi/add_namelist_to_readme` 本来只是改 README 名单，却被塞入了 UI 和翻译改动

---

## 责任人

| GitHub ID | 邮箱 | 破坏性提交 | 问题 |
|-----------|------|------------|------|
| **Stephen-QwQ** | 569923883@qq.com | `6a8cc31` | 主要破坏者：改坏了 AdminWorkloadPage 结构 + 打乱 I18n 翻译 |
| **whitebird11111** | — | `4771e40`, `5a665b3` | 在已损坏基础上进一步破坏，删字段不删引用 |

> 注：PR #19（Stephen 的多语种提案）目前仍 open 未合并，不影响 main。

---

## 修复措施

通过 GitHub API 直接推送 hotfix commit `bb24663` 到 main：
- 用 PR #20 合并后的正确版本覆盖了 3 个被损坏文件
- 验证：编译通过 ✅，应用正常启动 ✅

### 修复后对比（本地 vs main）

| 对比项 | 结果 |
|--------|------|
| src/ 下所有 .java 文件 | **仅 `App.java` 有内容差异**（本地有 pickFont 等增强，远端是旧版） |
| 其余 42 个 .java 文件 | ✅ 内容完全一致（仅换行符 CRLF/LF 差异） |
| images/ 资源文件 | ✅ 完全一致 |

### 待确认

- `App.java` 本地版本包含 `pickFont` 字体选择增强、更多 UIManager 设置，是否需要同步到 main

---

## 团队协作规范（建议）

为避免类似问题再次发生，建议团队遵守以下规范：

### 提交前

1. **`git pull origin main`** — 每次修改前先拉取最新代码
2. **本地编译验证** — 推送前必须确认编译通过：
   ```
   javac -encoding UTF-8 -d out -sourcepath src src/ebu6304/App.java
   ```
3. **本地运行测试** — 确认修改的功能可以正常使用

### 分支管理

4. **不要往无关分支塞代码** — README 分支不放 UI 改动，翻译分支不改页面结构
5. **一个 PR 只做一件事** — 避免多人多功能混在同一个 PR

### 合并前

6. **Reviewer 必须本地编译验证** — 不要直接在 GitHub 上点 merge
7. **有冲突必须本地解决后再推** — 不要强制合并

---

## 经验教训

- 多人协作项目中，**编译通过是最低底线**，任何提交都不应该破坏编译
- **即使改动很小，也要先拉取最新代码**，尤其是在有大型 PR（如 27 文件的 I18n）刚合并之后
- 建议后续引入简单的 CI 检查（至少做 `javac` 编译验证）

---

*文档创建时间：2026-04-07 08:44*
*修复 commit：`bb24663`*
*GitHub main 当前 SHA：`bb2466399c7ce4a4555f7df9a493864c4d66e289`*
