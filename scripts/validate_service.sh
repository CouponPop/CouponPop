#!/bin/bash
set -e
MAX_RETRIES=10
WAIT_SECONDS=6
COUNT=0

echo "--- validate_service.sh 시작 ---"
echo "서비스 헬스체크를 시작합니다 (최대 $MAX_RETRIES 시도)..."

while [ $COUNT -lt $MAX_RETRIES ]; do
    # EC2 인스턴스 내부에서 8080 포트로 헬스체크
    # Spring Boot Actuator가 켜져 있고 /actuator/health 엔드포인트가 있다고 가정
    response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)

    if [ "$response" = "200" ]; then
        echo "헬스체크 성공 (HTTP 200). 배포를 계속합니다."
        echo "--- validate_service.sh 종료 (성공) ---"
        exit 0 # 성공 (0을 반환해야 함)
    else
        echo "헬스체크 $((COUNT+1))차 시도 실패 (HTTP $response). $WAIT_SECONDS 초 후 재시도..."
        COUNT=$((COUNT+1))
        sleep $WAIT_SECONDS
    fi
done

echo "서비스 헬스체크가 $MAX_RETRIES 번 시도 후 최종 실패했습니다."
echo "--- validate_service.sh 종료 (실패) ---"
exit 1 # 실패 (0이 아닌 값을 반환하면 CodeDeploy가 롤백을 시작함)