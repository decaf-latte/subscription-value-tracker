#!/bin/bash
# Pre-commit hook: 커밋 전 테스트 실행

echo "🔍 Running pre-commit checks..."

# 프로젝트 루트로 이동
cd "$(git rev-parse --show-toplevel)" || exit 1

# 1. 테스트 실행
echo "🧪 Running tests..."
./gradlew test --quiet

if [ $? -ne 0 ]; then
    echo "❌ Tests failed! Commit blocked."
    echo "Fix the failing tests before committing."
    exit 2  # exit 2 = Claude에게 작업 차단 신호
fi

echo "✅ All tests passed!"
exit 0
