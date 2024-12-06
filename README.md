# Automated Code Review Assistant (ACRA)

## Overview
The Automated Code Review Assistant (ACRA) is a system that integrates with version control systems to perform automated code reviews based on predefined rules, best practices, and historical data.

## Features
- Integration with GitHub
- Advanced static code analysis using SonarQube and CheckStyle
- Support for multiple programming languages (Java, Python, JavaScript)
- Code style and formatting checks
- Security vulnerability scanning
- Performance impact estimation
- Historical trend analysis
- Customizable rule sets
- Report generation and feedback delivery
- Caching for improved performance
- Parallel processing of files during analysis
- Automatic posting of review comments on GitHub pull requests

## Technology Stack
- Java 17
- Spring Boot 3.1.5
- GraphQL
- PostgreSQL
- Docker
- GitHub API
- SonarQube
- CheckStyle

## Getting Started
### Prerequisites
- Java 17
- Maven
- PostgreSQL
- Docker (optional)
- SonarQube server
- GitHub account and personal access token

### Installation
1. Clone the repository
2. Configure application.properties with your GitHub token and SonarQube server details
3. Run `mvn clean install` to build the project
4. Start the application using `java -jar target/acra-1.0.0.jar`

## Usage
1. Create a new code review by sending a GraphQL mutation with the repository and pull request details
2. The system will automatically analyze the code and post comments on the GitHub pull request
3. View the review results and scores through the GraphQL API

## Configuration
- Customize rule sets through the provided API
- Adjust scoring weights in the application.properties file

## Contributing
Please read CONTRIBUTING.md for details on our code of conduct and the process for submitting pull requests.

## License
This project is licensed under the Apache-2.0 - see the LICENSE.md file for details