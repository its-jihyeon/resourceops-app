# ResourceOps - APP

### GitOps 기반 배포 자동화 및 AWS 비용 최적화 추천

> 이 저장소는 멋쟁이사자처럼 AWS 기반 DevOps 엔지니어 과정 팀 프로젝트(6인)의 fork 입니다. <br>
> 본 README는 담당 영역을 중심으로 재작성했습니다. <br>

<br>

원본 저장소
* **App 레포** : [team2-resourceops-app](https://github.com/CLD-05/team2-app)
* **Config 레포** : [team2-resourceops-config](https://github.com/CLD-05/team2-config)

<br>

## 프로젝트 개요

애플리케이션을 GitOps 방식으로 자동 배포하고 Prometheus와 CloudWatch 메트릭을 기반으로 

Kubernetes 리소스 비용 최적화 방향을 제시하는 프로젝트입니다.

<br>

CPU/Memory 요청량과 주요 AWS 컴포넌트 비용을 실시간으로 수집하여 현재 비용과 최적화 추천 비용을 비교 및 시각화하는 것을 핵심 목표로 합니다.

<br>

- 기간 : 2026.05.21 ~ 2026.06.01
- 팀 구성 : 6인
- 담당 : GitOps 기반 CI/CD 파이프라인 설계 및 구축

<br>

## CI/CD 다이어그램

<img width="3572" height="1492" alt="image" src="https://github.com/user-attachments/assets/9b052b9a-d28f-4665-a1a8-89e116be3321" />

<br>

## 내가 담당한 부분

### CI 파이프라인 구축
- App Repo에 코드 Push / PR Merge 시 자동으로 빌드 트리거
- Maven Build → Docker Build → Amazon ECR Push → Config Repo 이미지 태그 자동 갱신
- GitHub OIDC 기반 AWS 인증
- 빌드부터 Config Repo 반영까지 평균 1분 20초 수준으로 유지
- 관련 코드 : `.github/workflows/ci.yml`

<br>

**[CD 파이프라인 구축](https://github.com/its-jihyeon/resourceops-config)**

<br>

## 트러블슈팅

### [문제 제목]
- 상황 :
- 원인 분석 :
- 시도한 방법 :
- 최종 해결 :
- 배운 점 :

## 기술 스택

GitHub Actions · ArgoCD · Helm · Kustomize
