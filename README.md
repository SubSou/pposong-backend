# 뽀송 (Pposong) Backend

### Spring Boot 기반 SNS 백엔드 서버

뽀송(Pposong)은 사용자들이 일상과 사진을 공유하고, 좋아요와 댓글을 통해 소통할 수 있는 SNS 웹 서비스입니다.

Spring Boot와 MySQL을 기반으로 REST API를 개발했으며, JWT 인증, AWS S3 이미지 저장, WebSocket 실시간 접속자 관리 등의 기능을 구현했습니다.

개인 포트폴리오 프로젝트로 백엔드 설계부터 데이터베이스 연동, API 개발, AWS 인프라 구성 및 서버 배포까지 진행했습니다.

## 프로젝트 링크

- **서비스 URL:** https://pposong-frontend.vercel.app/
- **Frontend:** https://github.com/SubSou/pposong-frontend
- **API 서버:** https://pposong-api.duckdns.org

## 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot |
| Security | Spring Security, JWT |
| Database | MySQL |
| ORM | Spring Data JPA, Hibernate |
| Build Tool | Gradle |
| Cloud | AWS EC2, RDS, S3 |
| Web Server | Nginx |
| Real-time | WebSocket |
| Deployment | Ubuntu, systemd, HTTPS |
| Version Control | Git, GitHub |

## 주요 기능

### 1. 회원가입 및 로그인

- 이메일 기반 회원가입 및 로그인 API
- BCrypt를 이용한 비밀번호 암호화
- JWT Access Token 발급 및 검증
- Spring Security 기반 인증 및 인가
- 관리자 승인 상태에 따른 서비스 접근 제어
- 인증된 사용자 정보 조회 API

### 2. 게시글 관리

- 게시글 작성, 조회, 수정 및 삭제 API
- 사용자와 게시글 간 연관관계 관리
- 게시글 이미지 정보 저장
- 작성자 권한 검증
- DTO를 활용한 요청 및 응답 데이터 분리

### 3. 이미지 업로드

- AWS S3를 이용한 이미지 저장
- 이미지 업로드 API
- 업로드된 이미지 URL 관리
- 게시글 및 프로필 이미지 연동

### 4. 좋아요 및 댓글

- 게시글 좋아요 및 취소
- 사용자별 좋아요 상태 관리
- 댓글 작성, 조회, 수정 및 삭제
- 게시글과 댓글의 연관관계 관리

### 5. 사용자 프로필

- 사용자 프로필 조회
- 사용자 정보 수정
- 프로필 이미지 변경
- 사용자별 게시글 조회

### 6. 실시간 접속자

- WebSocket 기반 실시간 통신
- JWT를 이용한 WebSocket 연결 사용자 식별
- 접속 및 연결 종료 이벤트 처리
- 실시간 접속자 목록 관리
- 접속자 정보 클라이언트 전달

### 7. 게시글 신고

- 게시글 신고 API
- 신고 사유 관리
- 신고 데이터 저장 및 조회를 위한 구조 구현

## 프로젝트 구조

```text
src/main/java/com/pposong/pposongbackend/
├── config/           # Spring Security, AWS S3, WebSocket 설정
├── controller/       # REST API 엔드포인트
├── dto/              # 요청 및 응답 데이터 객체
│   ├── auth/
│   ├── comment/
│   ├── like/
│   ├── post/
│   ├── profile/
│   └── report/
├── entity/           # JPA 엔티티
├── exception/        # 공통 예외 처리
├── jwt/              # JWT 생성 및 인증 필터
├── repository/       # 데이터베이스 접근
├── service/          # 비즈니스 로직
├── websocket/        # 실시간 접속자 관리
└── PposongApplication.java
```

## 시스템 아키텍처

```text
Client (React + TypeScript)
          |
          | HTTPS / REST API
          v
    Vercel Frontend
          |
          | HTTPS / WSS
          v
     Nginx (EC2)
          |
          v
  Spring Boot Server
          |
          +------ MySQL (AWS RDS)
          |
          +------ AWS S3
          |
          +------ WebSocket
```

프론트엔드는 Vercel에 배포하고, 백엔드는 AWS EC2 Ubuntu 환경에서 운영합니다.

Nginx를 Reverse Proxy로 구성하여 Spring Boot 애플리케이션과 연결하고, HTTPS 및 WebSocket 보안 연결(WSS)을 지원합니다.

## 인증 처리 흐름

1. 사용자가 이메일과 비밀번호로 로그인 요청
2. 서버에서 사용자 정보 및 비밀번호 검증
3. 사용자 승인 상태 확인
4. JWT Access Token 발급
5. 클라이언트가 API 요청 시 JWT 전달
6. JWT 인증 필터에서 토큰 검증
7. 인증된 사용자에게 요청한 리소스 제공

## 주요 API

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/api/auth/signup` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| GET | `/api/auth/me` | 로그인 사용자 조회 |
| GET | `/api/posts` | 게시글 목록 조회 |
| POST | `/api/posts` | 게시글 작성 |
| GET | `/api/posts/{postId}` | 게시글 상세 조회 |
| PUT/PATCH | `/api/posts/{postId}` | 게시글 수정 |
| DELETE | `/api/posts/{postId}` | 게시글 삭제 |

※ 게시글 수정 메서드 및 기타 상세 API 경로는 실제 Controller 매핑을 기준으로 확인 후 기재해야 합니다.

## 실행 방법

### 1. 저장소 복제

```bash
git clone https://github.com/SubSou/pposong-backend.git
cd pposong-backend
```

### 2. 환경 설정

보안을 위해 `application.properties`는 GitHub에 포함하지 않았습니다.

`src/main/resources/application.properties` 파일을 생성하고 실행 환경에 맞게 설정합니다.

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

jwt.secret=${JWT_SECRET}
jwt.expiration=3600000

aws.access-key=${AWS_ACCESS_KEY_ID}
aws.secret-key=${AWS_SECRET_ACCESS_KEY}
aws.region=${AWS_REGION}
```

실제 비밀번호 및 인증 키는 환경변수로 관리합니다. 추가 설정은 프로젝트 환경에 맞게 구성해야 합니다.

### 3. 애플리케이션 실행

Windows:

```bash
gradlew.bat bootRun
```

## 배포 환경

- AWS EC2 Ubuntu 서버
- AWS RDS MySQL 데이터베이스
- AWS S3 이미지 저장소
- Nginx Reverse Proxy
- Let's Encrypt SSL 인증서
- systemd를 이용한 Spring Boot 프로세스 관리
- DuckDNS 도메인 연결

## 프로젝트를 통해 경험한 내용

- Spring Boot 기반 REST API 설계 및 구현
- Controller, Service, Repository 계층 분리
- JPA 엔티티 및 데이터베이스 연관관계 설계
- Spring Security와 JWT를 활용한 인증 처리
- BCrypt 기반 비밀번호 암호화
- AWS S3를 이용한 파일 저장 기능
- WebSocket 기반 실시간 접속자 관리
- AWS EC2, RDS, S3를 활용한 서비스 배포
- Nginx Reverse Proxy 및 HTTPS 설정
- CORS, HTTP 오류, 인증 및 배포 환경 문제 해결

## 프로젝트 안내

본 프로젝트는 개인 학습 및 개발 포트폴리오 목적으로 제작되었습니다.

사용자는 개인정보나 민감한 정보를 게시하지 않도록 주의해 주세요.
