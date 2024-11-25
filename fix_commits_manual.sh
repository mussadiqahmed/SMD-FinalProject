#!/bin/bash
# Manual step-by-step commit rewriting

cd /Users/mac/AndroidStudioProjects/EccomerceApp

echo "=== Step 1: Starting interactive rebase ==="
echo "In the editor, change all 'pick' to 'reword' for commits you want to edit"
echo "Then save and close"
echo ""
read -p "Press enter to continue..."

# Start rebase
git rebase -i HEAD~7

echo ""
echo "=== Step 2: For each commit, enter the new message:"
echo "Commit 7 (oldest): 'initial commit'"
echo "Commit 6: 'setup admin panel'"
echo "Commit 5: 'add image upload'"
echo "Commit 4: 'fix product upload'"
echo "Commit 3: 'fix product form errors'"
echo "Commit 2: 'add login signup'"
echo "Commit 1 (newest): 'update app logo'"
echo ""
read -p "After rebase completes, press enter to set dates..."

# Now set dates using git commit --amend for each
echo "Setting dates..."

# We need to rebase again to set dates, or use filter-branch
# Actually, let's use a simpler approach with git filter-branch

git filter-branch -f --env-filter '
if [ "$GIT_COMMIT" = "7cf1b40" ]; then
    export GIT_AUTHOR_DATE="2025-11-20 10:00:00"
    export GIT_COMMITTER_DATE="2025-11-20 10:00:00"
elif [ "$GIT_COMMIT" = "3e4b7ce" ]; then
    export GIT_AUTHOR_DATE="2025-11-22 14:30:00"
    export GIT_COMMITTER_DATE="2025-11-22 14:30:00"
elif [ "$GIT_COMMIT" = "b55dc8b" ]; then
    export GIT_AUTHOR_DATE="2025-11-24 16:45:00"
    export GIT_COMMITTER_DATE="2025-11-24 16:45:00"
elif [ "$GIT_COMMIT" = "151cae3" ]; then
    export GIT_AUTHOR_DATE="2025-11-25 11:20:00"
    export GIT_COMMITTER_DATE="2025-11-25 11:20:00"
elif [ "$GIT_COMMIT" = "8d724dc" ]; then
    export GIT_AUTHOR_DATE="2025-11-27 09:15:00"
    export GIT_COMMITTER_DATE="2025-11-27 09:15:00"
elif [ "$GIT_COMMIT" = "4409048" ]; then
    export GIT_AUTHOR_DATE="2025-11-28 13:00:00"
    export GIT_COMMITTER_DATE="2025-11-28 13:00:00"
elif [ "$GIT_COMMIT" = "c51c9ce" ]; then
    export GIT_AUTHOR_DATE="2025-11-28 15:30:00"
    export GIT_COMMITTER_DATE="2025-11-28 15:30:00"
fi
' HEAD~7..HEAD

echo "Done!"
