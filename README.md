# 🐾 Pet-Coummnity (반려동물 커뮤니티 플랫폼)
> **데이터 정합성과 성능 최적화를 고려한 Java 기반 백엔드 프로젝트**

---

## 🖥️ 프로젝트 소개 (Introduction)
* **개요**: 반려인들이 반려동물의 일상을 공유하고, 게시글·댓글·좋아요를 통해 소통할 수 있는 커뮤니티 플랫폼입니다.
* **핵심 가치**: 단순히 기능을 구현하는 것을 넘어, **트래픽 환경에서의 동시성 문제**와 **JPA 성능 최적화**를 해결하여 서비스의 안정성을 확보하는 데 집중했습니다.

---

## 🕰️ 개발 기간 및 인원 (Period & Members)
* **개발 기간**: 2025.06 ~ 현재 진행 중
* **참여 인원**: 1인 (개인 프로젝트 / 기여도 100%)
* **역할**: DB 설계 및 구현, 전체 백엔드 API 개발, Redis 기반 동시성 제어 및 캐싱, CI/CD 파이프라인 구축

---

## 📌 주요 기능 (Key Features)
* **게시판 서비스**: 게시글 작성, 수정, 삭제 및 페이징 처리된 목록 조회
* **좋아요 시스템**: **Redis INCR** 연산을 활용한 실시간 좋아요 카운팅 및 중복 방지
* **댓글 시스템**: 계층형 구조의 댓글/대댓글 API 및 조회 최적화
* **사용자 인증**: Spring Security를 활용한 회원 보안 및 권한 관리

---

## ⭐ 핵심 트러블 슈팅 (Troubleshooting)

### ✅ Redis를 활용한 '좋아요' 동시성 이슈 해결
* **문제 상황**: JMeter 테스트 시, 다수의 사용자가 동시에 좋아요를 클릭할 때 데이터 정합성 오류로 인해 카운트가 유실되는 현상 발생
* **원인 분석**: 다수의 요청이 동일한 데이터를 동시에 수정하면서 **Race Condition(경합 상태)** 발생 확인
* **해결 방법**: `post_like` 테이블을 분리하고, 싱글 스레드 기반 원자적 연산을 보장하는 **Redis의 `INCR` 명령어**를 도입하여 처리
* **결과**: 100명 동시 요청 부하 테스트에서 **데이터 누락률 0%** 달성 및 빠른 응답 속도 확보

### ✅ JPA N+1 문제 해결을 통한 조회 성능 최적화
* **문제 상황**: 게시글 목록 조회 시 연관된 작성자 정보를 가져오기 위해 게시글 수만큼 추가 쿼리가 발생하는 성능 저하 확인
* **해결 방법**: **Fetch Join**을 적용하여 게시글과 작성자 정보를 단일 쿼리로 한 번에 조회하도록 리팩토링
* **결과**: 기존 5회 발생하던 쿼리를 **2회(데이터+카운트)로 축소**하여 API 응답 속도 및 리소스 효율성 개선

---

## 📂 배포 및 CI/CD (Deployment)
* **인프라**: **AWS EC2** 환경에 **Docker** 컨테이너를 활용하여 실행 환경 표준화
* **자동화**: **GitHub Actions**를 연동하여 코드 병합 시 빌드, 테스트, 배포가 자동으로 수행되는 CI/CD 파이프라인 구축
* **성과**: 수동 배포의 번거로움을 제거하고 배포 시간을 단축하여 개발 생산성 향상
* **배포 주소**: [서비스 바로가기 🚀](http://3.35.234.158:8080/home)

---

## 🔧 기술 스택 (Tech Stack)

![Java](https://img.shields.io/badge/Code-Java17-blue)
![Spring Boot](https://img.shields.io/badge/Framework-SpringBoot3.x-6DB33F)
![MySQL](https://img.shields.io/badge/DBMS-MySQL-orange)
![Redis](https://img.shields.io/badge/Data-Redis-purple)
![Docker](https://img.shields.io/badge/Platform-Docker-2496ED)
![AWS](https://img.shields.io/badge/Infra-AWS_EC2-232F3E)
![CI/CD](https://img.shields.io/badge/CI%2FCD-GitHub_Actions-brightgreen)
![Project](https://img.shields.io/badge/Project-Member-green)