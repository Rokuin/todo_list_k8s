# StudyBuddy - 学習進捗＋リマインダーAPI

## 概要

StudyBuddyは、学習計画と学習実績を管理するREST APIです。DockerとKubernetesの習熟を目的としたSpring Bootアプリケーションです。

## 機能

- **Profiles**: 学習者の管理
- **Subjects**: 科目の管理
- **PlanItems**: 学習タスクの管理（締切・目標時間・状態）
- **SessionLogs**: 学習実績の記録
- **Dashboard**: 学習進捗のサマリー表示

## 技術スタック

- Java 17 (Temurin OpenJDK)
- Spring Boot 3.x
- PostgreSQL
- Flyway (DB Migration)
- Docker & Docker Compose
- Kubernetes (minikube)

## 必要なソフトウェア

- Temurin OpenJDK 17
- Docker Desktop (WSL2 backend推奨)
- kubectl 1.30+
- minikube 1.34+
- Git

## ビルド方法

```bash
./mvnw clean package
```

## ローカル実行 (Docker Compose)

```bash
cd docker
docker compose up -d --build
```

ヘルスチェック:
```bash
curl http://localhost:8080/actuator/health
```

## Kubernetes デプロイ (minikube)

```bash
# minikube起動
minikube start

# Ingressアドオン有効化
minikube addons enable ingress

# イメージをminikubeにロード
minikube image load yourrepo/studybuddy:0.1.0

# デプロイ
kubectl apply -k deploy/k8s/overlays/dev

# 確認
kubectl get pods -n studybuddy
```

## API エンドポイント

すべてのAPIは `/api/v1` をルートとします。

### Profiles
- `POST /api/v1/profiles` - プロフィール作成
- `GET /api/v1/profiles/{id}` - プロフィール取得

### Subjects
- `POST /api/v1/subjects` - 科目作成
- `GET /api/v1/subjects?profileId={id}` - 科目一覧取得
- `DELETE /api/v1/subjects/{id}?profileId={id}` - 科目削除

### Plan Items
- `POST /api/v1/plan-items` - 学習タスク作成
- `GET /api/v1/plan-items?profileId={id}` - タスク一覧取得
- `PATCH /api/v1/plan-items/{id}/done?profileId={id}` - タスク完了

### Session Logs
- `POST /api/v1/session-logs` - 学習実績記録
- `GET /api/v1/session-logs?profileId={id}` - 実績一覧取得

### Dashboard
- `GET /api/v1/dashboard/summary?profileId={id}` - サマリー取得

## プロジェクト構成

```
studybuddy/
├─ src/main/java/com/example/studybuddy/
│  ├─ StudyBuddyApplication.java
│  ├─ controller/
│  ├─ service/
│  ├─ repository/
│  ├─ entity/
│  ├─ dto/
│  ├─ config/
│  └─ exception/
├─ src/main/resources/
│  ├─ application.yml
│  └─ db/migration/
├─ docker/
│  ├─ Dockerfile
│  └─ docker-compose.yml
└─ deploy/k8s/
   ├─ base/
   └─ overlays/dev/
```

## 今後の拡張

- 認証機能 (JWT)
- リマインダーのCronJob化
- Grafana/Lokiによる監視
- RPSベースのHPA
