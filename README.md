# International-School-Teaching-Assistant-Recruitment-System
 a software application simulation that will be used by BUPT International School for recruiting Teaching Assistants.



## Group Name-list

- XYLeell: 25421 (TA)
- Forest1ogic: 231225281 (member)
- hjjfmz: 231225591 (lead)
- Stephen-QwQ:231225340(member)
- whitebird1111: 231225269(member)
- 6zyy6：231225173（member）
- tdxb423: 231225144 (member)
## GitHub User Name

Q MID (lead/member)
- whitebird11111: 231225269(member)
- 6zyy6：231225177（member）
## GitHub User Name

QMID (lead/member)
A comprehensive software application for BUPT International School to manage Teaching Assistant (TA) recruitment process.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [AI Features Module](#ai-features-module)
- [User Roles & Permissions](#user-roles--permissions)
- [Installation](#installation)
- [Configuration](#configuration)
- [Data Storage](#data-storage)
- [Security](#security)
- [Project Structure](#project-structure)
- [Development Guide](#development-guide)
- [FAQ](#faq)
- [Contributing](#contributing)
- [License](#license)

---

## Project Overview

The BUPT International School TA Recruitment System is a desktop application designed to streamline the process of recruiting and managing Teaching Assistants. Built with **Agile methodology**, this system integrates **AI-powered intelligent matching** to provide comprehensive recruitment management services for administrators, module organizers (MO), and applicants (TA).

### Core Values

 **Intelligent** - Skill matching and job recommendations powered by DeepSeek LLM  
 **Precise** - Multi-dimensional scoring system (skills/experience/domain)  
 **Visualized** - Intuitive interface and data display  
 **Secure** - PBKDF2 password encryption + role-based access control  
 **International** - Bilingual interface support (English/Chinese)  

---

## Features

### Administrator Features

- **User Management** - Create/edit/delete accounts, assign roles and permissions
- **Workload Monitoring** - Real-time TA workload distribution view
- **Data Export** - Multi-format data export (CSV/TXT)
- **System Configuration** - Custom CV formats, system parameters
- **Operation Logs** - Comprehensive audit tracking
- **AI Management** - AI model configuration and dataset management

### Department Manager (MO) Features

- **Job Posting** - Create and manage job openings
- **Smart Review** - AI-assisted applicant evaluation system
  - Fast Scoring (quick match calculation)
  - Detailed Match Analysis (skills/experience/domain dimensions)
  - AI Streaming Explanation (real-time AI feedback)
- **Resume Viewer** - Direct PDF/DOCX resume opening
- **Batch Operations** - Bulk Accept/Reject processing
- **My Posts** - Manage published job listings

### Teaching Assistant (TA) Features

- **Profile Management** - Create and manage personal profile
- **Resume Upload** - Support for PDF/DOCX format resumes
- **Job Search** - Browse available jobs with AI match analysis
- **Smart Recommendations** - Personalized AI-powered job recommendations
- **Status Tracking** - Real-time application status tracking (Pending/Accepted/Rejected)

### AI-Powered Features

- **Resume Parsing** - Automatic extraction of candidate skills and experience
- **Job Description Parsing** - Intelligent analysis of job requirements and keywords
- **Skill Matching** - Multi-dimensional matching scoring algorithm
- **JD Optimization** - Job description quality check and polishing suggestions
- **Job Recommendation** - Personalized recommendation engine
- **Streaming Output** - Real-time AI explanation and suggestion generation

---

## System Architecture

### Layered Architecture Design

```
┌─────────────────────────────────────────────┐
│              UI Layer (Swing)                │
│   Admin Panel │ MO Panel │ TA Panel          │
├─────────────────────────────────────────────┤
│            Controller Layer                  │
│  AiController │ PageController               │
├─────────────────────────────────────────────┤
│              Service Layer                   │
│  MatchService │ ParseService │ IndexService  │
├─────────────────────────────────────────────┤
│              Model Layer                     │
│  Applicant │ Application │ Job              │
├─────────────────────────────────────────────┤
│           Storage Layer (File-based)         │
│  XML │ JSON │ CSV │ TSV                      │
└─────────────────────────────────────────────┘
```

### Core Components

#### 1. Model Layer (Data Models)
- `Applicant` - Applicant basic information (ID, name, email, skills, CV path)
- `Application` - Application record (ID, applicant ID, job ID, status, AI score)
- `Job` - Job information (ID, title, description, required skills, hours per week)

#### 2. Storage Layer (Data Persistence)
- **XML Storage** - User accounts and system configuration (`admin_system.xml`)
- **JSON Storage** - Job postings and applications (`mo_jobs.json`, `ai_dataset.json`)
- **CSV Storage** - Applicant information (`ta_info.csv`)
- **TSV Storage** - Application status tracking
- **Properties** - AI configuration (`deepseek.properties`)

#### 3. UI Layer (User Interface)
- **Admin Panel** - System administration and monitoring
- **MO Panel** - Job management and review
- **TA Panel** - Application and profile management
- **Internationalization** - English/Chinese switching support (I18n)

#### 4. Utility Layer (Tools)
- `Csv.java` / `Tsv.java` - File I/O utilities
- `MiniJson.java` - JSON parser
- `XmlStore.java` / `AuthStore.java` - Storage wrappers
- `OperationLog.java` - Operation logging
- `I18n.java` - Internationalization manager

---

## AI Features Module

### Architecture Overview

```
AiModule
├── ApplicantMatchController    # Applicant matching controller
├── JdAssistantController       # JD assistant controller
├── JobRecommendationController # Job recommendation controller
└── AiIndexController          # Index management controller

Workflow (Workflows)
├── ApplicantJobMatchWorkflow  # Applicant-job matching workflow
├── JdOptimizationWorkflow     # JD optimization workflow
└── JobRecommendationWorkflow  # Job recommendation workflow

Service Layer
├── ResumeParseService        # Resume parsing service
├── JobParseService           # Job parsing service
├── MatchScoreCalculator      # Match score calculator
├── JobMatchExplainService    # Match explanation service
├── CandidateProfileIndexService
├── JobProfileIndexService
├── JdQualityCheckService     # JD quality check service
└── JdPolishService           # JD polishing service
```

### AI Capability Matrix

| Feature | Input | Output | Use Case |
|---------|-------|--------|----------|
| **Resume Parsing** | PDF/DOCX file | Structured skills/experience profile | Auto-extract during TA registration |
| **Job Parsing** | JD text | Standardized job profile | Analyze when MO posts job |
| **Skill Matching** | Applicant + Job profiles | Match score (0-100) | When MO reviews applicants |
| **Match Explanation** | Match result | Natural language explanation | Streaming output when MO views details |
| **JD Quality Check** | JD text | Issue list + improvement suggestions | When MO edits JD |
| **JD Polishing** | Original JD text | Optimized version | One-click JD optimization for MO |
| **Job Recommendation** | Applicant profile | Ranked job list | When TA browses jobs |

### Scoring Dimensions

```java
MatchScore = {
  overallScore: int,      // Overall score (0-100)
  skillScore: int,        // Skills match score (0-100)
  seniorityScore: int,    // Experience match score (0-100)
  domainScore: int,       // Domain relevance score (0-100),
  matchedSkills: List,    // List of matched skills,
  missingSkills: List,    // List of missing skills,
  recommendTag: String,   // Recommendation tag (STRONG/MEDIUM/WEAK),
  shortReason: String     // Brief recommendation reason
}
```

---

## User Roles & Permissions

### Permission Matrix

| Feature Module | Admin | MO | TA |
|----------------|-------|----|-----|
| User Management | ✅ Full Control | ❌ | ❌ |
| System Config | ✅ | ❌ | ❌ |
| Operation Logs | ✅ View | ❌ | ❌ |
| Data Export | ✅ | ❌ | ❌ |
| AI Management | ✅ | ❌ | ❌ |
| Job Posting | ❌ | ✅ Own jobs only | ❌ |
| Applicant Review | ❌ | ✅ Own jobs only | ❌ |
| AI Match Evaluation | ❌ | ✅ | ❌ |
| Job Browsing | ❌ | ✅ All jobs | ✅ Public jobs only |
| Application Submit | ❌ | ❌ | ✅ |
| Personal Profile | ❌ | ❌ | ✅ Own only |
| Resume Upload | ❌ | ❌ | ✅ Own only |
| Status Query | ❌ | ❌ | ✅ Own only |

### Default Accounts

| Role | Username | Password | Description |
|------|----------|----------|-------------|
| **Admin** | `admin` | `admin` | System Administrator |
| **MO** | `MO10001` | `123456` | Module Organizer Sample |
| **MO** | `MO10002` | `123456` | Module Organizer Sample 2 |
| **MO** | `MO10003` | `123456` | Module Organizer Sample 3 |
| **TA** | `2023213330` | (set during registration) | Student Account Sample |

> **Note**: TA accounts require registration before use. Please change default passwords in production environment.

---

## Installation

### Prerequisites

- **Java Development Kit (JDK)**: 8 or higher
- **IDE**: IntelliJ IDEA (recommended) or Eclipse
- **Operating System**: Windows 10/11, macOS, Linux
- **Network**: Access to DeepSeek API required (optional, for AI features)

### Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd International-School-Teaching-Assistant-Recruitment-System
   ```

2. **Import to IDE**
   - Open IntelliJ IDEA
   - File → Open → Select project root directory
   - Wait for dependencies to load

3. **Configure AI Features (Optional but Recommended)**
   
   Copy the AI configuration template:
   ```bash
   # Windows
   mkdir %USERPROFILE%\.is-ta-rs
   copy deepseek.properties.example %USERPROFILE%\.is-ta-rs\deepseek.properties
   
   # macOS/Linux
   mkdir -p ~/.is-ta-rs
   cp deepseek.properties.example ~/.is-ta-rs/deepseek.properties
   ```
   
   Edit configuration file and enter your API Key:
   ```properties
   api_key=your_actual_deepseek_api_key
   base_url=https://api.deepseek.com
   ```

4. **Run the Application**
   - Locate `src/ebu6304/App.java`
   - Right-click → Run 'App.main()'
   - Wait for main window to launch

5. **Login to System**
   - Use default accounts listed above
   - Different roles will see different feature menus

---

## Configuration

### AI Configuration (deepseek.properties)

```properties
# Required: DeepSeek API key
api_key=your_api_key_here

# Optional: API base URL (default: https://api.deepseek.com)
base_url=https://api.deepseek.com

# Optional: Maximum resume text length sent to LLM (default: 6000 characters)
max_resume_chars=6000
```

### System Configuration (admin_system.xml)

System administrators can configure through Admin panel:
- Supported CV file formats (pdf, docx, etc.)
- Password policy requirements
- Other system parameters

---

## Data Storage

### File Structure

```
data/
├── admin_system.xml        # User accounts + system config
├── mo_jobs.json            # Job postings + application records
├── ta_info.csv             # TA applicant information
├── temp_operation.txt      # Operation logs
├── ai_dataset.json         # AI index data (auto-generated)
└── cv/                     # Resume files directory
    ├── 2023213330.pdf
    ├── 2023213331.docx
    └── ...
```

### Data Format Examples

**mo_jobs.json Example Structure**:
```json
{
  "jobs": [
    {
      "id": "JOB10001",
      "title": "Java Teaching Assistant",
      "description": "Support Java programming course...",
      "requiredSkills": "Java,Spring Boot,MySQL",
      "hoursPerWeek": 6,
      "postedBy": "MO10001",
      "status": "OPEN",
      "applications": [
        {
          "id": "APP10001",
          "applicantId": "2023213330",
          "jobId": "JOB10001",
          "status": "SUBMITTED",
          "createdAt": 1704067200000,
          "aiScore": 85
        }
      ]
    }
  ]
}
```

**ta_info.csv Example Structure**:
```csv
id,name,email,skills,cvPath,description
2023213330,Zhang San,zhangsan@example.com,"Java,Python,Spring",cv/2023213330.pdf,"Experienced TA"
```

---

## Security

### Password Encryption
- **Algorithm**: PBKDF2WithHmacSHA256
- **Iteration Count**: 10,000 iterations
- **Salt**: Independently generated random salt per user

### Access Control
- **Role Verification**: Display corresponding menus based on role after login
- **Data Isolation**: MO can only see their own posted jobs and applications
- **Input Validation**: All user inputs are validated and sanitized

### Audit Logging
- **Logged Content**: Operator, operation type, timestamp, detailed parameters
- **Log Location**: `data/temp_operation.txt`
- **Real-time Notifications**: Administrators can receive operation alerts

---

## Project Structure

```
src/ebu6304/
├── App.java                          # Application entry point
├── model/                            # Data models
│   ├── Applicant.java               # Applicant entity
│   ├── Application.java             # Application record entity
│   └── Job.java                     # Job entity
├── storage/                          # Data persistence layer
│   ├── DataService.java             # Core data service
│   ├── AuthStore.java               # Authentication storage
│   ├── Csv.java / Tsv.java          # CSV/TSV utilities
│   ├── MiniJson.java                # JSON parser
│   ├── XmlStore.java                # XML storage
│   └── OperationLog.java            # Log recording
├── ui/                              # User interface layer
│   ├── AppLayout.java               # Main layout framework
│   ├── MainFrame.java               # Main window
│   ├── I18n.java                    # Internationalization manager
│   ├── UiTheme.java                 # UI theme styles
│   ├── WorkbenchPanel.java          # Workbench panel
│   ├── LoginPanel.java              # Login interface
│   ├── admin/                       # Admin pages
│   │   ├── AdminHomePage.java
│   │   ├── AdminUserManagementPage.java
│   │   ├── AdminWorkloadPage.java
│   │   ├── AdminJobDataPage.java
│   │   ├── AdminConfigPage.java
│   │   ├── AdminExportPage.java
│   │   ├── AdminLogPage.java
│   │   └── AdminAiPage.java
│   ├── mo/                          # MO pages
│   │   ├── MoHomePage.java
│   │   ├── MoPostJobPage.java
│   │   ├── MoApplicantsPage.java    # Applicant review (with AI features)
│   │   └── MoMyPostsPage.java
│   ├── ta/                          # TA pages
│   │   ├── TaHomePage.java
│   │   ├── TaProfilePage.java
│   │   ├── TaResumePage.java
│   │   ├── TaJobsPage.java          # Job search (with AI recommendations)
│   │   └── TaApplicationStatusPage.java
│   └── panels/                      # Panel components
│       ├── AdminPanel.java
│       ├── MoPanel.java
│       └── TaPanel.java
├── ai/                              # AI module
│   ├── AiModule.java                # AI module entry point
│   ├── DeepSeekConfig.java          # DeepSeek configuration
│   ├── ResumeTextExtractor.java     # Resume text extractor
│   ├── client/                      # AI clients
│   │   ├── AiClientFactory.java
│   │   ├── DefaultAiClientFactory.java
│   │   ├── DeepSeekAiChatClient.java
│   │   ├── AiChatClient.java
│   │   ├── AiJsonSupport.java
│   │   └── AiPrompt.java
│   ├── controller/                  # AI controllers
│   │   ├── AiIndexController.java
│   │   ├── ApplicantMatchController.java
│   │   ├── JdAssistantController.java
│   │   └── JobRecommendationController.java
│   ├── service/                     # AI services
│   │   ├── ResumeParseService.java
│   │   ├── JobParseService.java
│   │   ├── MatchScoreCalculator.java
│   │   ├── JobMatchExplainService.java
│   │   ├── CandidateProfileIndexService.java
│   │   ├── JobProfileIndexService.java
│   │   ├── JdQualityCheckService.java
│   │   └── JdPolishService.java
│   ├── workflow/                    # Workflows
│   │   ├── ApplicantJobMatchWorkflow.java
│   │   ├── JdOptimizationWorkflow.java
│   │   └── JobRecommendationWorkflow.java
│   ├── dto/                         # Data transfer objects
│   │   ├── CandidateProfileSourceDto.java
│   │   ├── JobDraftDto.java
│   │   └── JobProfileSourceDto.java
│   ├── vo/                          # View objects
│   │   ├── JobMatchResultVo.java
│   │   ├── CandidateProfileVo.java
│   │   ├── JobProfileVo.java
│   │   ├── JdPolishResultVo.java
│   │   ├── JdQualityIssueVo.java
│   │   ├── JobRecommendationVo.java
│   │   └── SeniorityLevel.java
│   ├── repository/                  # Repository
│   │   └── AiDatasetRepository.java
│   ├── prompt/                      # Prompt templates
│   │   ├── CandidateProfilePrompt.java
│   │   ├── JdPolishPrompt.java
│   │   ├── JdQualityCheckPrompt.java
│   │   ├── JobMatchExplainPrompt.java
│   │   └── JobProfilePrompt.java
│   └── util/                        # AI utilities
│       └── AiTextUtils.java
└── images/                          # Image resources
    ├── logo-full.png
    └── login-bg.jpg

lib/
└── pdfbox-app-3.0.4.jar            # PDF processing library

data/                                # Runtime data directory
templates/                           # Project document templates
docs/                                # Project documentation
```

---

## Development Guide

### Code Conventions

1. **Naming Conventions**
   - Class names: PascalCase (e.g., `DataService`)
   - Method names: camelCase (e.g., `getApplicant()`)
   - Constants: UPPER_SNAKE_CASE (e.g., `MAX_RESUME_CHARS`)
   - Package names: lowercase (e.g., `ebu6304.ui.ta`)

2. **Comment Requirements**
   - Public APIs must have Javadoc comments
   - Complex logic requires inline comments
   - Do not add meaningless comments

3. **Exception Handling**
   - Use specific exception types
   - Provide meaningful error messages
   - Log exceptions appropriately

### Adding New Features

1. Define data structures in corresponding `model/` package
2. Add data access methods in `storage/DataService.java`
3. Create page in `ui/` package for corresponding role
4. Register new page in `WorkbenchPanel.java`
5. Add translation texts in `I18n.java`
6. If involving AI functionality, extend in `ai/` module

### Internationalization (I18n)

Add new translation texts:

```java
// Add in static initialization block of I18n.java
en.put("key.name", "English text");
zh.put("key.name", "Chinese text");

// Use in code
String text = I18n.t("key.name");
```

---

## FAQ

### Q: AI features not working?
**A:** Please check:
1. Whether `deepseek.properties` is configured correctly
2. If API Key is valid and has sufficient balance
3. If network can access DeepSeek API
4. Check IDE console for error messages

### Q: Resume upload failed?
**A:** Please confirm:
1. File format is in allowed list (default: pdf, docx)
2. File size is reasonable (< 10MB)
3. `data/cv/` directory exists and is writable

### Q: Data lost?
**A:** The system uses file-based storage, please note:
1. Regularly backup `data/` directory
2. Do not manually edit data files while system is running
3. Use system's export function to backup data

### Q: How to reset system?
**A:** Delete all files under `data/` directory (keep directory structure), then restart application.

---

## Contributing

We welcome contributions of all forms!

### Ways to Contribute

1. **Report Bugs** - Describe issues and reproduction steps in Issues
2. **Feature Suggestions** - Propose new features with use cases
3. **Code Contributions** - Fork project and submit Pull Request
4. **Documentation Improvements** - Enhance docs or translations

### Pull Request Process

1. Fork this repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Review Standards

-  Code follows project style guidelines
-  Appropriate test coverage included
-  Documentation updated
-  No compilation errors or warnings
-  Passes all existing tests

---

## License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2024 BUPT International School TA Recruitment System Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

<div align="center">

** If this project helps you, please give it a Star! **

Made by BUPT International School Team

</div>
