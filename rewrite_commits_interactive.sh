#!/bin/bash
# This script will help rewrite commits with new messages and dates

cd /Users/mac/AndroidStudioProjects/EccomerceApp

echo "Starting interactive rebase to change commit messages and dates..."
echo ""

# Create a temporary script for the rebase editor
cat > /tmp/rebase_editor.sh << 'REBASE_EOF'
#!/bin/bash
# Change all 'pick' to 'reword' to edit messages
sed -i '' 's/^pick/reword/g' "$1"
REBASE_EOF

chmod +x /tmp/rebase_editor.sh

# Start rebase
GIT_SEQUENCE_EDITOR="/tmp/rebase_editor.sh" git rebase -i HEAD~7

