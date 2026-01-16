#!/bin/bash
# Lint hook: 커밋 전 코드 스타일 검사

echo "🔍 Running lint checks (Checkstyle)..."

# 프로젝트 루트로 이동
cd "$(git rev-parse --show-toplevel)" || exit 1

# Checkstyle 실행
./gradlew checkstyleMain checkstyleTest --quiet

if [ $? -ne 0 ]; then
    echo "❌ Lint check failed! Commit blocked."
    echo "Run './gradlew checkstyleMain' to see details."
    echo "Report: build/reports/checkstyle/main.html"
    exit 2  # exit 2 = Claude에게 작업 차단 신호
fi

echo "✅ Lint check passed!"
exit 0
