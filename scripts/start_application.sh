#!/bin/bash
set -e # 스크립트 실행 중 오류가 발생하면 즉시 중단합니다.

# --- 1. 변수 설정 ---
# 이 스크립트는 EC2 인스턴스의 IAM 역할 권한으로 실행됩니다.
AWS_REGION="ap-northeast-2" # 본인의 리전으로 변경
SECRET_ID="couponpop/secrets" # AWS Secrets Manager의 비밀 이름
SSM_PARAM_NAME="/couponpop/latest-image-uri" # AWS SSM Parameter Store의 파라미터 이름
CONTAINER_NAME="couponpop-app"

echo "--- start_application.sh 시작 ---"

# --- 2. Secrets Manager에서 비밀 정보 가져오기 ---
echo "Secrets Manager에서 비밀 정보를 가져옵니다: $SECRET_ID"
SECRET_JSON=$(aws secretsmanager get-secret-value --secret-id $SECRET_ID --region $AWS_REGION --query SecretString --output text)

# 환경변수로 export
export DB_URL=$(echo $SECRET_JSON | jq -r .DB_URL)
export DB_USERNAME=$(echo $SECRET_JSON | jq -r .DB_USERNAME)
export DB_PASSWORD=$(echo $SECRET_JSON | jq -r .DB_PASSWORD)
export JWT_SECRET_KEY=$(echo $SECRET_JSON | jq -r .JWT_SECRET_KEY)
# (필요한 다른 환경변수들 추가...)
echo "비밀 정보 로드 완료."

# --- 3. SSM Parameter Store에서 배포할 이미지 URI 가져오기 ---
# GHA CD 스텝이 방금 업데이트한 그 값입니다.
echo "SSM Parameter Store에서 이미지 URI를 가져옵니다: $SSM_PARAM_NAME"
IMAGE_URI=$(aws ssm get-parameter --name $SSM_PARAM_NAME --region $AWS_REGION --query Parameter.Value --output text)
ECR_REGISTRY=$(echo $IMAGE_URI | cut -d'/' -f1)
echo "배포할 이미지: $IMAGE_URI"

# --- 4. ECR 로그인 ---
echo "ECR에 로그인합니다: $ECR_REGISTRY"
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
echo "ECR 로그인 성공."

# --- 5. 새 이미지 PULL ---
echo "새 이미지를 pull 합니다..."
docker pull $IMAGE_URI
echo "이미지 pull 완료."

# --- 6. (ApplicationStart 훅) 새 컨테이너 실행 ---
# (ApplicationStop 훅이 이전에 실행되어 기존 컨테이너가 없다고 가정)
echo "새 컨테이너($CONTAINER_NAME)를 시작합니다..."
docker run -d --name $CONTAINER_NAME -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=$DB_URL \
  -e DB_USERNAME=$DB_USERNAME \
  -e DB_PASSWORD=$DB_PASSWORD \
  -e JWT_SECRET_KEY=$JWT_SECRET_KEY \
  $IMAGE_URI

echo "컨테이너 시작 명령 전송 완료."
echo "--- start_application.sh 종료 ---"