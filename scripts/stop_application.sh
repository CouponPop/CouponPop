#!/bin/bash
set -e
CONTAINER_NAME="couponpop-app"

echo "--- stop_application.sh 시작 ---"

# 1. 실행 중인 컨테이너가 있는지 확인하고 중지
if [ "$(docker ps -q -f name=$CONTAINER_NAME)" ]; then
    echo "실행 중인 $CONTAINER_NAME 컨테이너를 중지합니다."
    docker stop $CONTAINER_NAME
else
    echo "$CONTAINER_NAME 컨테이너가 실행 중이지 않습니다."
fi

# 2. 컨테이너가 존재하는지 확인하고 삭제
if [ "$(docker ps -a -q -f name=$CONTAINER_NAME)" ]; then
    echo "기존 $CONTAINER_NAME 컨테이너를 삭제합니다."
    docker rm $CONTAINER_NAME
else
    echo "삭제할 $CONTAINER_NAME 컨테이너가 없습니다."
fi

echo "--- stop_application.sh 종료 ---"