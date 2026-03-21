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
A comprehensive software application for BUPT International School to manage Teaching Assistant (TA) recruitment process.

## Table of Contents

- [Project Overview](#project-overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Installation](#installation)
- [Usage](#usage)
- [User Roles](#user-roles)
- [Data Storage](#data-storage)
- [Security](#security)
- [Contributing](#contributing)
- [License](#license)

## Project Overview

The BUPT International School TA Recruitment System is a desktop application designed to streamline the process of recruiting and managing Teaching Assistants for the university. The system provides a user-friendly interface for different stakeholders including administrators, department managers, and potential TAs.

## Features

### Administrator Features
- User management (create/edit/delete accounts)
- Job data management
- Workload management
- System configuration
- Operation logs monitoring
- Data export functionality

### Department Manager (MO) Features
- Post new job openings
- Manage posted jobs
- Review and evaluate applicants
- View application results

### Teaching Assistant (TA) Features
- User registration
- Job search and application
- Application status tracking
- Profile management
- Resume upload

## System Architecture

The system follows a layered architecture:

1. **Model Layer** - Defines core data structures
   - `Applicant` - TA applicant information
   - `Application` - Job application data
   - `Job` - Job posting details

2. **Storage Layer** - Handles data persistence
   - XML storage for user accounts and system configuration
   - JSON storage for job postings
   - CSV storage for applicant information
   - TSV storage for application data

3. **UI Layer** - Provides user interface
   - Admin panel
   - MO (Manager Officer) panel
   - TA (Teaching Assistant) panel

4. **Utility Layer** - Provides supporting functionality
   - Skill matching algorithm
   - Data validation
   - Internationalization support

## Installation

### Prerequisites
- Java Development Kit (JDK) 8 or higher
- IDE (IntelliJ IDEA recommended)

### Steps
1. Clone the repository
2. Open the project in your IDE
3. Build the project to resolve dependencies
4. Run the `App.java` file as the main entry point

## Usage

### Default Login Credentials

| Role | Username | Password |
|-------|---------|----------|
| Admin | admin | admin |
| MO | MO10001 | 123456 |
| TA | 2023213330 | (set during registration) |

### Getting Started
1. Launch the application
2. Log in with your credentials
3. Navigate through the system using the sidebar menu
4. Perform tasks based on your user role

## User Roles

### Administrator
- Full access to all system functions
- Responsible for system maintenance and user management
- Can monitor all activities through operation logs

### Department Manager (MO)
- Can post and manage job openings
- Review applications and select candidates
- View workload and allocation

### Teaching Assistant (TA)
- Can browse available job openings
- Submit applications for desired positions
- Track application status
- Manage personal profile and resume

## Data Storage

The system uses multiple storage formats for different types of data:

- `admin_system.xml` - User accounts and system configuration
- `mo_jobs.json` - Job postings and applications
- `ta_info.csv` - TA applicant information
- `temp_operation.txt` - System operation logs

All data is stored in the `data/` directory at the root of the project.

## Security

- Passwords are hashed using PBKDF2 with SHA-256
- Role-based access control
- Input validation to prevent malicious data
- Regular operation logging for audit purposes

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.