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

애플리케이션을 GitOps 방식으로 배포하고 Prometheus와 CloudWatch metric을 기반으로 

Kubernetes 리소스 비용 최적화 추천을 제공하는 프로젝트입니다.

단순 배포에 그치지 않고 CPU/Memory 요청량과 ALB·NAT Gateway 등 AWS 컴포넌트 비용을 

실시간으로 수집해 현재 비용과 추천 비용을 비교·시각화하는 것이 핵심 목표입니다.

<br>

- 기간 : 2026.05.21 ~ 2026.06.01
- 팀 구성 : 6인
- 담당 : GitOps 기반 CI/CD 파이프라인 설계 및 구축
