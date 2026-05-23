# BUPT TA 招聘系统 — 系统说明文档

> 版本：v1.0 | 更新：2026-05-23

---

## 一、系统概述

BUPT 国际学院助教（TA）招聘系统是一款 Java Swing 桌面应用，面向三类用户（Admin / MO / TA）提供 TA 岗位的发布、申请、审批全流程管理。

---

## 二、架构层次

```
┌──────────────────────────────────┐
│          UI 层（Swing）           │
│  AdminPanel / MOPanel / TAPanel  │
│  AppLayout（导航 + 顶栏 + 状态栏）│
└────────────┬─────────────────────┘
             │
┌────────────▼─────────────────────┐
│        业务/存储层                │
│  DataService（统一数据入口）      │
│  AuthStore（用户认证 XML 读写）   │
│  OperationLog（操作日志追加）     │
│  MiniJson / Csv / XmlStore       │
└────────────┬─────────────────────┘
             │
┌────────────▼─────────────────────┐
│          数据文件（data/）        │
│  见第三节                        │
└──────────────────────────────────┘
```

---

## 三、数据存储位置与格式（⭐ 核心）

所有数据文件位于项目根目录的 `data/` 文件夹。

### 3.1 实际使用的文件（DataService 读写）

| 文件 | 格式 | 内容 | 读写方 |
|------|------|------|--------|
| `admin_system.xml` | XML | 用户账户（Admin/MO/TA 三种角色）、系统配置、工作量配置 | AuthStore + DataService |
| `ta_info.csv` | CSV（逗号分隔，含表头）| TA 申请人档案：`id,name,email,skills,cvPath,description` | DataService |
| `mo_jobs.json` | JSON | 职位信息 + 嵌套的申请记录（`applications` 数组内含每个申请的状态）| DataService |
| `cv/` | 目录 | 简历 PDF 文件，命名规则：`{applicantId}.pdf` | DataService.storeCv() |
| `temp_operation.txt` | TSV（Tab 分隔）| 操作日志，格式：`时间戳\t级别\tactor=... action=... detail=...` | OperationLog |

### 3.2 遗留/废弃文件（⚠️ 不被 DataService 加载）

| 文件 | 说明 |
|------|------|
| `applicants.tsv` | 早期测试数据，使用 UUID 作为 ID，DataService **不**加载 |
| `applications.tsv` | 早期申请记录，DataService **不**加载（申请记录嵌在 `mo_jobs.json` 中）|
| `jobs.tsv` | 早期职位测试数据，DataService **不**加载 |

> ⚠️ **注意**：`applications.tsv` 中存有 6 条历史申请记录（含 ACCEPTED/REJECTED），但系统无法读取，这是测试数据不一致的已知问题。

### 3.3 数据格式详解

#### `ta_info.csv`
```csv
id,name,email,skills,cvPath,description
2023213371,hjj,hjj1904211461@bupt.edu.cn,,data/cv/2023213371.pdf,
hjj,Junjie He,hjj1904211461@bupt.edu.cn,,C:\Users\HUAWEI\Desktop\简历——何俊杰.pdf,
```
- `skills` 用分号 `;` 分隔多个技能
- `cvPath` 存储简历文件的绝对路径（跨机器时可能失效，系统会自动回落到 `data/cv/{id}.pdf`）

#### `mo_jobs.json`
```json
{
  "jobs": [
    {
      "id": "uuid",
      "title": "职位名称",
      "description": "职位描述",
      "requiredSkills": "Java;SQL",
      "hoursPerWeek": 10,
      "postedBy": "MO10001",
      "status": "OPEN|CLOSED|COMPLETED",
      "category": "分类",
      "applications": [
        {
          "id": "uuid",
          "applicantId": "申请人ID",
          "jobId": "职位ID",
          "status": "SUBMITTED|ACCEPTED|REJECTED",
          "createdAt": 1716432000000
        }
      ]
    }
  ]
}
```

#### `admin_system.xml`（用户部分）
```xml
<users>
  <user role="Admin" account="admin" name="Administrator"
        passwordHash="pbkdf2$sha256$..." enabled="true"/>
  <user role="MO" account="MO10001" name="MO Tester"
        passwordHash="pbkdf2$sha256$..." enabled="true"/>
  <user role="TA" account="2023213371" name="hjj"
        passwordHash="pbkdf2$sha256$..." enabled="true"/>
</users>
<config/>
<workload/>
```
- 密码使用 PBKDF2+SHA-256 加盐哈希（120000 次迭代）

#### `temp_operation.txt`（日志格式）
```
2026-05-23T10:00:00	INFO	actor=admin action=login account=admin ok=true
2026-05-23T10:01:00	WARN	actor=admin action=setUserEnabled account=MO10001 enabled=false ok=true
```

---

## 四、用户角色与权限

| 角色 | 账号格式 | 功能 |
|------|----------|------|
| **Admin** | `admin` | 用户管理（增删改启禁）、系统配置、工作量查看、日志查看、数据导出、AI 助手 |
| **MO**（管理员/教师）| `MO1xxxx` | 发布/管理职位、查看申请人、审批申请（ACCEPT/REJECT）、查看工作量 |
| **TA**（助教申请人）| 学号（如 `2023213371`）| 注册/登录、完善个人档案（技能/描述/简历）、浏览职位、提交/撤回申请、查看申请状态 |

### 默认测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| Admin | `admin` | `admin` |
| MO | `MO10001` | `123456` |
| TA | `2023213371` | （注册时设置）|

---

## 五、核心业务流程

```
TA 注册登录
    │
    ▼
完善档案（技能、简历上传）
    │
    ▼
浏览职位列表（OPEN 状态）──→ 提交申请（status=SUBMITTED）
                                    │
                        MO 查看申请列表
                                    │
                        ┌───────────┴──────────┐
                     ACCEPT               REJECT
                  （ACCEPTED）           （REJECTED）
                        │
                  TA 查看申请结果
```

---

## 六、Admin AI 助手

### 数据上下文
每次对话，AI 自动注入以下实时数据：
- 所有 TA 申请人（含技能、简历提取文字）
- 所有职位（含描述、技能要求、状态）
- 所有申请记录（申请人 → 职位 → 状态）
- 所有账户（按角色分组）

### 简历读取机制
1. 优先读取 `cvPath` 记录的绝对路径
2. 路径不存在时，回落到 `data/cv/{applicantId}.pdf`
3. 再回落到 `data/cv/{文件名}`
4. PDF 使用 FlateDecode（zlib）解压 + BT/ET 文本提取
5. 中文字体若使用自定义字形编码，可能需要 Apache PDFBox 才能完整提取

### API 配置
- **Endpoint**：`https://token-plan-cn.xiaomimimo.com/v1`
- **模型（推荐）**：`mimo-v2.5-pro`（速度快 + 分析能力强）
- **备选**：`mimo-v2-pro`、`mimo-v2.5`、`mimo-v2-omni`

---

## 七、常见操作指引

### 添加新 MO 账号
1. Admin 登录 → 左侧"User Management"
2. 点击"Add User"，Role 选 MO，Account 格式 `MO1xxxx`，设置初始密码
3. 点击 Save

### 发布新职位
1. MO 登录 → 左侧"Jobs"
2. 点击"Post New Job"，填写标题、描述、技能要求（分号分隔）、每周课时
3. 提交后状态为 OPEN，TA 可立即看到

### 审批申请
1. MO 登录 → 左侧"Applications"
2. 选择对应职位，在申请列表中点击 Accept 或 Reject

### 导出数据
1. Admin 登录 → 左侧"Export"
2. 选择导出类型（用户/职位/申请），选择保存路径

### 查看操作日志
1. Admin 登录 → 左侧"Logs"
2. 可按 actor / action / level / keyword 过滤；支持导出和清空

---

## 八、已知问题与注意事项

| 问题 | 描述 | 影响 |
|------|------|------|
| 遗留数据文件 | `applicants.tsv`/`applications.tsv`/`jobs.tsv` 不被加载 | AI 查询到的申请数量可能为 0 |
| CV 路径跨机器失效 | `cvPath` 是绝对路径，换机器后文件不存在 | 系统已有回落逻辑，但需将 PDF 放入 `data/cv/` |
| hjj 重复记录 | `ta_info.csv` 中 `2023213371` 和 `hjj` 两条记录邮箱相同 | AI 会识别为同一人，建议清理 |
| PDF 中文提取 | WPS 生成的 PDF 使用自定义字形编码 | 目前可能无法提取中文文字（需 PDFBox）|

---

## 九、项目目录结构

```
SWE-groupWRK/
├── src/
│   └── ebu6304/
│       ├── App.java                  # 主入口
│       ├── model/                    # 数据模型（Applicant, Job, Application）
│       ├── storage/                  # 存储层（DataService, AuthStore, ...）
│       └── ui/                       # 界面层
│           ├── AppLayout.java        # 主框架（导航+顶栏）
│           ├── admin/                # Admin 功能页
│           │   ├── AdminAiPage.java  # AI 助手（本文档描述功能）
│           │   ├── AdminLogPage.java
│           │   ├── AdminWorkloadPage.java
│           │   └── ...
│           ├── mo/                   # MO 功能页
│           └── ta/                   # TA 功能页
├── data/                             # ⭐ 运行时数据（见第三节）
│   ├── admin_system.xml
│   ├── ta_info.csv
│   ├── mo_jobs.json
│   ├── cv/
│   └── temp_operation.txt
├── out/                              # 编译输出（.class 文件）
└── SYSTEM_DOCS.md                    # 本文档
```
