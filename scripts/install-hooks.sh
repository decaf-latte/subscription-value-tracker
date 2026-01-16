#!/bin/bash
# Git hooks 설치 스크립트
# 사용법: ./scripts/install-hooks.sh

echo "🔧 Installing Git hooks..."

# 프로젝트 루트로 이동
cd "$(git rev-parse --show-toplevel)" || exit 1

# pre-commit hook 복사
cp scripts/pre-commit .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit

echo "✅ Git hooks installed successfully!"
echo ""
echo "설치된 hooks:"
echo "  - pre-commit: 커밋 전 린트 + 테스트 실행"
echo ""
echo "hooks 제거하려면: rm .git/hooks/pre-commit"
