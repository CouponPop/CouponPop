#!/bin/bash
set -e # 오류 발생 시 중단

# --- 1. 변수 설정 ---
AWS_REGION="ap-northeast-2"
SECRET_ID="couponpop/secrets"
CONTAINER_NAME="couponpop-app"
IMAGE_URI_FILE="/home/ubuntu/app/image_uri.txt"
FCM_KEY_PATH="/home/ubuntu/app/serviceAccountKey.json"
FCM_CONTAINER_KEY_PATH="/config/serviceAccountKey.json"

echo "--- start_application.sh 시작 ---"

# --- 2. Secrets Manager에서 비밀 정보 가져오기 ---
echo "Secrets Manager에서 비밀 정보를 가져옵니다: $SECRET_ID"
SECRET_JSON=$(aws secretsmanager get-secret-value --secret-id $SECRET_ID --region $AWS_REGION --query SecretString --output text)

# 환경변수로 export (docker run 명령어에서 사용)
export DB_URL=$(echo $SECRET_JSON | jq -r .DB_URL)
export DB_USERNAME=$(echo $SECRET_JSON | jq -r .DB_USERNAME)
export DB_PASSWORD=$(echo $SECRET_JSON | jq -r .DB_PASSWORD)
export JWT_SECRET_KEY=$(echo $SECRET_JSON | jq -r .JWT_SECRET_KEY)
echo "비밀 정보 로드 완료."

# FCM 키를 파일로 생성
echo "FCM 서비스 계정 키를 파일로 생성합니다."
echo $SECRET_JSON | jq -r .FCM_SERVICE_ACCOUNT_KEY_JSON > $FCM_KEY_PATH
echo "FCM 키 파일 생성 완료: $FCM_KEY_PATH"

# Dockerfile의 'appuser' (UID 1000)가 파일을 읽을 수 있도록 소유권을 변경합니다.
# 'ubuntu' 사용자가 이 파일을 읽을 수 있도록 권한을 설정해 줍니다.
chown ubuntu:ubuntu $HOST_KEY_PATH
chmod 644 $HOST_KEY_PATH
echo "FCM 키 파일의 소유권 및 권한 설정을 완료했습니다."

# --- 3. 파일에서 실행할 이미지 URI 읽어오기 ---
echo "$IMAGE_URI_FILE 에서 이미지 URI를 읽어옵니다."
IMAGE_URI=$(cat $IMAGE_URI_FILE)
echo "실행할 이미지: $IMAGE_URI"

# --- 4. 새 컨테이너 실행 ---
echo "새 컨테이너($CONTAINER_NAME)를 시작합니다..."
docker run -d --name $CONTAINER_NAME -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=$DB_URL \
  -e DB_USERNAME=$DB_USERNAME \
  -e DB_PASSWORD=$DB_PASSWORD \
  -e JWT_SECRET_KEY=$JWT_SECRET_KEY \
  -v $FCM_KEY_PATH:$FCM_CONTAINER_KEY_PATH \
  $IMAGE_URI

echo "컨테이너 시작 명령 전송 완료."
echo "--- start_application.sh 종료 ---"