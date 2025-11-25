#!/bin/bash
# Rewrite all 7 commits with new messages and dates

cd /Users/mac/AndroidStudioProjects/EccomerceApp

# Use git filter-branch to change both messages and dates
git filter-branch -f --msg-filter '
case "$GIT_COMMIT" in
  7cf1b40*)
    echo "initial commit"
    ;;
  3e4b7ce*)
    echo "setup admin panel"
    ;;
  b55dc8b*)
    echo "add image upload"
    ;;
  151cae3*)
    echo "fix product upload"
    ;;
  8d724dc*)
    echo "app product view"
    ;;
  4409048*)
    echo "app chart and left panel"
    ;;
  c51c9ce*)
    echo "final things"
    ;;
  *)
    cat
    ;;
esac
' --env-filter '
case "$GIT_COMMIT" in
  7cf1b40*)
    export GIT_AUTHOR_DATE="2025-11-20 10:00:00"
    export GIT_COMMITTER_DATE="2025-11-20 10:00:00"
    ;;
  3e4b7ce*)
    export GIT_AUTHOR_DATE="2025-11-22 14:30:00"
    export GIT_COMMITTER_DATE="2025-11-22 14:30:00"
    ;;
  b55dc8b*)
    export GIT_AUTHOR_DATE="2025-11-22 16:45:00"
    export GIT_COMMITTER_DATE="2025-11-22 16:45:00"
    ;;
  151cae3*)
    export GIT_AUTHOR_DATE="2025-11-23 11:20:00"
    export GIT_COMMITTER_DATE="2025-11-23 11:20:00"
    ;;
  8d724dc*)
    export GIT_AUTHOR_DATE="2025-11-23 09:15:00"
    export GIT_COMMITTER_DATE="2025-11-23 09:15:00"
    ;;
  4409048*)
    export GIT_AUTHOR_DATE="2025-11-24 13:00:00"
    export GIT_COMMITTER_DATE="2025-11-24 13:00:00"
    ;;
  c51c9ce*)
    export GIT_AUTHOR_DATE="2025-11-25 15:30:00"
    export GIT_COMMITTER_DATE="2025-11-25 15:30:00"
    ;;
esac
' HEAD~7..HEAD

echo "Done! Check with: git log --oneline"
echo "Then push with: git push --force-with-lease"
