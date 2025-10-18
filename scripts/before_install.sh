#!/bin/bash
set -e # 오류 발생 시 중단

# --- 1. 변수 설정 ---
AWS_REGION="ap-northeast-2"
SSM_PARAM_NAME="/couponpop/latest-image-uri"
APP_DIR="/home/ubuntu/app"
IMAGE_URI_FILE="$APP_DIR/image_uri.txt" # 이미지 URI를 저장할 파일

echo "--- before_install.sh 시작 ---"

# --- 1-1. 대상 디렉터리 생성 ---
echo "배포 디렉터리를 생성합니다: $APP_DIR"
mkdir -p $APP_DIR

# --- 2. SSM Parameter Store에서 배포할 이미지 URI 가져오기 ---
echo "SSM Parameter Store에서 이미지 URI를 가져옵니다: $SSM_PARAM_NAME"
IMAGE_URI=$(aws ssm get-parameter --name $SSM_PARAM_NAME --region $AWS_REGION --query Parameter.Value --output text)
ECR_REGISTRY=$(echo $IMAGE_URI | cut -d'/' -f1)
echo "배포할 이미지: $IMAGE_URI"

# --- 3. 다음 스크립트를 위해 이미지 URI를 파일에 저장 ---
echo $IMAGE_URI > $IMAGE_URI_FILE
echo "이미지 URI를 $IMAGE_URI_FILE 에 저장했습니다."

# --- 4. ECR 로그인 ---
echo "ECR에 로그인합니다: $ECR_REGISTRY"
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_REGISTRY
echo "ECR 로그인 성공."

# --- 5. 새 이미지 PULL ---
echo "새 이미지를 pull 합니다.: $IMAGE_URI"
docker pull $IMAGE_URI
echo "이미지 pull 완료."

# --- 6. 사용하지 않는 도커 이미지 정리 ---
docker image prune -f
echo "사용하지 않는 이미지 정리 완료."

echo "--- before_install.sh 종료 ---"