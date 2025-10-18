#!/bin/bash
set -e
CONTAINER_NAME="couponpop-app"

echo "--- stop_application.sh 시작 ---"

# 실행 중인 컨테이너가 있는지 확인하고 중지 및 제거
# `docker ps -q ...` : 컨테이너 ID가 있으면 [ID] 출력, 없으면 "" 출력
if [ "$(docker ps -q -f name=^/${CONTAINER_NAME}$)" ]; then
    echo "실행 중인 컨테이너($CONTAINER_NAME)를 중지합니다..."
    docker stop $CONTAINER_NAME
    echo "컨테이너를 제거합니다..."
    docker rm $CONTAINER_NAME
    echo "컨테이너($CONTAINER_NAME) 중지 및 제거 완료."
else
    echo "컨테이너($CONTAINER_NAME)가 실행 중이지 않습니다. 중지할 작업이 없습니다."
fi

echo "--- stop_application.sh 종료 ---"