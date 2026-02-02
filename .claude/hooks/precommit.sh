#!/bin/bash
# Pre-commit hook: 커밋 전 린트 + 테스트 실행

echo "🔍 Running pre-commit checks..."

# 프로젝트 루트로 이동
cd "$(git rev-parse --show-toplevel)" || exit 1

# 1. 린트 검사 (Checkstyle)
echo ""
echo "📋 Step 1/2: Lint check (Checkstyle)..."

# Checkstyle이 설정되어 있는지 확인
if ./gradlew tasks --all 2>&1 | grep -q "checkstyleMain"; then
    if ! ./gradlew checkstyleMain --quiet; then
        echo "❌ Lint check failed! Commit blocked."
        echo "Run './gradlew checkstyleMain' to see details."
        exit 1
    fi
    echo "✅ Lint check passed!"
else
    echo "⚠️  Checkstyle not configured, skipping..."
fi

# 2. 테스트 실행
echo ""
echo "🧪 Step 2/2: Running tests..."
./gradlew test --quiet

if [ $? -ne 0 ]; then
    echo "❌ Tests failed! Commit blocked."
    echo "Fix the failing tests before committing."
    exit 1
fi
echo "✅ All tests passed!"

echo ""
echo "🎉 All pre-commit checks passed! Proceeding with commit..."
exit 0
