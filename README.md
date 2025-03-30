# Prismedia-server-core(프리즈미디어 코어 서버)

프리즈미디어 서버 코어는 뉴스 아이템과 뉴스 클러스터를 제공하는 RESTful API 서버입니다.

## 기술 스택

- **언어**: Kotlin 1.9.22
- **프레임워크**: Spring Boot 3.2.4
- **빌드 도구**: Gradle 
- **데이터베이스**: 
  - Local: H2 인메모리 데이터베이스
  - Production: MySQL
- **보안**: Spring Security, OAuth2, JWT
- JDK 17 이상
- Docker

## 주요 기능

### 1. 뉴스 관리
- 뉴스 아이템 CRUD 작업
- 카테고리별 뉴스 조회
- 키워드 기반 뉴스 검색
- 페이징 및 정렬 지원

### 2. 뉴스 클러스터링
- 관련 뉴스 아이템 그룹화
- 클러스터 기반 뉴스 조회

### 3. 사용자 인증 및 권한 관리
- OAuth2 기반 소셜 로그인 (Google)
- JWT 토큰 기반 인증
- 역할 기반 접근 제어

#### 사전 준비사항
- Docker 및 Docker Compose 설치
- OAuth2 인증을 위한 환경 변수 설정

#### 개발 환경에서 실행 (H2 데이터베이스)
```bash
# 환경 변수 설정 (필요한 경우)
export GOOGLE_CLIENT_ID=your_google_client_id
export GOOGLE_CLIENT_SECRET=your_google_client_secret

# 개발 환경으로 실행
docker-compose -f docker-compose.yml -e SPRING_PROFILES_ACTIVE=local up
```

#### 배포 환경에서 실행 (MySQL 데이터베이스)
```bash
# 환경 변수 설정
export GOOGLE_CLIENT_ID=your_google_client_id
export GOOGLE_CLIENT_SECRET=your_google_client_secret
export APP_AUTH_TOKEN_SECRET=your_secure_token_secret

# 배포 환경으로 실행 (백그라운드 모드)
docker-compose up -d
```