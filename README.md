# 🐾 Petnish (반려동물 커뮤니티 플랫폼)
> **데이터 정합성과 성능 최적화를 고려한 Java 기반 백엔드 프로젝트**

---

## 🖥️ 프로젝트 소개 (Introduction)
* **개요**: 반려인들이 반려동물의 일상을 공유하고, 게시글·댓글·좋아요를 통해 소통할 수 있는 커뮤니티 플랫폼입니다.
* **핵심 가치**: 단순히 기능을 구현하는 것을 넘어, **트래픽 환경에서의 동시성 문제**와 **JPA 성능 최적화**를 해결하여 서비스의 안정성을 확보하는 데 집중했습니다.

---

## 🕰️ 개발 기간 및 인원 (Period & Members)
* **개발 기간**: 2025.06 ~ 2026.02
* **참여 인원**: 1인 (개인 프로젝트 / 기여도 100%)
* **역할**: DB 설계 및 구현, 전체 백엔드 API 개발, Redis 기반 동시성 제어 및 캐싱, CI/CD 파이프라인 구축

---

## 📌 주요 기능 (Key Features)
* **게시판 서비스**: 게시글 작성, 수정, 삭제 및 페이징 처리된 목록 조회
* **좋아요 시스템**: **Redis INCR** 연산을 활용한 실시간 좋아요 카운팅 및 중복 방지
* **댓글 시스템**: 계층형 구조의 댓글/대댓글 API 및 조회 최적화
* **사용자 인증**: Spring Security를 활용한 회원 보안 및 권한 관리

## 🚀 DB(MySQL)
<img width="1847" height="1590" alt="community" src="https://github.com/user-attachments/assets/64e58a68-08f3-4ea2-b66d-29578f0d0ae6" />


---

## ⭐ 핵심 트러블 슈팅 (Troubleshooting)

## ✅ Redis INCR을 활용한 '좋아요' 동시성 이슈 해결 및 데이터 정합성 확보
* **문제 상황**: JMeter를 이용한 1,000명 동시성 테스트 결과, 실제 클릭 수보다 적은 값이 저장되는 데이터 유실 현상 발생 (초기 에러율 약 27.5%)
* **원인 분석**: 다수의 트랜잭션이 동일한 행을 동시에 갱신하며 **Race Condition(경합 상태)**이 발생하고, DB 커넥션 풀 부족으로 인한 **Connection-timeout**이 병목의 주된 원인임을 확인
* **해결 방법**:
    * 싱글 스레드 기반으로 원자적(Atomic) 연산을 보장하는 **Redis의 `INCR` 명령어**를 도입하여 동시성 제어
    * 시스템 처리 한계를 고려하여 초당 유입 유저 수를 최적화(20→30)하고 인프라 튜닝 단행
* **결과**:
    * **데이터 누락률 0%** 및 에러율 0% 달성.
    * 평균 응답 시간을 **1,703ms에서 62ms로 약 27배 개선**하며 서비스 안정성 확보.

## ✅ 중첩 Fetch Join 적용을 통한 JPA N+1 문제 해결 및 쿼리 최적화
* **문제 상황**: 게시글 목록 조회 시 연관된 작성자(User) 및 프로필(Profile) 정보를 가져오기 위해 게시글 수만큼 추가 쿼리가 발생하는 **N+1 문제**로 인한 성능 저하 확인
* **원인 분석**:
    * 영속성 컨텍스트에 없는 연관 데이터를 참조할 때마다 개별 SELECT 쿼리가 반복 실행됨
    * 특히 `User`와 `Profile` 간의 지연 로딩 설정으로 인해 2중으로 쿼리가 발생하는 구조임을 확인
* **해결 방법**:
    * **JPA 중첩 Fetch Join**을 적용하여 게시글, 작성자, 프로필 정보를 단일 JOIN 쿼리로 한 번에 조회하도록 리팩토링
* **결과**:
    * 실행 쿼리 수(10개 기준)를 **8회에서 2회(데이터 조회 1회 + 카운트 1회)로 축소**
    * DB 조회 쿼리 수를 **기존 대비 약 75% 감소**시켜 API 응답 속도 및 리소스 효율성 극대화

## ✅ 복합 인덱스 설계를 통한 최신순 페이징 조회 성능 개선
* **문제 인식**
  * 게시판은 데이터가 지속적으로 누적되는 구조이며, 최신순 정렬 기반 목록 조회는 데이터 증가 시 성능 저하 가능성이 존재한다고 판단
  * 대량 데이터 환경에서 실행 계획(EXPLAIN)을 분석한 결과, 정렬 컬럼에 인덱스가 없어 **Full Table Scan + filesort**가 발생하는 구조임을 확인

* **테스트 환경**
  * DB: MySQL 8.4 (InnoDB)
  * 데이터 규모: 약 100,000건
  * 분석 방법: EXPLAIN 기반 실행 계획 비교
  * 실행 환경: Local (Spring Boot + JPA)
  * 실험 조건: 최신순 목록 조회 (`ORDER BY created_at DESC, id DESC`, `LIMIT 10`)

* **인덱스 적용 전 실행 계획**
  * `key = NULL`
  * `rows ≈ 99,608`
  * `Extra = Using filesort`
  * → `LIMIT 10`임에도 약 100,000건을 스캔 후 정렬 수행

* **해결 방법**
  * 최신순 정렬 조건과 일치하는 `(created_at, id)` 복합 인덱스 설계 및 적용
    ```sql
    CREATE INDEX idx_post_created_at_id
    ON post (created_at DESC, id DESC);
    ```

* **인덱스 적용 후 실행 계획**
  * `key = idx_post_created_at_id`
  * `rows = 10`
  * `Extra = Using index`
  * → 인덱스 정렬 구조를 활용하여 filesort 제거 및 필요한 10건만 조회

* **성과**
  * 스캔 대상 행 수 약 99,608 → 10으로 감소
  * 별도 정렬(filesort) 제거
  * 대량 데이터 환경에서도 안정적인 최신순 페이징 조회 구조 확보

---

## 📂 배포 및 CI/CD (Deployment)
* **인프라**: **AWS EC2** 환경에 **Docker** 컨테이너를 활용하여 실행 환경 표준화
* **자동화**: **GitHub Actions**를 연동하여 코드 병합 시 빌드, 테스트, 배포가 자동으로 수행되는 CI/CD 파이프라인 구축
* **성과**: 수동 배포의 번거로움을 제거하고 배포 시간을 단축하여 개발 생산성 향상
* **배포 주소**: [서비스 바로가기 🚀](https://petnish.com/)

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
