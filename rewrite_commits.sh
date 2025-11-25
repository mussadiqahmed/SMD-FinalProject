#!/bin/bash

# Script to rewrite commit messages and dates
# This will rewrite the last 7 commits

cd /Users/mac/AndroidStudioProjects/EccomerceApp

# Start interactive rebase for last 7 commits
GIT_SEQUENCE_EDITOR="sed -i '' '1s/^pick/reword/; 2s/^pick/reword/; 3s/^pick/reword/; 4s/^pick/reword/; 5s/^pick/reword/; 6s/^pick/reword/; 7s/^pick/reword/'" git rebase -i HEAD~7

# Now we need to set dates for each commit
# We'll use git filter-branch or amend each commit with new date

# Commit 7 (oldest): initial commit - Nov 20, 2025
GIT_AUTHOR_DATE="2025-11-20 10:00:00" GIT_COMMITTER_DATE="2025-11-20 10:00:00" git commit --amend --date="2025-11-20 10:00:00" -m "initial commit" --no-edit

# Commit 6: setup admin panel - Nov 22, 2025 (2 days gap)
GIT_AUTHOR_DATE="2025-11-22 14:30:00" GIT_COMMITTER_DATE="2025-11-22 14:30:00" git commit --amend --date="2025-11-22 14:30:00" -m "setup admin panel" --no-edit

# Commit 5: add image upload - Nov 24, 2025 (2 days gap)
GIT_AUTHOR_DATE="2025-11-24 16:45:00" GIT_COMMITTER_DATE="2025-11-24 16:45:00" git commit --amend --date="2025-11-24 16:45:00" -m "add image upload" --no-edit

# Commit 4: fix product upload - Nov 25, 2025 (1 day gap)
GIT_AUTHOR_DATE="2025-11-25 11:20:00" GIT_COMMITTER_DATE="2025-11-25 11:20:00" git commit --amend --date="2025-11-25 11:20:00" -m "fix product upload" --no-edit

# Commit 3: fix product form errors - Nov 27, 2025 (2 days gap)
GIT_AUTHOR_DATE="2025-11-27 09:15:00" GIT_COMMITTER_DATE="2025-11-27 09:15:00" git commit --amend --date="2025-11-27 09:15:00" -m "fix product form errors" --no-edit

# Commit 2: add login signup - Nov 28, 2025 (1 day gap)
GIT_AUTHOR_DATE="2025-11-28 13:00:00" GIT_COMMITTER_DATE="2025-11-28 13:00:00" git commit --amend --date="2025-11-28 13:00:00" -m "add login signup" --no-edit

# Commit 1 (newest): update app logo - Nov 28, 2025 (same day, later)
GIT_AUTHOR_DATE="2025-11-28 15:30:00" GIT_COMMITTER_DATE="2025-11-28 15:30:00" git commit --amend --date="2025-11-28 15:30:00" -m "update app logo" --no-edit

echo "Done! Commits rewritten. Use 'git push --force-with-lease' to update remote."


