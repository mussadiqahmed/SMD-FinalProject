#!/bin/bash

cd /Users/mac/AndroidStudioProjects/EccomerceApp

echo "Rewriting commits with new messages and dates..."
echo ""

# We'll use git filter-branch or rebase with date changes
# Method: Use interactive rebase, then amend each commit with new date

# Step 1: Create rebase script that changes all picks to rewording
cat > /tmp/rebase_script.sh << 'REBASE'
#!/bin/bash
sed -i '' 's/^pick/reword/g' "$1"
REBASE
chmod +x /tmp/rebase_script.sh

# Step 2: Start rebase (this will open editor for each commit message)
GIT_SEQUENCE_EDITOR="/tmp/rebase_script.sh" git rebase -i HEAD~7 << 'MESSAGES'
initial commit
setup admin panel
add image upload
fix product upload
fix product form errors
add login signup
update app logo
MESSAGES

# Step 3: Now set dates for each commit (we need to do this after rebase)
# This is complex, so let's use a different approach - git filter-branch

echo "Setting commit dates..."

# Use git rebase with exec to set dates
git rebase -i HEAD~7 << 'REBASE_DATES'
exec GIT_AUTHOR_DATE="2025-11-20 10:00:00" GIT_COMMITTER_DATE="2025-11-20 10:00:00" git commit --amend --date="2025-11-20 10:00:00" --no-edit
exec GIT_AUTHOR_DATE="2025-11-22 14:30:00" GIT_COMMITTER_DATE="2025-11-22 14:30:00" git commit --amend --date="2025-11-22 14:30:00" --no-edit
exec GIT_AUTHOR_DATE="2025-11-24 16:45:00" GIT_COMMITTER_DATE="2025-11-24 16:45:00" git commit --amend --date="2025-11-24 16:45:00" --no-edit
exec GIT_AUTHOR_DATE="2025-11-25 11:20:00" GIT_COMMITTER_DATE="2025-11-25 11:20:00" git commit --amend --date="2025-11-25 11:20:00" --no-edit
exec GIT_AUTHOR_DATE="2025-11-27 09:15:00" GIT_COMMITTER_DATE="2025-11-27 09:15:00" git commit --amend --date="2025-11-27 09:15:00" --no-edit
exec GIT_AUTHOR_DATE="2025-11-28 13:00:00" GIT_COMMITTER_DATE="2025-11-28 13:00:00" git commit --amend --date="2025-11-28 13:00:00" --no-edit
exec GIT_AUTHOR_DATE="2025-11-28 15:30:00" GIT_COMMITTER_DATE="2025-11-28 15:30:00" git commit --amend --date="2025-11-28 15:30:00" --no-edit
REBASE_DATES

echo "Done! Use 'git push --force-with-lease' to update remote."


