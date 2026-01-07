#!/bin/bash
# SessionEnd Hook: Automatically update CLAUDE.md with session changes
# This hook runs when exiting Claude Code (Ctrl+D, /exit, closing terminal)

set -e

# Session info is passed as JSON via stdin
SESSION_INFO=$(cat)

# Extract transcript path from session info using jq for reliable JSON parsing
TRANSCRIPT_PATH=$(echo "$SESSION_INFO" | jq -r '.transcript_path // empty')

# Validate transcript exists
if [ -z "$TRANSCRIPT_PATH" ] || [ ! -f "$TRANSCRIPT_PATH" ]; then
    echo "SessionEnd Hook: No transcript found, skipping CLAUDE.md update" >&2
    exit 0
fi

# Get project directory from session info
PROJECT_DIR=$(echo "$SESSION_INFO" | jq -r '.cwd // "."')
CLAUDE_MD="$PROJECT_DIR/CLAUDE.md"

# Check if CLAUDE.md exists
if [ ! -f "$CLAUDE_MD" ]; then
    echo "SessionEnd Hook: CLAUDE.md not found, skipping update" >&2
    exit 0
fi

# Log the update attempt
echo "SessionEnd Hook: Analyzing session transcript to update CLAUDE.md..." >&2

# Create a temporary file for the prompt and output
TEMP_PROMPT=$(mktemp)
TEMP_CLAUDE_MD=$(mktemp)

# Build the prompt with actual file contents (not shell substitution)
cat > "$TEMP_PROMPT" << 'PROMPT_EOF'
You are analyzing a completed Claude Code session to update project documentation.

TASK:
Review the session transcript and the current CLAUDE.md file, then generate an updated version of CLAUDE.md that reflects any changes made during this session.

SESSION TRANSCRIPT:
PROMPT_EOF

# Append the actual transcript content
cat "$TRANSCRIPT_PATH" >> "$TEMP_PROMPT"

# Continue building the prompt
cat >> "$TEMP_PROMPT" << 'PROMPT_EOF'

CURRENT CLAUDE.MD:
PROMPT_EOF

# Append the actual CLAUDE.md content
cat "$CLAUDE_MD" >> "$TEMP_PROMPT"

# Finish the prompt
cat >> "$TEMP_PROMPT" << 'PROMPT_EOF'

INSTRUCTIONS:
1. Carefully review the entire session transcript to identify:
   - New features or functionality added
   - Architecture or design pattern changes
   - New commands, scripts, or development workflows
   - File structure modifications
   - Updated dependencies or technology stack changes
   - New API endpoints or data models
   - Important implementation details or patterns
   - Any corrections to previously documented information

2. Update CLAUDE.md intelligently:
   - OVERWRITE outdated information with current information
   - PRESERVE information that is still accurate
   - ADD new sections for new features/functionality
   - UPDATE existing sections that have changed
   - MAINTAIN the overall structure and clarity of the document
   - Keep the tone consistent with the existing documentation

3. Output ONLY the complete updated CLAUDE.md content
   - Do not include explanations, commentary, or markdown code blocks
   - Output the raw markdown content that should replace CLAUDE.md
   - Start directly with '# CLAUDE.md' (the first line of the file)

4. If NO significant changes were made during the session:
   - Output the original CLAUDE.md content unchanged
   - Do not add unnecessary updates

Generate the updated CLAUDE.md now:
PROMPT_EOF

# Use Claude with the prepared prompt file (via stdin to avoid argument length limits)
claude -p < "$TEMP_PROMPT" > "$TEMP_CLAUDE_MD" 2>&1

# Clean up prompt file
rm -f "$TEMP_PROMPT"

# Check if the update was successful and the file has content
if [ -s "$TEMP_CLAUDE_MD" ]; then
    # Verify the output starts with "# CLAUDE.md" to ensure it's valid
    if head -n 1 "$TEMP_CLAUDE_MD" | grep -q "^# CLAUDE.md"; then
        # Replace CLAUDE.md with the updated version
        mv "$TEMP_CLAUDE_MD" "$CLAUDE_MD"
        echo "SessionEnd Hook: Successfully updated CLAUDE.md" >&2
    else
        echo "SessionEnd Hook: Generated output doesn't look like valid CLAUDE.md, skipping update" >&2
        rm "$TEMP_CLAUDE_MD"
    fi
else
    echo "SessionEnd Hook: Failed to generate updated CLAUDE.md, skipping update" >&2
    rm -f "$TEMP_CLAUDE_MD"
fi

exit 0
