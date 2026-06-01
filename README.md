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

team2-app/
├── src/
│ └── main/
│ ├── java/
│ │ ├── com/example/deploylab/
│ │ └── com/example/resourceops/
│ │ ├── recommendation/
│ │ │ ├── calculator/
│ │ │ ├── cloudwatch/
│ │ │ ├── config/
│ │ │ ├── controller/
│ │ │ ├── dto/
│ │ │ ├── metrics/
│ │ │ ├── pricing/
│ │ │ ├── scheduler/
│ │ │ └── service/
│ │ └── loadtest/
│ └── resources/
│ └── application.yml
├── Dockerfile
├── pom.xml
└── .github/workflows/ci.yml
```
실행 순서

사용자 요청
    
 ↓
    
Spring Boot API
    
 ↓
    
Prometheus 조회
    
 ↓
    
CPU / Memory 사용량 분석
    
 ↓
    
리소스 추천 알고리즘
    
 ↓
    
비용 계산
    
 ↓
    
결과 저장
    
 ↓
    
응답 반환


추천 알고리즘

CPU 사용률 = 평균 CPU 사용량 ÷ CPU Request

Memory 사용률 = 평균 Memory 사용량 ÷ Memory Request

사용률이 30% 미만
 → 과할당

사용률이 30~80%
 → 적정

사용률이 80% 이상
 → 증설 권장


비용 최적화

현재 비용
      
  ↓
      
추천 리소스 적용
      
  ↓
      
예상 비용 계산
      
  ↓
      
절감액 계산
      
  ↓
      
절감률 계산


Team2-Config
```bash
team2-config/
├── apps/
│ └── resource-ops/
│ ├── base/
│ │ ├── deployment.yaml
│ │ ├── service.yaml
│ │ ├── gateway.yaml
│ │ ├── httproute.yaml
│ │ ├── configmap.yaml
│ │ ├── external-secret.yaml
│ │ ├── serviceaccount.yaml
│ │ ├── servicemonitor.yaml
│ │ ├── load-balancer-configuration.yaml
│ │ ├── target-group-configuration.yaml
│ │ └── kustomization.yaml
│ └── overlays/
│ └── dev/
│ └── kustomization.yaml
├── argocd/
├── infra/
│ ├── backend.tf
│ ├── main.tf
│ ├── variables.tf
│ ├── outputs.tf
│ └── modules/
├── monitoring/
└── platform/
├── gateway-api/
└── external-secrets/
```

