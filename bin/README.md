# team-app

ResourceOps의 애플리케이션 레포지토리입니다.

## 기술 스택

- Spring Boot 3
- Java 17
- Maven
- Docker
- GitHub Actions
- Amazon ECR

## 주요 기능

- REST API 제공
- Prometheus Metrics 노출
- RDS MySQL 연동
- 비용 최적화 추천 API
- Docker 이미지 빌드 및 ECR Push

## 프로젝트 구조

```bash
src/
 └── main/
     ├── java/
     └── resources/

Dockerfile
pom.xml
.github/workflows/
