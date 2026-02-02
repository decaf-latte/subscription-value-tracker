#!/bin/bash
# Git hooks 설치 스크립트
# 사용법: ./scripts/install-hooks.sh

echo "🔧 Installing Git hooks..."

# 프로젝트 루트 경로 확인
PROJECT_ROOT="$(git rev-parse --show-toplevel)"
cd "$PROJECT_ROOT" || exit 1

# pre-commit hook 복사
cp "$PROJECT_ROOT/scripts/pre-commit" "$PROJECT_ROOT/.git/hooks/pre-commit"
chmod +x "$PROJECT_ROOT/.git/hooks/pre-commit"

echo "✅ Git hooks installed successfully!"
echo ""
echo "설치된 hooks:"
echo "  - pre-commit: 커밋 전 린트 + 테스트 실행"
echo ""
echo "hooks 제거하려면: rm .git/hooks/pre-commit"
